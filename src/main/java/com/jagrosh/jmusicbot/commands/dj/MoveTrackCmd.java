package com.jagrosh.jmusicbot.commands.dj;


import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.audio.AudioHandler;
import com.jagrosh.jmusicbot.audio.QueuedTrack;
import com.jagrosh.jmusicbot.commands.DJCommand;
import com.jagrosh.jmusicbot.queue.AbstractQueue;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Command that provides users the ability to move a track in the playlist.
 */
public class MoveTrackCmd extends DJCommand
{

    public MoveTrackCmd(Bot bot)
    {
        super(bot);
        this.name = "트랙이동";
        this.help = "현재 대기열의 트랙을 다른 위치로 이동합니다";
        this.arguments = "<기존 위치> <새 위치>";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.bePlaying = true;

        List<OptionData> options = new ArrayList<>();
        options.add(new OptionData(OptionType.INTEGER, "기존_위치", "이동시키고 싶은 트랙의 **현재 대기열 순서**").setRequired(true));
        options.add(new OptionData(OptionType.INTEGER, "새_위치", "해당 트랙을 이동시키고 싶은 **목표 대기열 순서**").setRequired(true));
        this.options = options;
    }

    @Override
    public void doCommand(SlashCommandEvent event)
    {
        var optionFrom = event.getOption("기존_위치");
        var optionTo = event.getOption("새_위치");

        int from = optionFrom == null ? 0 : optionFrom.getAsInt();
        int to = optionTo == null ? 0 : optionTo.getAsInt();

        if (from == to)
        {
            event.reply(event.getClient().getError() + " 트랙을 같은 위치로 이동할 수 없습니다.").setEphemeral(true).queue();
            return;
        }

        // Validate that from and to are available
        AudioHandler handler = (AudioHandler) Objects.requireNonNull(event.getGuild()).getAudioManager().getSendingHandler();
        AbstractQueue<QueuedTrack> queue = Objects.requireNonNull(handler).getQueue();
        if (isUnavailablePosition(queue, from))
        {
            String reply = String.format("`%d` (은)는 대기열에 올바른 위치가 아닙니다!", from);
            event.reply(event.getClient().getError() + " " + reply).setEphemeral(true).queue();
            return;
        }
        if (isUnavailablePosition(queue, to))
        {
            String reply = String.format("`%d` (은)는 대기열에 올바른 위치가 아닙니다!", to);
            event.reply(event.getClient().getError() + " " + reply).setEphemeral(true).queue();
            return;
        }

        // Move the track
        QueuedTrack track = queue.moveItem(from - 1, to - 1);
        String trackTitle = track.getTrack().getInfo().title;
        String reply = String.format("**%s** (을)를 `%d` 에서 `%d` 로 옮겼습니다.", trackTitle, from, to);
        event.reply(event.getClient().getSuccess() + " " + reply).queue();
    }

    private static boolean isUnavailablePosition(AbstractQueue<QueuedTrack> queue, int position)
    {
        return (position < 1 || position > queue.size());
    }
}