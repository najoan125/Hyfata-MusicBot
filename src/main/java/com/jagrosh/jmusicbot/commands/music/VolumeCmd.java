/*
 * Copyright 2016 John Grosh <john.a.grosh@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jagrosh.jmusicbot.commands.music;

import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.audio.AudioHandler;
import com.jagrosh.jmusicbot.commands.MusicCommand;
import com.jagrosh.jmusicbot.settings.Settings;
import com.jagrosh.jmusicbot.utils.FormatUtil;
import com.jagrosh.jmusicbot.utils.RnjsskaUtil;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.Collections;
import java.util.Objects;

/**
 *
 * @author John Grosh <john.a.grosh@gmail.com>
 */
public class VolumeCmd extends MusicCommand
{
    public VolumeCmd(Bot bot)
    {
        super(bot);
        this.name = "음량";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.help = "음량을 설정하거나 표시합니다";
        this.arguments = "[0-150]";

        this.options = Collections.singletonList(
                new OptionData(OptionType.INTEGER, "음량", "설정할 음량 (0-150)")
        );
    }

    @Override
    public void doCommand(SlashCommandEvent event)
    {
        var option = event.getOption("음량");
        Integer reqVolume = option == null ? null : option.getAsInt();

        AudioHandler handler = (AudioHandler) Objects.requireNonNull(event.getGuild()).getAudioManager().getSendingHandler();
        if (RnjsskaUtil.hasNoTrackPermission(handler, event.getMember())) {
            event.reply(event.getClient().getError() + "봇의 소유자가 권남 모드 활성화해서 볼륨 조정 못하쥬? ㅋㅋ 어쩔거임? ㅄ ㅋㅋ").queue();
            return;
        }

        Settings settings = event.getClient().getSettingsFor(event.getGuild());
        int volume = Objects.requireNonNull(handler).getPlayer().getVolume();
        if(reqVolume == null)
        {
            event.reply(FormatUtil.volumeIcon(volume)+" 현재 음량은 `"+volume+"` 입니다").queue();
        }
        else
        {
            if(reqVolume<0 || reqVolume>150)
                event.reply(event.getClient().getError()+" 음량은 0과 150 사이의 유효한 정수여야 합니다!").setEphemeral(true).queue();
            else
            {
                handler.getPlayer().setVolume(reqVolume);
                settings.setVolume(reqVolume);
                event.reply(FormatUtil.volumeIcon(reqVolume)+" 음량이 `"+volume+"` 에서 `"+reqVolume+"` (으)로 변경되었습니다").queue();
            }
        }
    }
    
}
