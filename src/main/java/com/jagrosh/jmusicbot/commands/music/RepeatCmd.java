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
package com.jagrosh.jmusicbot.commands.music;

import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.commands.MusicCommand;
import com.jagrosh.jmusicbot.settings.RepeatMode;
import com.jagrosh.jmusicbot.settings.Settings;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.Collections;

/**
 *
 * @author John Grosh <john.a.grosh@gmail.com>
 */
public class RepeatCmd extends MusicCommand
{
    public RepeatCmd(Bot bot)
    {
        super(bot);
        this.name = "반복";
        this.help = "대기열 전체를 반복하거나 노래 한곡만 단일 반복 합니다.";
        this.arguments = "[끄기|전체 반복|단일 반복]";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.contexts = new InteractionContextType[]{InteractionContextType.GUILD};
        this.options = Collections.singletonList(
                new OptionData(OptionType.STRING, "반복_모드", "반복 모드를 설정")
                        .addChoice("전체 반복", "all")
                        .addChoice("단일 반복", "single")
                        .addChoice("끄기", "off")
        );
    }
    
    // override music command's execute because we don't care where this is used
    @Override
    protected void execute(SlashCommandEvent event)
    {
        var option = event.getOption("반복_모드");
        String args = option == null ? "" : option.getAsString();
        RepeatMode value = RepeatMode.OFF;
        Settings settings = event.getClient().getSettingsFor(event.getGuild());
        if(args.isEmpty())
        {
            event.reply(event.getClient().getSuccess() + " 현재 반복 모드: `" + settings.getRepeatMode().getUserFriendlyName() + "`").queue();
            return;
        }
        else if(args.equalsIgnoreCase("true") || args.equalsIgnoreCase("on") || args.equalsIgnoreCase("all") || args.equalsIgnoreCase("켜기") || args.equalsIgnoreCase("전체"))
        {
            value = RepeatMode.ALL;
        }
        else if(args.equalsIgnoreCase("one") || args.equalsIgnoreCase("single") || args.equalsIgnoreCase("단일"))
        {
            value = RepeatMode.SINGLE;
        }
        else if (!args.equalsIgnoreCase("off"))
        {
            event.reply(event.getClient().getError() + " 유효한 옵션은 `전체 반복` 또는 `단일 반복` 또는 `끄기` 입니다.").setEphemeral(true).queue();
            return;
        }
        settings.setRepeatMode(value);
        event.reply(event.getClient().getSuccess() + " 반복 모드를 `"+value.getUserFriendlyName()+"`(으)로 설정했습니다!").queue();
    }

    @Override
    public void doCommand(SlashCommandEvent event) { /* Intentionally Empty */ }
}
