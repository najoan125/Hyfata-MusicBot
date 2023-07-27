package com.jagrosh.jmusicbot.utils;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;

public class TrackUtil {
    public static AudioTrackInfo getChangedTrackInfo(AudioTrack track, String title, String author, String uri){
        AudioTrackInfo oldTrackInfo = track.getInfo();

        return new AudioTrackInfo(
                title, author, oldTrackInfo.length, oldTrackInfo.identifier, oldTrackInfo.isStream, uri
        );
    }
}
