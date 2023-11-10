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
import com.jagrosh.jmusicbot.JMusicBot;
import com.jagrosh.jmusicbot.audio.AudioHandler;
import com.jagrosh.jmusicbot.audio.RequestMetadata;
import com.jagrosh.jmusicbot.commands.MusicCommand;

/**
 *
 * @author John Grosh <john.a.grosh@gmail.com>
 */
public class PauseCmd extends MusicCommand
{
    public PauseCmd(Bot bot)
    {
        super(bot);
        this.name = "\uC77C\uC2DC\uC815\uC9C0";
        this.help = "\uD604\uC7AC \uACE1\uC744 \uC77C\uC2DC\uC815\uC9C0\uD569\uB2C8\uB2E4";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.bePlaying = true;
    }

    @Override
    public void doCommand(CommandEvent event) 
    {
        AudioHandler handler = (AudioHandler)event.getGuild().getAudioManager().getSendingHandler();
        if (JMusicBot.rnjsska &&
                handler != null &&
                handler.getPlayer().getPlayingTrack() != null &&
                handler.getPlayer().getPlayingTrack().getUserData(RequestMetadata.class).user.id == bot.getConfig().getOwnerId() &&
                event.getMember().getIdLong() != bot.getConfig().getOwnerId()) {
            event.reply(event.getClient().getError() + "봇의 소유자가 권남 모드 활성화해서 일시정지 못함 ㅋㅋ ㅅㄱ");
            return;
        }
        if(handler.getPlayer().isPaused())
        {
            event.replyWarning("\uC774\uBBF8 \uD50C\uB808\uC774\uC5B4\uAC00 \uC77C\uC2DC\uC815\uC9C0 \uB418\uC5C8\uC2B5\uB2C8\uB2E4! `"+event.getClient().getPrefix()+"재생` \uB97C \uC0AC\uC6A9\uD558\uC5EC \uC77C\uC2DC\uC815\uC9C0\uB97C \uD574\uC81C\uD558\uC138\uC694!");
            return;
        }
        handler.getPlayer().setPaused(true);
        event.replySuccess("**"+handler.getPlayer().getPlayingTrack().getInfo().title+"** (\uC744)\uB97C \uC77C\uC2DC\uC815\uC9C0\uD558\uC600\uC2B5\uB2C8\uB2E4. `"+event.getClient().getPrefix()+"재생` \uB97C \uC785\uB825\uD558\uC5EC \uB2E4\uC2DC \uC7AC\uC0DD\uD558\uC138\uC694!");
    }
}
