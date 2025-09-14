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

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.settings.QueueType;
import com.jagrosh.jmusicbot.settings.RepeatMode;
import com.jagrosh.jmusicbot.settings.Settings;
import com.jagrosh.jmusicbot.utils.FormatUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;

/**
 *
 * @author John Grosh <john.a.grosh@gmail.com>
 */
public class SettingsCmd extends Command 
{
    private final static String EMOJI = "\uD83C\uDFA7"; // 🎧
    
    public SettingsCmd(Bot bot)
    {
        this.name = "settings";
        this.help = "\uBD07 \uC124\uC815\uC744 \uD45C\uC2DC\uD569\uB2C8\uB2E4";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.guildOnly = true;
    }
    
    @Override
    protected void execute(CommandEvent event) 
    {
        Settings s = event.getClient().getSettingsFor(event.getGuild());
        MessageCreateBuilder builder = new MessageCreateBuilder()
                .addContent(EMOJI + " **")
                .addContent(FormatUtil.filter(event.getSelfUser().getName()))
                .addContent("** \uC124\uC815:");
        TextChannel tchan = s.getTextChannel(event.getGuild());
        VoiceChannel vchan = s.getVoiceChannel(event.getGuild());
        Role role = s.getRole(event.getGuild());
        EmbedBuilder ebuilder = new EmbedBuilder()
                .setColor(event.getSelfMember().getColor())
                .setDescription("\uCC44\uD305 \uCC44\uB110: " + (tchan == null ? "Any" : "**#" + tchan.getName() + "**")
                        + "\n\uC74C\uC131 \uCC44\uB110: " + (vchan == null ? "Any" : vchan.getAsMention())
                        + "\nDJ \uC5ED\uD560: " + (role == null ? "\uC5C6\uC74C" : "**" + role.getName() + "**")
                        + "\n\uB9DE\uCDA4 \uCE6D\uD638: " + (s.getPrefix() == null ? "\uC5C6\uC74C" : "`" + s.getPrefix() + "`")
                        + "\nRepeat Mode: " + (s.getRepeatMode() == RepeatMode.OFF
                        ? s.getRepeatMode().getUserFriendlyName()
                        : "**"+s.getRepeatMode().getUserFriendlyName()+"**")
                        + "\nQueue Type: " + (s.getQueueType() == QueueType.FAIR
                        ? s.getQueueType().getUserFriendlyName()
                        : "**"+s.getQueueType().getUserFriendlyName()+"**")
                        + "\n\uAE30\uBCF8 \uC7AC\uC0DD\uBAA9\uB85D: " + (s.getDefaultPlaylist() == null ? "\uC5C6\uC74C" : "**" + s.getDefaultPlaylist() + "**")
                        )
                .setFooter(event.getJDA().getGuilds().size() + " \uC11C\uBC84 | "
                        + event.getJDA().getGuilds().stream().filter(g -> g.getSelfMember().getVoiceState().inAudioChannel()).count()
                        + " \uC624\uB514\uC624 \uC5F0\uACB0", null);
        event.getChannel().sendMessage(builder.setEmbeds(ebuilder.build()).build()).queue();
    }
    
}
