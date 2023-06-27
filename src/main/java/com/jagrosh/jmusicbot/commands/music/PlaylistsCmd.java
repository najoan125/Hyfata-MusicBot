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

import java.util.List;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.commands.MusicCommand;

/**
 *
 * @author John Grosh <john.a.grosh@gmail.com>
 */
public class PlaylistsCmd extends MusicCommand 
{
    public PlaylistsCmd(Bot bot)
    {
        super(bot);
        this.name = "playlists";
        this.help = "\uC0AC\uC6A9 \uAC00\uB2A5\uD55C \uC7AC\uC0DD \uBAA9\uB85D\uC744 \uD45C\uC2DC\uD569\uB2C8\uB2E4";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.guildOnly = true;
        this.beListening = false;
        this.beListening = false;
    }
    
    @Override
    public void doCommand(CommandEvent event) 
    {
        if(!bot.getPlaylistLoader().folderExists())
            bot.getPlaylistLoader().createFolder();
        if(!bot.getPlaylistLoader().folderExists())
        {
            event.reply(event.getClient().getWarning()+" \uC7AC\uC0DD \uBAA9\uB85D \uD3F4\uB354\uAC00 \uC5C6\uC73C\uBBC0\uB85C \uB9CC\uB4E4 \uC218 \uC5C6\uC2B5\uB2C8\uB2E4!");
            return;
        }
        List<String> list = bot.getPlaylistLoader().getPlaylistNames();
        if(list==null)
            event.reply(event.getClient().getError()+" \uC0AC\uC6A9 \uAC00\uB2A5\uD55C \uC7AC\uC0DD \uBAA9\uB85D\uC744 \uB85C\uB4DC\uD558\uC9C0 \uBABB\uD588\uC2B5\uB2C8\uB2E4!");
        else if(list.isEmpty())
            event.reply(event.getClient().getWarning()+" \uC7AC\uC0DD \uBAA9\uB85D \uD3F4\uB354\uC5D0 \uC7AC\uC0DD \uBAA9\uB85D\uC774 \uC5C6\uC2B5\uB2C8\uB2E4!");
        else
        {
            StringBuilder builder = new StringBuilder(event.getClient().getSuccess()+" \uC0AC\uC6A9 \uAC00\uB2A5\uD55C \uC7AC\uC0DD \uBAA9\uB85D:\n");
            list.forEach(str -> builder.append("| `").append(str).append("` "));
            builder.append("|\n`").append(event.getClient().getTextualPrefix()).append("재생 pl <name>` (\uC744)\uB97C \uC785\uB825\uD558\uC5EC \uC7AC\uC0DD \uBAA9\uB85D\uC744 \uC7AC\uC0DD\uD558\uC138\uC694");
            event.reply(builder.toString());
        }
    }
}
