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
import com.jagrosh.jdautilities.menu.OrderedMenu;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.audio.AudioHandler;
import com.jagrosh.jmusicbot.audio.QueuedTrack;
import com.jagrosh.jmusicbot.audio.RequestMetadata;
import com.jagrosh.jmusicbot.commands.MusicCommand;
import com.jagrosh.jmusicbot.utils.FormatUtil;
import com.jagrosh.jmusicbot.utils.TimeUtil;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException.Severity;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.components.ActionComponent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.restaction.MessageEditAction;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author John Grosh <john.a.grosh@gmail.com>
 */
public class SearchCmd extends MusicCommand {
    protected String searchPrefix = "ytsearch:";
    private final String searchingEmoji;

    //Listener.java에 사용할 HashMap
    public static HashMap<String, User> searchCmdMap = new HashMap<>();
    public static HashMap<String, AudioPlaylist> searchCmdPlaylist = new HashMap<>();
    public static HashMap<String, CommandEvent> searchCmdEvent = new HashMap<>();
    public static HashMap<String, ExecutorService> searchCmdExecutors = new HashMap<>();

    public SearchCmd(Bot bot) {
        super(bot);
        this.searchingEmoji = bot.getConfig().getSearching();
        this.name = "검색";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.arguments = "<검색어>";
        this.help = "제공된 요청을 Youtube에서 검색합니다.";
        this.beListening = true;
        this.bePlaying = false;
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
    }


    @Override
    public void doCommand(CommandEvent event) {
        if (event.getArgs().isEmpty()) {
            event.replyError("검색어를 포함하십시오.");
            return;
        }
        event.reply(searchingEmoji + " 검색 중... `[" + event.getArgs() + "]`",
                m -> bot.getPlayerManager().loadItemOrdered(event.getGuild(), searchPrefix + event.getArgs(), new ResultHandler(m, event)));
    }

    private class ResultHandler implements AudioLoadResultHandler {
        private final Message m;
        private final CommandEvent event;

        private ResultHandler(Message m, CommandEvent event) {
            this.m = m;
            this.event = event;
        }

        @Override
        public void trackLoaded(AudioTrack track) {
            if (bot.getConfig().isTooLong(track)) {
                m.editMessage(FormatUtil.filter(event.getClient().getWarning() + " 이 트랙 (**" + track.getInfo().title + "**) (은)는 허용된 최대치보다 깁니다: `"
                        + TimeUtil.formatTime(track.getDuration()) + "` > `" + bot.getConfig().getMaxTime() + "`")).queue();
                return;
            }
            AudioHandler handler = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();
            int pos = Objects.requireNonNull(handler).addTrack(new QueuedTrack(track, RequestMetadata.fromResultHandler(track, event))) + 1;
            m.editMessage(FormatUtil.filter(event.getClient().getSuccess() + " **" + track.getInfo().title
                    + "** (`" + TimeUtil.formatTime(track.getDuration()) + "`) " + (pos == 0 ? "(이)가 추가되어 재생을 시작합니다"
                    : " (이)가 대기열 위치 " + pos + "에 추가됨"))).queue();
        }

        @Override
        public void playlistLoaded(AudioPlaylist playlist) {
            //메시지 수정
            MessageEditAction ma = m.editMessage(FormatUtil.filter(
                    event.getClient().getSuccess() + "`" + event.getArgs() + "` 검색 결과:"));
            EmbedBuilder eb = new EmbedBuilder();
            eb.setColor(event.getSelfMember().getColor());

            //버튼 생성
            StringBuilder result = new StringBuilder();
            LinkedList<ActionComponent> actionRow = new LinkedList<>();
            for (int i = 0; i < 10 && i < playlist.getTracks().size(); i++) {
                AudioTrack track = playlist.getTracks().get(i);
                actionRow.add(Button.primary(String.valueOf(i + 1), String.valueOf(i + 1)));
                if (searchPrefix.equals("ytsearch:") || searchPrefix.equals("scsearch:")) {
                    result.append(OrderedMenu.NUMBERS[i]).append(" `[").append(TimeUtil.formatTime(track.getDuration())).append("]` ").append("[**").append(track.getInfo().title).append("**](").append(track.getInfo().uri).append(")\n");
                } else {
                    result.append(OrderedMenu.NUMBERS[i]).append(" `[").append(TimeUtil.formatTime(track.getDuration())).append("]` ").append("[**").append(track.getInfo().author).append(" - ").append(track.getInfo().title).append("**](").append(track.getInfo().uri).append(")\n");
                }
            }

            int actionRowSize = actionRow.size();
            List<ActionRow> actionRows = new ArrayList<>();
            for (int i = 0; i < actionRowSize; i += 5) {
                if (i + 5 <= actionRowSize) {
                    actionRows.add(ActionRow.of(actionRow.subList(i, i + 5)));
                } else {
                    actionRows.add(ActionRow.of(actionRow.subList(i, actionRowSize)));
                }
            }
            actionRows.add(ActionRow.of(Button.danger("cancel", "취소")));
            ma.setComponents(actionRows);

            //곡 제목들 표시
            eb.setDescription(result.toString());

            //새 ExecutorService 생성
            ExecutorService executorService = Executors.newSingleThreadExecutor();

            //HashMap에 등록
            searchCmdMap.put(m.getId(), event.getAuthor());
            searchCmdPlaylist.put(m.getId(), playlist);
            searchCmdEvent.put(m.getId(), event);
            searchCmdExecutors.put(m.getId(), executorService);

            //ExecutorService를 이용한 시간 초과 처리
            executorService.submit(() -> {
                try {
                    TimeUnit.SECONDS.sleep(30);
                } catch (InterruptedException e) {
                    return;
                }
                m.editMessage("검색이 취소되었습니다.")
                        .setEmbeds()
                        .setComponents()
                        .queue();


                //HashMap 등록 해제
                searchCmdMap.remove(m.getId());
                searchCmdPlaylist.remove(m.getId());
                searchCmdEvent.remove(m.getId());
                searchCmdExecutors.remove(m.getId());

//현재 곡이 재생 중이지 않고 대기열이 비어있을 경우
                AudioHandler handler = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();
                if (!Objects.requireNonNull(handler).isMusicPlaying(event.getJDA()) && handler.getQueue().isEmpty()) {
                    if (!bot.getConfig().getStay())
                        event.getGuild().getAudioManager().closeAudioConnection();
                }
            });

            ma.setEmbeds(eb.build()).queue();
        }

        @Override
        public void noMatches() {
            m.editMessage(FormatUtil.filter(event.getClient().getWarning() + " 검색 결과가 없음 `" + event.getArgs() + "`.")).queue();
        }

        @Override
        public void loadFailed(FriendlyException throwable) {
            if (throwable.severity == Severity.COMMON)
                m.editMessage(event.getClient().getError() + " 로드 오류: " + throwable.getMessage()).queue();
            else
                m.editMessage(event.getClient().getError() + " 로드하는데 오류가 발생했습니다.").queue();
        }
    }
}
