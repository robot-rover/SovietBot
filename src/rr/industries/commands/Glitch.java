package rr.industries.commands;

import rr.industries.util.*;
import sx.blah.discord.util.MessageBuilder;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Sam
 */

@CommandInfo(commandName = "glitch", helpText = "Creates Glitched out (Zalgo) text", pmSafe = true)
public class Glitch implements Command {
    private static String[] zalgoMiddle;
    private static String[] zalgoUp;
    private static String[] zalgoDown;

    static {
        //those go UP
        zalgoUp = new String[]{
                "\u030d", /*     ̍     */        "\u030e", /*     ̎     */        "\u0304", /*     ̄     */        "\u0305", /*     ̅     */
                "\u033f", /*     ̿     */        "\u0311", /*     ̑     */        "\u0306", /*     ̆     */        "\u0310", /*     ̐     */
                "\u0352", /*     ͒     */        "\u0357", /*     ͗     */        "\u0351", /*     ͑     */        "\u0307", /*     ̇     */
                "\u0308", /*     ̈     */        "\u030a", /*     ̊     */        "\u0342", /*     ͂     */        "\u0343", /*     ̓     */
                "\u0344", /*     ̈́     */        "\u034a", /*     ͊     */        "\u034b", /*     ͋     */        "\u034c", /*     ͌     */
                "\u0303", /*     ̃     */        "\u0302", /*     ̂     */        "\u030c", /*     ̌     */        "\u0350", /*     ͐     */
                "\u0300", /*     ̀     */        "\u0301", /*     ́     */        "\u030b", /*     ̋     */        "\u030f", /*     ̏     */
                "\u0312", /*     ̒     */        "\u0313", /*     ̓     */        "\u0314", /*     ̔     */        "\u033d", /*     ̽     */
                "\u0309", /*     ̉     */        "\u0363", /*     ͣ     */        "\u0364", /*     ͤ     */        "\u0365", /*     ͥ     */
                "\u0366", /*     ͦ     */        "\u0367", /*     ͧ     */        "\u0368", /*     ͨ     */        "\u0369", /*     ͩ     */
                "\u036a", /*     ͪ     */        "\u036b", /*     ͫ     */        "\u036c", /*     ͬ     */        "\u036d", /*     ͭ     */
                "\u036e", /*     ͮ     */        "\u036f", /*     ͯ     */        "\u033e", /*     ̾     */        "\u035b", /*     ͛     */
                "\u0346", /*     ͆     */        "\u031a" /*     ̚     */
        };

        //those go DOWN
        zalgoDown = new String[]{
                "\u0316", /*     ̖     */        "\u0317", /*     ̗     */        "\u0318", /*     ̘     */        "\u0319", /*     ̙     */
                "\u031c", /*     ̜     */        "\u031d", /*     ̝     */        "\u031e", /*     ̞     */        "\u031f", /*     ̟     */
                "\u0320", /*     ̠     */        "\u0324", /*     ̤     */        "\u0325", /*     ̥     */        "\u0326", /*     ̦     */
                "\u0329", /*     ̩     */        "\u032a", /*     ̪     */        "\u032b", /*     ̫     */        "\u032c", /*     ̬     */
                "\u032d", /*     ̭     */        "\u032e", /*     ̮     */        "\u032f", /*     ̯     */        "\u0330", /*     ̰     */
                "\u0331", /*     ̱     */        "\u0332", /*     ̲     */        "\u0333", /*     ̳     */        "\u0339", /*     ̹     */
                "\u033a", /*     ̺     */        "\u033b", /*     ̻     */        "\u033c", /*     ̼     */        "\u0345", /*     ͅ     */
                "\u0347", /*     ͇     */        "\u0348", /*     ͈     */        "\u0349", /*     ͉     */        "\u034d", /*     ͍     */
                "\u034e", /*     ͎     */        "\u0353", /*     ͓     */        "\u0354", /*     ͔     */        "\u0355", /*     ͕     */
                "\u0356", /*     ͖     */        "\u0359", /*     ͙     */        "\u035a", /*     ͚     */        "\u0323" /*     ̣     */
        };

        //those always stay in the middle
        zalgoMiddle = new String[]{
                "\u0315", /*     ̕     */        "\u031b", /*     ̛     */        "\u0340", /*     ̀     */        "\u0341", /*     ́     */
                "\u0358", /*     ͘     */        "\u0321", /*     ̡     */        "\u0322", /*     ̢     */        "\u0327", /*     ̧     */
                "\u0328", /*     ̨     */        "\u0334", /*     ̴     */        "\u0335", /*     ̵     */        "\u0336", /*     ̶     */
                "\u034f", /*     ͏     */        "\u035c", /*     ͜     */        "\u035d", /*     ͝     */        "\u035e", /*     ͞     */
                "\u035f", /*     ͟     */        "\u0360", /*     ͠     */        "\u0362", /*     ͢     */        "\u0338", /*     ̸     */
                "\u0337", /*     ̷     */        "\u0361", /*     ͡     */        "\u0489" /*     ҉_     */
        };
    }

    @SubCommand(name = "", Syntax = {@Syntax(helpText = "Creates normal Glitch Text using your input", args = {Arguments.LONGTEXT})})
    public void execute(CommContext cont) {
        String txt = cont.getConcatArgs(1);
        MessageBuilder message = cont.builder();
        Matcher mention = Pattern.compile("<@!?([0-9]{18})>").matcher(txt);
        while (mention.find()) {
            txt = txt.replace(mention.group(), cont.getClient().getUserByID(mention.group(1)).getDisplayName(cont.getMessage().getGuild()));
        }

        for (int i = 0; i < txt.length(); i++) {
            int num_Up;
            int numDown;
            int numMid;
            int charMult;
            //add the normal character
            message.appendContent(txt.substring(i, i + 1));
            if (txt.substring(i, i + 1).equals("`")) {
                continue;
            }
            charMult = -1 * (8 / (txt.length() / 2)) * Math.abs(i - (txt.length() / 2)) + 8;
            num_Up = (charMult > 0 ? rn.nextInt(charMult) : 0) + 1;
            numDown = (charMult > 0 ? rn.nextInt(charMult) : 0) + 1;
            numMid = rn.nextInt(1) + 1;

            for (int j = 0; j < num_Up; j++)
                message.appendContent(zalgoUp[rn.nextInt(zalgoUp.length)]);
            for (int j = 0; j < numDown; j++)
                message.appendContent(zalgoDown[rn.nextInt(zalgoDown.length)]);
            for (int j = 0; j < numMid; j++)
                message.appendContent(zalgoMiddle[rn.nextInt(zalgoMiddle.length)]);
        }
        cont.getActions().channels().sendMessage(message);
    }
}
