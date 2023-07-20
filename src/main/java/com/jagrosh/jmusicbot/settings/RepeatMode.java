/*
 * Copyright 2020 John Grosh <john.a.grosh@gmail.com>.
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
package com.jagrosh.jmusicbot.settings;

/**
 *
 * @author Michaili K
 */
public enum RepeatMode
{
    OFF("<:no_repeat:1131228812697403403>", "꺼짐"),
    ALL("<:repeat:1131227169742405725>", "켜짐(전체 반복)"), // 🔁
    SINGLE("<:repeat_single:1131227171600482314>", "켜짐(단일 반복)"); // 🔂

    private final String emoji;
    private final String userFriendlyName;

    private RepeatMode(String emoji, String userFriendlyName)
    {
        this.emoji = emoji;
        this.userFriendlyName = userFriendlyName;
    }

    public String getEmoji()
    {
        return emoji;
    }

    public String getUserFriendlyName()
    {
        return userFriendlyName;
    }
}