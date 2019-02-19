package com.sedmelluq.discord.lavaplayer.demo.d4j;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a wrapper around AudioPlayer which makes it behave as an IAudioProvider for D4J. As D4J calls canProvide
 * before every call to provide(), we pull the frame in canProvide() and use the frame we already pulled in
 * provide().
 */
public class AudioProvider extends discord4j.voice.AudioProvider {
  private static final Logger LOG = LoggerFactory.getLogger(AudioProvider.class);
  private final AudioPlayer audioPlayer;
  private AudioFrame lastFrame;

  /**
   * @param audioPlayer Audio player to wrap.
   */
  public AudioProvider(AudioPlayer audioPlayer) {
    this.audioPlayer = audioPlayer;
  }

  @Override
  public boolean provide() {
    if (lastFrame == null) {
      lastFrame = audioPlayer.provide();
    }

    byte[] data = lastFrame != null ? lastFrame.getData() : null;
    lastFrame = null;
    getBuffer().rewind();
    if(data != null) {
      getBuffer().limit(data.length);
      getBuffer().put(data);
      getBuffer().rewind();
    }
    return data != null;
  }
}
