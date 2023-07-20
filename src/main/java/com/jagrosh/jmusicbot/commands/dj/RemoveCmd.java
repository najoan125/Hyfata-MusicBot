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
import com.jagrosh.jmusicbot.audio.QueuedTrack;
import com.jagrosh.jmusicbot.commands.DJCommand;
import com.jagrosh.jmusicbot.settings.Settings;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;

/**
 *
 * @author John Grosh <john.a.grosh@gmail.com>
 */
public class RemoveCmd extends DJCommand
{
    public RemoveCmd(Bot bot)
    {
        super(bot);
        this.name = "제거";
        this.help = "\uB178\uB798\uB97C \uB300\uAE30\uC5F4\uC5D0\uC11C \uC81C\uAC70\uD569\uB2C8\uB2E4";
        this.arguments = "<position|ALL>";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.beListening = true;
        this.bePlaying = true;
    }

    @Override
    public void doCommand(CommandEvent event) 
    {
        AudioHandler handler = (AudioHandler)event.getGuild().getAudioManager().getSendingHandler();
        if(handler.getQueue().isEmpty())
        {
            event.replyError("\uADF8\uAC83\uC740 \uB300\uAE30\uC5F4\uC5D0 \uC5C6\uC2B5\uB2C8\uB2E4!");
            return;
        }
        if(event.getArgs().equalsIgnoreCase("all"))
        {
            int count = handler.getQueue().removeAll(event.getAuthor().getIdLong());
            if(count==0)
                event.replyWarning("\uB300\uAE30\uC5F4\uC5D0 \uC5B4\uB5A4 \uB178\uB798\uB3C4 \uC5C6\uC2B5\uB2C8\uB2E4!");
            else
                event.replySuccess("\uC131\uACF5\uC801\uC73C\uB85C "+count+" \uD56D\uBAA9\uC744 \uC81C\uAC70\uD558\uC600\uC2B5\uB2C8\uB2E4.");
            return;
        }
        int pos;
        try {
            pos = Integer.parseInt(event.getArgs());
        } catch(NumberFormatException e) {
            pos = 0;
        }
        if(pos<1 || pos>handler.getQueue().size())
        {
            event.replyError("\uC704\uCE58\uB294 1\uACFC "+handler.getQueue().size()+" \uC0AC\uC774\uC758 \uC720\uD6A8\uD55C \uC815\uC218\uC5EC\uC57C \uD569\uB2C8\uB2E4!");
            return;
        }
        Settings settings = event.getClient().getSettingsFor(event.getGuild());
        boolean isDJ = event.getMember().hasPermission(Permission.MANAGE_SERVER);
        if(!isDJ)
            isDJ = event.getMember().getRoles().contains(settings.getRole(event.getGuild()));
        QueuedTrack qt = handler.getQueue().get(pos-1);
        if(qt.getIdentifier()==event.getAuthor().getIdLong())
        {
            handler.getQueue().remove(pos-1);
            event.replySuccess("**"+qt.getTrack().getInfo().title+"** (\uC774)\uAC00 \uB300\uAE30\uC5F4\uC5D0\uC11C \uC81C\uAC70\uB428");
        }
        else if(isDJ)
        {
            handler.getQueue().remove(pos-1);
            User u;
            try {
                u = event.getJDA().getUserById(qt.getIdentifier());
            } catch(Exception e) {
                u = null;
            }
            event.replySuccess("**"+qt.getTrack().getInfo().title
                    +"** (\uC774)\uAC00 \uB300\uAE30\uC5F4\uC5D0\uC11C \uC81C\uAC70\uB428 ("+(u==null ? "someone" : "**"+u.getName()+"** \uC5D0 \uC758\uD574 \uC694\uCCAD\uB428")+")");
        }
        else
        {
            event.replyError("**"+qt.getTrack().getInfo().title+"** (\uC744)\uB97C \uCD94\uAC00\uD558\uC9C0 \uC54A\uC558\uC73C\uBBC0\uB85C \uC81C\uAC70\uD560 \uC218 \uC5C6\uC2B5\uB2C8\uB2E4!");
        }
    }
}
