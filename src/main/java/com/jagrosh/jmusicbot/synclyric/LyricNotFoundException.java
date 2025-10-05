package com.jagrosh.jmusicbot.synclyric;

public class LyricNotFoundException extends Exception {
    public LyricNotFoundException() {
        super("Lyric not found!");
    }
}
