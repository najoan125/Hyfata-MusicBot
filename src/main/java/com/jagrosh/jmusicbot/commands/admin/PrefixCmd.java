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

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.commands.AdminCommand;
import com.jagrosh.jmusicbot.settings.Settings;

/**
 *
 * @author John Grosh (john.a.grosh@gmail.com)
 */
public class PrefixCmd extends AdminCommand
{
    public PrefixCmd(Bot bot)
    {
        this.name = "prefix";
        this.help = "\uC11C\uBC84\uBCC4 \uC811\uB450\uC0AC\uB97C \uC124\uC815\uD569\uB2C8\uB2E4";
        this.arguments = "<\uC811\uB450\uC0AC|NONE>";
        this.aliases = bot.getConfig().getAliases(this.name);
    }
    
    @Override
    protected void execute(CommandEvent event) 
    {
        if(event.getArgs().isEmpty())
        {
            event.replyError("\uC811\uB450\uC0AC\uB97C \uD3EC\uD568\uD558\uAC70\uB098 NONE\uC744 \uC785\uB825\uD558\uC2ED\uC2DC\uC624");
            return;
        }
        
        Settings s = event.getClient().getSettingsFor(event.getGuild());
        if(event.getArgs().equalsIgnoreCase("none"))
        {
            s.setPrefix(null);
            event.replySuccess("\uC811\uB450\uC0AC\uAC00 \uCD08\uAE30\uD654\uB428.");
        }
        else
        {
            s.setPrefix(event.getArgs());
            event.replySuccess("*" + event.getGuild().getName() + "* \uC5D0\uC11C \uB9DE\uCDA4 \uCE6D\uD638\uAC00 `" + event.getArgs() + "` (\uC73C)\uB85C \uC124\uC815\uB428");
        }
    }
}
