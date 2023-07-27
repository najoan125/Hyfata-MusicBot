package com.jagrosh.jmusicbot.utils;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

public class JSON {
    public static JSONObject readFromURL(String url) throws IOException {
        String json;
        String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537";
        String accept = "application/json";
        URL temp = new URL(url);
        URLConnection connection = temp.openConnection();
        connection.setRequestProperty("User-Agent", userAgent);
        connection.setRequestProperty("Accept", accept);
        Scanner scanner = new Scanner(connection.getInputStream(), "UTF-8").useDelimiter("\\A");
        json = scanner.hasNext() ? scanner.next() : "";
        if (json.equals("")) {
            return null;
        }
        return new JSONObject(json);
    }

    public static JSONObject readFromURL(String url, int fromline) throws IOException {
        String json;
        String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537";
        String accept = "application/json";
        URL temp = new URL(url);
        URLConnection connection = temp.openConnection();
        connection.setRequestProperty("User-Agent", userAgent);
        connection.setRequestProperty("Accept", accept);

        // 데이터를 UTF-8로 읽을 수 있는 reader
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));

        // fromline까지 불필요한 줄 읽기
        for (int i = 0; i < fromline - 1; i++) {
            reader.readLine();
        }

        // 데이터 읽기
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line);
        }
        reader.close();
        json = stringBuilder.toString();
        if (json.equals("")) {
            return null;
        }
        return new JSONObject(json);
    }

    public static JSONObject readFromFile(String path) throws IOException {
        String json = "";
        json = new String(
                Files.readAllBytes(Paths.get(path))
        );
        return new JSONObject(json);
    }
}
