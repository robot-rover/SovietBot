package rr.industries.commands;

import discord4j.core.object.entity.Member;
import discord4j.core.object.util.Snowflake;
import reactor.core.publisher.Mono;
import rr.industries.exceptions.BotException;
import rr.industries.util.*;

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

    @SubCommand(name = "", Syntax = {@Syntax(helpText = "Creates normal Glitch Text using your input", args = {@Argument(description = "Text", value = Validate.LONGTEXT)})})
    public Mono<Void> execute(CommContext cont) throws BotException {
        String txt = cont.getConcatArgs(1);
        StringBuilder builder = new StringBuilder();
        Matcher mention = Pattern.compile("<@!?([0-9]{18})>").matcher(txt);
        Mono<String> processText = Mono.just(txt);
        while (mention.find()) {
            Mono<String> displayName = cont.getClient().getUserById(Snowflake.of(mention.group(1)))
                    .zipWith(Mono.justOrEmpty(cont.getMessage().getGuildId()))
                    .flatMap(v -> v.getT1().asMember(v.getT2()))
                    .map(Member::getDisplayName);
            processText = processText.zipWith(displayName)
                    .map(v -> v.getT1().replace(mention.group(), v.getT2()));
        }

        return processText.map(v -> {
            for (int i = 0; i < v.length(); i++) {
                int num_Up;
                int numDown;
                int numMid;
                int charMult;
                //add the normal character
                builder.append(v.substring(i, i + 1));
                if (v.substring(i, i + 1).equals("`") || v.substring(i, i + 1).equals("*")) {
                    continue;
                }
                charMult = -1 * (8 / (v.length() / 2)) * Math.abs(i - (v.length() / 2)) + 8;
                num_Up = (charMult > 0 ? rn.nextInt(charMult) : 0) + 1;
                numDown = (charMult > 0 ? rn.nextInt(charMult) : 0) + 1;
                numMid = rn.nextInt(1) + 1;

                for (int j = 0; j < num_Up; j++)
                    builder.append(zalgoUp[rn.nextInt(zalgoUp.length)]);
                for (int j = 0; j < numDown; j++)
                    builder.append(zalgoDown[rn.nextInt(zalgoDown.length)]);
                for (int j = 0; j < numMid; j++)
                    builder.append(zalgoMiddle[rn.nextInt(zalgoMiddle.length)]);
            }
            return builder.toString();
        })
                .zipWith(cont.getMessage().getMessage().getChannel())
                .flatMap(v -> v.getT2().createMessage(v.getT1()))
                .then();
    }
}
