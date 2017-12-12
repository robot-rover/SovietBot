package rr.industries.modules;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rr.industries.SovietBot;
import rr.industries.util.BotActions;
import rr.industries.util.sql.FilterTable;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.modules.IModule;
import sx.blah.discord.util.MessageBuilder;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;

/**
 * @author Brenn (@Brenn_#1786, https://github.com/BrennanStein)
 */

public class SwearFilter implements Module {
    private static final Logger LOG = LoggerFactory.getLogger(SwearFilter.class);
    public boolean enabled = false;
    public FilterConfig filterConfig;
    public BotActions actions;

   /*@SuppressWarnings("serial")
   public static Map<String, List<String>> letterReplacements = new HashMap<String, List<String>>() {
      {
         put("e", Collections.singletonList("3"));
         put("a", Collections.singletonList("4"));
         put("o", Collections.singletonList("0"));
         put("s", Collections.singletonList("$"));
      }
   };*/

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public void loadConfig(){
        try {
            filterConfig = SovietBot.gson.fromJson(new FileReader(actions.getConfig().filterConfigPath), FilterConfig.class);
        } catch (FileNotFoundException e) {
            LOG.error("Unable to load Filter Config", e);
            filterConfig = new FilterConfig();
        }
    }

    @Override
    public Module enableModule(BotActions actions) {
        this.actions = actions;
        loadConfig();
        actions.getClient().getDispatcher().registerListener(this);
        enabled = true;
        return this;
    }

    @Override
    public Module disableModule() {
        actions.getClient().getDispatcher().unregisterListener(this);
        enabled = false;
        return this;
    }

    @EventSubscriber
    public void OnMessage(MessageReceivedEvent e){
        if(actions.getTable(FilterTable.class).shouldFilter(e.getGuild().getLongID())){
            List<WordTracker> toFilter = testWords(e.getMessage().getContent());
            if(toFilter.size() > 0){
                actions.channels().sendMessage(new MessageBuilder(actions.getClient()).withChannel(e.getChannel())
                        .withContent(e.getAuthor().getName()).appendContent("#").appendContent(e.getAuthor().getDiscriminator()).appendContent(": ").appendContent(filter(e.getMessage().getContent(), toFilter)));
                e.getMessage().delete();
            }
        }
    }

    // Are input characters equivalent (per rules in letterReplacements)
    public boolean characterEquals(String character, String charAgainst) {
        if (charAgainst.equals(character))
            return true;
        if (filterConfig.letterReplacements.get(charAgainst) == null)
            return false;
        for (String str : filterConfig.letterReplacements.get(charAgainst)) {
            if (str.equals(character)) {
                return true;
            }
        }
        return false;
    }

    // Is character punctuation or spaces which should be disregarded
    public boolean isPunctuationOrSpace(String t) {
        return (filterConfig.punctuation.contains(t) || t.equals(" "));
    }

    // Returns list of WordTracker. If no censoring takes place, it is
    // empty
    public List<WordTracker> testWords(String input) {

        input = input.replaceAll("\\p{M}", "");
        // Kill diacritics. No zalgo.

        Map<String, List<WordTracker>> words = new HashMap<>();

        List<WordTracker> founds = new ArrayList<>();
        // To keep list of bad words found in input string

        for (int i = 0; i < input.length(); ++i) {
            char letter = Character.toLowerCase(input.charAt(i));
            for (String bad : filterConfig.bannedWords) {
                // Add new bad words to list
                if (letter == bad.charAt(0)) {
                    if (!words.containsKey(bad))
                        words.put(bad, new LinkedList<>());
                    words.get(bad).add(new WordTracker(i, bad));
                }
            }

            for (String key : words.keySet()) {
                List<WordTracker> list = words.get(key);
                List<WordTracker> updated = new LinkedList<>();
                while (list.size() != 0) {
                    if (isPunctuationOrSpace("" + letter)) {
                        updated.add(list.remove(0));
                        continue;
                    }
                    if (!characterEquals("" + letter,
                            "" + list.get(0).word.charAt(0))) {
                        list.remove(0);
                        continue;
                    }

                    WordTracker s = list.remove(0);
                    s.word = s.word.substring(1);
                    if (s.word.length() == 0) {
                        s.lastIndex = i;
                        founds.add(s);
                        s.word = key;
                    }
                    updated.add(s);
                }
                words.put(key, updated);
            }
        }
        return founds;
    }

    public String filter(String in, List<WordTracker> toFilter) {

        // This part reassembles everything into a display-ready String, replacing regions of bad words
        // with pound signs.
        StringBuilder sb = new StringBuilder(in);
        int negOffset = 0;
        for (WordTracker wt : toFilter) {
            StringBuffer word = new StringBuffer();
            for (int i = 0; i < wt.word.length(); i++) {
                word.append("#");
            }
            String nHashes = word.toString();
            sb.replace(wt.startIndex + negOffset, wt.lastIndex + 1 + negOffset,
                    nHashes);
            negOffset -= (wt.lastIndex + 1 - wt.startIndex) - nHashes.length();
        }
        return sb.toString();
    }

    public static class FilterConfig {
        public Map<String, List<String>> letterReplacements;
        public List<String> punctuation = Arrays.asList(",", ".", "\'");
        public String[] bannedWords = {"aabc", "porwe", "khsd"};
    }

    private static class WordTracker {
        public int startIndex;
        public int lastIndex;
        public String word;

        public WordTracker(int i, String w) {
            startIndex = i;
            word = w;
        }
    }
}