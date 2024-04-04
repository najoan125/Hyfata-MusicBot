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
import com.jagrosh.jmusicbot.utils.FormatUtil;
import com.jagrosh.jmusicbot.utils.RnjsskaUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;

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
        if (RnjsskaUtil.hasNoTrackPermission(handler, event.getMember())) {
            event.reply(event.getClient().getError() + "봇의 소유자가 권남 모드 활성화해서 스킵 못하쥬? 킹받쥬? ㅋㅋ ㅅㄱ");
            return;
        }
        RequestMetadata rm = handler.getRequestMetadata();
        double skipRatio = bot.getSettingsManager().getSettings(event.getGuild()).getSkipRatio();
        if(skipRatio == -1) {
            skipRatio = bot.getConfig().getSkipRatio();
        }
        if(event.getAuthor().getIdLong() == rm.getOwner() || skipRatio == 0)
        {
            event.reply(new MessageBuilder()
                    .setContent(bot.getConfig().getSuccess()+" 현재 재생 중인 항목을 건너뛰었습니다!")
                    .setEmbeds(new EmbedBuilder()
                            .setTitle(handler.getPlayer().getPlayingTrack().getInfo().title, handler.getPlayer().getPlayingTrack().getInfo().uri)
                            .setDescription("이 항목을 건너뛰었습니다!")
                            .build())
                    .build()
            );
            handler.getPlayer().stopTrack();
        }
        else
        {
            int listeners = (int)event.getSelfMember().getVoiceState().getChannel().getMembers().stream()
                    .filter(m -> !m.getUser().isBot() && !m.getVoiceState().isDeafened()).count();
            String msg;
            if(handler.getVotes().contains(event.getAuthor().getId()))
                msg = event.getClient().getWarning()+" 이미 현재 재생 중인 항목을 건너뛰기로 투표했습니다 `[";
            else
            {
                msg = event.getClient().getSuccess()+" 현재 재생 중인 항목을 건너뛰기로 투표했습니다 `[";
                handler.getVotes().add(event.getAuthor().getId());
            }
            int skippers = (int)event.getSelfMember().getVoiceState().getChannel().getMembers().stream()
                    .filter(m -> handler.getVotes().contains(m.getUser().getId())).count();
            int required = (int)Math.ceil(listeners * skipRatio);
            msg += skippers + "표, " + required + "/" + listeners + " 필요]`";
            String embed;
            if(skippers>=required)
            {
                msg += "\n" + bot.getConfig().getSuccess() + " 이 항목을 건너뛰었습니다!";
                embed = (rm.getOwner() == 0L ? "(자동 재생)" : "(**" + FormatUtil.formatUsername(rm.user) + "**에 의해 요청됨)");

                event.reply(new MessageBuilder()
                        .setContent(msg)
                        .setEmbeds(new EmbedBuilder()
                                .setTitle(handler.getPlayer().getPlayingTrack().getInfo().title, handler.getPlayer().getPlayingTrack().getInfo().uri)
                                .setDescription(embed)
                                .build()
                        ).build()
                );
                handler.getPlayer().stopTrack();
            }
            else
                event.reply(msg);
        }
    }
    
}
