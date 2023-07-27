package com.jagrosh.jmusicbot;

import com.jagrosh.jmusicbot.utils.JSON;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

public class TTSTest {
    public static void main(String[] args) throws IOException {
        JSONObject jo = JSON.readFromURL("https://www.google.com/async/translate_tts?yv=3&ttsp=tl:ko,txt:text,spd:2&cs=1&async=_fmt:jspb", 2);
        JSONArray array = jo.getJSONArray("translate_tts");
        System.out.println(array.getString(0));
    }
}
