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

import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.audio.AudioHandler;
import com.jagrosh.jmusicbot.commands.MusicCommand;
import com.jagrosh.jmusicbot.utils.RnjsskaUtil;

import java.util.Objects;

/**
 * @author John Grosh <john.a.grosh@gmail.com>
 */
public class StopCmd extends MusicCommand {
    public StopCmd(Bot bot) {
        super(bot);
        this.name = "나가";
        this.help = "대기열을 지우고 통화방에서 나갑니다.";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.bePlaying = false;
    }

    @Override
    public void doCommand(SlashCommandEvent event) {
        AudioHandler handler = (AudioHandler) Objects.requireNonNull(event.getGuild()).getAudioManager().getSendingHandler();
        if (RnjsskaUtil.hasNoTrackPermission(handler, event.getMember())) {
            event.reply(event.getClient().getError() + "봇의 소유자가 권남 모드 활성화해서 못 멈춤 ㅋㅋ ㅅㄱ").queue();
            return;
        }
        Objects.requireNonNull(handler).stopAndClear();
        event.getGuild().getAudioManager().closeAudioConnection();
        event.reply(":wave: 음악을 정지하고 나갈게요").queue();
    }
}
