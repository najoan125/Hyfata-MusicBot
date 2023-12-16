package com.jagrosh.jmusicbot;

import com.jagrosh.jmusicbot.utils.synclyric.LyricNotFoundException;
import com.jagrosh.jmusicbot.utils.synclyric.SyncLyricUtil;

import java.io.IOException;

public class SyncLyricTest {
    public static void main(String[] args) {
        try {
            String track = "夜に駆ける";
            String artist = "YOASOBI";
            for (double time : SyncLyricUtil.getLyric(track, artist).keySet()) {
                System.out.println(time);
            }
        } catch (LyricNotFoundException e) {
            System.out.println("Lyric not found!");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
