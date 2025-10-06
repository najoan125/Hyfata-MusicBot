package com.jagrosh.jmusicbot;

import com.jagrosh.jmusicbot.utils.JSON;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class PapagoTlitTest {
    public static void main(String[] args) throws Exception {
        String text = """
                駄目駄目駄目
                脳みその中から「やめろ馬鹿」と喚くモラリティ
                ダーリンベイビーダーリン
                半端なくラブ！ときらめき浮き足立つフィロソフィ
                死ぬほど可愛い上目遣い
                なにがし法に触れるくらい
                ばら撒く乱心 気づけば蕩尽
                この世に生まれた君が悪い
                やたらとしんどい恋煩い
                バラバラんなる頭とこの身体
                頸動脈からアイラブユーが噴き出て
                アイリスアウト
                一体どうしようこの想いを
                どうしようあばらの奥を
                ザラメが溶けてゲロになりそう
                瞳孔バチ開いて溺れ死にそう
                今この世で君だけ大正解
                Darlin', darlin', darlin', darlin'
                Darlin', darlin', darlin', darlin'
                ♪
                ひっくり返っても勝ちようない
                君だけルールは適用外
                四つともオセロは黒しかない
                カツアゲ放題
                君が笑顔で放ったアバダケダブラ
                デコにスティグマ 申し訳ねえな
                矢を刺して 貫いて
                ここ弱点
                死ぬほど可愛い上目遣い
                なにがし法に触れるくらい
                ばら撒く乱心 気づけば蕩尽
                この世に生まれた君が悪い
                パチモンでもいい何でもいい
                今君と名付いてる全て欲しい
                頸動脈からアイラブユーが噴き出て
                ア、ア、ア、ア、アイリスアウト
                ♪
                ア、ア、ア、ア、アイリスアウト
                ♪
                ア、ア、ア、ア、アイリスアウト
                一体どうしようこの想いを
                どうしようあばらの奥を
                ザラメが溶けてゲロになりそう
                瞳孔バチ開いて溺れ死にそう
                今この世で君だけ大正解
                Darlin', darlin', darlin', darlin'
                Darlin', darlin', darlin', darlin'
                """;
        String translated = text.replaceAll(" ", "") + " ";
        String lang = "ja";
        System.out.println(translated + "!");

        JSONObject json = JSON.getJsonObjectFromConnection(getConnection(lang, text));
        JSONArray array = json.getJSONObject("message").getJSONArray("tlitResult");
        for (int i = 0; i < array.length(); i++) {
            JSONObject object = array.getJSONObject(i);
            String tlit = object.getString("phoneme") + " ";
            String src = object.getString("token");
            System.out.println(tlit + "(" + src + ")");
            translated = translated.replaceFirst(src, tlit);
        }
        for (String s : translated.split("\n")) {
            System.out.println(s + "!");
        }
    }

    private static HttpURLConnection getConnection(String lang, String text) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new java.net.URI("https://papago.naver.com/apis/tlit/wtp").toURL().openConnection();
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
