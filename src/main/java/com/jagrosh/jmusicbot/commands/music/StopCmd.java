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
import com.jagrosh.jmusicbot.audio.AudioHandler;
import com.jagrosh.jmusicbot.commands.MusicCommand;

/**
 *
 * @author John Grosh <john.a.grosh@gmail.com>
 */
public class StopCmd extends MusicCommand
{
    public StopCmd(Bot bot)
    {
        super(bot);
        this.name = "\uC815\uC9C0";
        this.help = "\uD604\uC7AC \uB178\uB798\uB97C \uC911\uC9C0\uD558\uACE0 \uB300\uAE30\uC5F4\uC744 \uC9C0\uC6C1\uB2C8\uB2E4";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.bePlaying = false;
    }

    @Override
    public void doCommand(CommandEvent event) 
    {
        AudioHandler handler = (AudioHandler)event.getGuild().getAudioManager().getSendingHandler();
        handler.stopAndClear();
        event.getGuild().getAudioManager().closeAudioConnection();
        event.reply(event.getClient().getSuccess()+" \uD50C\uB808\uC774\uC5B4\uAC00 \uC911\uC9C0\uB418\uACE0 \uB300\uAE30\uC5F4\uC774 \uC9C0\uC6CC\uC84C\uC2B5\uB2C8\uB2E4.");
    }
}
