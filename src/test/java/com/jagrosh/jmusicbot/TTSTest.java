package com.jagrosh.jmusicbot;

import com.jagrosh.jmusicbot.utils.JSON;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class TTSTest {
    public static void main(String[] args) throws UnsupportedEncodingException {
        encodingTest();
    }

    public static void stringTest() throws IOException {
        JSONObject jo = JSON.readFromURL("https://www.google.com/async/translate_tts?yv=3&ttsp=tl:ko,txt:text,spd:2&cs=1&async=_fmt:jspb", 2);
        JSONArray array = jo.getJSONArray("translate_tts");
        System.out.println(array.getString(0));
    }

    public static void encodingTest() throws UnsupportedEncodingException {
        System.out.println(URLEncoder.encode(",", "UTF-8"));
    }
}
