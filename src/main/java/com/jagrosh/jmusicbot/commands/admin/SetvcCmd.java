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

import java.util.Collections;
import java.util.List;

import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.jagrosh.jdautilities.commons.utils.FinderUtil;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.commands.AdminCommand;
import com.jagrosh.jmusicbot.settings.Settings;
import com.jagrosh.jmusicbot.utils.FormatUtil;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

/**
 *
 * @author John Grosh <john.a.grosh@gmail.com>
 */
public class SetvcCmd extends AdminCommand 
{
    public SetvcCmd(Bot bot)
    {
        this.name = "설정_통화방";
        this.help = "음악 재생이 가능한 전용 통화방을 설정합니다";
        this.arguments = "<channel|NONE>";
        this.aliases = bot.getConfig().getAliases(this.name);

        this.options = Collections.singletonList(
                new OptionData(OptionType.STRING, "통화방", "통화방 이름을 입력해 설정하거나 `none`을 입력해 설정 해제")
                        .setRequired(true)
        );
    }

    @Override
    public void doCommand(SlashCommandEvent event)
    {
        if (event.getGuild() == null) {
            return;
        }
        var option = event.getOption("통화방");
        String args = option == null ? "" : option.getAsString();
        if(args.isEmpty())
        {
            event.reply(event.getClient().getError()+" 음성 채널 또는 NONE을 포함하십시오").setEphemeral(true).queue();
            return;
        }
        Settings s = event.getClient().getSettingsFor(event.getGuild());
        if(args.equalsIgnoreCase("none"))
        {
            s.setVoiceChannel(null);
            event.reply(event.getClient().getSuccess()+" 이제 모든 채널에서 음악을 재생할 수 있습니다").queue();
        }
        else
        {
            List<VoiceChannel> list = FinderUtil.findVoiceChannels(args, event.getGuild());
            if(list.isEmpty())
                event.reply(event.getClient().getWarning()+" 일치하는 음성 채널을 찾을 수 없음 \""+args+"\"").setEphemeral(true).queue();
            else if (list.size()>1)
                event.reply(event.getClient().getWarning()+ FormatUtil.listOfVChannels(list, args)).setEphemeral(true).queue();
            else
            {
                s.setVoiceChannel(list.getFirst());
                event.reply(event.getClient().getSuccess()+" 이제 음악은 오직 "+list.getFirst().getAsMention()+" 에서만 재생됩니다").queue();
            }
        }
    }
}
