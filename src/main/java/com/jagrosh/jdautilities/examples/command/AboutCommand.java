/*
 * Copyright 2016-2018 John Grosh (jagrosh) & Kaidan Gustave (TheMonitorLizard)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jagrosh.jdautilities.examples.command;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.JDAUtilitiesInfo;
import com.jagrosh.jdautilities.doc.standard.CommandInfo;
import com.jagrosh.jdautilities.examples.doc.Author;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDAInfo;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ApplicationInfo;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;

/**
 *
 * @author John Grosh (jagrosh)
 */
@CommandInfo(
    name = "About",
    description = "Gets information about the bot."
)
@Author("John Grosh (jagrosh)")
public class AboutCommand extends Command {
    private boolean IS_AUTHOR = true;
    private String REPLACEMENT_ICON = "+";
    private final Color color;
    private final String description;
    private final Permission[] perms;
    private String oauthLink;
    private final String[] features;
    
    public AboutCommand(Color color, String description, String[] features, Permission... perms)
    {
        this.color = color;
        this.description = description;
        this.features = features;
        this.name = "about";
        this.help = "봇에 대한 정보를 표시합니다";
        this.guildOnly = false;
        this.perms = perms;
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
    }
    
    public void setIsAuthor(boolean value)
    {
        this.IS_AUTHOR = value;
    }
    
    public void setReplacementCharacter(String value)
    {
        this.REPLACEMENT_ICON = value;
    }
    
    @Override
    protected void execute(CommandEvent event) {
        if (oauthLink == null) {
            try {
                ApplicationInfo info = event.getJDA().retrieveApplicationInfo().complete();
                oauthLink = info.isBotPublic() ? info.getInviteUrl(0L, perms) : "";
            } catch (Exception e) {
                Logger log = LoggerFactory.getLogger("OAuth2");
                log.error("Could not generate invite link ", e);
                oauthLink = "";
            }
        }
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(event.isFromType(ChannelType.TEXT) ? event.getGuild().getSelfMember().getColor() : color);
        builder.setAuthor("" + event.getSelfUser().getName() + "의 모든 것!", null, event.getSelfUser().getAvatarUrl());
        boolean join = !(event.getClient().getServerInvite() == null || event.getClient().getServerInvite().isEmpty());
        boolean inv = !oauthLink.isEmpty();
        String invline = "\n" + (join ? "Join my server [`here`](" + event.getClient().getServerInvite() + ")" : (inv ? "당신의 서버에 저를 " : ""))
                + (inv ? (join ? ", or " : "") + "[`초대`](" + oauthLink + ") 해주세요" : "") + "!";
        String author = event.getJDA().getUserById(event.getClient().getOwnerId())==null ? "<@" + event.getClient().getOwnerId()+">" 
                : event.getJDA().getUserById(event.getClient().getOwnerId()).getName();
        StringBuilder descr = new StringBuilder().append("안녕하세요! 저는 **").append(event.getSelfUser().getName()).append("** 입니다, ")
                .append(description).append("\n저는 ").append(IS_AUTHOR ? "was written in Java" : "").append("**")
                .append(author).append("** 에 의해 만들어졌고 " + JDAUtilitiesInfo.AUTHOR + "의 [Commands Extension](" + JDAUtilitiesInfo.GITHUB + ") (")
                .append(JDAUtilitiesInfo.VERSION).append(") 과 [JDA library](https://github.com/DV8FromTheWorld/JDA) (")
                .append(JDAInfo.VERSION).append(") 를 사용하였어요\n`").append(event.getClient().getTextualPrefix()).append(event.getClient().getHelpWord())
                .append("` 를 입력하여 명령어를 볼 수 있어요!").append(join || inv ? invline : "").append("\n\n이 봇의 특징: ```css");
        for (String feature : features)
            descr.append("\n").append(event.getClient().getSuccess().startsWith("<") ? REPLACEMENT_ICON : event.getClient().getSuccess()).append(" ").append(feature);
        descr.append(" ```");
        builder.setDescription(descr);
        if (event.getJDA().getShardInfo() == JDA.ShardInfo.SINGLE)
        {
            builder.addField("통계", event.getJDA().getGuilds().size() + " 서버\n1 shard", true);
            builder.addField("사용자", event.getJDA().getUsers().size() + " unique\n" + event.getJDA().getGuilds().stream().mapToInt(g -> g.getMembers().size()).sum() + " total", true);
            builder.addField("채널", event.getJDA().getTextChannels().size() + " 채팅\n" + event.getJDA().getVoiceChannels().size() + " 음성", true);
        }
        else
        {
            builder.addField("Stats", (event.getClient()).getTotalGuilds() + " 서버\nShard " + (event.getJDA().getShardInfo().getShardId() + 1)
                    + "/" + event.getJDA().getShardInfo().getShardTotal(), true);
            builder.addField("This shard", event.getJDA().getUsers().size() + " 사용자\n" + event.getJDA().getGuilds().size() + " 서버", true);
            builder.addField("", event.getJDA().getTextChannels().size() + " 채팅 채널\n" + event.getJDA().getVoiceChannels().size() + " 음성 채널", true);
        }
        builder.setFooter("최근 재시작", null);
        builder.setTimestamp(event.getClient().getStartTime());
        event.reply(builder.build());
    }
    
}
