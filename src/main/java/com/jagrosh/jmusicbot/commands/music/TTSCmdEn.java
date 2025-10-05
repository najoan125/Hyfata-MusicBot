package com.jagrosh.jmusicbot.commands.music;

import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.audio.AudioHandler;
import com.jagrosh.jmusicbot.commands.MusicCommand;
import com.jagrosh.jmusicbot.utils.RnjsskaUtil;
import com.jagrosh.jmusicbot.utils.TTSCmdUtil;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.Collections;
import java.util.Objects;

public class TTSCmdEn extends MusicCommand
{
    public TTSCmdEn(Bot bot)
    {
        super(bot);
        this.name = "tts_en";
        this.help = "영문 TTS를 재생합니다";
        this.arguments = "<text>";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.bePlaying = false;
        this.beListening = true;

        this.options = Collections.singletonList(
                new OptionData(OptionType.STRING, "영문_텍스트", "TTS를 요청할 영문 텍스트").setRequired(true)
        );
    }

    @Override
	public void doCommand(SlashCommandEvent event)
	{
        var option = event.getOption("영문_텍스트");
        String args = option == null ? "" : option.getAsString();

        AudioHandler handler = (AudioHandler) Objects.requireNonNull(event.getGuild()).getAudioManager().getSendingHandler();
        if (RnjsskaUtil.hasNoTrackPermission(handler, event.getMember())) {
            event.reply(event.getClient().getError() + "봇의 소유자가 권남 모드 활성화해서 TTS 못함 ㅋㅋ ㅅㄱ").queue();
            return;
        }
		if (args.isEmpty()) {
			event.reply(event.getClient().getError() + " 재생할 텍스트를 알려주세요. 사용법: `/tts_en <text>`").setEphemeral(true).queue();
			return;
		}
		TTSCmdUtil.playTTS(event,bot,"en", args);
	}
}