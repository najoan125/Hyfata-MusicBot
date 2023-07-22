package com.jagrosh.jmusicbot.utils;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

public class UserUtil {
    public static String getUserCustomNickname(Member member){
        if (member != null){
            String customNickname = member.getNickname();
            if (customNickname != null){
                return customNickname;
            } else{
                User user = member.getUser();
                return user.getName();
            }
        }
        return null;
    }
}
