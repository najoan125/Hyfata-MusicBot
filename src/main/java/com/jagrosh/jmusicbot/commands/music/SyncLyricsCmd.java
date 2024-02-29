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
import com.jagrosh.jmusicbot.audio.AudioHandler;
import com.jagrosh.jmusicbot.commands.MusicCommand;
import com.jagrosh.jmusicbot.utils.synclyric.LyricNotFoundException;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;

import java.io.IOException;
import java.util.Objects;

/**
 * @author John Grosh <john.a.grosh@gmail.com>
 */
public class SyncLyricsCmd extends MusicCommand {
    public SyncLyricsCmd(Bot bot) {
        super(bot);
        this.name = "싱크가사";
        this.help = "현재 재생 중인 곡의 가사를 실시간으로 표시합니다(유튜브 뮤직으로 재생한 곡만 지원)";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
    }

    @Override
    public void doCommand(CommandEvent event) {
        AudioHandler handler = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();
        Message m;
        try {
            m = Objects.requireNonNull(handler).getLyric(event.getJDA());
        } catch (LyricNotFoundException e) {
            event.reply(bot.getConfig().getWarning() + " 싱크 가사를 찾을 수 없습니다! 유튜브 뮤직으로 재생했는지 확인해주세요!");
            return;
        } catch (IOException e) {
            event.reply(bot.getConfig().getError() + " 싱크 가사를 불러오는 중 오류가 발생하였습니다! : " + e.getMessage());
            e.printStackTrace(System.out);
            return;
        }
        if (m == null) {
            event.reply(handler.getNoMusicPlaying(event.getJDA()));
            bot.getSyncLyricHandler().clearLastLyricMessage(event.getGuild());
        } else {
            event.reply(m, msg -> bot.getSyncLyricHandler().setLastLyricMessage(msg));
            bot.getNowplayingHandler().clearLastNPMessage(event.getGuild());
        }
    }
}