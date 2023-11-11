package com.jagrosh.jmusicbot.utils;

import com.jagrosh.jmusicbot.JMusicBot;
import com.jagrosh.jmusicbot.audio.AudioHandler;
import com.jagrosh.jmusicbot.audio.RequestMetadata;
import net.dv8tion.jda.api.entities.Member;

import java.io.*;
import java.util.ArrayList;

public class RnjsskaUtil {
    private static final ArrayList<Long> allowedUsers = new ArrayList<>();
    private static long ownerId;
    private static final File file = new File(System.getProperty("user.dir"), "rnjsska.txt");

    public static void init(long owner) {
        if (!file.exists()) {
            ownerId = owner;
            allowedUsers.add(owner);
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(owner + "\n");
            } catch (IOException e) {
                JMusicBot.LOG.error("Error in rnjsska", e);
            }
        } else {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    allowedUsers.add(Long.valueOf(line));
                }
            } catch (IOException e) {
                JMusicBot.LOG.error("Error in rnjsska", e);
            }
        }
    }

    public static boolean hasNoTrackPermission(AudioHandler handler, Member member) {
        return JMusicBot.rnjsska &&
                handler != null &&
                handler.getPlayer().getPlayingTrack() != null &&
                allowedUsers.contains(handler.getPlayer().getPlayingTrack().getUserData(RequestMetadata.class).user.id) &&
                !allowedUsers.contains(member.getIdLong());
    }

    public static void addAllowUser(long id) throws IOException {
        if (allowedUsers.contains(id)) {
            return;
        }
        allowedUsers.add(id);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
            writer.write(id + "\n");
        }
    }

    public static void removeAllowUser(long id) throws IOException {
        if (id == ownerId) return;
        if (!allowedUsers.contains(id)) return;
        allowedUsers.remove(id);

        StringBuilder stringBuilder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.equals(String.valueOf(id))) {
                    stringBuilder.append(line).append("\n");
                }
            }
        }
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(stringBuilder.toString());
        }
    }

    public static ArrayList<Long> getAllowedUsers() {
        return allowedUsers;
    }
}
