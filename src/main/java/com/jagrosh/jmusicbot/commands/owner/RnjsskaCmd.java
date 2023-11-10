/*
 * Copyright 2017 John Grosh <john.a.grosh@gmail.com>.
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
package com.jagrosh.jmusicbot.commands.owner;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.JMusicBot;
import com.jagrosh.jmusicbot.commands.OwnerCommand;

/**
 *
 * @author John Grosh <john.a.grosh@gmail.com>
 */
public class RnjsskaCmd extends OwnerCommand
{

    public RnjsskaCmd(Bot bot)
    {
        this.name = "rnjsska";
        this.help = "권남을 사용합니다 ㅋㅋ";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.guildOnly = false;
    }
    
    @Override
    protected void execute(CommandEvent event)
    {
        event.replyWarning(JMusicBot.rnjsska ? "권남 모드를 해제합니다 ㅜㅜ" : "권남 모드를 활성화 합니다! 이제 마음껏 즐길 수 있겠군 ㅋㅋ ㅅㄱ ㅋㅋㅋㅋㅋㅋ");
        JMusicBot.rnjsska = !JMusicBot.rnjsska;
    }
}
