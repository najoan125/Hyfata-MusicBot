/*
 * Copyright 2022 John Grosh <john.a.grosh@gmail.com>.
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
import com.jagrosh.jmusicbot.audio.AudioHandler;
import com.jagrosh.jmusicbot.commands.AdminCommand;
import com.jagrosh.jmusicbot.settings.QueueType;
import com.jagrosh.jmusicbot.settings.Settings;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Wolfgang Schwendtbauer
 */
public class QueueTypeCmd extends AdminCommand
{
    public QueueTypeCmd(Bot bot)
    {
        super();
        this.name = "대기열_타입";
        this.help = "대기열 유형을 확인하거나 변경합니다.";
        this.arguments = "[" + String.join("|", QueueType.getNames()) + "]";
        this.aliases = bot.getConfig().getAliases(this.name);

        List<Command.Choice> choices = new ArrayList<>();
        for (QueueType type : QueueType.values())
            choices.add(new Command.Choice(type.name(), type.getUserFriendlyName()));

        this.options = Collections.singletonList(
                new OptionData(OptionType.STRING, "대기열_타입", "원하는 대기열 타입을 지정합니다.")
                        .addChoices(choices)
        );
    }

    @Override
    public void doCommand(SlashCommandEvent event)
    {
        if (event.getGuild() == null)
            return;
        var option = event.getOption("대기열_타입");

        String args = option == null ? "" : option.getAsString();
        QueueType value;
        Settings settings = event.getClient().getSettingsFor(event.getGuild());

        if (args.isEmpty())
        {
            QueueType currentType = settings.getQueueType();
            event.reply(currentType.getEmoji() + " 현재 대기열 유형: `" + currentType.getUserFriendlyName() + "`.").queue();
            return;
        }

        try
        {
            value = QueueType.valueOf(args.toUpperCase());
        }
        catch (IllegalArgumentException e)
        {
            event.reply(event.getClient().getError() + " 대기열 유형이 잘못되었습니다. 유효한 유형: [" + String.join("|", QueueType.getNames()) + "]").setEphemeral(true).queue();
            return;
        }

        if (settings.getQueueType() != value)
        {
            settings.setQueueType(value);

            AudioHandler handler = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();
            if (handler != null)
                handler.setQueueType(value);
        }

        event.reply(value.getEmoji() + " 대기열 유형이 `" + value.getUserFriendlyName() + "`으(로) 설정되었습니다.").queue();
    }
}