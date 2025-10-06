package com.jagrosh.jmusicbot.synclyric;

import com.jagrosh.jmusicbot.JMusicBot;
import com.jagrosh.jmusicbot.utils.JSON;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PapagoAPI {
    public static List<String> getTranslation(String lang, String[] lyricLine) {
        String textToTranslate = String.join("\n", lyricLine);
        JSONObject jsonResponse;
        try {
            jsonResponse = JSON.getJsonObjectFromConnection(getTranslatorConnection(lang, textToTranslate));
        } catch (IOException | URISyntaxException e) {
            JMusicBot.LOG.error("An error occurred while getting translation in PapagoAPI", e);
            return null;
        }
        String translatedText = jsonResponse.getString("translatedText");

        // The addition of "\n " is a workaround to prevent split() from discarding trailing empty strings.
        String[] translatedLines = (translatedText + "\n ").split("\n");

        return new ArrayList<>(Arrays.asList(translatedLines));
    }

    public static List<String> getTlit(String lang, String[] lyricLine) {
        String textToTranslate = String.join("\n", lyricLine)
                .replaceAll(" ", "")
                .replaceAll("！", "")
                .replaceAll("\\(", "")
                .replaceAll("\\)", "")
                .replaceAll("「", "")
                .replaceAll("」", "")
                .replaceAll(",", "")
                .replaceAll("。", "")
                .replaceAll("？", "")+ " ";
        JSONObject jsonResponse;
        try {
            jsonResponse = JSON.getJsonObjectFromConnection(getTlitConnection(lang, textToTranslate));
        } catch (Exception e) {
            JMusicBot.LOG.error("An error occurred while getting translation in PapagoAPI", e);
            return null;
        }
        JSONArray array = jsonResponse.getJSONObject("message").getJSONArray("tlitResult");

        StringBuilder resultBuilder = new StringBuilder();
        int currentIndex = 0;

        for (int i = 0; i < array.length(); i++) {
            JSONObject object = array.getJSONObject(i);
            String tlit = object.getString("phoneme") + " ";
            String src = object.getString("token");

            // 현재 인덱스부터 시작해서 교체할 토큰(src)의 위치를 찾습니다.
            int foundIndex = textToTranslate.indexOf(src, currentIndex);

            if (foundIndex != -1) {
                // 토큰을 찾았을 경우:
                // 1. 마지막 처리 위치(currentIndex)부터 토큰 직전까지의 텍스트를 추가합니다.
                resultBuilder.append(textToTranslate, currentIndex, foundIndex);
                // 2. 토큰 대신 발음(tlit)을 추가합니다.
                resultBuilder.append(tlit);
                // 3. 다음 탐색 위치를 현재 찾은 토큰의 끝으로 업데이트합니다.
                currentIndex = foundIndex + src.length();
            }
        }

        // 마지막 교체 이후에 남은 텍스트가 있다면 모두 추가합니다.
        if (currentIndex < textToTranslate.length()) {
            resultBuilder.append(textToTranslate.substring(currentIndex));
        }

        String[] translatedLines = resultBuilder.toString().split("\n");
        return new ArrayList<>(Arrays.asList(translatedLines));
    }

    private static HttpURLConnection getTranslatorConnection(String lang, String text) throws URISyntaxException, IOException {
        URL url = new URI("https://papago.naver.com/apis/n2mt/translate").toURL();

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        connection.setRequestProperty("Accept-Language", "ko");
        connection.setDoOutput(true);

        String postData = String.format("source=%s&target=ko&text=%s", lang, URLEncoder.encode(text, StandardCharsets.UTF_8));
        byte[] postDataBytes = postData.getBytes(StandardCharsets.UTF_8);
        connection.setFixedLengthStreamingMode(postDataBytes.length);
        connection.getOutputStream().write(postDataBytes);
        return connection;
    }

    private static HttpURLConnection getTlitConnection(String lang, String text) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new URI("https://papago.naver.com/apis/tlit/wtp").toURL().openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        connection.setRequestProperty("Accept-Language", "ko");
        connection.setDoOutput(true);

        String postData = String.format("srcLang=%s&tlitLang=ko&index=0&query=%s", lang, URLEncoder.encode(text, StandardCharsets.UTF_8));
        byte[] postDataBytes = postData.getBytes(StandardCharsets.UTF_8);
        connection.setFixedLengthStreamingMode(postDataBytes.length);
        connection.getOutputStream().write(postDataBytes);
        return connection;
    }
}
