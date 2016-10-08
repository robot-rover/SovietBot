package rr.industries.commands;

import rr.industries.exceptions.BotException;
import rr.industries.exceptions.IncorrectArgumentsException;
import rr.industries.util.*;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static javax.sound.sampled.AudioSystem.getAudioInputStream;
import static sx.blah.discord.util.audio.AudioPlayer.getAudioPlayerForGuild;

@CommandInfo(
        commandName = "rekt",
        helpText = "Plays a slightly obnoxious sound in voice chat.",
        permLevel = Permissions.REGULAR
)
public class Rekt implements Command {
    private List<Entry<String, AudioInputStream>> sfx;

    public Rekt() {
        sfx = new ArrayList<>();
        try {
            sfx.add(new Entry<>("wombo", getAudioInputStream(resourceLoader.getResource("ohs/womboCombo.mp3"))));
            sfx.add(new Entry<>("wrong", getAudioInputStream(resourceLoader.getResource("ohs/wrongNumber.mp3"))));
            sfx.add(new Entry<>("airhorn", getAudioInputStream(resourceLoader.getResource("ohs/violinAirhorn.mp3"))));
            sfx.add(new Entry<>("never", getAudioInputStream(resourceLoader.getResource("ohs/noOneHasEver.mp3"))));
            sfx.add(new Entry<>("scope", getAudioInputStream(resourceLoader.getResource("ohs/noscoped.mp3"))));
            sfx.add(new Entry<>("nope", getAudioInputStream(resourceLoader.getResource("ohs/nopeSong.mp3"))));
        } catch (IOException | UnsupportedAudioFileException ex) {
            LOG.warn("Error initializing audio streams", ex);
        }
    }

    @SubCommand(name = "", Syntax = {
            @Syntax(helpText = "Plays a random rekt clip", args = {}),
            @Syntax(helpText = "Plays the specified rekt clip", args = {Arguments.TEXT}, options = {"wombo", "wrong", "airhorn", "never", "scope", "nope"})
    })
    public void execute(CommContext cont) throws BotException {
        try {
            if (cont.getArgs().size() > 1) {
                Optional<Entry<String, AudioInputStream>> entry = sfx.stream().filter(v -> v.first().equals(cont.getArgs().get(1))).findAny();
                if (entry.isPresent())
                    getAudioPlayerForGuild(cont.getMessage().getGuild()).queue(entry.get().second());
                else
                    throw new IncorrectArgumentsException("`" + cont.getArgs().get(1) + "` is not the name of a clip");
            } else {

                getAudioPlayerForGuild(cont.getMessage().getGuild()).queue(sfx.get(rn.nextInt(sfx.size())).second());
            }
        } catch (IOException ex) {
            throw new InternalError("IOException on Rekt Command", ex);
        }
    }
}
