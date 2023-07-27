package com.jagrosh.jmusicbot.commands.music;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.commands.MusicCommand;
import com.jagrosh.jmusicbot.utils.AudioUtil;
import com.jagrosh.jmusicbot.utils.TTSResultHandler;
import com.jagrosh.jmusicbot.utils.TTSTooLongException;

import java.io.IOException;

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
		if (event.getArgs().isEmpty()) {
			event.replyError("재생할 텍스트를 알려주세요. 사용법: `" + event.getClient().getPrefix() + "ttsJp <text>`");
			return;
		}
		String ttsBase64;
		try {
			ttsBase64 = AudioUtil.getTTSBase64("ja",event.getArgs());
		} catch (IOException e) {
			e.printStackTrace();
			event.replyError("API에서 TTS를 불러오는 도중 오류가 발생하였습니다!");
			return;
		} catch (TTSTooLongException e) {
			event.replyError("텍스트가 글자 수 제한인 200자를 초과했습니다!");
			return;
		}

		String file; //name.mp3
		try {
			file = AudioUtil.createMP3FromBase64(ttsBase64, event.getMessage().getId());
		} catch (IOException e) {
			e.printStackTrace();
			event.replyError("TTS 변환 도중 알 수 없는 문제가 발생하였습니다! 관리자에게 문의해주세요!");
			return;
		}

		bot.getPlayerManager().loadItemOrdered(event.getGuild(), file, new TTSResultHandler(event, file));
	}
}