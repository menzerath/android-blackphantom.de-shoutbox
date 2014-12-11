package eu.menzerath.bpchat.chat;

/**
 * Klasse basiert auf https://github.com/delight-im/Emoji:
 *
 * Copyright 2014 www.delight.im <info@delight.im>
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

import java.util.HashMap;
import java.util.regex.Pattern;

public class Emoji {
    private static final String REGEX_CLASS_SPECIAL_CHARS = "[-_)(;:*<>=/A-Za-z0-9]";
    private static final String REGEX_NEGATIVE_LOOKBEHIND = "(?<!" + REGEX_CLASS_SPECIAL_CHARS + ")";
    private static final String REGEX_NEGATIVE_LOOKAHEAD = "(?!" + REGEX_CLASS_SPECIAL_CHARS + ")";

    private static String getUnicodeChar(int codepoint) {
        return new String(Character.toChars(codepoint));
    }

    private static String replaceEmoticon(String text, String emoticon) {
        ReplacementsMap replacements = ReplacementsMap.getInstance();
        Integer codepoint = replacements.get(emoticon);
        if (codepoint == null) {
            return text;
        } else {
            String unicodeChar = getUnicodeChar(codepoint);
            return text.replaceAll(getEmoticonSearchRegex(emoticon), unicodeChar);
        }
    }

    private static String getEmoticonSearchRegex(String emoticon) {
        // Sorgt dafür, dass die RegEx kein Emoticon in eine URL oder eine Zeichenkette einsetzt
        return REGEX_NEGATIVE_LOOKBEHIND + "(" + Pattern.quote(emoticon) + ")" + REGEX_NEGATIVE_LOOKAHEAD;
    }

    /**
     * Ersetzt alle Emoticons im String mit dem passenden Unicode-Emoji
     *
     * @param text der String, in dem nach Emoticons gesucht werden soll
     * @return der neue String mit Unicode-Emojis
     */
    public static String replaceInText(String text) {
        ReplacementsMap replacements = ReplacementsMap.getInstance();
        for (String emoticon : replacements.keySet()) {
            text = replaceEmoticon(text, emoticon);
        }
        return text;
    }

    private static class ReplacementsMap extends HashMap<String, Integer> {
        private static ReplacementsMap mInstance;

        private ReplacementsMap() {
            super();
            put(":)", 0x1F603);
            put(";)", 0x1F609);
            put(":(", 0x1F61E);
            put(":D", 0x1F601);
            put(":'D", 0x1F602);
            put(":P", 0x1F61C);
            put(":O", 0x1F628);
            put(":3", 0x1F60A);
            put(":*", 0x1F618);
            put(":/", 0x1F612);
            put("<3", 0x2764);
            put("xD", 0x1F604);
            put("XD", 0x1F604);
            put(":lol:", 0x1F604);
            put("^^", 0x1F606);
            put("o.O", 0x1F627);
            put("*_*", 0x1F60D);
            put("-_-", 0x1F611);
            put("-.-", 0x1F611);
            put(">:[", 0x1F621);
            put(":y:", 0x1F44D);
        }

        public static ReplacementsMap getInstance() {
            if (mInstance == null) mInstance = new ReplacementsMap();
            return mInstance;
        }
    }
}