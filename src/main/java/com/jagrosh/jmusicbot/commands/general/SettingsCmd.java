/*
 * Copyright 2017 John Grosh <john.a.grosh@gmail.com>.
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
package com.jagrosh.jmusicbot.commands.general;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.settings.QueueType;
import com.jagrosh.jmusicbot.settings.RepeatMode;
import com.jagrosh.jmusicbot.settings.Settings;
import com.jagrosh.jmusicbot.utils.FormatUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;

import java.util.Objects;

/**
 *
 * @author John Grosh <john.a.grosh@gmail.com>
 */
public class SettingsCmd extends SlashCommand
{
    private final static String EMOJI = "🎧"; // 🎧

    public SettingsCmd(Bot bot)
    {
        this.name = "설정";
        this.help = "봇 설정을 표시합니다";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.contexts = new InteractionContextType[]{InteractionContextType.GUILD};
    }

    @Override
    protected void execute(SlashCommandEvent event)
    {
        Settings s = event.getClient().getSettingsFor(event.getGuild());
        MessageCreateBuilder builder = new MessageCreateBuilder()
                .addContent(EMOJI + " **")
                .addContent(FormatUtil.filter(event.getJDA().getSelfUser().getName()))
                .addContent("** 설정:");
        TextChannel tchan = s.getTextChannel(event.getGuild());
        VoiceChannel vchan = s.getVoiceChannel(event.getGuild());
        Role role = s.getRole(event.getGuild());
        EmbedBuilder ebuilder = new EmbedBuilder()
                .setColor(Objects.requireNonNull(event.getGuild()).getSelfMember().getColor())
                .setDescription("채팅 채널: " + (tchan == null ? "모든 채널" : "**#" + tchan.getName() + "**")
                        + "\n음성 채널: " + (vchan == null ? "모든 채널" : vchan.getAsMention())
                        + "\nDJ 역할: " + (role == null ? "없음" : "**" + role.getName() + "**")
                        + "\n맞춤 칭호: " + (s.getPrefix() == null ? "없음" : "`" + s.getPrefix() + "`")
                        + "\n반복 모드: " + (s.getRepeatMode() == RepeatMode.OFF
                        ? s.getRepeatMode().getUserFriendlyName()
                        : "**"+s.getRepeatMode().getUserFriendlyName()+"**")
                        + "\n대기열 유형: " + (s.getQueueType() == QueueType.FAIR
                        ? s.getQueueType().getUserFriendlyName()
                        : "**"+s.getQueueType().getUserFriendlyName()+"**")
                        + "\n기본 재생목록: " + (s.getDefaultPlaylist() == null ? "없음" : "**" + s.getDefaultPlaylist() + "**")
                        )
                .setFooter(event.getJDA().getGuilds().size() + " 서버 | "
                        + event.getJDA().getGuilds().stream().filter(g -> Objects.requireNonNull(g.getSelfMember().getVoiceState()).inAudioChannel()).count()
                        + " 오디오 연결", null);
        event.reply(builder.setEmbeds(ebuilder.build()).build()).queue();
    }
    
}
