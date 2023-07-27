package com.jagrosh.jmusicbot.commands.music;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jmusicbot.Bot;
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
		if (event.getArgs().isEmpty()) {
			event.replyError("재생할 텍스트를 알려주세요. 사용법: `" + event.getClient().getPrefix() + "tts <text>`");
			return;
		}
		TTSCmdUtil.playTTS(event,bot,"en");
	}
}