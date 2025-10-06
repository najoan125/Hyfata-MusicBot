package com.jagrosh.jmusicbot.synclyric;

import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.audio.AudioHandler;
import com.jagrosh.jmusicbot.utils.FormatUtil;
import com.jagrosh.jmusicbot.utils.TimeUtil;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditData;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class SyncLyric {
    private final Bot bot;
    private final AudioHandler audioHandler;
    private final Guild guild;

    private String trackUri;
    private LinkedHashMap<Double, String> lyricLinesByTime;
    private ArrayList<Double> lyricTimestamps;
    private List<String> translations;
    private List<String> tlits;
    private int currentLyricIndex = 0;
    private double ping; // seconds

    public SyncLyric(Bot bot, AudioHandler audioHandler, Guild guild) {
        this.bot = bot;
        this.audioHandler = audioHandler;
        this.guild = guild;
    }

    private void initTrack(AudioTrack track) throws Exception {
        SyncLyricAPI api = new SyncLyricAPI();
        this.trackUri = track.getInfo().uri;

        if (track.getInfo().isrc != null) {
            this.lyricLinesByTime = api.getLyricByIsrc(bot, track.getInfo().isrc);
        } else {
            this.lyricLinesByTime = api.getLyric(
                    bot,
                    track.getInfo().title,
                    track.getInfo().author.replace(" - Topic", "")
            );
        }
        this.lyricTimestamps = new ArrayList<>(lyricLinesByTime.keySet());
        this.translations = api.getTranslations();
        this.tlits = api.getTlits();
    }

    public MessageEditData getLyric(JDA jda, long ping) throws Exception {
        this.ping = FormatUtil.formatTimeDouble(ping);

        AudioTrack track = getPlayingTrack();

        if (audioHandler.isMusicPlaying(jda) && isNotTTS(track)) {
            if (!track.getInfo().uri.equals(trackUri)) {
                initTrack(track);
            }
            return generateLyricMessage();
        }
        return null;
    }

    boolean hasTTSPlayed = false;
    protected MessageEditData pollLyricUpdate(JDA jda) throws Exception {
        AudioTrack track = getPlayingTrack();
        if (!isNotTTS(track)) {
            hasTTSPlayed = true;
        }
        if (audioHandler.isMusicPlaying(jda) && isNotTTS(track)) {
            // if the track changed
            if (!track.getInfo().uri.equals(trackUri)) {
                hasTTSPlayed = false;
                initTrack(track);
                return generateLyricMessage();
            }

            // if not loaded yet
            if (lyricTimestamps == null || lyricTimestamps.isEmpty()) {
                return null;
            }

            // When played again(Repeat) || track position moved back
            if (currentLyricIndex > -1 && getTrackPosition(track) < getCurrentAdjustedTime()) {
                if (hasTTSPlayed) {
                    return null;
                }
                return generateLyricMessage();
            }

            // When track position moved forward
            if (currentLyricIndex < getLastLyricIndex() && getTrackPosition(track) >= getNextAdjustedTime()) {
                hasTTSPlayed = false;
                return generateLyricMessage();
            }
        }
        return null;
    }

    private MessageEditData generateLyricMessage() {
        AudioTrack track = getPlayingTrack();
        MessageCreateBuilder mb = new MessageCreateBuilder();
        EmbedBuilder eb = new EmbedBuilder();

        calculateCurrentLyricIndex(getTrackPosition(track));
        eb.setColor(guild.getSelfMember().getColor());
        eb.setDescription(generateLyricString()); // sync lyric

        // playing text
        mb.addContent("`오프셋: ").addContent(FormatUtil.secondsToMillis(this.ping) + "ms`\n")
                .addContent("**")
                .addContent(bot.getConfig().getSuccess()).addContent(" ")
                .addContent(track.getInfo().author)
                .addContent(" - ")
                .addContent(track.getInfo().title)
                .addContent(" 재생 중...**\n");

        // progress bar
        double progress = (double) track.getPosition() / track.getDuration();
        mb.addContent(audioHandler.getStatusEmoji()).addContent(" ")
                .addContent(FormatUtil.progressBar(progress, "http://a"))
                .addContent(" `")
                .addContent(TimeUtil.formatTime(track.getPosition())).addContent(" / ").addContent(TimeUtil.formatTime(track.getDuration()))
                .addContent("`\n\n");

        // lyric
        if (currentLyricIndex >= 0) {
            if (tlits != null) {
                mb.addContent("**").addContent(tlits.get(currentLyricIndex)).addContent("**\n");
            }
            if (translations != null) {
                mb.addContent(translations.get(currentLyricIndex)).addContent("\n");
            }
            if (tlits != null || translations != null)
                mb.addContent("\n번역, 발음: [Papago](https://papago.naver.com)\n");
        }
        mb.addContent("가사 제공: [Musixmatch](https://www.musixmatch.com)");
        return MessageEditData.fromCreateData(mb.setEmbeds(eb.build()).build());
    }

    private String generateLyricString() {
        StringBuilder result = new StringBuilder();
        int firstIndex;
        int lastIndex;
        int totalLyricSize = lyricLinesByTime.size();
        if (getLastLyric().isEmpty())
            totalLyricSize--;

        if (totalLyricSize <= 3) {
            firstIndex = 0;
            lastIndex = totalLyricSize - 1;
        } else if (currentLyricIndex <= 0) { // before first lyric and first lyric
            firstIndex = 0;
            lastIndex = 2;
        } else if (totalLyricSize > currentLyricIndex + 1) { // middle lyric
            firstIndex = currentLyricIndex - 1;
            lastIndex = currentLyricIndex + 1;
        } else { // last lyric
            firstIndex = totalLyricSize - 3;
            lastIndex = totalLyricSize - 1;
        }

        for (int i = firstIndex; i <= lastIndex; i++) {
            if (i == currentLyricIndex)
                result.append("# ");
            else
                result.append("### ");

            result.append(getLyric(i)).append("\n");
        }

        return result.toString();
    }

    private void calculateCurrentLyricIndex(double trackPosition) {
        if (trackPosition < getFirstAdjustedTime()) {
            currentLyricIndex = -1;
            return;
        }

        for (int i = 0; i < lyricTimestamps.size(); i++) {
            if (trackPosition >= getAdjustedTime(i))
                currentLyricIndex = i;
            else
                break;
        }
    }

    private boolean isNotTTS(AudioTrack track) {
        return track.getInfo() == null || track.getInfo().uri == null || !track.getInfo().uri.startsWith("TTS");
    }

    private AudioTrack getPlayingTrack() {
        return audioHandler.getPlayer().getPlayingTrack();
    }

    private double getTrackPosition(AudioTrack track) {
        return FormatUtil.formatTimeDouble(track.getPosition());
    }

    private double getAdjustedTime(int index) {
        return this.lyricTimestamps.get(index) - this.ping;
    }

    private double getFirstAdjustedTime() {
        return getAdjustedTime(0);
    }

    private double getCurrentAdjustedTime() {
        return getAdjustedTime(currentLyricIndex);
    }

    private double getNextAdjustedTime() {
        return getAdjustedTime(currentLyricIndex + 1);
    }

    private int getLastTimeIndex() {
        return lyricTimestamps.size() - 1;
    }

    private int getLastLyricIndex() {
        return lyricLinesByTime.size() - 1;
    }

    private String getLyric(int index) {
        return lyricLinesByTime.get(lyricTimestamps.get(index));
    }

    private String getLastLyric() {
        return getLyric(getLastTimeIndex());
    }
}
