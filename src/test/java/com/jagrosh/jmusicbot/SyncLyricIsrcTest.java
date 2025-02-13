package com.jagrosh.jmusicbot;

import org.apache.http.client.HttpResponseException;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class SyncLyricIsrcTest {
    private static final String ISRC_URL = "https://apic-desktop.musixmatch.com/ws/1.1/track.subtitles.get?format=json&user_language=en&namespace=lyrics_synched&f_subtitle_length_max_deviation=1&subtitle_format=mxm&app_id=web-desktop-app-v1.0&usertoken=201219dbdb0f6aaba1c774bd931d0e79a28024e28db027ae72955c";

    public static void main(String[] args) throws IOException {
        String isrc = "JPU902403617"; //QZEKE2084155 ZZA0P2305775
        JSONObject json = getJsonObjectFromConnection(getMusixmatchConnectionByIsrc(isrc));
        System.out.println(json);
        if (json.getJSONObject("message").getJSONObject("header").getInt("available") == 0) {
            System.out.println("tlqkf");
        }
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
