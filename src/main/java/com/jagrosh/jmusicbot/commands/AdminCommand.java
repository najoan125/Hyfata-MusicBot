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
package com.jagrosh.jmusicbot.commands;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.InteractionContextType;

import java.util.Objects;

/**
 *
 * @author John Grosh (john.a.grosh@gmail.com)
 */
public abstract class AdminCommand extends SlashCommand
{
    public AdminCommand()
    {
        this.category = new Category("관리자");
        this.contexts = new InteractionContextType[]{InteractionContextType.GUILD};
    }

    private boolean check(SlashCommandEvent event) {
        if(event.getUser().getId().equals(event.getClient().getOwnerId()))
            return true;
        if(event.getGuild()==null)
            return true;
        return Objects.requireNonNull(event.getMember()).hasPermission(Permission.MANAGE_SERVER);
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        if (check(event)) {
            doCommand(event);
        } else {
            event.reply(event.getClient().getError() + " 이 명령어를 사용할 권한이 없습니다!").setEphemeral(true).queue();
        }
    }

    public abstract void doCommand(SlashCommandEvent event);
}
