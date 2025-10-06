/*
 * Copyright 2017 John Grosh <john.a.grosh@gmail.com>.
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
package com.jagrosh.jmusicbot.commands.owner;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.JMusicBot;
import com.jagrosh.jmusicbot.commands.OwnerCommand;
import com.jagrosh.jmusicbot.utils.RnjsskaUtil;

import java.io.IOException;

/**
 * @author John Grosh <john.a.grosh@gmail.com>
 */
public class RnjsskaCmd extends OwnerCommand {

    public RnjsskaCmd(Bot bot) {
        this.name = "rnjsska";
        this.help = "권남을 사용합니다 ㅋㅋ";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.guildOnly = false;
        this.arguments = "<add|remove|list|NONE> <user id>";
    }

    @Override
    protected void execute(CommandEvent event) {
        if (event.getArgs().isEmpty()) {
            event.replySuccess(JMusicBot.rnjsska ? "권남 모드를 해제합니다 ㅜㅜ" : "권남 모드를 활성화 합니다! 이제 마음껏 즐길 수 있겠군 ㅋㅋ ㅅㄱ ㅋㅋㅋㅋㅋㅋ");
            JMusicBot.rnjsska = !JMusicBot.rnjsska;
        } else {
            String[] parts = event.getArgs().split("\\s+", 2);
            if (parts.length > 0 && parts[0].equals("list")) {
                StringBuilder stringBuilder = new StringBuilder();
                int i = 1;
                for (Long id : RnjsskaUtil.getAllowedUsers()) {
                    stringBuilder.append(i).append(".<@").append(id).append("> ");
                    i++;
                }
                event.reply(stringBuilder.toString());
            } else if (parts.length < 2) {
                event.replyError("유효한 인덱스를 두 개 포함하십시오.");
                return;
            }

            if (parts[0].equals("add")) {
                try {
                    RnjsskaUtil.addAllowUser(Long.parseLong(parts[1]));
                    event.replySuccess("성공적으로 권남 유저 <@" + parts[1] + "> 을(를) 추가하였습니다!");
                } catch (IOException e) {
                    event.replyError("권남 유저를 추가하는 도중 오류가 발생하였습니다! 제대로 사용자 ID를 기입했는지 확인해 주세요!");
                }
            } else if (parts[0].equals("remove")) {
                if (parts[1].length() != 1) {
                    try {
                        RnjsskaUtil.removeAllowUser(Long.parseLong(parts[1]));
                        event.replySuccess("성공적으로 권남 유저 <@" + parts[1] + "> 을(를) 제거하였습니다!");
                    } catch (IOException e) {
                        event.replyError("권남 유저를 제거하는 도중 오류가 발생하였습니다! 제대로 사용자 ID를 기입했는지 확인해 주세요!");
                    }
                } else {
                    try {
                        long id = RnjsskaUtil.getAllowedUsers().get(Integer.parseInt(parts[1]) - 1);
                        RnjsskaUtil.removeAllowUser(id);
                        event.replySuccess("성공적으로 권남 유저 <@" + id + "> 을(를) 제거하였습니다!");
                    } catch (IOException | NumberFormatException e) {
                        event.replyError("권남 유저를 제거하는 도중 오류가 발생하였습니다! 제대로 사용자 ID를 기입했는지 확인해 주세요!");
                    }
                }
            }
        }
    }
}
