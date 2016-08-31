package rr.industries.commands;

import net.dv8tion.d4j.player.MusicPlayer;
import net.dv8tion.jda.player.Playlist;
import net.dv8tion.jda.player.source.AudioInfo;
import net.dv8tion.jda.player.source.AudioSource;
import rr.industries.util.BotActions;
import rr.industries.util.CommContext;
import rr.industries.util.Logging;
import sx.blah.discord.handle.audio.IAudioProvider;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.MessageBuilder;
import sx.blah.discord.util.audio.AudioPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import static sx.blah.discord.util.audio.AudioPlayer.getAudioPlayerForGuild;

/**
 * Created by Sam on 8/28/2016.
 */
//todo: Music Loading Progress Bar
public class Music extends Command {
    public Music() {
        commandName = "music";
        helpText = "Plays YouTube and Sound Cloud music.";
    }

    @Override
    public void execute(CommContext cont) {
        AudioPlayer aPlayer = getAudioPlayerForGuild(cont.getMessage().getMessage().getGuild());
        if (cont.getArgs().size() < 2) {
            Logging.missingArgs(cont.getMessage(), "music", cont.getArgs(), LOG);
        } else if (cont.getArgs().get(1).equals("skip")) {
            IAudioProvider provider = aPlayer.getCurrentTrack().getProvider();
            if (provider instanceof MusicPlayer) {
                ((MusicPlayer) provider).skipToNext();
            } else {
                aPlayer.skip();
            }
        } else if (cont.getArgs().get(1).equals("queue")) {
            List<AudioPlayer.Track> playlist = aPlayer.getPlaylist();
            ArrayList<String> messageLines = new ArrayList<>();
            int i = 1;
            for (AudioPlayer.Track track : playlist) {
                if (track.getProvider() instanceof MusicPlayer) {
                    MusicPlayer player = (MusicPlayer) track.getProvider();
                    AudioSource currentSource = player.getCurrentAudioSource();
                    messageLines.add("```Now Playing - [" + currentSource.getInfo().getDuration().getTimestamp() + "] - " + currentSource.getInfo().getTitle() + " - Now Playing");
                    for (AudioSource source : player.getAudioQueue()) {
                        messageLines.add(String.format("%1$" + 12 + "s.", i) + " [" + source.getInfo().getDuration().getTimestamp() + "] - " + source.getInfo().getTitle());
                        i++;
                    }
                }
            }
            int characters = 0;
            int line = 0;
            String message = "";
            for (String s : messageLines) {
                characters += s.length();
                if (characters >= 1970 || line > 11) {
                    message = message + "            + " + (messageLines.size() - line) + " more...";
                    break;
                } else {
                    message = message + s + "\n";
                }
                line++;

            }
            if (message.equals("")) {
                message = "```Queue is Empty";
            }
            message = message + "```";
            IMessage delete = BotActions.sendMessage(new MessageBuilder(cont.getClient()).withContent(message).withChannel(cont.getMessage().getMessage().getChannel()));
            BotActions.delayDelete(delete, 15000);
        } else {
            LOG.info("starting music");
            MusicPlayer player = new MusicPlayer();
            player.setVolume(1);
            aPlayer.queue(player);
            String url = cont.getArgs().get(1);
            LOG.info("making playlist");
            Playlist playlist;
            try {
                playlist = Playlist.getPlaylist(url);
            } catch (NullPointerException ex) {
                LOG.warn("The YT-DL playlist process resulted in a null or zero-length INFO!");
                return;
            }
            LOG.info("got playlist");
            ConcurrentLinkedQueue<AudioSource> sources = new ConcurrentLinkedQueue<>(playlist.getSources());
            LOG.info("playlist into array");
            for (AudioSource source : sources) {
                AudioInfo info = source.getInfo();
                List<AudioSource> queue = player.getAudioQueue();
                if (info.getError() == null) {
                    queue.add(source);
                    if (player.isStopped()) {
                        player.play();
                    }
                } else {
                    LOG.warn("Error in music source, skipping...");
                    sources.remove(source);
                }
            }
            LOG.info("done processing sources");
        }
    }
}
