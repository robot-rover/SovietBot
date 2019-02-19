package rr.industries.modules;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Member;
import discord4j.core.object.util.Snowflake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import rr.industries.SovietBot;
import rr.industries.util.BotActions;
import rr.industries.util.sql.GreetingTable;

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
    public Disposable dispose;

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
        actions.getClient().getEventDispatcher().on(MessageCreateEvent.class).flatMap(this::onMessage).subscribe();
        enabled = true;
        return this;
    }

    @Override
    public Module disableModule() {
        if(dispose != null) {
            dispose.dispose();
        }
        enabled = false;
        return this;
    }

    public Mono<Void> onMessage(MessageCreateEvent e) {
        if(e.getGuildId().isEmpty()) {
            LOG.warn("SwearFilter received message without guild");
            return Mono.empty();
        }
                                          // \/ Hint doesn't show up here
        Snowflake guildId = e.getGuildId().get();
                                                                        // \/ Hint shows up here
        if(actions.getTable(GreetingTable.class).shouldFilter(e.getGuildId().get())){
            List<WordTracker> toFilter = testWords(e.getMessage().getContent().orElse(""));
            if(toFilter.size() > 0){
                if(e.getMember().isEmpty()) {
                    LOG.warn("SwearFilter received message without guild");
                    return Mono.empty();
                }

                Member author = e.getMember().get();
                String content = "**" + author.getUsername() + "**" + "#" + author.getDiscriminator() + ": " + filter(e.getMessage().getContent().orElse(""), toFilter);
                return e.getMessage().delete().then(e.getMessage().getChannel().flatMap(v -> v.createMessage(content))).then();
            }
        }
        return Mono.empty();
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
                if (characterEquals(letter, bad.charAt(0))) {
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

    private boolean characterEquals(char letter, char c) {
        return characterEquals(String.valueOf(letter), String.valueOf(c));
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
        public List<String> punctuation;
        public String[] bannedWords;
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