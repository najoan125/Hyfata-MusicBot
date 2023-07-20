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

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.commands.MusicCommand;
import com.jagrosh.jmusicbot.settings.RepeatMode;
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
        this.help = "대기열 전체를 반복하거나 노래 한곡만 단일 반복 합니다.";
        this.arguments = "[끄기|켜기(전체)|단일]";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.guildOnly = true;
    }
    
    // override musiccommand's execute because we don't actually care where this is used
    @Override
    protected void execute(CommandEvent event) 
    {
        String args = event.getArgs();
        RepeatMode value;
        Settings settings = event.getClient().getSettingsFor(event.getGuild());
        if(args.isEmpty())
        {
            if(settings.getRepeatMode() == RepeatMode.OFF)
                value = RepeatMode.ALL;
            else
                value = RepeatMode.OFF;
        }
        else if(args.equalsIgnoreCase("false") || args.equalsIgnoreCase("off") || args.equalsIgnoreCase("끄기"))
        {
        	value = RepeatMode.OFF;
        }
        else if(args.equalsIgnoreCase("true") || args.equalsIgnoreCase("on") || args.equalsIgnoreCase("all") || args.equalsIgnoreCase("켜기") || args.equalsIgnoreCase("전체"))
        {
            value = RepeatMode.ALL;
        }
        else if(args.equalsIgnoreCase("one") || args.equalsIgnoreCase("single") || args.equalsIgnoreCase("단일"))
        {
            value = RepeatMode.SINGLE;
        }
        else
        {
            event.replyError("\uC720\uD6A8\uD55C \uC635\uC158\uC740 `전체` \uB610\uB294 `끄기` 또는 `단일` \uC785\uB2C8\uB2E4 (또는 비워두면 `꺼짐` 과 `전체` 로 전환됩니다)");
            return;
        }
        settings.setRepeatMode(value);
        event.replySuccess("반복 모드: `"+value.getUserFriendlyName()+"`");
    }

    @Override
    public void doCommand(CommandEvent event) { /* Intentionally Empty */ }
}
