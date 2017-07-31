package rr.industries.util;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rr.industries.Information;
import rr.industries.SovietBot;
import rr.industries.commands.Command;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;
import static org.apache.commons.lang3.StringEscapeUtils.escapeHtml4;

/**
 * @author robot_rover
 */
public class GenHelpDocs {
    private static Logger LOG = LoggerFactory.getLogger(GenHelpDocs.class);
    static final boolean gitPush = true;

    public static void generate(List<Command> commands) {
        Information info = new Information();
        ClassLoader resourceLoader = SovietBot.class.getClassLoader();
        File repo = new File("SovietBot");
        if (!repo.exists() || !repo.isDirectory()) {
            LOG.error("gh-pages not mirrored, Can not generate CommDocs");
            return;
        }
        git("pull");
        git("checkout", "gh-pages");
        try {
            File docsFolder = new File(repo.getName() + File.separator + "commands");
            if (docsFolder.exists() && docsFolder.isDirectory())
                delete(docsFolder);
            delete(new File(repo.getAbsoluteFile() + File.separator + "commandList.html"));
            delete(new File(repo.getAbsoluteFile() + File.separator + "index.html"));
            docsFolder.mkdir();
            LOG.info(docsFolder.getCanonicalPath());
            for (Command command : commands) {
                CommandInfo commInfo = command.getClass().getAnnotation(CommandInfo.class);
                if (commInfo.permLevel() == Permissions.BOTOPERATOR)
                    continue;
                File htmlFile = new File(docsFolder.getAbsolutePath() + File.separator + commInfo.commandName() + ".html");
                StringBuilder body = new StringBuilder();
                List<SubCommand> subCommands = new ArrayList<>();
                for (Method method : command.getClass().getMethods()) {
                    if (method.getAnnotation(SubCommand.class) != null) {
                        subCommands.add(method.getAnnotation(SubCommand.class));
                    }
                }
                for (SubCommand subComm : subCommands) {
                    for (Syntax syntax : subComm.Syntax()) {
                        body.append("<h2>").append(escapeHtml4("\t" + info.defaultConfig.commChar + commInfo.commandName() + " " + subComm.name() + "\n"));
                        for (Argument arg : syntax.args())
                            body.append(escapeHtml4(" <" + (arg.description().equals("") ? arg.value().defaultLabel : arg.description()) + ">"));
                        body.append("</h2>\n<p>").append(escapeHtml4(syntax.helpText())).append("</p>\n");
                        body.append("<p>").append(Arrays.stream(syntax.args()).filter(v -> v.options().length > 0).map(v -> Arrays.stream(v.options()).collect(Collectors.joining(" | ", (v.description().equals("") ? v.value().defaultLabel : v.description()) + " ( ", " )"))).collect(Collectors.joining("<p>", "</p><p>", "</p>")));
                    }

                }
                String string = IOUtils.toString(resourceLoader.getResourceAsStream("template.txt"));
                String htmlBody = "<h1>Syntax</h1>\n" + "<p>Possible Syntaxes for this command. Type what you see exactly, but replace what is between <strong>&lt;&gt;</strong> with your own values.\n" + body.toString();
                string = string.replace("{css-prefix}", "../");
                string = string.replace("{title}", escapeHtml4(info.defaultConfig.commChar + commInfo.commandName()));
                string = string.replace("{header}", escapeHtml4(info.defaultConfig.commChar + commInfo.commandName()));
                string = string.replace("{description}", escapeHtml4(commInfo.helpText()));
                string = string.replace("{content}", htmlBody);
                BufferedWriter writer = Files.newBufferedWriter(htmlFile.toPath());
                writer.write(string);
                writer.close();
            }
            BufferedWriter writer = Files.newBufferedWriter(new File(repo.getAbsolutePath() + File.separator + "commandList.html").toPath());
            String string = IOUtils.toString(resourceLoader.getResourceAsStream("template.txt"));
            StringBuilder commandListText = new StringBuilder("<ul class=\"container\">\n");
            List<CommandInfo> commInfos = new ArrayList<>();
            for (Command command : commands) {
                CommandInfo commInfo = command.getClass().getAnnotation(CommandInfo.class);
                if (commInfo.permLevel() == Permissions.BOTOPERATOR)
                    continue;
                commInfos.add(commInfo);
            }
            commInfos.sort(comparing(CommandInfo::commandName));
            for (CommandInfo commInfo : commInfos) {
                commandListText.append("<a href=\"commands").append(File.separator).append(commInfo.commandName()).append(".html").append("\"><li>").append(escapeHtml4(info.defaultConfig.commChar + commInfo.commandName())).append("</li></a>\n");
            }
            commandListText.append("<div style=\"clear:both\"></ul>");
            string = string.replace("{css-prefix}", "");
            string = string.replace("{title}", "Command List");
            string = string.replace("{header}", "Commands");
            string = string.replace("{description}", escapeHtml4("All of the Commands that SovietBot implements"));
            string = string.replace("{content}", commandListText.toString());
            writer.write(string);
            writer.close();
            BufferedWriter writer2 = Files.newBufferedWriter(new File(repo.getAbsolutePath() + File.separator + "index.html").toPath());
            String string2 = IOUtils.toString(resourceLoader.getResourceAsStream("template.txt"));
            String body2 = IOUtils.toString(resourceLoader.getResourceAsStream("index.txt"));
            string2 = string2.replace("{css-prefix}", "");
            string2 = string2.replace("{title}", escapeHtml4("SovietBot by robot-rover"));
            string2 = string2.replace("{header}", "SovietBot");
            string2 = string2.replace("{description}", escapeHtml4("Fun and Simple discord bot with support for youtube music streaming."));
            string2 = string2.replace("{content}", body2);
            writer2.write(string2);
            writer2.close();
        } catch (IOException ex) {
            LOG.error("Error writing Command Docs", ex);
        }
        git("add", ".");
        git("commit", "-m", "\"Automated Command Documentation Update\"");
        git("push");
    }

    private static void delete(File f) {
        if (f.isDirectory()) {
            //noinspection ConstantConditions
            for (File c : f.listFiles())
                delete(c);
        }
        if (!f.delete())
            LOG.warn("Failed to delete file: " + f);
    }

    private static void git(String... args) {
        if (gitPush) {
            List<String> modArgs = new ArrayList<>();
            String log = "git";
            modArgs.add("git");
            for (String s : args) {
                modArgs.add(s);
                log = log.concat(" " + s);
            }
            try {
                Process git = new ProcessBuilder(modArgs).directory(new File("SovietBot")).start();
                LOG.info(log + "\nGit: " + IOUtils.toString(git.getInputStream()));
                if (git.waitFor() != 0)
                    throw new IOException("Did not Commit..." + IOUtils.toString(git.getErrorStream()));
            } catch (IOException | InterruptedException ex) {
                LOG.warn("Error with Git: " + ex.getMessage());
            }
        }
    }
}
