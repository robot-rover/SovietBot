package rr.industries.commands;

import reactor.core.publisher.Mono;
import rr.industries.exceptions.BotException;
import rr.industries.util.*;

import java.util.HashMap;
import java.util.Map;

@CommandInfo(commandName = "flip", helpText = "flips text upside-down", pmSafe = true)
public class Flip implements Command {
    @SubCommand(name = "", Syntax = {@Syntax(helpText = "flips the supplied text", args = {@Argument(description = "Text", value = Validate.LONGTEXT)})})
    public Mono<Void> execute(CommContext cont) throws BotException {
        char[] inputChars = cont.getConcatArgs(1).toCharArray();
        StringBuilder response = new StringBuilder();
        for(int i = inputChars.length - 1; i >= 0; i--) {
            char c = inputChars[i];
            String outputChar = mapObject.get(c);
            if(outputChar == null) {
                outputChar = String.valueOf(c);
            }
            response.append(outputChar);
        }
        return cont.getMessage().getMessage().getChannel().flatMap(v -> v.createMessage(response.toString())).then();
    }

    private Map<Character, String> mapObject = new HashMap<>();

    public Flip() {
        mapObject.put('a', "ɐ");
        mapObject.put('b', "q");
        mapObject.put('c', "ɔ");
        mapObject.put('d', "p");
        mapObject.put('e', "ǝ");
        mapObject.put('f', "ɟ");
        mapObject.put('g', "ƃ");
        mapObject.put('h', "ɥ");
        mapObject.put('i', "ı");
        mapObject.put('j', "ɾ");
        mapObject.put('k', "ʞ");
        mapObject.put('l', "ן");
        mapObject.put('m', "ɯ");
        mapObject.put('n', "u");
        mapObject.put('r', "ɹ");
        mapObject.put('t', "ʇ");
        mapObject.put('v', "ʌ");
        mapObject.put('w', "ʍ");
        mapObject.put('y', "ʎ");
        mapObject.put('A', "∀");
        mapObject.put('B', "𐐒");
        mapObject.put('C', "Ɔ");
        mapObject.put('D', "◖");
        mapObject.put('E', "Ǝ");
        mapObject.put('F', "Ⅎ");
        mapObject.put('G', "⅁");
        mapObject.put('J', "ſ");
        mapObject.put('K', "⋊");
        mapObject.put('L', "˥");
        mapObject.put('M', "W");
        mapObject.put('P', "Ԁ");
        mapObject.put('Q', "Ό");
        mapObject.put('R', "ᴚ");
        mapObject.put('T', "⊥");
        mapObject.put('U', "∩");
        mapObject.put('V', "Λ");
        mapObject.put('Y', "⅄");
        mapObject.put('&', "⅋");
        mapObject.put('.', "˙");
        mapObject.put(',', "'");
        mapObject.put('[', "]");
        mapObject.put(']', "[");
        mapObject.put('(', ")");
        mapObject.put(')', "(");
        mapObject.put('{', "}");
        mapObject.put('}', "{");
        mapObject.put('?', "¿");
        mapObject.put('!', "¡");
        mapObject.put('\'', ",");
        mapObject.put('<', ">");
        mapObject.put('_', "‾");
        mapObject.put('\"', "„");
        mapObject.put('\\', "/");
        mapObject.put('/', "\\");
        mapObject.put('`', ",");
        mapObject.put('1', "Ɩ");
        mapObject.put('2', "ᄅ");
        mapObject.put('3', "Ɛ");
        mapObject.put('4', "ㄣ");
        mapObject.put('5', "ϛ");
        mapObject.put('6', "9");
        mapObject.put('7', "ㄥ");
        mapObject.put('9', "6");
    }
}
