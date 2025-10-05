package com.jagrosh.jmusicbot.utils;

import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.JMusicBot;
import net.dv8tion.jda.api.entities.Message;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class TTSCmdUtil {
    private static String getTTSBase64(SlashCommandEvent event, String lang, String args, Message m) {
        try {
            return AudioUtil.getTTSBase64(lang,args);
        } catch (IOException e) {
            JMusicBot.LOG.error("An error occurred while getting base64 TTS in TTSCmdUtil", e);
            m.editMessage(event.getClient().getError()+" API에서 TTS를 불러오는 도중 오류가 발생하였습니다!").queue();
            return null;
        } catch (TTSTooLongException e) {
            m.editMessage(event.getClient().getError()+" 텍스트가 글자 수 제한인 200자를 초과했습니다!").queue();
            return null;
        }
    }

    private static String createMP3FromBase64(SlashCommandEvent event, String base64, Message m) {
        try {
            return AudioUtil.createMP3FromBase64(base64, m.getId());
        } catch (IOException e) {
            JMusicBot.LOG.error("An error occurred while creating MP3 from base64 TTS in TTSCmdUtil", e);
            m.editMessage(event.getClient().getError()+" TTS 변환 도중 알 수 없는 문제가 발생하였습니다! 관리자에게 문의해주세요!").queue();
            try {
                Files.delete(Paths.get(m.getId() + ".mp3"));
            } catch (IOException ex) {
                JMusicBot.LOG.error("An error occurred while deleting MP3 from base64 TTS in TTSCmdUtil", ex);
            }
            return null;
        }
    }

    public static void playTTS(SlashCommandEvent event, Bot bot, String lang, String args){
        event.deferReply().queue(hook -> hook.retrieveOriginal().queue(m -> {
            String ttsBase64 = getTTSBase64(event, lang, args, m);
            if(ttsBase64==null) return;

            String file = createMP3FromBase64(event, ttsBase64, m); //name.mp3
            if(file==null) return;

            bot.getPlayerManager().loadItemOrdered(event.getGuild(), file, new TTSResultHandler(event, file, m, args));
        }));
    }

    public static void playTTSsPgmld(SlashCommandEvent event, Bot bot, String lang, String text, String args){
        event.deferReply().queue(hook -> hook.retrieveOriginal().queue(m -> {
            String ttsBase64 = getTTSBase64(event, lang, args, m);
            if(ttsBase64==null) return;

            String file = createMP3FromBase64(event, ttsBase64, m); //name.mp3
            if(file==null) return;

            bot.getPlayerManager().loadItemOrdered(event.getGuild(), file, new TTSResultHandler(event, file, m, true, text, args));
        }));
    }
}
