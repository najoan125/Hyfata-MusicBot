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
package com.jagrosh.jmusicbot.commands.admin;

import java.util.Collections;
import java.util.List;

import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.jagrosh.jdautilities.commons.utils.FinderUtil;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.commands.AdminCommand;
import com.jagrosh.jmusicbot.settings.Settings;
import com.jagrosh.jmusicbot.utils.FormatUtil;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

/**
 *
 * @author John Grosh <john.a.grosh@gmail.com>
 */
public class SetdjCmd extends AdminCommand
{
    public SetdjCmd(Bot bot)
    {
        this.name = "설정_dj역할";
        this.help = "특정 음악 명령의 DJ 역할을 설정합니다";
        this.arguments = "<역할이름|NONE>";
        this.aliases = bot.getConfig().getAliases(this.name);

        this.options = Collections.singletonList(
                new OptionData(OptionType.STRING, "역할_이름", "역할 이름을 입력해 DJ 역할을 설정하거나 `none`을 입력해 설정 취소").setRequired(true)
        );
    }

    @Override
    public void doCommand(SlashCommandEvent event)
    {
        if (event.getGuild() == null) {
            return;
        }
        var option = event.getOption("역할_이름");
        String args = option == null ? "" : option.getAsString();
        if(args.isEmpty())
        {
            event.reply(event.getClient().getError()+" 역할 이름 또는 NONE을 포함하십시오.").setEphemeral(true).queue();
            return;
        }
        Settings s = event.getClient().getSettingsFor(event.getGuild());
        if(args.equalsIgnoreCase("none"))
        {
            s.setDJRole(null);
            event.reply(event.getClient().getSuccess()+" DJ역할 초기화됨. 관리자만 DJ명령어를 사용할 수 있습니다.").queue();
        }
        else
        {
            List<Role> list = FinderUtil.findRoles(args, event.getGuild());
            if(list.isEmpty())
                event.reply(event.getClient().getWarning()+" 일치하는 역할을 찾을 수 없습니다 \""+args+"\"").setEphemeral(true).queue();
            else if (list.size()>1)
                event.reply(event.getClient().getWarning()+ FormatUtil.listOfRoles(list, args)).setEphemeral(true).queue();
            else
            {
                s.setDJRole(list.getFirst());
                event.reply(event.getClient().getSuccess()+" 이제 **"+list.getFirst().getName()+"** 역할의 사용자가 DJ 명령을 사용할 수 있습니다.").queue();
            }
        }
    }

}
