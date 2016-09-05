package rr.industries.commands;

import rr.industries.util.BotActions;
import rr.industries.util.CommContext;
import rr.industries.util.CommandInfo;
import rr.industries.util.Parsable;
import sx.blah.discord.util.MessageBuilder;

@CommandInfo(
        commandName = "quote",
        helpText = "Tells an \"In Soviet Russia\" joke."
)
public class Quote implements Command {
    private final String[] quotes;
    public Quote() {
        quotes = new String[]{
                "In Soviet Russia, command type you.",
                "In Soviet Russia, the lowest rank in the military is Public, not Private",
                "In soviet Russia, furball coughs up cat!",
                "In Soviet Russia, noun verb you!",
                "In soviet Russia, Chuck Norris still rules.",
                "In Soviet Russia, party throw you!",
                "In Soviet Russia, Christmas steals the grinch!!",
                "In Soviet Russia, waldo finds you!",
                "http://i1.kym-cdn.com/photos/images/original/000/008/724/ISR__Dividing_by_zero_by_RainbowJerk.png",
                "http://i2.kym-cdn.com/photos/images/original/000/000/948/in-soviet-russia.png",
                "In Soviet Russia, a van steals you",
                "In Soviet Russia, jokes crack you.",
                "http://ci.memecdn.com/689/2331689.jpg",
                "http://67.media.tumblr.com/tumblr_meknuzRXuD1rxustho1_500.jpg",
                "https://cdn.meme.am/instances/10678438.jpg",
                "http://files.sharenator.com/in_soviet_russia_holy_crap_not_another_internet_meme_demotivational_poster_1247942328-s640x458-173710.jpg"
        };
    }

    @Override
    public void execute(CommContext cont) {
        if (cont.getArgs().size() >= 2 && Parsable.tryInt(cont.getArgs().get(1))) {
            BotActions.sendMessage(new MessageBuilder(cont.getClient()).withContent(quotes[Integer.parseInt(cont.getArgs().get(1))]).withChannel(cont.getMessage().getMessage().getChannel()));
        } else {
            BotActions.sendMessage(new MessageBuilder(cont.getClient()).withContent(quotes[rn.nextInt(quotes.length)]).withChannel(cont.getMessage().getMessage().getChannel()));
        }
    }
}
