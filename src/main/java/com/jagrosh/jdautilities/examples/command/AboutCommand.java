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

import com.jagrosh.jdautilities.commons.JDAUtilitiesInfo;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.doc.standard.CommandInfo;
import com.jagrosh.jdautilities.examples.doc.Author;
import net.dv8tion.jda.api.entities.ApplicationInfo;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDAInfo;
import net.dv8tion.jda.api.Permission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;

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
        this.help = "\uBD07\uC5D0 \uB300\uD55C \uC815\uBCF4\uB97C \uD45C\uC2DC\uD569\uB2C8\uB2E4";
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
        builder.setAuthor("" + event.getSelfUser().getName() + "\uC758 \uBAA8\uB4E0 \uAC83!", null, event.getSelfUser().getAvatarUrl());
        boolean join = !(event.getClient().getServerInvite() == null || event.getClient().getServerInvite().isEmpty());
        boolean inv = !oauthLink.isEmpty();
        String invline = "\n" + (join ? "Join my server [`here`](" + event.getClient().getServerInvite() + ")" : (inv ? "\uB2F9\uC2E0\uC758 \uC11C\uBC84\uC5D0 \uC800\uB97C " : "")) 
                + (inv ? (join ? ", or " : "") + "[`\uCD08\uB300`](" + oauthLink + ") \uD574\uC8FC\uC138\uC694" : "") + "!";
        String author = event.getJDA().getUserById(event.getClient().getOwnerId())==null ? "<@" + event.getClient().getOwnerId()+">" 
                : event.getJDA().getUserById(event.getClient().getOwnerId()).getName();
        StringBuilder descr = new StringBuilder().append("\uC548\uB155\uD558\uC138\uC694! \uC800\uB294 **").append(event.getSelfUser().getName()).append("** \uC785\uB2C8\uB2E4, ")
                .append(description).append("\n\uC800\uB294 ").append(IS_AUTHOR ? "was written in Java" : "").append("**")
                .append(author).append("** \uC5D0 \uC758\uD574 \uB9CC\uB4E4\uC5B4\uC84C\uACE0 " + JDAUtilitiesInfo.AUTHOR + "\uC758 [Commands Extension](" + JDAUtilitiesInfo.GITHUB + ") (")
                .append(JDAUtilitiesInfo.VERSION).append(") \uACFC [JDA library](https://github.com/DV8FromTheWorld/JDA) (")
                .append(JDAInfo.VERSION).append(") \uB97C \uC0AC\uC6A9\uD558\uC600\uC5B4\uC694\n`").append(event.getClient().getTextualPrefix()).append(event.getClient().getHelpWord())
                .append("` \uB97C \uC785\uB825\uD558\uC5EC \uBA85\uB839\uC5B4\uB97C \uBCFC \uC218 \uC788\uC5B4\uC694!").append(join || inv ? invline : "").append("\n\n\uC774 \uBD07\uC758 \uD2B9\uC9D5: ```css");
        for (String feature : features)
            descr.append("\n").append(event.getClient().getSuccess().startsWith("<") ? REPLACEMENT_ICON : event.getClient().getSuccess()).append(" ").append(feature);
        descr.append(" ```");
        builder.setDescription(descr);
        if (event.getJDA().getShardInfo() == null)
        {
            builder.addField("\uD1B5\uACC4", event.getJDA().getGuilds().size() + " \uC11C\uBC84\n1 shard", true);
            builder.addField("\uC0AC\uC6A9\uC790", event.getJDA().getUsers().size() + " unique\n" + event.getJDA().getGuilds().stream().mapToInt(g -> g.getMembers().size()).sum() + " total", true);
            builder.addField("\uCC44\uB110", event.getJDA().getTextChannels().size() + " \uCC44\uD305\n" + event.getJDA().getVoiceChannels().size() + " \uC74C\uC131", true);
        }
        else
        {
            builder.addField("Stats", (event.getClient()).getTotalGuilds() + " \uC11C\uBC84\nShard " + (event.getJDA().getShardInfo().getShardId() + 1) 
                    + "/" + event.getJDA().getShardInfo().getShardTotal(), true);
            builder.addField("This shard", event.getJDA().getUsers().size() + " \uC0AC\uC6A9\uC790\n" + event.getJDA().getGuilds().size() + " \uC11C\uBC84", true);
            builder.addField("", event.getJDA().getTextChannels().size() + " \uCC44\uD305 \uCC44\uB110\n" + event.getJDA().getVoiceChannels().size() + " \uC74C\uC131 \uCC44\uB110", true);
        }
        builder.setFooter("\uCD5C\uADFC \uC7AC\uC2DC\uC791", null);
        builder.setTimestamp(event.getClient().getStartTime());
        event.reply(builder.build());
    }
    
}
