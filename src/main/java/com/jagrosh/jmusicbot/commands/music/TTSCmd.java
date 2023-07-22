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

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.audio.AudioHandler;
import com.jagrosh.jmusicbot.audio.QueuedTrack;
import com.jagrosh.jmusicbot.commands.MusicCommand;
import com.jagrosh.jmusicbot.utils.CustomAudioTrack;
import com.jagrosh.jmusicbot.utils.UserUtil;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import net.dv8tion.jda.api.entities.Member;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 *
 * @author John Grosh <john.a.grosh@gmail.com>
 */
public class TTSCmd extends MusicCommand
{
    public TTSCmd(Bot bot)
    {
        super(bot);
        this.name = "tts";
        this.help = "TTS를 재생합니다";
        this.arguments = "<text>";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.bePlaying = false;
        this.beListening = true;
    }
    
    public boolean isplaying = false;
    AudioTrack audiotrack = null;

    @Override
    public void doCommand(CommandEvent event)
    {
    	if (event.getArgs().isEmpty()) {
    		event.replyError("재생할 텍스트를 알려주세요. 사용법: `" + event.getClient().getPrefix() + "tts <text>`");
    		return;
    	}
    	AudioHandler handler = (AudioHandler)event.getGuild().getAudioManager().getSendingHandler();
		String args = null;
		try {
			args = "http://translate.google.com/translate_tts?ie=UTF-8&total=1&idx=0&textlen=32&client=tw-ob&q=" + URLEncoder.encode(event.getArgs(), "UTF-8") + "&tl=ko-kr";
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		bot.getPlayerManager().loadItemOrdered(event.getGuild(), args, new AudioLoadResultHandler() {

			@Override
			public void loadFailed(FriendlyException arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void noMatches() {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void playlistLoaded(AudioPlaylist arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void trackLoaded(AudioTrack track) {
				String newTitle = UserUtil.getUserCustomNickname(event.getMember()) +"님의 TTS";
				String newAuthor = "TTS";

				CustomAudioTrack at = new CustomAudioTrack(track, getChangedTrackInfo(track,newTitle,newAuthor));
				if (handler.getNowPlaying(event.getJDA()) != null) {
					handler.addTrackToFront(new QueuedTrack(at, event.getAuthor()));
					event.replySuccess("TTS를 대기열에 추가했습니다 (현재 재생중인 곡이 끝나면 TTS가 바로 재생됩니다)");
				}
				else {
					handler.addTrackToFront(new QueuedTrack(at, event.getAuthor()));
					event.replySuccess("TTS를 재생합니다");
				}
				
			}
        	
        });
    }

	public AudioTrackInfo getChangedTrackInfo(AudioTrack track, String title, String author){
		AudioTrackInfo oldTrackInfo = track.getInfo();

		return new AudioTrackInfo(
				title, author, oldTrackInfo.length, oldTrackInfo.identifier, oldTrackInfo.isStream, oldTrackInfo.uri
		);
	}
}
