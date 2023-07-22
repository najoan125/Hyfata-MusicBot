package com.jagrosh.jmusicbot.utils;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.track.*;
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame;
import com.sedmelluq.discord.lavaplayer.track.playback.AudioTrackExecutor;
import com.sedmelluq.discord.lavaplayer.track.playback.LocalAudioTrackExecutor;
import com.sedmelluq.discord.lavaplayer.track.playback.MutableAudioFrame;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class CustomAudioTrack implements AudioTrack, InternalAudioTrack {
    AudioTrack track;
    InternalAudioTrack iaTrack;
    AudioTrackInfo trackInfo;
    public CustomAudioTrack(AudioTrack track, AudioTrackInfo trackInfo){
        this.track = track;
        iaTrack = (InternalAudioTrack) track;
        this.trackInfo = trackInfo;
    }
    @Override
    public AudioTrackInfo getInfo() {
        return trackInfo;
    }

    @Override
    public String getIdentifier() {
        return track.getIdentifier();
    }

    @Override
    public AudioTrackState getState() {
        return track.getState();
    }

    @Override
    public void stop() {
        track.stop();
    }

    @Override
    public boolean isSeekable() {
        return track.isSeekable();
    }

    @Override
    public long getPosition() {
        return track.getPosition();
    }

    @Override
    public void setPosition(long position) {
        track.setPosition(position);
    }

    @Override
    public void setMarker(TrackMarker marker) {
        track.setMarker(marker);
    }

    @Override
    public long getDuration() {
        return track.getDuration();
    }

    @Override
    public AudioTrack makeClone() {
        return track.makeClone();
    }

    @Override
    public AudioSourceManager getSourceManager() {
        return track.getSourceManager();
    }

    @Override
    public void setUserData(Object userData) {
        track.setUserData(userData);
    }

    @Override
    public Object getUserData() {
        return track.getUserData();
    }

    @Override
    public <T> T getUserData(Class<T> klass) {
        return track.getUserData(klass);
    }

    @Override
    public void assignExecutor(AudioTrackExecutor executor, boolean applyPrimordialState) {
        iaTrack.assignExecutor(executor, applyPrimordialState);
    }

    @Override
    public AudioTrackExecutor getActiveExecutor() {
        return iaTrack.getActiveExecutor();
    }

    @Override
    public void process(LocalAudioTrackExecutor executor) throws Exception {
        iaTrack.process(executor);
    }

    @Override
    public AudioTrackExecutor createLocalExecutor(AudioPlayerManager playerManager) {
        return iaTrack.createLocalExecutor(playerManager);
    }

    @Override
    public AudioFrame provide() {
        return iaTrack.provide();
    }

    @Override
    public AudioFrame provide(long timeout, TimeUnit unit) throws TimeoutException, InterruptedException {
        return iaTrack.provide(timeout, unit);
    }

    @Override
    public boolean provide(MutableAudioFrame targetFrame) {
        return iaTrack.provide(targetFrame);
    }

    @Override
    public boolean provide(MutableAudioFrame targetFrame, long timeout, TimeUnit unit) throws TimeoutException, InterruptedException {
        return iaTrack.provide(targetFrame,timeout,unit);
    }
}
