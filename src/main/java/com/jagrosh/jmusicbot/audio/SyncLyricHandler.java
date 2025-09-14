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
package com.jagrosh.jmusicbot.audio;

import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.JMusicBot;
import com.jagrosh.jmusicbot.commands.music.SyncLyricsCmd;
import com.jagrosh.jmusicbot.entities.Pair;
import com.jagrosh.jmusicbot.utils.synclyric.LyricNotFoundException;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditData;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author John Grosh (john.a.grosh@gmail.com)
 */
public class SyncLyricHandler {
    private final Bot bot;
    private final HashMap<Long, Pair<Long, Long>> lastLyric; // guild -> channel,message

    public SyncLyricHandler(Bot bot) {
        this.bot = bot;
        this.lastLyric = new HashMap<>();
    }

    public void init() {
        if (!bot.getConfig().useNPImages())
            bot.getThreadpool().scheduleWithFixedDelay(this::updateAll, 0, 10, TimeUnit.MILLISECONDS);
    }

    public void setLastLyricMessage(Message m) {
        lastLyric.put(m.getGuild().getIdLong(), new Pair<>(m.getChannel().asTextChannel().getIdLong(), m.getIdLong()));
    }

    public void clearLastLyricMessage(Guild guild) {
        lastLyric.remove(guild.getIdLong());
    }

    private void updateAll() {
        Set<Long> toRemove = new HashSet<>();
        for (long guildId : lastLyric.keySet()) {
            Guild guild = bot.getJDA().getGuildById(guildId);
            if (guild == null) {
                toRemove.add(guildId);
                continue;
            }
            Pair<Long, Long> pair = lastLyric.get(guildId);
            TextChannel tc = guild.getTextChannelById(pair.getKey());
            if (tc == null) {
                toRemove.add(guildId);
                continue;
            }
            AudioHandler handler = (AudioHandler) guild.getAudioManager().getSendingHandler();

            // set msg
            MessageEditData msg;
            boolean error = false;
            try {
                if (!Objects.requireNonNull(handler).isMusicPlaying(bot.getJDA())) {
                    msg = handler.getNoMusicPlaying(bot.getJDA());
                    toRemove.add(guildId);
                } else {
                    msg = handler.getSyncLyric(bot.getJDA());
                }
            } catch (LyricNotFoundException e) {
                msg = new MessageEditBuilder().setContent(bot.getConfig().getWarning() + SyncLyricsCmd.LYRIC_NOT_FOUND).build();
                toRemove.add(guildId);
                error = true;
            } catch (Exception e) {
                msg = new MessageEditBuilder().setContent(bot.getConfig().getError() + SyncLyricsCmd.LYRIC_ERROR + e.getMessage()).build();
                JMusicBot.LOG.error("isrc: {}", Objects.requireNonNull(handler).getPlayer().getPlayingTrack().getInfo().isrc);
                e.printStackTrace(System.out);
                toRemove.add(guildId);
                error = true;
            }

            // edit message
            try {
                if (msg != null && !error) {
                    tc.editMessageById(pair.getValue(), msg).queue(m -> {
                    }, t -> lastLyric.remove(guildId));
                } else if (msg != null) {
                    tc.editMessageById(pair.getValue(), msg).setEmbeds().queue(m -> {
                    }, t -> lastLyric.remove(guildId));
                }
            } catch (Exception e) {
                toRemove.add(guildId);
            }
        }
        toRemove.forEach(lastLyric::remove);
    }

    public void onMessageDelete(Guild guild, long messageId) {
        Pair<Long, Long> pair = lastLyric.get(guild.getIdLong());
        if (pair == null)
            return;
        if (pair.getValue() == messageId)
            lastLyric.remove(guild.getIdLong());
    }
}
