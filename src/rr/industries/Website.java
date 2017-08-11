package rr.industries;

import org.apache.commons.io.IOUtils;
import rr.industries.commands.Command;
import rr.industries.util.*;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;
import static org.apache.commons.lang3.StringEscapeUtils.escapeHtml4;

/**
 *
 */
//todo: add class description
public class Website {
    public String index;
    public String help;
    public HashMap<String, String> commands = new HashMap<>();
    public HashMap<String, String> styleSheets = new HashMap<>();
    public String javascript;
    public HashMap<String, byte[]> images = new HashMap<>();
    private final ClassLoader loader = SovietBot.class.getClassLoader();

    private String[] styleSheetsFiles = {"github-light.css", "print.css", "stylesheet.css"};
    private String[] imagesFiles = {"body-bg.jpg", "download-button.png", "download-button-discord.png", "download-button-home.png", "download-button-help.png", "github-button.png", "header-bg.jpg", "highlight-bg.jpg", "sidebar-bg.jpg"};

    public Website() throws IOException {
        ClassLoader resourceLoader = SovietBot.class.getClassLoader();
        index = IOUtils.toString(resourceLoader.getResourceAsStream("template.txt"));
        String indexBody = IOUtils.toString(resourceLoader.getResourceAsStream("index.txt"));
        index = index.replace("{css-prefix}", "");
        index = index.replace("{title}", escapeHtml4("SovietBot by robot-rover"));
        index = index.replace("{header}", "SovietBot");
        index = index.replace("{description}", escapeHtml4("Fun and Simple discord bot with support for youtube music streaming."));
        index = index.replace("{content}", indexBody);

        help = IOUtils.toString(resourceLoader.getResourceAsStream("template.txt"));
        StringBuilder commandListText = new StringBuilder("<ul class=\"container\">\n");
        List<CommandInfo> commInfos = new ArrayList<>();
        for (Command command : CommandList.getCommandList()) {
            CommandInfo commInfo = command.getClass().getAnnotation(CommandInfo.class);
            if (commInfo.permLevel() == Permissions.BOTOPERATOR)
                continue;
            commInfos.add(commInfo);
        }
        commInfos.sort(comparing(CommandInfo::commandName));
        for (CommandInfo commInfo : commInfos) {
            commandListText.append("<a href=\"commands").append(File.separator).append(commInfo.commandName()).append(".html").append("\"><li>").append(escapeHtml4(Information.defaultCommChar + commInfo.commandName())).append("</li></a>\n");
        }
        commandListText.append("<div style=\"clear:both\"></ul>");
        help = help.replace("{css-prefix}", "");
        help = help.replace("{title}", "Command List");
        help = help.replace("{header}", "Commands");
        help = help.replace("{description}", escapeHtml4("All of the Commands that SovietBot implements"));
        help = help.replace("{content}", commandListText.toString());
        for (String url : styleSheetsFiles) {
            styleSheets.put("stylesheets/" + url, loadResourceString("stylesheets/" + url));
        }
        for (String url : imagesFiles) {
            images.put("images/" + url, loadResource("images/" + url));
        }
        javascript = loadResourceString("javascripts/main.js");

        for (Command command : CommandList.getCommandList()) {
            CommandInfo commInfo = command.getClass().getAnnotation(CommandInfo.class);
            if (commInfo.permLevel() == Permissions.BOTOPERATOR)
                continue;
            StringBuilder body = new StringBuilder();
            List<SubCommand> subCommands = new ArrayList<>();
            for (Method method : command.getClass().getMethods()) {
                if (method.getAnnotation(SubCommand.class) != null) {
                    subCommands.add(method.getAnnotation(SubCommand.class));
                }
            }
            for (SubCommand subComm : subCommands) {
                for (Syntax syntax : subComm.Syntax()) {
                    body.append("<h2>").append(escapeHtml4("\t" + Information.defaultCommChar + commInfo.commandName() + " " + subComm.name() + "\n"));
                    for (Argument arg : syntax.args())
                        body.append(escapeHtml4(" <" + (arg.description().equals("") ? arg.value().defaultLabel : arg.description()) + ">"));
                    body.append("</h2>\n<p>").append(escapeHtml4(syntax.helpText())).append("</p>\n");
                    body.append("<p>").append(Arrays.stream(syntax.args()).filter(v -> v.options().length > 0).map(v -> Arrays.stream(v.options()).collect(Collectors.joining(" | ", (v.description().equals("") ? v.value().defaultLabel : v.description()) + " ( ", " )"))).collect(Collectors.joining("<p>", "</p><p>", "</p>")));
                }

            }
            String string = IOUtils.toString(resourceLoader.getResourceAsStream("template.txt"));
            String htmlBody = "<h1>Syntax</h1>\n" + "<p>Possible Syntaxes for this command. Type what you see exactly, but replace what is between <strong>&lt;&gt;</strong> with your own values.\n" + body.toString();
            string = string.replace("{css-prefix}", "../");
            string = string.replace("{title}", escapeHtml4(Information.defaultCommChar + commInfo.commandName()));
            string = string.replace("{header}", escapeHtml4(Information.defaultCommChar + commInfo.commandName()));
            string = string.replace("{description}", escapeHtml4(commInfo.helpText()));
            string = string.replace("{content}", htmlBody);
            commands.put(commInfo.commandName() + ".html", string);
        }
    }


    private String loadResourceString(String url) throws IOException {
        return spark.utils.IOUtils.toString(loader.getResourceAsStream(url));
    }

    private byte[] loadResource(String url) throws IOException {
        return spark.utils.IOUtils.toByteArray(loader.getResourceAsStream(url));

    }
}
