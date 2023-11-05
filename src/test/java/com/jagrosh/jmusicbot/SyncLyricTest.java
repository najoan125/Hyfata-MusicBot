package com.jagrosh.jmusicbot;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Objects;

public class SyncLyricTest {
    public static void main(String[] args) {
        try {
            String track = "GODS";
            String artist = "League of Legends";

            String baseUrl = "https://apic-desktop.musixmatch.com/ws/1.1/macro.subtitles.get?format=json&user_language=ko&namespace=lyrics_synched&f_subtitle_length_max_deviation=1&subtitle_format=mxm&app_id=web-desktop-app-v1.0&usertoken=201219dbdb0f6aaba1c774bd931d0e79a28024e28db027ae72955c";
            String query = "&q_track=" + URLEncoder.encode(track, "UTF-8") + "&q_artist=" + URLEncoder.encode(artist, "UTF-8");
            System.out.println(baseUrl+query);
            URL url = new URL(baseUrl + query);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            // Set headers
            connection.setRequestProperty("Cookie", "AWSELB=55578B011601B1EF8BC274C33F9043CA947F99DCFF0A80541772015CA2B39C35C0F9E1C932D31725A7310BCAEB0C37431E024E2B45320B7F2C84490C2C97351FDE34690157");
            connection.setRequestProperty("Origin", "musixmatch.com");

            int responseCode = connection.getResponseCode();
            JSONObject json = null;
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                
                json = new JSONObject(response.toString());
            } else {
                System.out.println("HTTP Request failed with response code: " + responseCode);
            }

            String jsonString = Objects.requireNonNull(json)
                    .getJSONObject("message")
                    .getJSONObject("body")
                    .getJSONObject("macro_calls")
                    .getJSONObject("track.subtitles.get")
                    .getJSONObject("message")
                    .getJSONObject("body")
                    .getJSONArray("subtitle_list")
                    .getJSONObject(0)
                    .getJSONObject("subtitle")
                    .getString("subtitle_body");
            JSONArray jsonArray = new JSONArray(jsonString);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String text = jsonObject.getString("text");
                JSONObject timeObject = jsonObject.getJSONObject("time");
                double total = timeObject.getDouble("total");

                // 필요한 정보를 사용하여 작업 수행
                System.out.println("Text: " + (text.isEmpty() ? ((i == jsonArray.length()-1) ? "" : "♪") : text));
                System.out.println("Total: " + total);
            }
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }
}
