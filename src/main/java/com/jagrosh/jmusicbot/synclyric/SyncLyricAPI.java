package com.jagrosh.jmusicbot.synclyric;

import com.jagrosh.jmusicbot.Bot;
import org.apache.http.client.HttpResponseException;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;

/*
 * API: https://github.com/OrfiDev/orpheusdl-musixmatch/blob/master/musixmatch_api.py
 */
public class SyncLyricAPI {

    public static LinkedHashMap<Double, String> getLyric(Bot bot, String track, String artist) throws Exception {
        JSONObject json = getJsonObjectFromConnection(getMusixmatchConnection(bot, track, artist));
        int statusCode = json.getJSONObject("message").getJSONObject("header").getInt("status_code");
        errorHandling(statusCode);

        JSONObject subtitleObject = json
                .getJSONObject("message")
                .getJSONObject("body")
                .getJSONObject("macro_calls")
                .getJSONObject("track.subtitles.get")
                .getJSONObject("message");
        int subtitleStatusCode = subtitleObject.getJSONObject("header").getInt("status_code");
        errorHandling(subtitleStatusCode);

        String lyricArrayString = subtitleObject
                .getJSONObject("body")
                .getJSONArray("subtitle_list")
                .getJSONObject(0)
                .getJSONObject("subtitle")
                .getString("subtitle_body");
        return getTimeLyricLinkedHashMap(new JSONArray(lyricArrayString));
    }

    public static LinkedHashMap<Double, String> getLyricByIsrc(Bot bot, String isrc) throws Exception {
        JSONObject json = getJsonObjectFromConnection(getMusixmatchConnectionByIsrc(bot, isrc));
        int statusCode = json.getJSONObject("message").getJSONObject("header").getInt("status_code");
        errorHandling(statusCode);

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

    private static void errorHandling(int statusCode) throws Exception {
        switch (statusCode) {
            case 200:
                break;
            case 404:
                throw new LyricNotFoundException();
            case 401:
                throw new Exception("musixmatch 인증에 실패했습니다! 사용량이 너무 많아 문제가 발생한 듯 합니다. 잠시 후에 다시 시도하세요.");
            case 402:
                throw new Exception("musixmatch 사용 한도에 도달했습니다! 하루 요청 한도를 초과한 듯 합니다. 내일 다시 시도하세요.");
            case 500:
                throw new Exception("앗, 뭔가 잘못됐어요! musixmatch 서버에 오류가 발생한 듯 합니다. 나중에 다시 시도하세요.");
            case 503:
                throw new Exception("현재 musixmatch 시스템이 너무 바빠서 요청을 처리할 수 없습니다! 나중에 다시 시도하세요.");
            default:
                throw new Exception("HTTP " + statusCode + " Error!");
        }
    }

    private static String getAPI(Bot bot, boolean isrc) {
        String method = isrc ? "track" : "macro";
        return "https://apic-desktop.musixmatch.com/ws/1.1/" + method + ".subtitles.get?" +
                "format=json&" +
                "user_language=en&" +
                "namespace=lyrics_synched&" +
                "f_subtitle_length_max_deviation=1&" +
                "subtitle_format=mxm&" +
                "app_id=web-desktop-app-v1.0&" +
                "usertoken=" + bot.getConfig().getMusixmatchToken();
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
    private static HttpURLConnection getMusixmatchConnection(Bot bot, String track, String artist) throws IOException, URISyntaxException {
        String query = "&q_track=" + URLEncoder.encode(track, StandardCharsets.UTF_8) + "&q_artist=" + URLEncoder.encode(artist, StandardCharsets.UTF_8);
        URL url = new URI(getAPI(bot,false) + query).toURL();

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Cookie", "AWSELB=55578B011601B1EF8BC274C33F9043CA947F99DCFF0A80541772015CA2B39C35C0F9E1C932D31725A7310BCAEB0C37431E024E2B45320B7F2C84490C2C97351FDE34690157");
        connection.setRequestProperty("Origin", "musixmatch.com");
        return connection;
    }

    @NotNull
    private static HttpURLConnection getMusixmatchConnectionByIsrc(Bot bot, String isrc) throws IOException, URISyntaxException {
        String query = "&track_isrc=" + URLEncoder.encode(isrc, StandardCharsets.UTF_8);
        URL url = new URI(getAPI(bot,true) + query).toURL();

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
