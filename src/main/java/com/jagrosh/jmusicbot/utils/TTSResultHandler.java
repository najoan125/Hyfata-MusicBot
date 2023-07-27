package com.jagrosh.jmusicbot.utils;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jmusicbot.audio.AudioHandler;
import com.jagrosh.jmusicbot.audio.QueuedTrack;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

public class TTSResultHandler implements AudioLoadResultHandler {
    CommandEvent event;
    String filepath;

    public TTSResultHandler(CommandEvent event, String filepath){
        this.event = event;
        this.filepath = filepath;
    }
    @Override
    public void trackLoaded(AudioTrack track) {
        AudioHandler handler = (AudioHandler)event.getGuild().getAudioManager().getSendingHandler();
        AudioTrack nowPlaying = Objects.requireNonNull(handler).getPlayer().getPlayingTrack();

        boolean isTTS;
        if (Objects.requireNonNull(handler).getNowPlaying(event.getJDA()) != null)
            isTTS = nowPlaying.getInfo().uri.startsWith("TTS");
        else
            isTTS = false;

        if (isTTS) {
            event.replySuccess("TTS를 재생합니다(현재 재생 중인 TTS가 끝나면 재생됩니다)");
        } else {
            event.replySuccess("TTS를 재생합니다");
        }

        String newTitle = UserUtil.getUserCustomNickname(event.getMember()) +"님의 TTS";
        String newAuthor = "TTS";
        CustomAudioTrack at = new CustomAudioTrack(track, TrackUtil.getChangedTrackInfo(track,newTitle,newAuthor, "TTS "+filepath));

        if (Objects.requireNonNull(handler).getNowPlaying(event.getJDA()) != null && !isTTS) {
            AudioTrack cloned = nowPlaying.makeClone();
            cloned.setPosition(nowPlaying.getPosition());
            handler.addTrackToFront(new QueuedTrack(cloned, handler.getRequestMetadata()));
            handler.addTrackToFront(new QueuedTrack(at, event.getAuthor()));
            handler.getPlayer().stopTrack();
        }
        else {
            handler.addTrackToFront(new QueuedTrack(at, event.getAuthor()));
        }
    }

    @Override
    public void playlistLoaded(AudioPlaylist playlist) {

    }

    @Override
    public void noMatches() {
        event.replyError("트랙을 찾을 수 없습니다.");
        Path fileToDelete = Paths.get(filepath);
        try {
            Files.delete(fileToDelete);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void loadFailed(FriendlyException exception) {
        event.replyError("TTS 로드를 실패했습니다! 관리자에게 문의하세요!");
        exception.printStackTrace();
        Path fileToDelete = Paths.get(filepath);
        try {
            Files.delete(fileToDelete);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
