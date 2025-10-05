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
package com.jagrosh.jmusicbot.commands.dj;

import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.audio.AudioHandler;
import com.jagrosh.jmusicbot.audio.QueuedTrack;
import com.jagrosh.jmusicbot.audio.RequestMetadata;
import com.jagrosh.jmusicbot.commands.DJCommand;
import com.jagrosh.jmusicbot.utils.FormatUtil;
import com.jagrosh.jmusicbot.utils.TimeUtil;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.Collections;
import java.util.Objects;

/**
 *
 * @author John Grosh (john.a.grosh@gmail.com)
 */
public class PlaynextCmd extends DJCommand
{
    public PlaynextCmd(Bot bot)
    {
        super(bot);
        this.name = "새치기";
        this.arguments = "<제목|URL>";
        this.help = "현재 곡이 끝나고 새치기 하여 제공된 음악을 재생합니다";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.beListening = true;
        this.bePlaying = false;

        this.options = Collections.singletonList(
                new OptionData(OptionType.STRING, "제목_또는_url", "제목 또는 URL로 제공된 곡을 새치기하여 재생").setRequired(true)
        );
    }

    @Override
    public void doCommand(SlashCommandEvent event)
    {
        var option = event.getOption("제목_또는_url");
        String args = option == null ? "" : option.getAsString();

        event.deferReply().queue(
                m -> bot.getPlayerManager().loadItemOrdered(event.getGuild(), args, new ResultHandler(m,event,false, args))
        );
    }

    private class ResultHandler implements AudioLoadResultHandler
    {
        private final InteractionHook m;
        private final SlashCommandEvent event;
        private final boolean ytsearch;
        private final String args;

        private ResultHandler(InteractionHook m, SlashCommandEvent event, boolean ytsearch, String args)
        {
            this.m = m;
            this.event = event;
            this.ytsearch = ytsearch;
            this.args = args;
        }

        private void loadSingle(AudioTrack track)
        {
            if(bot.getConfig().isTooLong(track))
            {
                m.editOriginal(FormatUtil.filter(event.getClient().getWarning()+" This track (**"+track.getInfo().title+"**) is longer than the allowed maximum: `"
                        + TimeUtil.formatTime(track.getDuration())+"` > `"+ TimeUtil.formatTime(bot.getConfig().getMaxSeconds()*1000)+"`")).queue();
                return;
            }
            AudioHandler handler = (AudioHandler) Objects.requireNonNull(event.getGuild()).getAudioManager().getSendingHandler();
            int pos = Objects.requireNonNull(handler).addTrackToFront(new QueuedTrack(track, RequestMetadata.fromResultHandler(track, event, args)))+1;
            String addMsg = FormatUtil.filter(event.getClient().getSuccess()+" **"+track.getInfo().title
                    +"** (`"+ TimeUtil.formatTime(track.getDuration())+"`) "+(pos==0?"to begin playing":" (이)가 "+pos+"위치에 추가되었습니다"));
            m.editOriginal(addMsg).queue();
        }
        
        @Override
        public void trackLoaded(AudioTrack track)
        {
            loadSingle(track);
        }

        @Override
        public void playlistLoaded(AudioPlaylist playlist)
        {
            AudioTrack single;
            if(playlist.getTracks().size()==1 || playlist.isSearchResult())
                single = playlist.getSelectedTrack()==null ? playlist.getTracks().getFirst() : playlist.getSelectedTrack();
            else if (playlist.getSelectedTrack()!=null)
                single = playlist.getSelectedTrack();
            else
                single = playlist.getTracks().getFirst();
            loadSingle(single);
        }

        @Override
        public void noMatches()
        {
            if(ytsearch)
                m.editOriginal(FormatUtil.filter(event.getClient().getWarning()+" `"+args+"`에 대한 검색 결과가 없습니다.")).queue();
            else
                bot.getPlayerManager().loadItemOrdered(event.getGuild(), "ytsearch:"+args, new ResultHandler(m,event,true, args));
        }

        @Override
        public void loadFailed(FriendlyException throwable)
        {
            if(throwable.severity==FriendlyException.Severity.COMMON)
                m.editOriginal(event.getClient().getError()+" 로딩 오류: "+throwable.getMessage()).queue();
            else
                m.editOriginal(event.getClient().getError()+" 트랙을 불러오는 동안 오류가 발생했습니다.").queue();
        }
    }
}
