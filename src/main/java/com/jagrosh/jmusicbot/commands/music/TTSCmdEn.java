package com.jagrosh.jmusicbot.commands.music;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.JMusicBot;
import com.jagrosh.jmusicbot.audio.AudioHandler;
import com.jagrosh.jmusicbot.audio.RequestMetadata;
import com.jagrosh.jmusicbot.commands.MusicCommand;
import com.jagrosh.jmusicbot.utils.TTSCmdUtil;

public class TTSCmdEn extends MusicCommand
{
    public TTSCmdEn(Bot bot)
    {
        super(bot);
        this.name = "ttsEn";
        this.help = "영문 TTS를 재생합니다";
        this.arguments = "<text>";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.bePlaying = false;
        this.beListening = true;
    }

    @Override
	public void doCommand(CommandEvent event)
	{
        AudioHandler handler = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();
        if (JMusicBot.rnjsska &&
                handler != null &&
                handler.getPlayer().getPlayingTrack().getUserData(RequestMetadata.class).user.id == bot.getConfig().getOwnerId() &&
                event.getMember().getIdLong() != bot.getConfig().getOwnerId()) {
            event.reply(event.getClient().getError() + "봇의 소유자가 권남 모드 활성화해서 TTS 못함 ㅋㅋ ㅅㄱ");
            return;
        }
		if (event.getArgs().isEmpty()) {
			event.replyError("재생할 텍스트를 알려주세요. 사용법: `" + event.getClient().getPrefix() + "ttsEn <text>`");
			return;
		}
		TTSCmdUtil.playTTS(event,bot,"en");
	}
}