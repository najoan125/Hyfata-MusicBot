package com.jagrosh.jmusicbot.utils.synclyric;

import org.apache.http.client.HttpResponseException;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;

/*
 * API: https://github.com/OrfiDev/orpheusdl-musixmatch/blob/master/musixmatch_api.py
 */
public class SyncLyricUtil {

    private static final String BASE_URL = "https://apic-desktop.musixmatch.com/ws/1.1/macro.subtitles.get?format=json&user_language=en&namespace=lyrics_synched&f_subtitle_length_max_deviation=1&subtitle_format=mxm&app_id=web-desktop-app-v1.0&usertoken=201219dbdb0f6aaba1c774bd931d0e79a28024e28db027ae72955c";
    private static final String ISRC_URL = "https://apic-desktop.musixmatch.com/ws/1.1/track.subtitles.get?format=json&user_language=en&namespace=lyrics_synched&f_subtitle_length_max_deviation=1&subtitle_format=mxm&app_id=web-desktop-app-v1.0&usertoken=201219dbdb0f6aaba1c774bd931d0e79a28024e28db027ae72955c";

    public static LinkedHashMap<Double, String> getLyric(String track, String artist) throws IOException, LyricNotFoundException {
        JSONObject json = getJsonObjectFromConnection(getMusixmatchConnection(track, artist));

        JSONObject subtitleObject = json
                .getJSONObject("message")
                .getJSONObject("body")
                .getJSONObject("macro_calls")
                .getJSONObject("track.subtitles.get")
                .getJSONObject("message");
        if (subtitleObject.getJSONObject("header").getInt("status_code") == 404) {
            throw new LyricNotFoundException();
        }

        String lyricArrayString = subtitleObject
                .getJSONObject("body")
                .getJSONArray("subtitle_list")
                .getJSONObject(0)
                .getJSONObject("subtitle")
                .getString("subtitle_body");
        return getTimeLyricLinkedHashMap(new JSONArray(lyricArrayString));
    }

    public static LinkedHashMap<Double, String> getLyricByIsrc(String isrc) throws Exception {
        JSONObject json = getJsonObjectFromConnection(getMusixmatchConnectionByIsrc(isrc));
        int statusCode = json.getJSONObject("message").getJSONObject("header").getInt("status_code");
        switch (statusCode) {
            case 200: break;
            case 404: throw new LyricNotFoundException();
            case 401: throw new Exception("HTTP 401 Error! 사용량이 너무 많아 문제가 발생했을 수 있습니다. 잠시 후에 다시 시도하세요!");
            default: throw new Exception("HTTP " + statusCode + " Error!");
        }
        if (json.getJSONObject("message").getJSONObject("header").getInt("available") == 0) {
            throw new LyricNotFoundException();
        }

        String lyricArrayString = json
                .getJSONObject("message")
                .getJSONObject("body")
                .getJSONArray("subtitle_list")
                .getJSONObject(0)
                .getJSONObject("subtitle")
                .getString("subtitle_body");
        return getTimeLyricLinkedHashMap(new JSONArray(lyricArrayString));
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
    private static HttpURLConnection getMusixmatchConnection(String track, String artist) throws IOException {
        String query = "&q_track=" + URLEncoder.encode(track, StandardCharsets.UTF_8) + "&q_artist=" + URLEncoder.encode(artist, StandardCharsets.UTF_8);
        URL url = new URL(BASE_URL + query);

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Cookie", "AWSELB=55578B011601B1EF8BC274C33F9043CA947F99DCFF0A80541772015CA2B39C35C0F9E1C932D31725A7310BCAEB0C37431E024E2B45320B7F2C84490C2C97351FDE34690157");
        connection.setRequestProperty("Origin", "musixmatch.com");
        return connection;
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

    @NotNull
    private static JSONObject getJsonObjectFromConnection(HttpURLConnection connection) throws IOException {
        JSONObject result;
        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
            reader.close();
            result = new JSONObject(stringBuilder.toString());
        } else {
            throw new HttpResponseException(responseCode, "HTTP Request failed with response code");
        }
        return result;
    }
}
