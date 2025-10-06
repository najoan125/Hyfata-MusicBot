package com.jagrosh.jmusicbot;

import com.jagrosh.jmusicbot.utils.JSON;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;

public class SyncLyricIsrcTest {
    private static final String ISRC_URL = "https://apic-desktop.musixmatch.com/ws/1.1/track.subtitles.get?format=json&user_language=en&namespace=lyrics_synched&f_subtitle_length_max_deviation=1&subtitle_format=mxm&app_id=web-desktop-app-v1.0&usertoken=";

    public static void main(String[] args) throws IOException {
        String isrc = "TCACO1699220"; //QZEKE2084155 ZZA0P2305775 TCJPQ2122112 TCACO1699220 JPU902502821
        JSONObject json = JSON.getJsonObjectFromConnection(getMusixmatchConnectionByIsrc(isrc));
        System.out.println(json);
        String lyricArrayString = json
                .getJSONObject("message")
                .getJSONObject("body")
                .getJSONArray("subtitle_list")
                .getJSONObject(0)
                .getJSONObject("subtitle")
                .getString("subtitle_body");

        System.out.println(json
                .getJSONObject("message")
                .getJSONObject("body")
                .getJSONArray("subtitle_list")
                .getJSONObject(0)
                .getJSONObject("subtitle")
                .getString("subtitle_language"));
        for (String line : getTimeLyricLinkedHashMap(new JSONArray(lyricArrayString)).values())
            System.out.println(line);
    }

    @NotNull
    private static LinkedHashMap<Double, String> getTimeLyricLinkedHashMap(JSONArray lyricArray) {
        LinkedHashMap<Double, String> result = new LinkedHashMap<>(); // time, lyricText
        for (int i = 0; i < lyricArray.length(); i++) {
            JSONObject lyricObject = lyricArray.getJSONObject(i);
            JSONObject timeObject = lyricObject.getJSONObject("time");

            String text = lyricObject.getString("text");
            double time = timeObject.getDouble("total");

            if (text.isEmpty()) {
                if (i == lyricArray.length() - 1) {
                    text = ""; // end
                } else {
                    text = "♪"; // interlude
                }
            }
            result.put(time, text);
        }
        return result;
    }

    @NotNull
    private static HttpURLConnection getMusixmatchConnectionByIsrc(String isrc) throws IOException {
        String query = "&track_isrc=" + URLEncoder.encode(isrc, StandardCharsets.UTF_8);
        URL url = new URL(ISRC_URL + query);

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Cookie", "AWSELB=55578B011601B1EF8BC274C33F9043CA947F99DCFF0A80541772015CA2B39C35C0F9E1C932D31725A7310BCAEB0C37431E024E2B45320B7F2C84490C2C97351FDE34690157");
        connection.setRequestProperty("Origin", "musixmatch.com");
        return connection;
    }
}
