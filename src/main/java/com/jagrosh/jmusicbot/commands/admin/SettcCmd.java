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
import net.dv8tion.jda.api.entities.TextChannel;

/**
 *
 * @author John Grosh <john.a.grosh@gmail.com>
 */
public class SettcCmd extends AdminCommand 
{
    public SettcCmd(Bot bot)
    {
        this.name = "settc";
        this.help = "\uC74C\uC545 \uBA85\uB839\uC758 \uD14D\uC2A4\uD2B8 \uCC44\uB110\uC744 \uC124\uC815\uD569\uB2C8\uB2E4";
        this.arguments = "<channel|NONE>";
        this.aliases = bot.getConfig().getAliases(this.name);
    }
    
    @Override
    protected void execute(CommandEvent event) 
    {
        if(event.getArgs().isEmpty())
        {
            event.reply(event.getClient().getError()+" \uD14D\uC2A4\uD2B8 \uCC44\uB110 \uB610\uB294 NONE\uC744 \uD3EC\uD568\uD558\uC2ED\uC2DC\uC624");
            return;
        }
        Settings s = event.getClient().getSettingsFor(event.getGuild());
        if(event.getArgs().equalsIgnoreCase("none"))
        {
            s.setTextChannel(null);
            event.reply(event.getClient().getSuccess()+" \uC774\uC81C \uBAA8\uB4E0 \uCC44\uB110\uC5D0\uC11C \uC74C\uC545 \uBA85\uB839\uC744 \uC0AC\uC6A9\uD560 \uC218 \uC788\uC2B5\uB2C8\uB2E4");
        }
        else
        {
            List<TextChannel> list = FinderUtil.findTextChannels(event.getArgs(), event.getGuild());
            if(list.isEmpty())
                event.reply(event.getClient().getWarning()+" \uC77C\uCE58\uD558\uB294 \uD14D\uC2A4\uD2B8 \uCC44\uB110\uC774 \uC5C6\uC2B5\uB2C8\uB2E4 \""+event.getArgs()+"\"");
            else if (list.size()>1)
                event.reply(event.getClient().getWarning()+FormatUtil.listOfTChannels(list, event.getArgs()));
            else
            {
                s.setTextChannel(list.get(0));
                event.reply(event.getClient().getSuccess()+" \uC774\uC81C \uC74C\uC545 \uBA85\uB839\uC5B4\uB294 \uC624\uC9C1 <#"+list.get(0).getId()+"> \uC5D0\uC11C\uB9CC \uC0AC\uC6A9 \uAC00\uB2A5\uD569\uB2C8\uB2E4");
            }
        }
    }
    
}
