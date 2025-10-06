package com.jagrosh.jmusicbot;

import com.jagrosh.jmusicbot.utils.JSON;
import org.json.JSONObject;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class PapagoTranslateTest {
    public static void main(String[] args) throws URISyntaxException, IOException {
        String text = """
                On one side
                The forces of darkness
                Are gathering in their legions
                On the other
                A secret alliance
                Is sworn to keep the flame of truth alight
                Ti pyar vie iyad ra che uf kro sho
                Rat dava rof nii proti ei kof
                Ryuf voshu to ryaba ya kat kosu is
                Vi rien tsen vu hies taha chies ni ien fru
                If two implacable foes
                Are both fighting for what they hold most dear
                What length will they go to in their struggle to prevail
                And how much further will they go
                If what both they're fighting for is the same thing
                In a desperate conflict
                With a ruthless enemy
                Zuorhi viyantas was festsu ruor proi
                Yuk dalfe suoivo swenne yat vu henvi nes
                Sho fu briyu praffi stassui tsenva chies
                Ien ryus sois nyat pyaro shennie fru
                Prasueno
                Turoden shes vi hyu vu praviya
                Tyu prostes fis hien hesnie ryanmie proshuka
                Wi swen ryasta grouts froine shienhie var yat
                Nyam raika rit skuois trapa tof
                Ti pyar vie iyad ra che uf kro sho
                Rat dava rof nii proti ei kof
                Ryuf voshu to ryaba ya kat kosu is
                Vi rien tsen vu hies taha chies ni ien fru
                A man born to fight
                An enemy bent on conquest
                Let battle commence
                Zuorhi viyantas was festsu ruor proi
                Yuk dalfe suoivo swenne yat vu henvi nes
                Sho fu briyu praffi stassui tsenva chies
                Ien ryus sois nyat pyaro shennie fru
                Prasueno
                Turoden shes vi hyu vu praviya
                Tyu prostes fis hien hesnie ryanmie proshuka
                Wi swen ryasta grouts froine shienhie var yat
                Nyam raika rit skuois trapa tof
                """;
        String lang = "en";

        JSONObject json = JSON.getJsonObjectFromConnection(getConnection(lang, text));
        System.out.println(json.getString("translatedText"));
    }

    private static HttpURLConnection getConnection(String lang, String text) throws URISyntaxException, IOException {
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
}
