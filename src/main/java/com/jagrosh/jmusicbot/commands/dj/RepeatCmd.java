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
package com.jagrosh.jmusicbot.commands.dj;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.commands.MusicCommand;
import com.jagrosh.jmusicbot.settings.Settings;

/**
 *
 * @author John Grosh <john.a.grosh@gmail.com>
 */
public class RepeatCmd extends MusicCommand
{
    public RepeatCmd(Bot bot)
    {
        super(bot);
        this.name = "\uBC18\uBCF5";
        this.help = "\uD604\uC7AC \uB178\uB798\uAC00 \uB05D\uB098\uBA74 \uADF8 \uB178\uB798\uB97C \uB300\uAE30\uC5F4\uC5D0 \uB2E4\uC2DC \uB123\uC2B5\uB2C8\uB2E4.";
        this.arguments = "[\uCF1C\uAE30|\uB044\uAE30]";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.guildOnly = true;
    }
    
    // override musiccommand's execute because we don't actually care where this is used
    @Override
    protected void execute(CommandEvent event) 
    {
        boolean value;
        Settings settings = event.getClient().getSettingsFor(event.getGuild());
        if(event.getArgs().isEmpty())
        {
            value = !settings.getRepeatMode();
        }
        else if(event.getArgs().equalsIgnoreCase("on") || event.getArgs().equalsIgnoreCase("\uCF1C\uAE30"))
        {
            value = true;
        }
        else if(event.getArgs().equalsIgnoreCase("off") || event.getArgs().equalsIgnoreCase("\uB044\uAE30"))
        {
            value = false;
        }
        else
        {
            event.replyError("\uC720\uD6A8\uD55C \uC635\uC158\uC740 `on` \uB610\uB294 `off` \uC785\uB2C8\uB2E4 (\uB610\uB294 \uBE44\uC6CC \uB450\uBA74 \uC804\uD658\uB429\uB2C8\uB2E4)");
            return;
        }
        settings.setRepeatMode(value);
        event.replySuccess("\uBC18\uBCF5 \uBAA8\uB4DC\uAC00 `"+(value ? "\uCF1C\uC9D0" : "\uAEBC\uC9D0")+"`");
    }

    @Override
    public void doCommand(CommandEvent event) { /* Intentionally Empty */ }
}
