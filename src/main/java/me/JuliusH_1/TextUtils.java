package me.JuliusH_1;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class TextUtils {
    private static final MiniMessage miniMessage = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer legacySerializer = LegacyComponentSerializer.builder()
            .character('&')
            .hexColors()
            .useUnusualXRepeatedCharacterHexFormat()
            .build();

    public static String centerText(String text, int width) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        int padding = (width - text.length()) / 2;
        StringBuilder centeredText = new StringBuilder();
        for (int i = 0; i < padding; i++) {
            centeredText.append(" ");
        }
        centeredText.append(text);
        return centeredText.toString();
    }

     public static Component parse(String message) {
            // First, parse legacy color codes
            Component component = legacySerializer.deserialize(message);
            // Then, parse MiniMessage tags for gradients and other advanced formatting
            return miniMessage.deserialize(legacySerializer.serialize(component));
    }
}
