package com.jagrosh.jmusicbot.utils.synclyric;

public class LyricNotFoundException extends Exception {
    public LyricNotFoundException() {
        super("Lyric not found!");
    }
}
