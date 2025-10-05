/*
 * Copyright 2019 John Grosh <john.a.grosh@gmail.com>.
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
import com.jagrosh.jmusicbot.commands.DJCommand;
import com.jagrosh.jmusicbot.utils.FormatUtil;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.Collections;
import java.util.Objects;

/**
 *
 * @author Michaili K.
 */
public class ForceRemoveCmd extends DJCommand
{
    public ForceRemoveCmd(Bot bot)
    {
        super(bot);
        this.name = "강제제거";
        this.help = "대기열에서 사용자가 입력한 모든 항목을 제거합니다.";
        this.arguments = "<user>";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.beListening = false;
        this.bePlaying = true;
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};

        this.options = Collections.singletonList(
                new OptionData(OptionType.USER, "사용자", "현재 대기열에서 제거할 사용자").setRequired(true)
        );
    }

    @Override
    public void doCommand(SlashCommandEvent event)
    {
        if (event.getGuild() == null)
            return;
        var option = event.getOption("사용자");
        User user = option == null ? null : option.getAsUser();

        AudioHandler handler = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();
        if (Objects.requireNonNull(handler).getQueue().isEmpty())
        {
            event.reply(event.getClient().getError() + " 대기열에 아무것도 없습니다!").setEphemeral(true).queue();
            return;
        }

        removeAllEntries(user, event);

    }

    private void removeAllEntries(User target, SlashCommandEvent event)
    {
        int count = event.getGuild() == null ? 0 : ((AudioHandler) Objects.requireNonNull(event.getGuild().getAudioManager().getSendingHandler())).getQueue().removeAll(target.getIdLong());
        if (count == 0)
        {
            event.reply(event.getClient().getWarning() + " **"+target.getName()+"** (은)는 대기열에 노래가 없습니다!").setEphemeral(true).queue();
        }
        else
        {
            event.reply(event.getClient().getSuccess() + " `"+count+"` 가 **"+ FormatUtil.formatUsername(target) +"에서 성공적으로 제거됨.").queue();
        }
    }
}
