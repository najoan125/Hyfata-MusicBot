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
import com.jagrosh.jmusicbot.audio.AudioHandler;
import com.jagrosh.jmusicbot.commands.DJCommand;
import net.dv8tion.jda.api.entities.User;

/**
 *
 * @author John Grosh <john.a.grosh@gmail.com>
 */
public class ForceskipCmd extends DJCommand 
{
    public ForceskipCmd(Bot bot)
    {
        super(bot);
        this.name = "forceskip";
        this.help = "\uD604\uC7AC \uB178\uB798\uB97C \uAC74\uB108\uB701\uB2C8\uB2E4";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.bePlaying = true;
    }

    @Override
    public void doCommand(CommandEvent event) 
    {
        AudioHandler handler = (AudioHandler)event.getGuild().getAudioManager().getSendingHandler();
        User u = event.getJDA().getUserById(handler.getRequester());
        event.reply(event.getClient().getSuccess()+" **"+handler.getPlayer().getPlayingTrack().getInfo().title
                +"** (\uC744)\uB97C \uAC74\uB108\uB6F0\uC5C8\uC2B5\uB2C8\uB2E4 ("+(u==null ? "someone" : "**"+u.getName()+"** \uB2D8\uC5D0 \uC758\uD574 \uC694\uCCAD\uB428")+")");
        handler.getPlayer().stopTrack();
    }
}
