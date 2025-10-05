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
package com.jagrosh.jmusicbot.commands.dj;

import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.audio.AudioHandler;
import com.jagrosh.jmusicbot.audio.QueuedTrack;
import com.jagrosh.jmusicbot.commands.DJCommand;
import com.jagrosh.jmusicbot.settings.Settings;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.Collections;
import java.util.Objects;

/**
 *
 * @author John Grosh <john.a.grosh@gmail.com>
 */
public class RemoveCmd extends DJCommand
{
    public RemoveCmd(Bot bot)
    {
        super(bot);
        this.name = "제거";
        this.help = "노래를 대기열에서 제거합니다";
        this.arguments = "<트랙 위치|ALL>";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.beListening = true;
        this.bePlaying = true;

        this.options = Collections.singletonList(
                new OptionData(OptionType.STRING, "트랙_위치", "`/대기열`에서 제거할 트랙 위치. `all`을 입력하면 모두 제거")
        );
    }

    @Override
    public void doCommand(SlashCommandEvent event)
    {
        var option = event.getOption("트랙_위치");
        String args = option == null ? "" : option.getAsString();
        AudioHandler handler = (AudioHandler) Objects.requireNonNull(event.getGuild()).getAudioManager().getSendingHandler();
        if(Objects.requireNonNull(handler).getQueue().isEmpty())
        {
            event.reply(event.getClient().getError() + " 대기열이 비어있습니다!").setEphemeral(true).queue();
            return;
        }
        if(args.equalsIgnoreCase("all"))
        {
            int count = handler.getQueue().removeAll(event.getUser().getIdLong());
            if(count==0)
                event.reply(event.getClient().getWarning() + " 대기열에 어떤 노래도 없습니다!").setEphemeral(true).queue();
            else
                event.reply(event.getClient().getSuccess() + " 성공적으로 "+count+" 항목을 제거하였습니다.").queue();
            return;
        }
        int pos;
        try {
            pos = Integer.parseInt(args);
        } catch(NumberFormatException e) {
            pos = 0;
        }
        if(pos<1 || pos>handler.getQueue().size())
        {
            event.reply(event.getClient().getError() + " 위치는 1과 "+handler.getQueue().size()+" 사이의 유효한 정수여야 합니다!").setEphemeral(true).queue();
            return;
        }
        Settings settings = event.getClient().getSettingsFor(event.getGuild());
        boolean isDJ = Objects.requireNonNull(event.getMember()).hasPermission(Permission.MANAGE_SERVER);
        if(!isDJ)
            isDJ = event.getMember().getRoles().contains(settings.getRole(event.getGuild()));
        QueuedTrack qt = handler.getQueue().get(pos-1);
        if(qt.getIdentifier()==event.getUser().getIdLong())
        {
            handler.getQueue().remove(pos-1);
            event.reply(event.getClient().getSuccess() + " **"+qt.getTrack().getInfo().title+"** (이)가 대기열에서 제거됨").queue();
        }
        else if(isDJ)
        {
            handler.getQueue().remove(pos-1);
            User u;
            try {
                u = event.getJDA().getUserById(qt.getIdentifier());
            } catch(Exception e) {
                u = null;
            }
            event.reply(event.getClient().getSuccess() + " **"+qt.getTrack().getInfo().title
                    +"** (이)가 대기열에서 제거됨 ("+(u==null ? "someone" : "**"+u.getName()+"** 에 의해 요청됨")+")").queue();
        }
        else
        {
            event.reply(event.getClient().getError() + " **"+qt.getTrack().getInfo().title+"** (을)를 추가하지 않았으므로 제거할 수 없습니다!").setEphemeral(true).queue();
        }
    }
}
