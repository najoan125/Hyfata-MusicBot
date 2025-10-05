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
package com.jagrosh.jmusicbot.commands.dj;

import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.audio.AudioHandler;
import com.jagrosh.jmusicbot.commands.DJCommand;
import com.jagrosh.jmusicbot.utils.RnjsskaUtil;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.Collections;
import java.util.Objects;

/**
 *
 * @author John Grosh <john.a.grosh@gmail.com>
 */
public class SkiptoCmd extends DJCommand 
{
    public SkiptoCmd(Bot bot)
    {
        super(bot);
        this.name = "skipto";
        this.help = "지정된 대기열 위치로 건너뜁니다";
        this.arguments = "<대기열 위치>";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.bePlaying = true;

        this.options = Collections.singletonList(
                new OptionData(OptionType.INTEGER, "대기열_위치", "건너뛸 대기열 위치(번호)")
        );
    }

    @Override
    public void doCommand(SlashCommandEvent event)
    {
        var option = event.getOption("대기열_위치");
        int index = option == null ? 0 : option.getAsInt();

        AudioHandler handler = (AudioHandler) Objects.requireNonNull(event.getGuild()).getAudioManager().getSendingHandler();
        Objects.requireNonNull(handler);
        if (RnjsskaUtil.hasNoTrackPermission(handler, event.getMember())) {
            event.reply(event.getClient().getError() + "봇의 소유자가 권남 모드 활성화해서 스킵 못함 ㅋㅋ ㅅㄱ").queue();
            return;
        }
        if(index<1 || index>handler.getQueue().size())
        {
            event.reply(event.getClient().getError()+" 위치는 1과 "+handler.getQueue().size()+" 사이의 유효한 정수여야 합니다!").setEphemeral(true).queue();
            return;
        }
        handler.getQueue().skip(index-1);
        event.reply(event.getClient().getSuccess()+" **"+handler.getQueue().get(0).getTrack().getInfo().title+"** (으)로 건너뜀").queue();
        handler.getPlayer().stopTrack();
    }
}
