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

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.JMusicBot;
import com.jagrosh.jmusicbot.audio.AudioHandler;
import com.jagrosh.jmusicbot.commands.MusicCommand;
import com.jagrosh.jmusicbot.utils.synclyric.LyricNotFoundException;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.utils.messages.MessageEditData;

import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * @author John Grosh <john.a.grosh@gmail.com>
 */
public class SyncLyricsCmd extends MusicCommand {
    public static final String LYRIC_NOT_FOUND = " 싱크 가사를 찾을 수 없습니다! (Spotify 또는 Apple Music 으로 재생하면 싱크 가사가 지원될 가능성이 높습니다)";
    public static final String LYRIC_ERROR = " 싱크 가사를 불러오는 중 오류가 발생하였습니다 : ";
    public SyncLyricsCmd(Bot bot) {
        super(bot);
        this.name = "싱크가사";
        this.help = "현재 재생 중인 곡의 가사를 실시간으로 표시합니다 (Youtube Music, Spotify, Apple Music으로 재생한 곡만 지원)";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
    }

    @Override
    public void doCommand(CommandEvent event) {
        AudioHandler handler = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();
        event.reply(bot.getConfig().getLoading() + " 불러오는 중...", msg -> {
            long ping = event.getMessage().getTimeCreated().until(msg.getTimeCreated(), ChronoUnit.MILLIS);
            MessageEditData lyricMsg;
            try {
                lyricMsg = Objects.requireNonNull(handler).getLyric(event.getJDA(), ping);
            } catch (LyricNotFoundException e) {
                msg.editMessage(bot.getConfig().getWarning() + LYRIC_NOT_FOUND).queue();
                bot.getSyncLyricHandler().clearLastLyricMessage(event.getGuild());
                return;
            } catch (Exception e) {
                msg.editMessage(bot.getConfig().getError() + LYRIC_ERROR + e.getMessage()).queue();
                JMusicBot.LOG.error("isrc: {}", Objects.requireNonNull(handler).getPlayer().getPlayingTrack().getInfo().isrc);
                e.printStackTrace(System.out);
                bot.getSyncLyricHandler().clearLastLyricMessage(event.getGuild());
                return;
            }

            if (lyricMsg == null) {
                msg.editMessage(Objects.requireNonNull(handler).getNoMusicPlaying(event.getJDA())).queue();
                bot.getSyncLyricHandler().clearLastLyricMessage(event.getGuild());
            } else {
                msg.editMessage(lyricMsg).queue();
                bot.getSyncLyricHandler().setLastLyricMessage(msg);
                bot.getNowplayingHandler().clearLastNPMessage(event.getGuild());
            }
        });
    }
}
