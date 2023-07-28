package com.jagrosh.jmusicbot.utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Base64;
import java.util.Objects;

public class AudioUtil {

    /**
     * @param lang ko, en, ja, etc...
     * @throws TTSTooLongException when text's length(Byte) is longer than 200
     */
    public static String getTTSBase64(String lang, String text) throws IOException, TTSTooLongException {
        if (text.length() <= 200) {
            String tts = "https://www.google.com/async/translate_tts?yv=3&ttsp=tl:" + lang + ",txt:" + URLEncoder.encode(text, "UTF-8").replace("%","%25") + ",spd:2&cs=1&async=_fmt:jspb";
            JSONObject jo = JSON.readFromURL(tts, 2);
            JSONArray array = Objects.requireNonNull(jo).getJSONArray("translate_tts");
            return array.getString(0);
        }
        throw new TTSTooLongException("TTS is Too Long!");
    }

    public static String createMP3FromBase64(String base64, String filename) throws IOException {
        byte[] audioData = Base64.getDecoder().decode(base64);
        try (FileOutputStream outputStream = new FileOutputStream(filename + ".mp3")) {
            outputStream.write(audioData);
        }
        return filename + ".mp3";
    }
}