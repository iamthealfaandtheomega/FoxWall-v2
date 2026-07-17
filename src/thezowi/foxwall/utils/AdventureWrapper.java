package thezowi.foxwall.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;

public class AdventureWrapper {
    public static Component toJson(final thezowi.foxwall.libs.net.kyori.adventure.text.Component component) {
        final String json = thezowi.foxwall.libs.net.kyori.adventure.text.serializer.gson.GsonComponentSerializer.gson().serialize(component);
        final JsonElement parsed = JsonParser.parseString(json);
        return GsonComponentSerializer.gson().deserializeFromTree(parsed);
    }
}