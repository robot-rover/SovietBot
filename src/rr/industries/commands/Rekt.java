package rr.industries.commands;

import net.dv8tion.d4j.player.MusicPlayer;
import net.dv8tion.jda.player.Playlist;
import net.dv8tion.jda.player.source.AudioInfo;
import net.dv8tion.jda.player.source.AudioSource;
import rr.industries.exceptions.BotException;
import rr.industries.exceptions.IncorrectArgumentsException;
import rr.industries.exceptions.InternalError;
import rr.industries.util.*;
import sx.blah.discord.handle.audio.IAudioManager;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static rr.industries.commands.Music.DEFAULT_VOLUME;

@CommandInfo(
        commandName = "rekt",
        helpText = "Plays a slightly obnoxious sound in voice chat.",
        permLevel = Permissions.REGULAR
)
public class Rekt implements Command {
    private List<String[]> sfx;

    public Rekt() {
        sfx = new ArrayList<>();
        sfx.add(new String[]{"wombo", "happy feet, **WOMBO COMBO**", "https://youtu.be/ZrR2fGqCId8"});
        sfx.add(new String[]{"wrong", "SIIIIIKKE, thats the **WRONG NUMBER!!!**", "https://youtu.be/0WBGONGLOpA"});
        sfx.add(new String[]{"airhorn", ":postal_horn: *Eeeeeeeeeeeeeh*", "https://youtu.be/RJ7UPM4L2_g"});
        sfx.add(new String[]{"never", "get **REEEEKT**", "https://youtu.be/YxCiFXXVqSw"});
        sfx.add(new String[]{"scope", "**NOSCOOOOOOPED!!!**", "https://youtu.be/RP5P7zuKkpM"});
        sfx.add(new String[]{"nope", "**NOPE!**", "https://youtu.be/C-eqQqZICiM"});
    }

    @SubCommand(name = "", Syntax = {
            @Syntax(helpText = "Plays a random rekt clip", args = {}),
            @Syntax(helpText = "Plays the specified rekt clip", args = {Arguments.TEXT}, options = {"wombo", "wrong", "airhorn", "never", "scope", "nope"})
    })
    public void execute(CommContext cont) throws BotException {
        String[] link;
        if (cont.getArgs().size() > 1) {
            link = sfx.stream().filter(v -> v[0].equals(cont.getArgs().get(1))).findAny()
                    .orElseThrow(() -> new IncorrectArgumentsException("You must chose one of " + sfx.stream().map(v -> v[0]).collect(Collectors.joining("|"))));
        } else {
            link = sfx.get(rn.nextInt(sfx.size()));
        }
        LOG.info("Chosen {} \"{}\" {}", link[0], link[1], link[2]);
        IAudioManager manager = cont.getMessage().getGuild().getAudioManager();
        MusicPlayer player;
        if (manager.getAudioProvider() instanceof MusicPlayer) {
            player = (MusicPlayer) manager.getAudioProvider();
        } else {
            player = new MusicPlayer();
            player.setVolume(DEFAULT_VOLUME);
            manager.setAudioProvider(player);
        }
        try {
            LOG.info(link[2]);
            Playlist playlist = Playlist.getPlaylist(link[2]);
            List<AudioSource> sources = new LinkedList<>(playlist.getSources());
            if (sources.size() == 1) {
                AudioSource source = sources.get(0);
                AudioInfo info = source.getInfo();
                List<AudioSource> queue = player.getAudioQueue();
                if (info.getError() == null) {
                    queue.add(source);
                    if (player.isStopped())
                        player.play();
                } else {
                    LOG.info(info.getError());
                }
            }
        } catch (NullPointerException ex) {
            throw new InternalError("Error finding Rekt Video", ex);
        }
        cont.getActions().channels().sendMessage(cont.builder().withContent(link[1]));
    }
}
