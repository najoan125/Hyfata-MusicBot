package com.jagrosh.jmusicbot.utils;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.jagrosh.jmusicbot.Bot;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class TTSCmdUtil {
    public static void playTTS(SlashCommandEvent event, Bot bot, String lang, String args){
        event.deferReply().queue(hook -> hook.retrieveOriginal().queue(m -> {
            String ttsBase64;
            try {
                ttsBase64 = AudioUtil.getTTSBase64(lang,args);
            } catch (IOException e) {
                e.printStackTrace();
                m.editMessage(event.getClient().getError()+" API에서 TTS를 불러오는 도중 오류가 발생하였습니다!").queue();
                return;
            } catch (TTSTooLongException e) {
                m.editMessage(event.getClient().getError()+" 텍스트가 글자 수 제한인 200자를 초과했습니다!").queue();
                return;
            }

            String file; //name.mp3
            try {
                file = AudioUtil.createMP3FromBase64(ttsBase64, m.getId());
            } catch (IOException e) {
                e.printStackTrace();
                m.editMessage(event.getClient().getError()+" TTS 변환 도중 알 수 없는 문제가 발생하였습니다! 관리자에게 문의해주세요!").queue();
                try {
                    Files.delete(Paths.get(m.getId() + ".mp3"));
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                return;
            }

            bot.getPlayerManager().loadItemOrdered(event.getGuild(), file, new TTSResultHandler(event, file, m, args));
        }));
    }

    public static void playTTSsPgmld(SlashCommandEvent event, Bot bot, String lang, String text, String args){
        event.deferReply().queue(hook -> hook.retrieveOriginal().queue(m -> {
            String ttsBase64;
            try {
                ttsBase64 = AudioUtil.getTTSBase64(lang,text);
            } catch (IOException e) {
                e.printStackTrace();
                m.editMessage(event.getClient().getError()+" API에서 TTS를 불러오는 도중 오류가 발생하였습니다!").queue();
                return;
            } catch (TTSTooLongException e) {
                m.editMessage(event.getClient().getError()+" 텍스트가 글자 수 제한인 200자를 초과했습니다!").queue();
                return;
            }

            String file; //name.mp3
            try {
                file = AudioUtil.createMP3FromBase64(ttsBase64, m.getId());
            } catch (IOException e) {
                e.printStackTrace();
                m.editMessage(event.getClient().getError()+" TTS 변환 도중 알 수 없는 문제가 발생하였습니다! 관리자에게 문의해주세요!").queue();
                try {
                    Files.delete(Paths.get(m.getId() + ".mp3"));
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                return;
            }

            bot.getPlayerManager().loadItemOrdered(event.getGuild(), file, new TTSResultHandler(event, file, m, true, text, args));
        }));
    }
}
