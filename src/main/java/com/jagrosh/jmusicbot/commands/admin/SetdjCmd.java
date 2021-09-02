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
import net.dv8tion.jda.api.entities.Role;

/**
 *
 * @author John Grosh <john.a.grosh@gmail.com>
 */
public class SetdjCmd extends AdminCommand
{
    public SetdjCmd(Bot bot)
    {
        this.name = "setdj";
        this.help = "\uD2B9\uC815 \uC74C\uC545 \uBA85\uB839\uC758 DJ \uC5ED\uD560\uC744 \uC124\uC815\uD569\uB2C8\uB2E4";
        this.arguments = "<\uC5ED\uD560\uC774\uB984|NONE>";
        this.aliases = bot.getConfig().getAliases(this.name);
    }
    
    @Override
    protected void execute(CommandEvent event) 
    {
        if(event.getArgs().isEmpty())
        {
            event.reply(event.getClient().getError()+" \uC5ED\uD560 \uC774\uB984 \uB610\uB294 NONE\uC744 \uD3EC\uD568\uD558\uC2ED\uC2DC\uC624.");
            return;
        }
        Settings s = event.getClient().getSettingsFor(event.getGuild());
        if(event.getArgs().equalsIgnoreCase("none"))
        {
            s.setDJRole(null);
            event.reply(event.getClient().getSuccess()+" DJ\uC5ED\uD560 \uCD08\uAE30\uD654\uB428; \uAD00\uB9AC\uC790\uB9CC DJ\uBA85\uB839\uC5B4\uB97C \uC0AC\uC6A9\uD560 \uC218 \uC788\uC2B5\uB2C8\uB2E4.");
        }
        else
        {
            List<Role> list = FinderUtil.findRoles(event.getArgs(), event.getGuild());
            if(list.isEmpty())
                event.reply(event.getClient().getWarning()+" \uC77C\uCE58\uD558\uB294 \uC5ED\uD560\uC744 \uCC3E\uC744 \uC218 \uC5C6\uC2B5\uB2C8\uB2E4 \""+event.getArgs()+"\"");
            else if (list.size()>1)
                event.reply(event.getClient().getWarning()+FormatUtil.listOfRoles(list, event.getArgs()));
            else
            {
                s.setDJRole(list.get(0));
                event.reply(event.getClient().getSuccess()+" \uC774\uC81C **"+list.get(0).getName()+"** \uC5ED\uD560\uC758 \uC0AC\uC6A9\uC790\uAC00 DJ \uBA85\uB839\uC744 \uC0AC\uC6A9\uD560 \uC218 \uC788\uC2B5\uB2C8\uB2E4.");
            }
        }
    }
    
}
