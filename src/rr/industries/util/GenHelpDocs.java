package rr.industries.util;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rr.industries.SovietBot;
import rr.industries.commands.Command;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Sam
 * @project sovietBot
 * @created 9/13/2016
 */
public class GenHelpDocs {
    private static Logger LOG = LoggerFactory.getLogger(GenHelpDocs.class);

    public static void generate(List<Command> commands) {
        File repo = new File("SovietBot");
        if (!repo.exists() || !repo.isDirectory()) {
            LOG.error("gh-pages not mirrored, Can not generate CommDocs");
            return;
        }
        List<String> pages = new ArrayList<>();
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
                        body.append("<h2>\t").append(SovietBot.defaultConfig.commChar).append(commInfo.commandName()).append(" ").append(subComm.name()).append("\n");
                        for (Arguments arg : syntax.args())
                            body.append(arg.text.replace("<", "&lt;").replace(">", "&gt;"));
                        body.append("</h2>\n<p>").append(syntax.helpText()).append("</p>\n");
                    }

                }
                String string = IOUtils.toString(SovietBot.resourceLoader.getResourceAsStream("template.txt"));
                String htmlBody = "<h1>Syntax</h1>\n" + "<p>Possible Syntaxes for this command. Type what you see exactly, but replace what is between <strong>&lt;&gt;</strong> with your own values.\n" + body.toString();
                string = string.replace("{css-prefix}", "../");
                string = string.replace("{title}", SovietBot.defaultConfig.commChar + commInfo.commandName());
                string = string.replace("{header}", SovietBot.defaultConfig.commChar + commInfo.commandName());
                string = string.replace("{description}", commInfo.helpText());
                string = string.replace("{content}", htmlBody);
                BufferedWriter writer = Files.newBufferedWriter(htmlFile.toPath());
                writer.write(string);
                writer.close();
            }
            BufferedWriter writer = Files.newBufferedWriter(new File(repo.getAbsolutePath() + File.separator + "commandList.html").toPath());
            String string = IOUtils.toString(SovietBot.resourceLoader.getResourceAsStream("template.txt"));
            StringBuilder commandListText = new StringBuilder("<ul>\n");
            for (Command command : commands) {
                CommandInfo info = command.getClass().getAnnotation(CommandInfo.class);
                commandListText.append("<li><a href=\"commands" + File.separator + info.commandName() + ".html" + "\">" + SovietBot.defaultConfig.commChar + info.commandName() + "</a></li>\n");
            }
            commandListText.append("</ul>");
            string = string.replace("{css-prefix}", "");
            string = string.replace("{title}", "Command List");
            string = string.replace("{header}", "List of Commands");
            string = string.replace("{description}", "All of the Commands that SovietBot implements");
            string = string.replace("{content}", commandListText.toString());
            writer.write(string);
            writer.close();
            BufferedWriter writer2 = Files.newBufferedWriter(new File(repo.getAbsolutePath() + File.separator + "index.html").toPath());
            String string2 = IOUtils.toString(SovietBot.resourceLoader.getResourceAsStream("template.txt"));
            String body2 = IOUtils.toString(SovietBot.resourceLoader.getResourceAsStream("index.txt"));
            string2 = string2.replace("{css-prefix}", "");
            string2 = string2.replace("{title}", "Sovietbot by robot-rover");
            string2 = string2.replace("{header}", "Sovietbot");
            string2 = string2.replace("{description}", "Fun and Simple discord bot with support for youtube music streaming.");
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

    private static void delete(File f) throws FileNotFoundException {
        if (f.isDirectory()) {
            for (File c : f.listFiles())
                delete(c);
        }
        if (!f.delete())
            LOG.warn("Failed to delete file: " + f);
    }

    private static void git(String... args) {
        List<String> modArgs = new ArrayList<>();
        String log = "git";
        modArgs.add("git");
        for (String s : args) {
            modArgs.add(s);
            log = log.concat(" " + s);
        }
        try {
            String output;
            Process git = new ProcessBuilder(modArgs).directory(new File("SovietBot")).start();
            LOG.info(log + "\nGit: " + IOUtils.toString(git.getInputStream()));
            if (git.waitFor() != 0)
                throw new IOException("Did not Commit..." + IOUtils.toString(git.getErrorStream()));
        } catch (IOException | InterruptedException ex) {
            LOG.warn("Error with Git: " + ex.getMessage());
        }
    }
}
