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
package com.jagrosh.jmusicbot.commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.settings.Settings;
import com.jagrosh.jmusicbot.audio.AudioHandler;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.exceptions.PermissionException;

/**
 *
 * @author John Grosh <john.a.grosh@gmail.com>
 */
public abstract class MusicCommand extends Command 
{
    protected final Bot bot;
    protected boolean bePlaying;
    protected boolean beListening;
    
    public MusicCommand(Bot bot)
    {
        this.bot = bot;
        this.guildOnly = true;
        this.category = new Category("Music");
    }
    
    @Override
    protected void execute(CommandEvent event) 
    {
        Settings settings = event.getClient().getSettingsFor(event.getGuild());
        TextChannel tchannel = settings.getTextChannel(event.getGuild());
        if(tchannel!=null && !event.getTextChannel().equals(tchannel))
        {
            try 
            {
                event.getMessage().delete().queue();
            } catch(PermissionException ignore){}
            event.replyInDm(event.getClient().getError()+" \uB2F9\uC2E0\uC740 "+tchannel.getAsMention()+" \uC5D0\uC11C\uB9CC \uBA85\uB839\uC5B4\uB97C \uC0AC\uC6A9\uD560 \uC218 \uC788\uC2B5\uB2C8\uB2E4!");
            return;
        }
        bot.getPlayerManager().setUpHandler(event.getGuild()); // no point constantly checking for this later
        if(bePlaying && !((AudioHandler)event.getGuild().getAudioManager().getSendingHandler()).isMusicPlaying(event.getJDA()))
        {
            event.reply(event.getClient().getError()+" \uADF8\uAC78 \uC774\uC6A9\uD558\uB824\uBA74 \uC74C\uC545\uC774 \uC7AC\uC0DD\uB418\uACE0 \uC788\uC5B4\uC57C \uD574\uC694!");
            return;
        }
        if(beListening)
        {
            VoiceChannel current = event.getGuild().getSelfMember().getVoiceState().getChannel();
            if(current==null)
                current = settings.getVoiceChannel(event.getGuild());
            GuildVoiceState userState = event.getMember().getVoiceState();
            if(!userState.inVoiceChannel() || userState.isDeafened() || (current!=null && !userState.getChannel().equals(current)))
            {
                event.replyError(""+(current==null ? "\uC74C\uC131 \uCC44\uB110" : "**"+current.getName()+"**")+" \uC5D0 \uC788\uC5B4\uC57C\uB9CC \uC0AC\uC6A9\uD560 \uC218 \uC788\uC2B5\uB2C8\uB2E4!");
                return;
            }

            VoiceChannel afkChannel = userState.getGuild().getAfkChannel();
            if(afkChannel != null && afkChannel.equals(userState.getChannel()))
            {
                event.replyError("AFK(\uC7A0\uC218) \uCC44\uB110\uC5D0\uC11C\uB294 \uC774 \uBA85\uB839\uC744 \uC0AC\uC6A9\uD560 \uC218 \uC5C6\uC2B5\uB2C8\uB2E4!");
                return;
            }

            if(!event.getGuild().getSelfMember().getVoiceState().inVoiceChannel())
            {
                try 
                {
                    event.getGuild().getAudioManager().openAudioConnection(userState.getChannel());
                    event.getGuild().getAudioManager().setSelfDeafened(true);
                }
                catch(PermissionException ex) 
                {
                    event.reply(event.getClient().getError()+" "+userState.getChannel().getAsMention()+" *\uC73C*\uB85C \uC5F0\uACB0\uD558\uB294 \uAC83\uC774 \uBD88\uAC00\uB2A5\uD569\uB2C8\uB2E4!");
                    return;
                }
            }
        }
        
        doCommand(event);
    }
    
    public abstract void doCommand(CommandEvent event);
}
