package dev.benny.tools;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Replaces configured words (and optionally your own username) in chat while streamer mode is on. */
public final class Redactor {
    private Redactor() {}

    public static Component apply(Component original) {
        if (!Config.streamer || original == null) return original;

        List<String> words = new ArrayList<>();
        for (String w : Config.redactWords.split(",")) {
            String t = w.trim();
            if (!t.isEmpty()) words.add(t);
        }
        String own = null;
        if (Config.redactOwnName) {
            own = Minecraft.getInstance().getUser().getName();
            if (own != null && !own.isEmpty()) words.add(own);
        }
        if (words.isEmpty()) return original;

        String text = original.getString();
        String out = text;
        for (String w : words) {
            String replacement = w.equals(own) ? "Streamer" : "*".repeat(Math.max(3, Math.min(w.length(), 12)));
            out = Pattern.compile(Pattern.quote(w), Pattern.CASE_INSENSITIVE)
                    .matcher(out).replaceAll(Matcher.quoteReplacement(replacement));
        }
        // Rebuilding from plain text drops colours/hover events, but only for lines that matched.
        return out.equals(text) ? original : Component.literal(out);
    }
}
