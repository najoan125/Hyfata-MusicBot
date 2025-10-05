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

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.audio.AudioHandler;
import com.jagrosh.jmusicbot.audio.QueuedTrack;
import com.jagrosh.jmusicbot.audio.RequestMetadata;
import com.jagrosh.jmusicbot.commands.MusicCommand;
import com.jagrosh.jmusicbot.playlist.PlaylistLoader;
import com.jagrosh.jmusicbot.utils.FormatUtil;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

/**
 *
 * @author John Grosh <john.a.grosh@gmail.com>
 */
public class PlaylistsCmd extends MusicCommand 
{
    public PlaylistsCmd(Bot bot)
    {
        super(bot);
        this.name = "플리";
        this.help = "플레이리스트";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.contexts = new InteractionContextType[]{InteractionContextType.GUILD};
        this.beListening = false;
        this.children = new SlashCommand[]{new PlaylistPlayCmd(bot), new PlayListListCmd(bot)};
    }
    
    @Override
    public void doCommand(SlashCommandEvent event)
    { }

    public static class PlayListListCmd extends MusicCommand {
        public PlayListListCmd(Bot bot) {
            super(bot);
            this.name = "목록";
            this.help = "사용 가능한 플레이리스트를 표시합니다";
            this.aliases = bot.getConfig().getAliases(this.name);
            this.contexts = new InteractionContextType[]{InteractionContextType.GUILD};
            this.beListening = false;
        }

        @Override
        public void doCommand(SlashCommandEvent event) {
            if(!bot.getPlaylistLoader().folderExists())
                bot.getPlaylistLoader().createFolder();
            if(!bot.getPlaylistLoader().folderExists())
            {
                event.reply(event.getClient().getWarning()+" 재생 목록 폴더가 없으므로 만들 수 없습니다!").setEphemeral(true).queue();
                return;
            }
            List<String> list = bot.getPlaylistLoader().getPlaylistNames();
            if(list==null)
                event.reply(event.getClient().getError()+" 사용 가능한 재생 목록을 로드하지 못했습니다!").setEphemeral(true).queue();
            else if(list.isEmpty())
                event.reply(event.getClient().getWarning()+" 재생 목록 폴더에 재생 목록이 없습니다!").setEphemeral(true).queue();
            else
            {
                StringBuilder builder = new StringBuilder(event.getClient().getSuccess()+" 사용 가능한 재생 목록:\n");
                list.forEach(str -> builder.append("| `").append(str).append("` "));
                builder.append("|\n`").append(event.getClient().getTextualPrefix()).append("재생 pl <name>` (을)를 입력하여 재생 목록을 재생하세요");
                event.reply(builder.toString()).queue();
            }
        }
    }

    public static class PlaylistPlayCmd extends MusicCommand
    {
        public PlaylistPlayCmd(Bot bot)
        {
            super(bot);
            this.name = "재생";
            this.aliases = new String[]{"pl"};
            this.arguments = "<name>";
            this.help = "제공된 재생 목록을 재생합니다";
            this.beListening = true;
            this.bePlaying = false;

            this.options = Collections.singletonList(
                    new OptionData(OptionType.STRING, "이름", "재생 목록의 이름. `/플리`로 확인")
            );
        }

        @Override
        public void doCommand(SlashCommandEvent event)
        {
            var option = event.getOption("이름");
            String args = option == null ? "" : option.getAsString();

            if(args.isEmpty())
            {
                event.reply(event.getClient().getError()+" 재생 목록 이름을 포함하십시오.").setEphemeral(true).queue();
                return;
            }
            PlaylistLoader.Playlist playlist = bot.getPlaylistLoader().getPlaylist(args);
            if(playlist==null)
            {
                event.reply(event.getClient().getError() + " `"+args+".txt` 를 플레이리스트 폴더에서 찾을 수 없습니다.").setEphemeral(true).queue();
                return;
            }
            event.getChannel().sendMessage(" 재생 목록 **"+args+"** 로딩중... ("+playlist.getItems().size()+" 항목)").queue(m ->
            {
                AudioHandler handler = (AudioHandler) Objects.requireNonNull(event.getGuild()).getAudioManager().getSendingHandler();
                playlist.loadTracks(bot.getPlayerManager(), (at)-> Objects.requireNonNull(handler).addTrack(new QueuedTrack(at, RequestMetadata.fromResultHandler(at, event, args))), () -> {
                    StringBuilder builder = new StringBuilder(playlist.getTracks().isEmpty()
                            ? event.getClient().getWarning()+" 트랙이 로드되지 않았습니다!"
                            : event.getClient().getSuccess()+" **"+playlist.getTracks().size()+"** 트랙 로드됨!");
                    if(!playlist.getErrors().isEmpty())
                        builder.append("\n이 트랙들을 로드하는 데 실패하였습니다:");
                    playlist.getErrors().forEach(err -> builder.append("\n`[").append(err.getIndex()+1).append("]` **").append(err.getItem()).append("**: ").append(err.getReason()));
                    String str = builder.toString();
                    if(str.length()>2000)
                        str = str.substring(0,1994)+" (...)";
                    m.editMessage(FormatUtil.filter(str)).queue();
                });
            });
        }
    }
}
