/*
 * Copyright 2018 John Grosh <john.a.grosh@gmail.com>.
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
package com.jagrosh.jmusicbot.commands.admin;

import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.commands.AdminCommand;
import com.jagrosh.jmusicbot.settings.Settings;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.Collections;

/**
 *
 * @author John Grosh (john.a.grosh@gmail.com)
 */
public class SkipratioCmd extends AdminCommand
{
    public SkipratioCmd(Bot bot)
    {
        this.name = "설정_스킵비율";
        this.help = "건너뛰기 비율을 청취자의 몇 %로 할지 설정합니다.";
        this.arguments = "<0 - 100>";
        this.aliases = bot.getConfig().getAliases(this.name);

        this.options = Collections.singletonList(
                new OptionData(OptionType.INTEGER, "건너뛰기_비율", "0 ~ 100 사이의 값을 입력해 건너뛰기 비율(%)을 설정")
                        .setRequired(true)
        );
    }

    @Override
    public void doCommand(SlashCommandEvent event)
    {
        if(event.getGuild()==null)
            return;
        var option = event.getOption("건너뛰기_비율");
        String args = option == null ? "" : option.getAsString();
        try
        {
            int val = Integer.parseInt(args.endsWith("%") ? args.substring(0,args.length()-1) : args);
            if( val < 0 || val > 100)
            {
                event.reply(event.getClient().getError() + " 값은 0에서 100 사이여야 합니다!").setEphemeral(true).queue();
                return;
            }
            Settings s = event.getClient().getSettingsFor(event.getGuild());
            s.setSkipRatio(val / 100.0);
            event.reply(event.getClient().getSuccess() + " 건너뛰기 비율이 *" + event.getGuild().getName() + "* 청취자의 `" + val + "%` 로 설정되었습니다.").queue();
        }
        catch(NumberFormatException ex)
        {
            event.reply(event.getClient().getError() + " 0에서 100 사이의 정수를 포함하십시오(기본값은 55). 이 숫자는 노래를 건너뛰기 위해 투표해야 하는 청취 사용자의 비율입니다.").queue();
        }
    }
}