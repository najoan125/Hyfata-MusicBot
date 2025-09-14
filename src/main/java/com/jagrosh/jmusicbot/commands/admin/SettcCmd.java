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

import java.util.List;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.utils.FinderUtil;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.commands.AdminCommand;
import com.jagrosh.jmusicbot.settings.Settings;
import com.jagrosh.jmusicbot.utils.FormatUtil;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

/**
 *
 * @author John Grosh <john.a.grosh@gmail.com>
 */
public class SettcCmd extends AdminCommand 
{
    public SettcCmd(Bot bot)
    {
        this.name = "settc";
        this.help = "음악 명령의 텍스트 채널을 설정합니다";
        this.arguments = "<channel|NONE>";
        this.aliases = bot.getConfig().getAliases(this.name);
    }

    @Override
    protected void execute(CommandEvent event)
    {
        if(event.getArgs().isEmpty())
        {
            event.reply(event.getClient().getError()+" 텍스트 채널 또는 NONE을 포함하십시오");
            return;
        }
        Settings s = event.getClient().getSettingsFor(event.getGuild());
        if(event.getArgs().equalsIgnoreCase("none"))
        {
            s.setTextChannel(null);
            event.reply(event.getClient().getSuccess()+" 이제 모든 채널에서 음악 명령을 사용할 수 있습니다");
        }
        else
        {
            List<TextChannel> list = FinderUtil.findTextChannels(event.getArgs(), event.getGuild());
            if(list.isEmpty())
                event.reply(event.getClient().getWarning()+" 일치하는 텍스트 채널이 없습니다 \""+event.getArgs()+"\"");
            else if (list.size()>1)
                event.reply(event.getClient().getWarning()+ FormatUtil.listOfTChannels(list, event.getArgs()));
            else
            {
                s.setTextChannel(list.getFirst());
                event.reply(event.getClient().getSuccess()+" 이제 음악 명령어는 오직 <#"+list.getFirst().getId()+"> 에서만 사용 가능합니다");
            }
        }
    }
    
}
