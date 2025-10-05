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

import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.jagrosh.jmusicbot.audio.RequestMetadata;
import com.jagrosh.jmusicbot.utils.TimeUtil;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.menu.ButtonMenu;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.audio.AudioHandler;
import com.jagrosh.jmusicbot.audio.QueuedTrack;
import com.jagrosh.jmusicbot.commands.MusicCommand;
import com.jagrosh.jmusicbot.utils.FormatUtil;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException.Severity;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.exceptions.PermissionException;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageEditAction;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author John Grosh <john.a.grosh@gmail.com>
 */
public class PlayCmd extends MusicCommand
{
    private final static String LOAD = "\uD83D\uDCE5"; // 📥
    private final static String CANCEL = "\uD83D\uDEAB"; // 🚫


    public PlayCmd(Bot bot)
    {
        super(bot);
        this.name = "재생";
        this.arguments = "<제목|URL|subcommand>";
        this.help = "제공된 노래를 재생합니다";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.beListening = true;
        this.bePlaying = false;

        List<OptionData> options = new java.util.ArrayList<>();
        options.add(new OptionData(OptionType.STRING, "제목_또는_url", "재생하기 위한 키워드 또는 URL"));
        options.add(new OptionData(OptionType.ATTACHMENT, "파일", "재생할 음악 파일 업로드"));
        this.options = options;
    }

    @Override
    public void doCommand(SlashCommandEvent event)
    {
        var optionTitle = event.getOption("제목_또는_url");
        String args = optionTitle == null ? "" : optionTitle.getAsString();

        var optionFile = event.getOption("파일");
        Message.Attachment attachment = optionFile == null ? null : optionFile.getAsAttachment();

        if(args.isEmpty() && attachment == null)
        {
            AudioHandler handler = (AudioHandler) Objects.requireNonNull(event.getGuild()).getAudioManager().getSendingHandler();
            if(Objects.requireNonNull(handler).getPlayer().getPlayingTrack()!=null && handler.getPlayer().isPaused())
            {
                handler.getPlayer().setPaused(false);
                event.reply(event.getClient().getSuccess() + " **"+handler.getPlayer().getPlayingTrack().getInfo().title+"** (이)가 다시 재생됨.").queue();
                return;
            }
            StringBuilder builder = new StringBuilder(event.getClient().getWarning()+" 재생 명령어:\n");
            builder.append("\n`").append(event.getClient().getPrefix()).append(name).append(" <노래 제목>` - 유튜브에서 첫번째 결과를 재생합니다");
            builder.append("\n`").append(event.getClient().getPrefix()).append(name).append(" <URL>` - 제공된 노래, 재생 목록 또는 실시간 영상을 재생합니다.");
            for(Command cmd: children)
                builder.append("\n`").append(event.getClient().getPrefix()).append(name).append(" ").append(cmd.getName()).append(" ").append(cmd.getArguments()).append("` - ").append(cmd.getHelp());
            event.reply(builder.toString()).setEphemeral(true).queue();
            return;
        }
        String resultArgs = args.startsWith("<") && args.endsWith(">")
                ? args.substring(1,args.length()-1)
                : args.isEmpty() ? attachment.getUrl() : args;
        event.deferReply().queue(m -> bot.getPlayerManager().loadItemOrdered(event.getGuild(), resultArgs, new ResultHandler(m,event,false, resultArgs)));
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
        
        private void loadSingle(AudioTrack track, AudioPlaylist playlist)
        {
            if(bot.getConfig().isTooLong(track))
            {
            	m.editOriginal(FormatUtil.filter(event.getClient().getWarning()+" 이 트랙 (**"+track.getInfo().title+ "**) (은)는 허용된 최대치보다 깁니다: `"
                        + TimeUtil.formatTime(track.getDuration())+"` > `"+ TimeUtil.formatTime(bot.getConfig().getMaxSeconds()*1000)+"`")).queue();
                return;
            }
            AudioHandler handler = (AudioHandler) Objects.requireNonNull(event.getGuild()).getAudioManager().getSendingHandler();
            int pos = Objects.requireNonNull(handler).addTrack(new QueuedTrack(track, RequestMetadata.fromResultHandler(track, event, args)))+1;
            String addMsg = FormatUtil.filter(event.getClient().getSuccess()+
                    (pos==0?" 요청한 항목을 바로 재생합니다":" 요청한 항목이 **대기열 위치 "+pos+"** 에 추가되었습니다"));
            if(playlist==null || !event.getGuild().getSelfMember().hasPermission(event.getTextChannel(), Permission.MESSAGE_ADD_REACTION)) {
                WebhookMessageEditAction<Message> ma = m.editOriginal(addMsg);
                EmbedBuilder eb = new EmbedBuilder();
                eb.setTitle(track.getInfo().title, track.getInfo().uri);
                eb.setDescription("요청한 항목"+(pos==0?"을 바로 재생합니다!":"이 대기열에 추가되었습니다!")+"\n(`"+ TimeUtil.formatTime(track.getDuration())+"`)");
                ma.setEmbeds(eb.build()).queue();
            }
            else
            {
                new ButtonMenu.Builder()
                        .setText(addMsg+"\n"+event.getClient().getWarning()+" 이 곡에는 **"+playlist.getTracks().size()+"곡**이 포함된 재생목록이 있습니다.  "+LOAD+" 버튼을 눌러 재생목록을 불러오세요.")
                        .setChoices(LOAD, CANCEL)
                        .setEventWaiter(bot.getWaiter())
                        .setTimeout(30, TimeUnit.SECONDS)
                        .setAction(re ->
                        {
                            if(re.getName().equals(LOAD))
                                m.editOriginal(addMsg+"\n"+event.getClient().getSuccess()+" **"+loadPlaylist(playlist, track)+"곡을** 더 불러왔습니다!").queue();
                            else
                                m.editOriginal(addMsg).queue();
                        }).setFinalAction(m ->
                        {
                            try{ m.clearReactions().queue(); }catch(PermissionException ignore) {}
                        }).build().display(event.getChannel());
            }
        }
        
        private int loadPlaylist(AudioPlaylist playlist, AudioTrack exclude)
        {
            int[] count = {0};
            playlist.getTracks().forEach((track) -> {
                if(!bot.getConfig().isTooLong(track) && !track.equals(exclude))
                {
                    AudioHandler handler = (AudioHandler) Objects.requireNonNull(event.getGuild()).getAudioManager().getSendingHandler();
                    Objects.requireNonNull(handler).addTrack(new QueuedTrack(track, RequestMetadata.fromResultHandler(track, event, args)));
                    count[0]++;
                }
            });
            return count[0];
        }
        
        @Override
        public void trackLoaded(AudioTrack track)
        {
            loadSingle(track, null);
        }

        @Override
        public void playlistLoaded(AudioPlaylist playlist)
        {
            if(playlist.getTracks().size()==1 || playlist.isSearchResult())
            {
                AudioTrack single = playlist.getSelectedTrack()==null ? playlist.getTracks().getFirst() : playlist.getSelectedTrack();
                loadSingle(single, null);
            }
            else if (playlist.getSelectedTrack()!=null)
            {
                AudioTrack single = playlist.getSelectedTrack();
                loadSingle(single, playlist);
            }
            else
            {
                int count = loadPlaylist(playlist, null);
                if(playlist.getTracks().isEmpty())
                {
                    m.editOriginal(FormatUtil.filter(event.getClient().getWarning() + " 해당 재생목록" + (playlist.getName() == null ? "" : "(**" + playlist.getName()
                            + "**)") + "을(를) 불러올 수 없거나 비어있습니다.")).queue();
                }
                else if(count==0)
                {
                    m.editOriginal(FormatUtil.filter(event.getClient().getWarning() + " 해당 재생목록" + (playlist.getName() == null ? "" : "(**" + playlist.getName()
                            + "**)") + "에 포함된 모든 곡이 최대 재생 시간(`" + bot.getConfig().getMaxTime() + "`)을 초과하여 추가되지 않았습니다.")).queue();
                }
                else
                {
                    m.editOriginal(FormatUtil.filter(event.getClient().getSuccess() + " `" + playlist.getTracks().size() + "`곡이 담긴 "
                            + (playlist.getName() == null ? "재생목록을 발견하여" : "**" + playlist.getName() + "** 재생목록을 발견하여") + " 대기열에 추가했습니다!"
                            + (count < playlist.getTracks().size() ? "\n" + event.getClient().getWarning() + " 최대 재생 시간(`"
                            + bot.getConfig().getMaxTime() + "`)을 초과하는 곡은 제외되었습니다." : ""))).queue();
                }
            }
        }

        @Override
        public void noMatches()
        {
            if(ytsearch)
                m.editOriginal(FormatUtil.filter(event.getClient().getWarning()+" `"+args+"`에 대한 검색 결과 없음.")).queue();
            else
                bot.getPlayerManager().loadItemOrdered(event.getGuild(), "ytsearch:"+args, new ResultHandler(m,event,true, args));
        }

        @Override
        public void loadFailed(FriendlyException throwable)
        {
            if(throwable.severity==Severity.COMMON)
                m.editOriginal(event.getClient().getError()+" 로드 오류: "+throwable.getMessage()).queue();
            else
                m.editOriginal(event.getClient().getError()+" 트랙을 불러오는 도중 오류 발생").queue();
        }
    }
}