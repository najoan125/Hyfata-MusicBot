/*
 * Copyright 2016 John Grosh <john.a.grosh@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jagrosh.jmusicbot.audio;

import com.jagrosh.jmusicbot.playlist.PlaylistLoader.Playlist;
import com.jagrosh.jmusicbot.queue.AbstractQueue;
import com.jagrosh.jmusicbot.settings.QueueType;
import com.jagrosh.jmusicbot.settings.RepeatMode;
import com.jagrosh.jmusicbot.utils.synclyric.LyricNotFoundException;
import com.jagrosh.jmusicbot.utils.synclyric.SyncLyricUtil;
import com.jagrosh.jmusicbot.settings.Settings;
import com.jagrosh.jmusicbot.utils.FormatUtil;
import com.jagrosh.jmusicbot.utils.TimeUtil;

import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioTrack;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.ByteBuffer;
import java.util.*;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.audio.AudioSendHandler;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;

import org.jetbrains.annotations.NotNull;
import org.slf4j.LoggerFactory;

/**
 * @author John Grosh <john.a.grosh@gmail.com>
 */
public class AudioHandler extends AudioEventAdapter implements AudioSendHandler {
    public final static String PLAY_EMOJI = "<:play:1131227162805018634>"; // ▶
    public final static String PAUSE_EMOJI = "<:pause:1131234191984578681>"; // ⏸
    //public final static String STOP_EMOJI = "<:stop:1131234190067761173>"; // ⏹
    public final static String NEXT_EMOJI = "<:next:1131227160846274582>";

    private final List<AudioTrack> defaultQueue = new LinkedList<>();
    private final Set<String> votes = new HashSet<>();

    private final PlayerManager manager;
    private final AudioPlayer audioPlayer;
    private final long guildId;

    private AudioFrame lastFrame;
    private AbstractQueue<QueuedTrack> queue;

    protected AudioHandler(PlayerManager manager, Guild guild, AudioPlayer player) {
        this.manager = manager;
        this.audioPlayer = player;
        this.guildId = guild.getIdLong();

        this.setQueueType(manager.getBot().getSettingsManager().getSettings(guildId).getQueueType());
    }

    public void setQueueType(QueueType type) {
        queue = type.createInstance(queue);
    }

    public int addTrackToFront(QueuedTrack qtrack) {
        if (audioPlayer.getPlayingTrack() == null) {
            audioPlayer.playTrack(qtrack.getTrack());
            return -1;
        } else {
            queue.addAt(0, qtrack);
            return 0;
        }
    }

    public int addTrack(QueuedTrack qtrack) {
        if (audioPlayer.getPlayingTrack() == null) {
            audioPlayer.playTrack(qtrack.getTrack());
            return -1;
        } else
            return queue.add(qtrack);
    }

    public AbstractQueue<QueuedTrack> getQueue() {
        return queue;
    }

    public void stopAndClear() {
        queue.clear();
        defaultQueue.clear();
        audioPlayer.stopTrack();
        //current = null;
    }

    public boolean isMusicPlaying(JDA jda) {
        return Objects.requireNonNull(guild(jda).getSelfMember().getVoiceState()).inVoiceChannel() && audioPlayer.getPlayingTrack() != null;
    }

    public Set<String> getVotes() {
        return votes;
    }

    public AudioPlayer getPlayer() {
        return audioPlayer;
    }

    public RequestMetadata getRequestMetadata() {
        if (audioPlayer.getPlayingTrack() == null)
            return RequestMetadata.EMPTY;
        RequestMetadata rm = audioPlayer.getPlayingTrack().getUserData(RequestMetadata.class);
        return rm == null ? RequestMetadata.EMPTY : rm;
    }

    public boolean playFromDefault() {
        if (!defaultQueue.isEmpty()) {
            audioPlayer.playTrack(defaultQueue.remove(0));
            return true;
        }
        Settings settings = manager.getBot().getSettingsManager().getSettings(guildId);
        if (settings == null || settings.getDefaultPlaylist() == null)
            return false;

        Playlist pl = manager.getBot().getPlaylistLoader().getPlaylist(settings.getDefaultPlaylist());
        if (pl == null || pl.getItems().isEmpty())
            return false;
        pl.loadTracks(manager, (at) ->
        {
            if (audioPlayer.getPlayingTrack() == null)
                audioPlayer.playTrack(at);
            else
                defaultQueue.add(at);
        }, () ->
        {
            if (pl.getTracks().isEmpty() && !manager.getBot().getConfig().getStay())
                manager.getBot().closeAudioConnection(guildId);
        });
        return true;
    }

    // Audio Events
    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        RepeatMode repeatMode = manager.getBot().getSettingsManager().getSettings(guildId).getRepeatMode();
        // if the track ended normally, and we're in repeat mode, re-add it to the queue
        String trackUri = track.getInfo().uri;
        if (endReason == AudioTrackEndReason.FINISHED && repeatMode != RepeatMode.OFF && !trackUri.startsWith("TTS")) {
            QueuedTrack clone = new QueuedTrack(track.makeClone(), track.getUserData(RequestMetadata.class));
            if (repeatMode == RepeatMode.ALL)
                queue.add(clone);
            else
                queue.addAt(0, clone);
        }

        if (trackUri.startsWith("TTS")) {
            String filepath = trackUri.split(" ")[1];
            Path fileToDelete = Paths.get(filepath);
            try {
                Files.delete(fileToDelete);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (queue.isEmpty()) {
            if (!playFromDefault()) {
                manager.getBot().getNowplayingHandler().onTrackUpdate(null);
                if (!manager.getBot().getConfig().getStay())
                    manager.getBot().closeAudioConnection(guildId);
                // unpause, in the case when the player was paused and the track has been skipped.
                // this is to prevent the player being paused next time it's being used.
                player.setPaused(false);
            }
        } else {
            QueuedTrack qt = queue.pull();
            player.playTrack(qt.getTrack());
        }
    }

    @Override
    public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException exception) {
        LoggerFactory.getLogger("AudioHandler").error("Track " + track.getIdentifier() + " has failed to play", exception);
    }

    @Override
    public void onTrackStart(AudioPlayer player, AudioTrack track) {
        votes.clear();
        manager.getBot().getNowplayingHandler().onTrackUpdate(track);
    }


    // Formatting
    public Message getNowPlaying(JDA jda) {
        if (isMusicPlaying(jda)) {
            Guild guild = guild(jda);
            AudioTrack track = audioPlayer.getPlayingTrack();
            MessageBuilder mb = new MessageBuilder();
            mb.append(FormatUtil.filter(manager.getBot().getConfig().getSuccess() + " **현재 " + Objects.requireNonNull(Objects.requireNonNull(guild.getSelfMember().getVoiceState()).getChannel()).getAsMention() + "에서 재생중...**"));
            EmbedBuilder eb = new EmbedBuilder();
            eb.setColor(guild.getSelfMember().getColor());
            RequestMetadata rm = getRequestMetadata();
            if (rm.getOwner() != 0L) {
                User u = guild.getJDA().getUserById(rm.user.id);
                if (u == null)
                    eb.setAuthor(FormatUtil.formatUsername(rm.user), null, rm.user.avatar);
                else
                    eb.setAuthor(FormatUtil.formatUsername(u), null, u.getEffectiveAvatarUrl());
            }

            try {
                eb.setTitle(track.getInfo().title, track.getInfo().uri);
            } catch (Exception e) {
                eb.setTitle(track.getInfo().title);
            }

            if (track instanceof YoutubeAudioTrack && manager.getBot().getConfig().useNPImages()) {
                eb.setThumbnail("https://img.youtube.com/vi/" + track.getIdentifier() + "/mqdefault.jpg");
            }

            String trackInfo = "";
            if (track.getInfo().author != null && !track.getInfo().author.isEmpty())
                trackInfo = "**" + track.getInfo().author + "**";

            double progress = (double) audioPlayer.getPlayingTrack().getPosition() / track.getDuration();
            RepeatMode repeatMode = manager.getBot().getSettingsManager().getSettings(guildId).getRepeatMode();
            eb.setDescription(
                    trackInfo + "\n\n"
                            + getStatusEmoji()
                            + " " + FormatUtil.progressBar(progress)
                            + " `" + TimeUtil.formatTime(track.getPosition()) + " / " + TimeUtil.formatTime(track.getDuration()) + "` \n\n"
                            + repeatMode.getEmoji() + " **" + getRepeatModeName(repeatMode) + "** | "
                            + FormatUtil.volumeIcon(audioPlayer.getVolume()) + " **" + audioPlayer.getVolume() + "%**"
            );
            ActionRow actionRow1 = ActionRow.of(
                    Button.secondary("pause", Emoji.fromMarkdown(getReverseStatusEmoji())),
                    Button.secondary("next", Emoji.fromMarkdown(NEXT_EMOJI))
            );
            ActionRow actionRow2 = ActionRow.of(
                    Button.secondary("volumeDown", Emoji.fromUnicode("\uD83D\uDD09")),
                    Button.secondary("volumeUp", Emoji.fromUnicode("\uD83D\uDD0A")),
                    Button.secondary("repeat", Emoji.fromMarkdown(repeatMode.getEmoji()))
            );
            mb.setActionRows(actionRow1, actionRow2);
            return mb.setEmbeds(eb.build()).build();
        } else return null;
    }

    AudioTrack track;
    LinkedHashMap<Double, String> lyrics;
    ArrayList<Double> lyricTimes;
    int currentLyricIndex = 0;
    double ping;

    public Message getInitLyric(Guild guild, AudioTrack track) throws LyricNotFoundException, IOException {
        if (track.getInfo().isrc != null) {
            lyrics = SyncLyricUtil.getLyricByIsrc(track.getInfo().isrc);
        } else {
            lyrics = SyncLyricUtil.getLyric(track.getInfo().title, track.getInfo().author.replace(" - Topic", ""));
        }
        this.track = track;
        lyricTimes = new ArrayList<>(lyrics.keySet());
        return getNewLyricMessage(guild, track);
    }

    public Message getLyric(JDA jda, long ping) throws LyricNotFoundException, IOException {
        this.ping = FormatUtil.formatTimeDouble(ping);
        AudioTrack track = audioPlayer.getPlayingTrack();
        if (isMusicPlaying(jda) && track != null && !(track.getInfo() != null && track.getInfo().uri != null && track.getInfo().uri.startsWith("TTS"))) {
            Guild guild = guild(jda);
            if (this.track == null || !this.track.getInfo().uri.equals(track.getInfo().uri)) {
                return getInitLyric(guild, track);
            }
            return getNewLyricMessage(guild, track);
        }
        return null;
    }

    public Message getSyncLyric(JDA jda) throws LyricNotFoundException, IOException {
        AudioTrack track = audioPlayer.getPlayingTrack();
        if (isMusicPlaying(jda) && track != null && !(track.getInfo() != null && track.getInfo().uri != null && track.getInfo().uri.startsWith("TTS"))) {
            double trackPosition = FormatUtil.formatTimeDouble(track.getPosition());
            Guild guild = guild(jda);

            // init
            if (this.track == null || !this.track.getInfo().uri.equals(track.getInfo().uri)) {
                return getInitLyric(guild, track);
            }

            // currentLyric == lastLyric
            if (lyricTimes.size() == currentLyricIndex + 1) {
                if (trackPosition < lyricTimes.get(currentLyricIndex) - ping) {
                    return getNewLyricMessage(guild, track);
                }
            }
            // ex: When played again from the beginning || current track position >= nextLyric time
            else if (currentLyricIndex != -1 && trackPosition < lyricTimes.get(currentLyricIndex) - ping ||
                    trackPosition >= lyricTimes.get(currentLyricIndex + 1) - ping) {
                return getNewLyricMessage(guild, track);
            }
        }
        return null;
    }

    private void loadCurrentLyricIndex(double trackPosition) {
        int i = 0;
        for (double time : lyricTimes) {
            if (trackPosition >= time - ping) {
                currentLyricIndex = i;
            } else {
                break;
            }
            i++;
        }
    }

    private Message getNewLyricMessage(Guild guild, AudioTrack track) {
        double trackPosition = FormatUtil.formatTimeDouble(track.getPosition());
        MessageBuilder mb = new MessageBuilder();
        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(guild.getSelfMember().getColor());

        if (trackPosition < lyricTimes.get(0) - ping) {
            currentLyricIndex = -1;
        } else {
            loadCurrentLyricIndex(trackPosition);
        }
        return getLyricMessage(mb, eb);
    }

    @NotNull
    private Message getLyricMessage(MessageBuilder mb, EmbedBuilder eb) {
        if (currentLyricIndex == -1) {
            eb.setDescription(
                    " \n \n### " + lyrics.get(lyricTimes.get(0))
            );
        } else if (currentLyricIndex == 0) {
            eb.setDescription(
                    " \n"
                            + "# " + lyrics.get(lyricTimes.get(currentLyricIndex)) + "\n### "
                            + lyrics.get(lyricTimes.get(currentLyricIndex + 1))
            );
        } else if (lyrics.size() != currentLyricIndex + 1) {
            String nextLyric = lyrics.get(lyricTimes.get(currentLyricIndex + 1));
            if (!nextLyric.isEmpty()) {
                nextLyric = "### " + nextLyric;
            }
            eb.setDescription(
                    "### " + lyrics.get(lyricTimes.get(currentLyricIndex - 1))
                            + "\n"
                            + "# " + lyrics.get(lyricTimes.get(currentLyricIndex)) + "\n"
                            + nextLyric
            );
        } else {
            String lyric = lyrics.get(lyricTimes.get(currentLyricIndex));
            if (!lyric.isEmpty()) {
                lyric = "# " + lyric;
            }
            eb.setDescription(
                    "### " + lyrics.get(lyricTimes.get(currentLyricIndex - 1))
                            + "\n"
                            + lyric + "\n"
                            + " "
            );
        }
        eb.setFooter("가사 제공: Musixmatch");
        mb.append("**").append(manager.getBot().getConfig().getSuccess())
                .append(" ")
                .append(track.getInfo().author)
                .append(" - ").append(track.getInfo().title).append(" 재생 중...**");

        double progress = (double) audioPlayer.getPlayingTrack().getPosition() / track.getDuration();
        mb.append("\n")
                .append(getStatusEmoji()).append(" ")
                .append(FormatUtil.progressBar(progress))
                .append(" `")
                .append(TimeUtil.formatTime(audioPlayer.getPlayingTrack().getPosition())).append(" / ").append(TimeUtil.formatTime(track.getDuration()))
                .append("`");
        return mb.setEmbeds(eb.build()).build();
    }

    private String getRepeatModeName(RepeatMode repeatMode) {
        if (repeatMode == RepeatMode.OFF) {
            return "반복 안함";
        } else if (repeatMode == RepeatMode.ALL) {
            return "전체 반복";
        }
        return "단일 반복";
    }

    public Message getNoMusicPlaying(JDA jda) {
        Guild guild = guild(jda);
        return new MessageBuilder()
                .setContent(FormatUtil.filter(manager.getBot().getConfig().getSuccess() + " **재생중인 음악 없음...**"))
                .setEmbeds(new EmbedBuilder()
                        .setTitle("현재 재생중인 음악이 없습니다!")
                        .setDescription("`;재생` 또는 `;검색` 명령어를 이용해 음악을 재생하세요!")
                        .setColor(guild.getSelfMember().getColor())
                        .build()).build();
    }

    public String getStatusEmoji() {
        return audioPlayer.isPaused() ? PAUSE_EMOJI : PLAY_EMOJI;
    }

    public String getReverseStatusEmoji() {
        return audioPlayer.isPaused() ? PLAY_EMOJI : PAUSE_EMOJI;
    }

    // Audio Send Handler methods
    /*@Override
    public boolean canProvide() 
    {
        if (lastFrame == null)
            lastFrame = audioPlayer.provide();

        return lastFrame != null;
    }

    @Override
    public byte[] provide20MsAudio() 
    {
        if (lastFrame == null) 
            lastFrame = audioPlayer.provide();

        byte[] data = lastFrame != null ? lastFrame.getData() : null;
        lastFrame = null;

        return data;
    }*/

    @Override
    public boolean canProvide() {
        lastFrame = audioPlayer.provide();
        return lastFrame != null;
    }

    @Override
    public ByteBuffer provide20MsAudio() {
        return ByteBuffer.wrap(lastFrame.getData());
    }

    @Override
    public boolean isOpus() {
        return true;
    }


    // Private methods
    private Guild guild(JDA jda) {
        return jda.getGuildById(guildId);
    }
}