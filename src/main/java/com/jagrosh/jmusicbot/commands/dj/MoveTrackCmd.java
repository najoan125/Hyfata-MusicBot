package com.jagrosh.jmusicbot.commands.dj;


import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.audio.AudioHandler;
import com.jagrosh.jmusicbot.audio.QueuedTrack;
import com.jagrosh.jmusicbot.commands.MusicCommand;
import com.jagrosh.jmusicbot.queue.FairQueue;

/**
 * Command that provides users the ability to move a track in the playlist.
 */
public class MoveTrackCmd extends MusicCommand
{

    public MoveTrackCmd(Bot bot)
    {
        super(bot);
        this.name = "movetrack";
        this.help = "\uD604\uC7AC \uB300\uAE30\uC5F4\uC758 \uD2B8\uB799\uC744 \uB2E4\uB978 \uC704\uCE58\uB85C \uC774\uB3D9\uD569\uB2C8\uB2E4";
        this.arguments = "<from> <to>";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.bePlaying = true;
    }

    @Override
    public void doCommand(CommandEvent event)
    {
        int from;
        int to;

        String[] parts = event.getArgs().split("\s+", 2);
        if(parts.length < 2)
        {
            event.replyError("\uC720\uD6A8\uD55C \uC778\uB371\uC2A4\uB97C \uB450 \uAC1C \uD3EC\uD568\uD558\uC2ED\uC2DC\uC624.");
            return;
        }

        try
        {
            // Validate the args
            from = Integer.parseInt(parts[0]);
            to = Integer.parseInt(parts[1]);
        }
        catch (NumberFormatException e)
        {
            event.replyError("\uB450 \uAC1C\uC758 \uC720\uD6A8\uD55C \uC778\uB371\uC2A4\uB97C \uC81C\uACF5\uD558\uC2ED\uC2DC\uC624.");
            return;
        }

        if (from == to)
        {
            event.replyError("\uD2B8\uB799\uC744 \uAC19\uC740 \uC704\uCE58\uB85C \uC774\uB3D9\uD560 \uC218 \uC5C6\uC2B5\uB2C8\uB2E4.");
            return;
        }

        // Validate that from and to are available
        AudioHandler handler = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();
        FairQueue<QueuedTrack> queue = handler.getQueue();
        if (isUnavailablePosition(queue, from))
        {
            String reply = String.format("`%d` (\uC740)\uB294 \uB300\uAE30\uC5F4\uC5D0 \uC62C\uBC14\uB978 \uC704\uCE58\uAC00 \uC544\uB2D9\uB2C8\uB2E4!", from);
            event.replyError(reply);
            return;
        }
        if (isUnavailablePosition(queue, to))
        {
            String reply = String.format("`%d` (\uC740)\uB294 \uB300\uAE30\uC5F4\uC5D0 \uC62C\uBC14\uB978 \uC704\uCE58\uAC00 \uC544\uB2D9\uB2C8\uB2E4!", to);
            event.replyError(reply);
            return;
        }

        // Move the track
        QueuedTrack track = queue.moveItem(from - 1, to - 1);
        String trackTitle = track.getTrack().getInfo().title;
        String reply = String.format("**%s** (\uC744)\uB97C `%d` \uC5D0\uC11C `%d` \uB85C \uC62E\uACBC\uC2B5\uB2C8\uB2E4.", trackTitle, from, to);
        event.replySuccess(reply);
    }

    private static boolean isUnavailablePosition(FairQueue<QueuedTrack> queue, int position)
    {
        return (position < 1 || position > queue.size());
    }
}