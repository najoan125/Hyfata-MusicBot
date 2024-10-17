/*
 * Copyright 2020 John Grosh <john.a.grosh@gmail.com>.
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
import com.jagrosh.jmusicbot.commands.DJCommand;
import com.jagrosh.jmusicbot.commands.MusicCommand;
import com.jagrosh.jmusicbot.utils.TimeUtil;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;


/**
 * @author Whew., Inc.
 */
public class SeekCmd extends MusicCommand
{
    private final static Logger LOG = LoggerFactory.getLogger("Seeking");

    public SeekCmd(Bot bot)
    {
        super(bot);
        this.name = "탐색";
        this.help = "현재 곡의 재생 위치를 이동합니다";
        this.arguments = "[+ | -] <HH:MM:SS | MM:SS | SS>|<0h0m0s | 0m0s | 0s>";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.beListening = true;
        this.bePlaying = true;
    }

    @Override
    public void doCommand(CommandEvent event)
    {
        AudioHandler handler = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();
        AudioTrack playingTrack = Objects.requireNonNull(handler).getPlayer().getPlayingTrack();
        if (!playingTrack.isSeekable())
        {
            event.replyError("이 트랙은 탐색할 수 없습니다.");
            return;
        }

        if (!DJCommand.checkDJPermission(event) && playingTrack.getUserData(RequestMetadata.class).getOwner() != event.getAuthor().getIdLong())
        {
            event.replyError("**" + playingTrack.getInfo().title + "**은(는) 직접 추가한 곡이 아니기 때문에 탐색할 수 없습니다!");
            return;
        }

        String args = event.getArgs();
        TimeUtil.SeekTime seekTime = TimeUtil.parseTime(args);
        if (seekTime == null)
        {
            event.replyError("잘못된 탐색입니다! 예상 형식: " + arguments + "\n예: `1:02:23` `+1:10` `-90`, `1h10m`, `+90s`");
            return;
        }

        long currentPosition = playingTrack.getPosition();
        long trackDuration = playingTrack.getDuration();

        long seekMilliseconds = seekTime.relative ? currentPosition + seekTime.milliseconds : seekTime.milliseconds;
        if (seekMilliseconds > trackDuration)
        {
            event.replyError("현재 트랙의 길이가 `" + TimeUtil.formatTime(trackDuration) + "` 이기 때문에 `" + TimeUtil.formatTime(seekMilliseconds) + "` 위치로 이동할 수 없습니다!");
            return;
        }

        try {
            playingTrack.setPosition(seekMilliseconds);
        }
        catch (Exception e)
        {
            event.replyError("탐색을 시도하는 도중 오류가 발생하였습니다: " + e.getMessage());
            LOG.warn("Failed to seek track " + playingTrack.getIdentifier(), e);
            return;
        }
        event.replySuccess("성공적으로 `" + TimeUtil.formatTime(playingTrack.getPosition()) + "/" + TimeUtil.formatTime(playingTrack.getDuration()) + "`으(로) 탐색했습니다!");
    }
}