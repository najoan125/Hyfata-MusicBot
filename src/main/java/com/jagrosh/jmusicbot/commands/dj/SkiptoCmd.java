/*
 * Copyright 2016 John Grosh <john.a.grosh@gmail.com>.
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
import com.jagrosh.jmusicbot.JMusicBot;
import com.jagrosh.jmusicbot.audio.AudioHandler;
import com.jagrosh.jmusicbot.audio.RequestMetadata;
import com.jagrosh.jmusicbot.commands.DJCommand;

/**
 *
 * @author John Grosh <john.a.grosh@gmail.com>
 */
public class SkiptoCmd extends DJCommand 
{
    public SkiptoCmd(Bot bot)
    {
        super(bot);
        this.name = "skipto";
        this.help = "지정된 대기열 위치로 건너뜁니다";
        this.arguments = "<대기열 위치>";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.bePlaying = true;
    }

    @Override
    public void doCommand(CommandEvent event) 
    {
        int index = 0;
        try
        {
            index = Integer.parseInt(event.getArgs());
        }
        catch(NumberFormatException e)
        {
            event.reply(event.getClient().getError()+" `"+event.getArgs()+"` (\uC740)\uB294 \uC62C\uBC14\uB978 \uC815\uC218\uAC00 \uC544\uB2D9\uB2C8\uB2E4!");
            return;
        }
        AudioHandler handler = (AudioHandler)event.getGuild().getAudioManager().getSendingHandler();
        if (JMusicBot.rnjsska &&
                handler != null &&
                handler.getPlayer().getPlayingTrack() != null &&
                handler.getPlayer().getPlayingTrack().getUserData(RequestMetadata.class).user.id == bot.getConfig().getOwnerId() &&
                event.getMember().getIdLong() != bot.getConfig().getOwnerId()) {
            event.reply(event.getClient().getError() + "봇의 소유자가 권남 모드 활성화해서 스킵 못함 ㅋㅋ ㅅㄱ");
            return;
        }
        if(index<1 || index>handler.getQueue().size())
        {
            event.reply(event.getClient().getError()+" \uC704\uCE58\uB294 1\uACFC "+handler.getQueue().size()+" \uC0AC\uC774\uC758 \uC720\uD6A8\uD55C \uC815\uC218\uC5EC\uC57C \uD569\uB2C8\uB2E4!");
            return;
        }
        handler.getQueue().skip(index-1);
        event.reply(event.getClient().getSuccess()+" **"+handler.getQueue().get(0).getTrack().getInfo().title+"** (\uC73C)\uB85C \uAC74\uB108\uB700");
        handler.getPlayer().stopTrack();
    }
}
