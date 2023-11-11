package com.jagrosh.jmusicbot.commands.music;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.audio.AudioHandler;
import com.jagrosh.jmusicbot.commands.MusicCommand;
import com.jagrosh.jmusicbot.utils.RnjsskaUtil;
import com.jagrosh.jmusicbot.utils.TTSCmdUtil;

public class TTSCmdJp extends MusicCommand
{
    public TTSCmdJp(Bot bot)
    {
        super(bot);
        this.name = "ttsJp";
        this.help = "일본어 TTS를 재생합니다";
        this.arguments = "<text>";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.bePlaying = false;
        this.beListening = true;
    }

    @Override
	public void doCommand(CommandEvent event)
	{
        AudioHandler handler = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();
        if (RnjsskaUtil.hasNoTrackPermission(handler, event.getMember())) {
            event.reply(event.getClient().getError() + "봇의 소유자가 권남 모드 활성화해서 TTS 못함 ㅋㅋ ㅅㄱ");
            return;
        }
		if (event.getArgs().isEmpty()) {
			event.replyError("재생할 텍스트를 알려주세요. 사용법: `" + event.getClient().getPrefix() + "ttsJp <text>`");
			return;
		}
		TTSCmdUtil.playTTS(event,bot,"ja");
	}
}