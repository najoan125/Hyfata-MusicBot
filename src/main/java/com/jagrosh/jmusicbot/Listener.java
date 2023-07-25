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
package com.jagrosh.jmusicbot;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jmusicbot.audio.AudioHandler;
import com.jagrosh.jmusicbot.audio.QueuedTrack;
import com.jagrosh.jmusicbot.audio.RequestMetadata;
import com.jagrosh.jmusicbot.commands.music.SearchCmd;
import com.jagrosh.jmusicbot.settings.RepeatMode;
import com.jagrosh.jmusicbot.settings.Settings;
import com.jagrosh.jmusicbot.utils.FormatUtil;
import com.jagrosh.jmusicbot.utils.OtherUtil;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.ShutdownEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceSelfDeafenEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageDeleteEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author John Grosh (john.a.grosh@gmail.com)
 */
public class Listener extends ListenerAdapter
{
    private final Bot bot;
    public Listener(Bot bot)
    {
        this.bot = bot;
    }

    @Override
    public void onReady(ReadyEvent event)
    {
        if(event.getJDA().getGuildCache().isEmpty())
        {
            Logger log = LoggerFactory.getLogger("MusicBot");
            log.warn("This bot is not on any guilds! Use the following link to add the bot to your guilds!");
            log.warn(event.getJDA().getInviteUrl(JMusicBot.RECOMMENDED_PERMS));
        }
        credit(event.getJDA());
        event.getJDA().getGuilds().forEach((guild) ->
        {
            try
            {
                String defpl = bot.getSettingsManager().getSettings(guild).getDefaultPlaylist();
                VoiceChannel vc = bot.getSettingsManager().getSettings(guild).getVoiceChannel(guild);
                if(defpl!=null && vc!=null && bot.getPlayerManager().setUpHandler(guild).playFromDefault())
                {
                    guild.getAudioManager().openAudioConnection(vc);
                }
            }
            catch(Exception ignore) {}
        });
        if(bot.getConfig().useUpdateAlerts())
        {
            bot.getThreadpool().scheduleWithFixedDelay(() ->
            {
                try
                {
                	User owner = bot.getJDA().retrieveUserById(bot.getConfig().getOwnerId()).complete();
                    String currentVersion = OtherUtil.getCurrentVersion();
                    String latestVersion = OtherUtil.getLatestVersion();
                    if(latestVersion!=null && !currentVersion.equalsIgnoreCase(latestVersion) && !currentVersion.equalsIgnoreCase("UNKNOWN"))
                    {
                        String msg = String.format(OtherUtil.NEW_VERSION_AVAILABLE, currentVersion, latestVersion);
                        owner.openPrivateChannel().queue(pc -> pc.sendMessage(msg).queue());
                    }
                }
                catch(Exception ex) {} //ignored
            }, 0, 24, TimeUnit.HOURS);
        }
    }

    @Override
    public void onGuildMessageDelete(GuildMessageDeleteEvent event)
    {
        bot.getNowplayingHandler().onMessageDelete(event.getGuild(), event.getMessageIdLong());
    }

    @Override
    public void onGuildVoiceUpdate(@NotNull GuildVoiceUpdateEvent event)
    {
        bot.getAloneInVoiceHandler().onVoiceUpdate(event);
    }

    @Override
    public void onGuildVoiceSelfDeafen(GuildVoiceSelfDeafenEvent event)
    {
    	bot.getAloneInVoiceHandler().Deafen(event);
    }

    @Override
    public void onShutdown(ShutdownEvent event)
    {
        bot.shutdown();
    }

    @Override
    public void onGuildJoin(GuildJoinEvent event)
    {
        credit(event.getJDA());
    }

    @Override
    public void onButtonClick(ButtonClickEvent event) {
    	String messageId = event.getMessageId();
    	User user = SearchCmd.searchCmdMap.get(messageId);

        AudioHandler handler = (AudioHandler) Objects.requireNonNull(event.getGuild()).getAudioManager().getSendingHandler();
        int volume = handler.getPlayer().getVolume();
        if(event.getComponentId().equals("pause") && Objects.requireNonNull(handler).isMusicPlaying(event.getJDA())){
            handler.getPlayer().setPaused(!handler.getPlayer().isPaused());
            event.editMessage(handler.getNowPlaying(event.getJDA())).queue();
        }
    	else if (event.getComponentId().equals("next") && Objects.requireNonNull(handler).isMusicPlaying(event.getJDA())){
            nextCmdClicked(event);
        }
        else if (event.getComponentId().equals("volumeDown")){
            if (volume - 10 >= 0){
                handler.getPlayer().setVolume(volume - 10);
                event.editMessage(handler.getNowPlaying(event.getJDA())).queue();
            }
            else {
                event.deferEdit().queue();
            }
        }
        else if (event.getComponentId().equals("volumeUp")){
            if (volume + 10 <= 150){
                handler.getPlayer().setVolume(volume + 10);
                event.editMessage(handler.getNowPlaying(event.getJDA())).queue();
            }
            else{
                event.deferEdit().queue();
            }
        }
        else if (event.getComponentId().equals("repeat")){
            Settings settings = bot.getSettingsManager().getSettings(event.getGuild());
            RepeatMode value;
            if(settings.getRepeatMode() == RepeatMode.OFF)
                value = RepeatMode.ALL;
            else
                value = RepeatMode.OFF;
            settings.setRepeatMode(value);
            event.editMessage(handler.getNowPlaying(event.getJDA())).queue();
        }
    	else if (user != null && Objects.requireNonNull(event.getMember()).getUser().equals(user)) {
    		searchCmdClicked(event);
    	}
    }

    private void nextCmdClicked(ButtonClickEvent event){
        AudioHandler handler = (AudioHandler)event.getGuild().getAudioManager().getSendingHandler();
        RequestMetadata rm = handler.getRequestMetadata();
        if(event.getUser().getIdLong() == rm.getOwner())
        {
            event.reply(new MessageBuilder()
                    .setContent(bot.getConfig().getSuccess()+"**<@"+event.getUser().getId()+">**님에 의해 건너뛰어졌습니다!")
                    .setEmbeds(new EmbedBuilder()
                        .setTitle(handler.getPlayer().getPlayingTrack().getInfo().title, handler.getPlayer().getPlayingTrack().getInfo().uri)
                        .setDescription("이 항목을 건너뛰었습니다!")
                        .build()).build()
            ).queue();
            handler.getPlayer().stopTrack();
        }
        else
        {
            int listeners = (int)event.getGuild().getSelfMember().getVoiceState().getChannel().getMembers().stream()
                    .filter(m -> !m.getUser().isBot() && !m.getVoiceState().isDeafened()).count();
            String msg;
            if(handler.getVotes().contains(event.getUser().getId()))
                msg = bot.getConfig().getWarning()+" <@"+event.getUser().getId()+"> 이미 현재 재생 중인 항목을 건너뛰기로 투표했습니다 `[";
            else
            {
                msg = bot.getConfig().getSuccess()+" <@"+event.getUser().getId()+"> 현재 재생 중인 항목을 건너뛰기로 투표했습니다 `[";
                handler.getVotes().add(event.getUser().getId());
            }
            int skippers = (int)event.getGuild().getSelfMember().getVoiceState().getChannel().getMembers().stream()
                    .filter(m -> handler.getVotes().contains(m.getUser().getId())).count();
            int required = (int)Math.ceil(listeners * bot.getSettingsManager().getSettings(event.getGuild()).getSkipRatio());
            msg += skippers + "표, " + required + "/" + listeners + " 필요]`";
            String embed;
            if(skippers>=required)
            {
                msg += "\n" + bot.getConfig().getSuccess() + " 이 항목을 건너뛰었습니다!";
                embed = (rm.getOwner() == 0L ? "(자동 재생)" : "(**" + rm.user.username + "**에 의해 요청됨)");

                event.reply(new MessageBuilder()
                        .setContent(msg)
                        .setEmbeds(new EmbedBuilder()
                                .setTitle(handler.getPlayer().getPlayingTrack().getInfo().title, handler.getPlayer().getPlayingTrack().getInfo().uri)
                                .setDescription(embed)
                                .build()
                        ).build()
                ).queue();

                handler.getPlayer().stopTrack();
            }
            else
                event.reply(msg).queue();
        }
    }

    private void searchCmdClicked(ButtonClickEvent event) {
    	String messageId = event.getMessageId();
    	SearchCmd.searchCmdExecutors.get(messageId).shutdownNow();
    	SearchCmd.searchCmdExecutors.remove(messageId);

    	AudioPlaylist pl = SearchCmd.searchCmdPlaylist.get(messageId);
    	CommandEvent ev = SearchCmd.searchCmdEvent.get(messageId);
        AudioHandler handler = (AudioHandler)ev.getGuild().getAudioManager().getSendingHandler();
    	if (event.getComponentId().equals("cancel")) {
    		event.editMessage("검색이 취소되었습니다.").setEmbeds().setActionRows().queue();
            if (!handler.isMusicPlaying(ev.getJDA()) && handler.getQueue().isEmpty()){
                if (!bot.getConfig().getStay())
                    ev.getGuild().getAudioManager().closeAudioConnection();
            }
    	}
    	else {
    		AudioTrack track = pl.getTracks().get(Integer.parseInt(event.getButton().getId())-1);
    		if(bot.getConfig().isTooLong(track))
            {
    		    event.getMessage().delete().queue();
                ev.replyWarning("\uC774 \uD2B8\uB799 (**"+track.getInfo().title+"**) (\uC740)\uB294 \uD5C8\uC6A9\uB41C \uCD5C\uB300\uCE58\uBCF4\uB2E4 \uAE41\uB2C8\uB2E4: `"
                        +FormatUtil.formatTime(track.getDuration())+"` > `"+bot.getConfig().getMaxTime()+"`");
                return;
            }
            int pos = handler.addTrack(new QueuedTrack(track, ev.getAuthor()))+1;
            Message addMsg = new MessageBuilder().setContent(FormatUtil.filter(bot.getConfig().getSuccess()+
                    (pos==0?" 요청한 항목을 바로 재생합니다":" 요청한 항목이 **대기열 위치 "+pos+"** 에 추가되었습니다"))).build();
            MessageAction ma = event.getMessage().editMessage(addMsg);
            EmbedBuilder eb = new EmbedBuilder();
            eb.setTitle(track.getInfo().title, track.getInfo().uri);
            eb.setDescription("요청한 항목"+(pos==0?"을 바로 재생합니다!":"이 대기열에 추가되었습니다!")+"\n(`"+FormatUtil.formatTime(track.getDuration())+"`)");
            ma.setEmbeds(eb.build()).queue();
            event.deferEdit().queue();
    	}
    	SearchCmd.searchCmdMap.remove(messageId);
    	SearchCmd.searchCmdPlaylist.remove(messageId);
    	SearchCmd.searchCmdEvent.remove(messageId);
    }

    // make sure people aren't adding clones to dbots
    private void credit(JDA jda)
    {
        Guild dbots = jda.getGuildById(110373943822540800L);
        if(dbots==null)
            return;
        if(bot.getConfig().getDBots())
            return;
        jda.getTextChannelById(119222314964353025L)
                .sendMessage("This account is running JMusicBot. Please do not list bot clones on this server, <@"+bot.getConfig().getOwnerId()+">.").complete();
        dbots.leave().queue();
    }
}
