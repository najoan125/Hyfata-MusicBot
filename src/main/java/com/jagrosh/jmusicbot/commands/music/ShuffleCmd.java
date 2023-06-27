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
public class ShuffleCmd extends MusicCommand 
{
    public ShuffleCmd(Bot bot)
    {
        super(bot);
        this.name = "\uC154\uD50C";
        this.help = "\uCD94\uAC00\uD55C \uB178\uB798\uB97C \uC11E\uC2B5\uB2C8\uB2E4";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.beListening = true;
        this.bePlaying = true;
    }

    @Override
    public void doCommand(CommandEvent event) 
    {
        AudioHandler handler = (AudioHandler)event.getGuild().getAudioManager().getSendingHandler();
        int s = handler.getQueue().shuffle(event.getAuthor().getIdLong());
        switch (s) 
        {
            case 0:
                event.replyError("\uB300\uAE30\uC5F4\uC5D0 \uC11E\uC744 \uC74C\uC545\uC774 \uC5C6\uC2B5\uB2C8\uB2E4!");
                break;
            case 1:
                event.replyWarning("\uB300\uAE30\uC5F4\uC5D0 \uB178\uB798\uAC00 \uD558\uB098\uB9CC \uC788\uC2B5\uB2C8\uB2E4!");
                break;
            default:
                event.replySuccess(s+" \uD56D\uBAA9\uC744 \uC131\uACF5\uC801\uC73C\uB85C \uC11E\uC5C8\uC2B5\uB2C8\uB2E4");
                break;
        }
    }
    
}
