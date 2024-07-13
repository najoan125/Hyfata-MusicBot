package com.jagrosh.jmusicbot.utils;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jmusicbot.audio.AudioHandler;
import com.jagrosh.jmusicbot.audio.QueuedTrack;
import com.jagrosh.jmusicbot.audio.RequestMetadata;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Message;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

public class TTSResultHandler implements AudioLoadResultHandler {
    CommandEvent event;
    String filepath;
    Message m;
    boolean sPgmld = false;
    String sPgmldStr = "";

    public TTSResultHandler(CommandEvent event, String filepath, Message message){
        this.event = event;
        this.filepath = filepath;
        this.m = message;
    }
    public TTSResultHandler(CommandEvent event, String filepath, Message message, boolean sPgmld, String text){
        this.event = event;
        this.filepath = filepath;
        this.m = message;
        this.sPgmld = sPgmld;
        sPgmldStr = text;
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
            if (!sPgmld)
                m.editMessage(event.getClient().getSuccess()+" TTS를 재생합니다(현재 재생 중인 TTS가 끝나면 재생됩니다)").queue();
            else
                m.editMessage(event.getClient().getSuccess()+" TTS를 재생합니다(현재 재생 중인 TTS가 끝나면 재생됩니다)\n녜힁: "+sPgmldStr).queue();
        } else {
            if (!sPgmld)
                m.editMessage(event.getClient().getSuccess()+" TTS를 재생합니다").queue();
            else
                m.editMessage(event.getClient().getSuccess()+" TTS를 재생합니다\n녜힁: "+sPgmldStr).queue();
        }

        String newTitle = UserUtil.getUserCustomNickname(event.getMember()) +"님의 TTS";
        String newAuthor = "TTS";
        CustomAudioTrack at = new CustomAudioTrack(track, getChangedTrackInfo(track,newTitle,newAuthor, "TTS "+filepath));

        if (Objects.requireNonNull(handler).getNowPlaying(event.getJDA()) != null && !isTTS) {
            AudioTrack cloned = nowPlaying.makeClone();
            cloned.setPosition(nowPlaying.getPosition());
            handler.addTrackToFront(new QueuedTrack(cloned, handler.getRequestMetadata()));
            handler.addTrackToFront(new QueuedTrack(at, RequestMetadata.fromResultHandler(at, event)));
            handler.getPlayer().stopTrack();
        }
        else {
            handler.addTrackToFront(new QueuedTrack(at, RequestMetadata.fromResultHandler(at, event)));
        }

        if (!event.getGuild().getAudioManager().isConnected()){
            GuildVoiceState userState = event.getMember().getVoiceState();
            event.getGuild().getAudioManager().openAudioConnection(Objects.requireNonNull(userState).getChannel());
        }
    }

    @Override
    public void playlistLoaded(AudioPlaylist playlist) {

    }

    @Override
    public void noMatches() {
        m.editMessage(event.getClient().getError()+" 트랙을 찾을 수 없습니다.").queue();
        Path fileToDelete = Paths.get(filepath);
        try {
            Files.delete(fileToDelete);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void loadFailed(FriendlyException exception) {
        m.editMessage(event.getClient().getError()+" TTS 로드를 실패했습니다! 관리자에게 문의하세요!").queue();
        exception.printStackTrace();
        Path fileToDelete = Paths.get(filepath);
        try {
            Files.delete(fileToDelete);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static AudioTrackInfo getChangedTrackInfo(AudioTrack track, String title, String author, String uri){
        AudioTrackInfo oldTrackInfo = track.getInfo();

        return new AudioTrackInfo(
                title, author, oldTrackInfo.length/2, oldTrackInfo.identifier, oldTrackInfo.isStream, uri
        );
    }
}
