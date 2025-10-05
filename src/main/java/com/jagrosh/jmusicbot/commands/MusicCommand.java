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

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.settings.Settings;
import com.jagrosh.jmusicbot.audio.AudioHandler;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.exceptions.PermissionException;
import net.dv8tion.jda.api.interactions.InteractionContextType;

import java.util.Objects;

/**
 *
 * @author John Grosh <john.a.grosh@gmail.com>
 */
public abstract class MusicCommand extends SlashCommand
{
    protected final Bot bot;
    protected boolean bePlaying;
    protected boolean beListening;
    
    public MusicCommand(Bot bot)
    {
        this.bot = bot;
        this.contexts = new InteractionContextType[]{InteractionContextType.GUILD};
        this.category = new Category("Music");
    }
    
    @Override
    protected void execute(SlashCommandEvent event)
    {
        if (event.getGuild() == null) {
            return;
        }
        Settings settings = event.getClient().getSettingsFor(event.getGuild());
        TextChannel tchannel = settings.getTextChannel(event.getGuild());
        if(tchannel!=null && !event.getTextChannel().equals(tchannel))
        {
            event.reply(event.getClient().getError()+" 당신은 "+tchannel.getAsMention()+" 에서만 명령어를 사용할 수 있습니다!").setEphemeral(true).queue();
            return;
        }
        bot.getPlayerManager().setUpHandler(event.getGuild()); // no point constantly checking for this later
        if(bePlaying && !((AudioHandler) Objects.requireNonNull(event.getGuild().getAudioManager().getSendingHandler())).isMusicPlaying(event.getJDA()))
        {
            event.reply(event.getClient().getError()+" 그걸 이용하려면 음악이 재생되고 있어야 해요!").setEphemeral(true).queue();
            return;
        }
        if(beListening)
        {
            VoiceChannel current = null;
            AudioChannelUnion union = Objects.requireNonNull(event.getGuild().getSelfMember().getVoiceState()).getChannel();
            if(union==null)
                current = settings.getVoiceChannel(event.getGuild());
            GuildVoiceState userState = Objects.requireNonNull(event.getMember()).getVoiceState();
            if(!Objects.requireNonNull(userState).inAudioChannel() || userState.isDeafened() || (current!=null && !Objects.requireNonNull(userState.getChannel()).equals(current)))
            {
                event.reply(event.getClient().getError() + " " + (current==null ? "음성 채널" : "**"+current.getName()+"**")+" 에 있어야만 사용할 수 있습니다!").setEphemeral(true).queue();
                return;
            }

            VoiceChannel afkChannel = userState.getGuild().getAfkChannel();
            if(afkChannel != null && afkChannel.equals(userState.getChannel()))
            {
                event.reply(event.getClient().getError() + " AFK(잠수) 채널에서는 이 명령을 사용할 수 없습니다!").setEphemeral(true).queue();
                return;
            }

            if(!event.getGuild().getSelfMember().getVoiceState().inAudioChannel())
            {
                try
                {
                    event.getGuild().getAudioManager().openAudioConnection(Objects.requireNonNull(userState.getChannel()));
                }
                catch(PermissionException ex)
                {
                    event.reply(event.getClient().getError()+" "+ Objects.requireNonNull(userState.getChannel()).getAsMention()+" 으(로) 연결하는 것이 불가능합니다!").setEphemeral(true).queue();
                    return;
                }
            }
        }

        if (check(event))
            doCommand(event);
        else
            event.reply(event.getClient().getError() + " 이 명령어를 사용할 권한이 없습니다!").setEphemeral(true).queue();
    }

    public boolean check(SlashCommandEvent event) {
        return true;
    }
    
    public abstract void doCommand(SlashCommandEvent event);
}
