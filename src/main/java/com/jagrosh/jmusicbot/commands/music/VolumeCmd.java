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
import com.jagrosh.jmusicbot.JMusicBot;
import com.jagrosh.jmusicbot.audio.AudioHandler;
import com.jagrosh.jmusicbot.audio.RequestMetadata;
import com.jagrosh.jmusicbot.commands.MusicCommand;
import com.jagrosh.jmusicbot.settings.Settings;
import com.jagrosh.jmusicbot.utils.FormatUtil;

/**
 *
 * @author John Grosh <john.a.grosh@gmail.com>
 */
public class VolumeCmd extends MusicCommand
{
    public VolumeCmd(Bot bot)
    {
        super(bot);
        this.name = "\uC74C\uB7C9";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.help = "\uBCFC\uB968\uC744 \uC124\uC815\uD558\uAC70\uB098 \uD45C\uC2DC\uD569\uB2C8\uB2E4";
        this.arguments = "[0-150]";
    }

    @Override
    public void doCommand(CommandEvent event)
    {
        AudioHandler handler = (AudioHandler)event.getGuild().getAudioManager().getSendingHandler();
        if (JMusicBot.rnjsska &&
                handler != null &&
                handler.getPlayer().getPlayingTrack() != null &&
                handler.getPlayer().getPlayingTrack().getUserData(RequestMetadata.class).user.id == bot.getConfig().getOwnerId() &&
                event.getMember().getIdLong() != bot.getConfig().getOwnerId()) {
            event.reply(event.getClient().getError() + "봇의 소유자가 권남 모드 활성화해서 볼륨 조정 못하쥬? ㅋㅋ 어쩔거임? ㅄ ㅋㅋ");
            return;
        }
        Settings settings = event.getClient().getSettingsFor(event.getGuild());
        int volume = handler.getPlayer().getVolume();
        if(event.getArgs().isEmpty())
        {
            event.reply(FormatUtil.volumeIcon(volume)+" \uD604\uC7AC \uC74C\uB7C9\uC740 `"+volume+"` \uC785\uB2C8\uB2E4");
        }
        else
        {
            int nvolume;
            try{
                nvolume = Integer.parseInt(event.getArgs());
            }catch(NumberFormatException e){
                nvolume = -1;
            }
            if(nvolume<0 || nvolume>150)
                event.reply(event.getClient().getError()+" \uC74C\uB7C9\uC740 0\uACFC 150 \uC0AC\uC774\uC758 \uC720\uD6A8\uD55C \uC815\uC218\uC5EC\uC57C \uD569\uB2C8\uB2E4!");
            else
            {
                handler.getPlayer().setVolume(nvolume);
                settings.setVolume(nvolume);
                event.reply(FormatUtil.volumeIcon(nvolume)+" \uC74C\uB7C9\uC774 `"+volume+"` \uC5D0\uC11C `"+nvolume+"` (\uC73C)\uB85C \uBCC0\uACBD\uB418\uC5C8\uC2B5\uB2C8\uB2E4");
            }
        }
    }
    
}
