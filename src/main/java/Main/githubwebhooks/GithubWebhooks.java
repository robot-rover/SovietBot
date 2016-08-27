package Main.githubwebhooks;

import Main.Instance;
import Main.Main;
import com.google.gson.Gson;
import spark.Request;
import spark.Response;
import spark.Spark;
import sx.blah.discord.api.IDiscordClient;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class GithubWebhooks {

    private final List<ChannelSettings> channels = new ArrayList<>();
    private final Gson gson = new Gson();
    private final IDiscordClient client;

    /**
     * Should be initalized in ReadyEvent
     *
     * @param port
     * @param client
     */

    public GithubWebhooks(int port, IDiscordClient client, final String secret) {
        this.client = client;

        final SecretKeySpec keySpec = secret == null ? null : new SecretKeySpec(secret.getBytes(), "HmacSHA1");
        final Mac mac;
        Mac tmpMac = null;
        if (secret != null) {
            try {
                tmpMac = Mac.getInstance("HmacSHA1");
                tmpMac.init(keySpec);
            } catch (NoSuchAlgorithmException | InvalidKeyException ex) {
                Instance.log.warn("Error Getting Decryption Mac", ex);
            }
        }
        mac = tmpMac;
        Spark.port(port);
        Spark.post("/github", (Request request, Response response) -> {
            if (!"application/json".equals(request.headers("Content-Type"))) {
                Instance.log.warn("Received Non-JSON POST");
                response.status(500);
                return "Content-Type must be application/json!";
            }

            if (secret != null && mac != null) {
                String signature = request.headers("X-Hub-Signature");

                byte[] digestBytes = mac.doFinal(request.body().getBytes());
                StringBuilder builder = new StringBuilder();
                for (byte b : digestBytes) {
                    builder.append(String.format("%02x", b));
                }

                String digest = "sha1=" + builder.toString();
                if (!signature.equals(digest)) {
                    Instance.log.warn("Signature did not match: got " + signature + ", digested " + digest);
                    response.status(500);
                    return "Signatures did not match!";
                }
            }

            String event = request.headers("X-Github-Event");
            try {
                String message = "";

                if (event.equalsIgnoreCase("push")) {
                    Instance.log.info("Webhook server received Push POST");
                    Commit commit = gson.fromJson(request.body(), Commit.class);
                    boolean shouldSend = false;

                    message = message + (
                            "New commits pushed to **" + commit.repository.full_name + "** in the `" +
                                    commit.ref.replace("refs/heads/", "") + "` Branch\n");
                    for (Commit.CommitContent commitContent : commit.commits) {
                        // ignores commits with %ignore% in it, case sensitive
                        if (commitContent.message.contains("%ignore%"))
                            continue;
                        message = message + ("`" + commitContent.id.substring(0, 7) + "` " +
                                commitContent.message.split("\\n", 2)[0] + " [" + commitContent.author.username +
                                "]\n");
                        shouldSend = true;
                    }

                    if (shouldSend)
                        sendMessageToChannels(commit.repository.full_name, event, message);
                } else if (event.equalsIgnoreCase("ping")) {
                    Ping ping = gson.fromJson(request.body(), Ping.class);
                    Instance.log.info("Ping from webhook " + ping.hook_id + " with zen " + ping.zen);
                } else if (event.equalsIgnoreCase("pull_request")) {
                    Instance.log.info("Webhook server received Pull Request POST");
                    PullRequest pr = gson.fromJson(request.body(), PullRequest.class);
                    if (pr.action.contains("opened") || pr.action.equals("closed")) {
                        message = message + (
                                "Pull requests for **" + pr.repository.full_name + "**\n");

                        /*if (pr.action.equals("closed")) {
                            if (pr.pull_request.merged) {
                                message = message + (":white_check_mark:");
                            } else {
                                message = message + (":x:");
                            }
                        } else if (pr.action.contains("opened")) {
                            message = message + (":new:");
                        }*/

                        message = message + (" `#" + pr.number + "` - " + pr.pull_request.title + " [" +
                                pr.pull_request.user.login + "] has been **" + pr.action + "** by " +
                                pr.sender.login + "\n");
                        message = message + ("<" + pr.pull_request.html_url + ">");

                        sendMessageToChannels(pr.repository.full_name, event, message);
                    }
                } else if (event.equalsIgnoreCase("issues")) {
                    Instance.log.info("Webhook server received Issue POST");
                    Issue issue = gson.fromJson(request.body(), Issue.class);

                    if (issue.action.contains("opened") || issue.action.equals("closed")) {
                        message = message + ("Issues for **" + issue.repository.full_name + "**\n");

                        /*if (issue.action.equals("closed")) {
                            message = message + (":lock:");
                        } else if (issue.action.contains("opened")) {
                            message = message + (":unlock:");
                        }*/

                        message = message + (" `#" + issue.issue.number + "` - " + issue.issue.title + " [" +
                                issue.issue.user.login + "] has been **" + issue.action + "** by " +
                                issue.sender.login + "\n");
                        message = message + ("<" + issue.issue.html_url + ">");

                        sendMessageToChannels(issue.repository.full_name, event, message);
                    }
                } else if (event.equalsIgnoreCase("release")) {
                    Instance.log.info("Webhook server received Release POST");
                    Release release = gson.fromJson(request.body(), Release.class);

                    if (!release.release.draft) {
                        message = message + (" New ");
                        if (release.release.prerelease) {
                            message = message + ("*Pre-*");
                        }
                        message = message + "Release for **" + release.repository.full_name + "**\n";
                        message = message + (" `" + release.release.tag_name + "` " + release.release.name +
                                "\n");
                        message = message + ("<" + release.release.html_url + ">");
                        sendMessageToChannels(release.repository.full_name, event, message);
                        //Main.getBot().downloadUpdate(release.release.html_url);
                    }
                }
            } catch (Exception ex) {
                Instance.log.warn("An error has occured in GithubWebhooks", ex);

                response.status(500);
                return "An exception occurred. " + ex.toString();
            }

            // 👌 OK
            response.status(200);
            return "\uD83D\uDC4C OK";
        });

        Instance.log.info("Initialized webhooks on port " + port);
    }

    public GithubWebhooks addChannel(ChannelSettings cs) {
        channels.add(cs);

        return this;
    }

    private void sendMessageToChannels(String repo, String event, String content) {
        /*channels.forEach(cs -> {
            if (!cs.events.contains(event) && !cs.events.contains("*"))
                return;

            if (cs.branches.size() > 0) {
                if (!cs.branches.contains("*") && !cs.branches.contains(repo.toLowerCase(Locale.ROOT)))
                    return;
            }

            IChannel channel = client.getChannelByID(cs.id);

            if (channel != null) {
                Main.getBot().sendMessage(content, channel);
                Instance.log.info("Sent a webhook message to channel " + channel.getName() + " (" + channel.getID() +
                        ") in guild " + (channel.getGuild() == null ? null : channel.getGuild().getName()) +
                        " for event " + event);
            } else {
                Instance.log.warn(
                        "Couldn't find channel " + cs.id + " when sending a webhook message for event " + event + "!");
            }
        });*/
        Instance.log.info("Sent a webhook message to channels for event " + event);
        Main.getBot().sendMessage(content, Main.getBot().client.getChannelByID("161155978199302144"));
    }

    private static class ChannelSettings {

        String id;

        /**
         * If empty, will not accept any events. Use * to indicate any event.
         */

        List<String> events = new ArrayList<>();
        /**
         * If empty, takes all repos. Syntax is name/repo.
         */

        List<String> branches = new ArrayList<>();

        public ChannelSettings(String id) {
            this.id = id;
        }

        public ChannelSettings addEvent(String event) {
            events.remove(event);
            events.add(event);

            return this;
        }

        public ChannelSettings removeEvent(String event) {
            events.remove(event);

            return this;
        }

        public ChannelSettings addRepo(String repo) {
            repo = repo.toLowerCase(Locale.ROOT);

            branches.remove(repo);
            branches.add(repo);

            return this;
        }

        public ChannelSettings removeRepo(String repo) {
            branches.remove(repo);

            return this;
        }

    }

}
