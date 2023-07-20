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
package com.jagrosh.jmusicbot.commands.music;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.audio.AudioHandler;
import com.jagrosh.jmusicbot.audio.RequestMetadata;
import com.jagrosh.jmusicbot.commands.MusicCommand;

/**
 *
 * @author John Grosh <john.a.grosh@gmail.com>
 */
public class SkipCmd extends MusicCommand 
{
    public SkipCmd(Bot bot)
    {
        super(bot);
        this.name = "스킵";
        this.help = "\uD604\uC7AC \uB178\uB798 \uAC74\uB108\uB6F0\uAE30\uC5D0 \uD22C\uD45C\uD569\uB2C8\uB2E4.";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.beListening = true;
        this.bePlaying = true;
    }

    @Override
    public void doCommand(CommandEvent event) 
    {
        AudioHandler handler = (AudioHandler)event.getGuild().getAudioManager().getSendingHandler();
        RequestMetadata rm = handler.getRequestMetadata();
        if(event.getAuthor().getIdLong() == rm.getOwner())
        {
            event.reply(event.getClient().getSuccess()+" **"+handler.getPlayer().getPlayingTrack().getInfo().title+"** (\uC774)\uAC00 \uAC74\uB108\uB6F0\uC5B4\uC9D0");
            handler.getPlayer().stopTrack();
        }
        else
        {
            int listeners = (int)event.getSelfMember().getVoiceState().getChannel().getMembers().stream()
                    .filter(m -> !m.getUser().isBot() && !m.getVoiceState().isDeafened()).count();
            String msg;
            if(handler.getVotes().contains(event.getAuthor().getId()))
                msg = event.getClient().getWarning()+" \uC774\uBBF8 \uC774 \uB178\uB798\uB97C \uAC74\uB108\uB6F0\uB3C4\uB85D \uD22C\uD45C\uD588\uC2B5\uB2C8\uB2E4 `[";
            else
            {
                msg = event.getClient().getSuccess()+" \uB2F9\uC2E0\uC740 \uADF8 \uB178\uB798\uB97C \uAC74\uB108\uB6F0\uAE30\uB85C \uD22C\uD45C\uD588\uC2B5\uB2C8\uB2E4 `[";
                handler.getVotes().add(event.getAuthor().getId());
            }
            int skippers = (int)event.getSelfMember().getVoiceState().getChannel().getMembers().stream()
                    .filter(m -> handler.getVotes().contains(m.getUser().getId())).count();
            int required = (int)Math.ceil(listeners * bot.getSettingsManager().getSettings(event.getGuild()).getSkipRatio());
            msg += skippers + " votes, " + required + "/" + listeners + " needed]`";
            if(skippers>=required)
            {
                msg += "\n" + event.getClient().getSuccess() + " Skipped **" + handler.getPlayer().getPlayingTrack().getInfo().title
                        + "** " + (rm.getOwner() == 0L ? "(autoplay)" : "(requested by **" + rm.user.username + "**)");
                handler.getPlayer().stopTrack();
            }
            event.reply(msg);
        }
    }
    
}
