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
import com.jagrosh.jlyrics.LyricsClient;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.audio.AudioHandler;
import com.jagrosh.jmusicbot.commands.MusicCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.Collections;
import java.util.Objects;

/**
 *
 * @author John Grosh (john.a.grosh@gmail.com)
 */
public class LyricsCmd extends MusicCommand
{
    private final LyricsClient client = new LyricsClient("Bugs");
    
    public LyricsCmd(Bot bot)
    {
        super(bot);
        this.name = "가사";
        this.arguments = "[노래 제목]";
        this.help = "현재 재생 중인 노래의 가사를 벅스(Bugs!)에서 찾아 보여줍니다";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};

        this.options = Collections.singletonList(
                new OptionData(OptionType.STRING, "제목", "찾을 가사의 노래 제목")
        );
    }

    @Override
    public void doCommand(SlashCommandEvent event)
    {
        var option = event.getOption("제목");
        String args = option == null ? "" : option.getAsString();

        String title;
        if(args.isEmpty())
        {
            AudioHandler sendingHandler = (AudioHandler) Objects.requireNonNull(event.getGuild()).getAudioManager().getSendingHandler();
            if (Objects.requireNonNull(sendingHandler).isMusicPlaying(event.getJDA()))
                title = sendingHandler.getPlayer().getPlayingTrack().getInfo().title;
            else
            {
                event.reply(event.getClient().getError() + " 사용법: `/가사 <노래 제목>`").setEphemeral(true).queue();
                return;
            }
        }
        else
            title = args;
        event.getChannel().sendTyping().queue();
        client.getLyrics(title).thenAccept(lyrics -> 
        {
            if(lyrics == null)
            {
                event.reply(event.getClient().getError() + " `" + title + "` 의 가사를 찾을 수 없습니다!" + (args.isEmpty() ? " 수동으로 노래 이름을 입력해 보십시오 (`가사 [노래 제목]`)" : "")).setEphemeral(true).queue();
                return;
            }

            EmbedBuilder eb = new EmbedBuilder()
                    .setAuthor(lyrics.getAuthor())
                    .setColor(Objects.requireNonNull(event.getGuild()).getSelfMember().getColor())
                    .setTitle(lyrics.getTitle(), lyrics.getURL());
            if(lyrics.getContent().length()>15000)
            {
                event.reply(event.getClient().getWarning() + " `" + title + "` 의 가사가 발견되었지만 정확하지 않을 것 같습니다: " + lyrics.getURL()).setEphemeral(true).queue();
            }
            else if(lyrics.getContent().length()>2000)
            {
                String content = lyrics.getContent().trim();
                while(content.length() > 2000)
                {
                    int index = content.lastIndexOf("\n\n", 2000);
                    if(index == -1)
                        index = content.lastIndexOf("\n", 2000);
                    if(index == -1)
                        index = content.lastIndexOf(" ", 2000);
                    if(index == -1)
                        index = 2000;
                    event.replyEmbeds(eb.setDescription(content.substring(0, index).trim()).build()).queue();
                    content = content.substring(index).trim();
                    eb.setAuthor(null).setTitle(null, null);
                }
                event.replyEmbeds(eb.setDescription(content).build()).queue();
            }
            else
                event.replyEmbeds(eb.setDescription(lyrics.getContent()).build()).queue();
        });
    }
}
