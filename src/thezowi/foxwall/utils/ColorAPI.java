package thezowi.foxwall.utils;

import java.awt.Color;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import thezowi.foxwall.libs.lombok.NonNull;
import thezowi.foxwall.libs.net.kyori.adventure.text.Component;
import thezowi.foxwall.libs.net.kyori.adventure.text.event.ClickEvent;
import thezowi.foxwall.libs.net.kyori.adventure.text.event.HoverEvent;
import thezowi.foxwall.libs.net.kyori.adventure.text.format.NamedTextColor;
import thezowi.foxwall.libs.net.kyori.adventure.text.format.Style;
import thezowi.foxwall.libs.net.kyori.adventure.text.format.TextColor;
import thezowi.foxwall.libs.net.kyori.adventure.text.format.TextDecoration;
import thezowi.foxwall.libs.net.kyori.adventure.text.minimessage.MiniMessage;
import thezowi.foxwall.libs.net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import thezowi.foxwall.utils.MinecraftVersion.V;

public class ColorAPI {
	private static MiniMessage MINIMESSAGE_PARSER = null;
	public static boolean force = false;
	
	public static void load() {
		if(MinecraftVersion.atLeast(V.v1_16) || force) {
			MINIMESSAGE_PARSER = MiniMessage.builder().tags(TagResolver.standard()).strict(false).preProcessor(UnaryOperator.identity()).postProcessor(UnaryOperator.identity()).debug(null).build();
		} else {
			MINIMESSAGE_PARSER = MiniMessage.miniMessage();	
		}
	}

    public static String toLegacy(String text) {
    	String t2 = CompChatColor.convertMiniToLegacy(text);
        try {
            Class<?> chatColorClass = Class.forName("org.bukkit.ChatColor");
            java.lang.reflect.Method translateMethod = chatColorClass.getMethod("translateAlternateColorCodes", char.class, String.class);
            return (String) translateMethod.invoke(null, '&', t2);
        } catch (Exception e) { return t2; }
    }
    
    public static Component component(String t) { return component(t, null, null); } 
    public static Component component(String t, HoverEvent<?> hE) { return component(t, null, null); } 
    public static Component component(String t, HoverEvent<?> hE, ClickEvent<?> cE) {
        String t2 = CompChatColor.convertLegacyToMini(t, false);
        Component co = MINIMESSAGE_PARSER.deserialize(t2);
        if (hE != null) { co = co.hoverEvent(hE); }
        if (cE != null) { co = co.clickEvent(cE); }
        return co;
    }
    
    public static String stripColor(String text) {
    	text = CompChatColor.convertMiniToLegacy(text);
    	return CompChatColor.stripColorCodes(text);
    }
    
    public final static class CompChatColor implements TextColor {
    	public static final char COLOR_CHAR = '\u00A7';
    	public static final String ALL_CODES = "0123456789AaBbCcDdEeFfKkLlMmNnOoRrXx";
    	public static final Map<String, String> MINI_TO_LEGACY = new HashMap<>();
    	public static final Map<String, TextColor> MINI_TO_COLOR = new HashMap<>();
    	public static final Map<String, TextDecoration> MINI_TO_DECORATION = new HashMap<>();
    	public static final Map<String, String> LEGACY_TO_MINI = new HashMap<>();
    	private static final Map<Character, CompChatColor> BY_CHAR = new HashMap<>();
    	private static final Map<String, CompChatColor> BY_NAME = new HashMap<>();
    	private static final Pattern LEGACY_COLOR_MATCH = Pattern.compile("(&|" + COLOR_CHAR + ")([0-9a-fk-or])");
    	private static final Color[] LEGACY_COLORS = {
    			new Color(0, 0, 0),
    			new Color(0, 0, 170),
    			new Color(0, 170, 0),
    			new Color(0, 170, 170),
    			new Color(170, 0, 0),
    			new Color(170, 0, 170),
    			new Color(255, 170, 0),
    			new Color(170, 170, 170),
    			new Color(85, 85, 85),
    			new Color(85, 85, 255),
    			new Color(85, 255, 85),
    			new Color(85, 255, 255),
    			new Color(255, 85, 85),
    			new Color(255, 85, 255),
    			new Color(255, 255, 85),
    			new Color(255, 255, 255),
    	};
    	public static final CompChatColor BLACK = new CompChatColor('0', "black", new Color(0x000000));
    	public static final CompChatColor DARK_BLUE = new CompChatColor('1', "dark_blue", new Color(0x0000AA));
    	public static final CompChatColor DARK_GREEN = new CompChatColor('2', "dark_green", new Color(0x00AA00));
    	public static final CompChatColor DARK_AQUA = new CompChatColor('3', "dark_aqua", new Color(0x00AAAA));
    	public static final CompChatColor DARK_RED = new CompChatColor('4', "dark_red", new Color(0xAA0000));
    	public static final CompChatColor DARK_PURPLE = new CompChatColor('5', "dark_purple", new Color(0xAA00AA));
    	public static final CompChatColor GOLD = new CompChatColor('6', "gold", new Color(0xFFAA00));
    	public static final CompChatColor GRAY = new CompChatColor('7', "gray", new Color(0xAAAAAA));
    	public static final CompChatColor DARK_GRAY = new CompChatColor('8', "dark_gray", new Color(0x555555));
    	public static final CompChatColor BLUE = new CompChatColor('9', "blue", new Color(0x5555FF));
    	public static final CompChatColor GREEN = new CompChatColor('a', "green", new Color(0x55FF55));
    	public static final CompChatColor AQUA = new CompChatColor('b', "aqua", new Color(0x55FFFF));
    	public static final CompChatColor RED = new CompChatColor('c', "red", new Color(0xFF5555));
    	public static final CompChatColor LIGHT_PURPLE = new CompChatColor('d', "light_purple", new Color(0xFF55FF));
    	public static final CompChatColor YELLOW = new CompChatColor('e', "yellow", new Color(0xFFFF55));
    	public static final CompChatColor WHITE = new CompChatColor('f', "white", new Color(0xFFFFFF));
    	public static final CompChatColor MAGIC = new CompChatColor('k', "obfuscated");
    	public static final CompChatColor BOLD = new CompChatColor('l', "bold");
    	public static final CompChatColor STRIKETHROUGH = new CompChatColor('m', "strikethrough");
    	public static final CompChatColor UNDERLINE = new CompChatColor('n', "underlined");
    	public static final CompChatColor ITALIC = new CompChatColor('o', "italic");
    	public static final CompChatColor RESET = new CompChatColor('r', "reset");
    	private static final Pattern GENERIC_DOMAIN_PATTERN = Pattern.compile("(?<![\\d.])(?:(?!\\d+\\.\\d*[a-df-zA-DF-Z]|\\d*\\.\\d+[a-df-zA-DF-Z])[a-zA-Z0-9\\-.*])+\\s?(\\.|\\*|dot|\\(dot\\)|-|\\(\\*\\)|;|:|,)\\s?(c(| +)o(| +)m|o(| +)r(| +)g|n(| +)e(| +)t|(?<! )c(| +)z|(?<! )c(| +)o|(?<! )u(| +)k|(?<! )s(| +)k|b(| +)i(| +)z|(?<! )m(| +)o(| +)b(| +)i|(?<! )x(| +)x(| +)x|(?<! )e(| +)u|(?<! )m(| +)e|(?<! )i(| +)o|(?<! )o(| +)n(| +)l(| +)i(| +)n(| +)e|(?<! )x(| +)y(| +)z|(?<! )f(| +)r|(?<! )b(| +)e|(?<! )d(| +)e|(?<! )c(| +)a|(?<! )a(| +)l|(?<! )a(| +)i|(?<! )d(| +)e(| +)v|(?<! )a(| +)p(| +)p|(?<! )i(| +)n|(?<! )i(| +)s|(?<! )g(| +)g|(?<! )t(| +)o|(?<! )p(| +)h|(?<! )n(| +)l|(?<! )i(| +)d|(?<! )i(| +)n(| +)c|(?<! )u(| +)s|(?<! )p(| +)w|(?<! )p(| +)r(| +)o|(?<! )t(| +)v|(?<! )c(| +)x|(?<! )m(| +)x|(?<! )f(| +)m|(?<! )c(| +)c|(?<! )v(| +)i(| +)p|(?<! )f(| +)u(| +)n|(?<! )i(| +)c(| +)u)\\b"), COLOR_CODE_PATTERN = Pattern.compile("(?i)(?:[§&][0-9a-fk-or])+");
    	private final char code;
    	private final String name;
    	private final Color color;
    	private final String toString;

      	public String getName() { return this.name; }
      	public Color getColor() { return this.color; }
    	private CompChatColor(final char code, final String name) { this(code, name, null); }
    	private CompChatColor(final char code, final String name, final Color color) {
    		this.code = code;
    		this.name = name;
    		this.color = color;
    		this.toString = new String(new char[] { COLOR_CHAR, code });
    		BY_CHAR.put(code, this);
    		BY_NAME.put(name.toUpperCase(Locale.ROOT), this);
    	}

    	private CompChatColor(final String name, final String toString, final int rgb) {
    		this.code = '#';
    		this.name = name;
    		this.color = new Color(rgb);
    		this.toString = toString;
    	}

    	@Override
    	public int hashCode() {
    		int hash = 7;
    		hash = 53 * hash + Objects.hashCode(this.toString);
    		return hash;
    	}

    	@Override
    	public boolean equals(final Object obj) {
    		if (this == obj) { return true; }
    		if (obj == null || this.getClass() != obj.getClass()) { return false; }
    		return Objects.equals(this.toString, ((CompChatColor) obj).toString);
    	}

    	public char getCode() { return this.code; }
    	public boolean isHex() { return this.code == '#'; }
    	public String toColorizedChatString() { return this.toString + this.toChatString(); }
    	public String toChatString() { return this.isHex() ? this.getName() : capitalizeFully(getName()); }
    	public String toSaveableString() { return this.getName(); }
    	public boolean isFormat() { return this.color == null; }
        public static String capitalizeFully(@NonNull String name) {
        	if (name == null) throw new NullPointerException("name is marked non-null but is null"); 
            return capitalize(name.toLowerCase().replace("_", " "));
        }
        public static String capitalize(String message) {
        	if (message != null && message.length() != 0) {
        		int strLen = message.length();
        		StringBuffer buffer = new StringBuffer(strLen);
        		boolean capitalizeNext = true;
        		for (int i = 0; i < strLen; i++) {
        			char letter = message.charAt(i);
        			if (Character.isWhitespace(letter)) {
        				buffer.append(letter);
        				capitalizeNext = true;
        			} else if (capitalizeNext) {
        				buffer.append(Character.toTitleCase(letter));
        				capitalizeNext = false;
        			} else { buffer.append(letter); } 
        		} 
        		return buffer.toString();
        	} 
        	return message;
        }
    	public boolean isColor() { return !this.isFormat() && this != RESET; }
    	public Style toStyle() { return Style.style(this.isFormat() ? this.toTextDecoration() : this.toTextColor()); }
    	public TextColor toTextColor() {
    		if (this == BLACK) { return NamedTextColor.BLACK; }
    		else if (this == DARK_BLUE) { return NamedTextColor.DARK_BLUE; }
    		else if (this == DARK_GREEN) { return NamedTextColor.DARK_GREEN; }
    		else if (this == DARK_AQUA) { return NamedTextColor.DARK_AQUA; }
    		else if (this == DARK_RED) { return NamedTextColor.DARK_RED; }
    		else if (this == DARK_PURPLE) { return NamedTextColor.DARK_PURPLE; }
    		else if (this == GOLD) { return NamedTextColor.GOLD; }
    		else if (this == GRAY) { return NamedTextColor.GRAY; }
    		else if (this == DARK_GRAY) { return NamedTextColor.DARK_GRAY; }
    		else if (this == BLUE) { return NamedTextColor.BLUE; }
    		else if (this == GREEN) { return NamedTextColor.GREEN; }
    		else if (this == AQUA) { return NamedTextColor.AQUA; }
    		else if (this == RED) { return NamedTextColor.RED; }
    		else if (this == LIGHT_PURPLE) {return NamedTextColor.LIGHT_PURPLE; }
    		else if (this == YELLOW) { return NamedTextColor.YELLOW; }
    		else if (this == WHITE) { return NamedTextColor.WHITE; }
    		else if (this == RESET) { return null; }
    		else if (this.color == null) return null;
    		else return TextColor.color(this.color.getRGB());
    	}
    	public TextDecoration toTextDecoration() {
    		if (this == BOLD) { return TextDecoration.BOLD; }
    		else if (this == STRIKETHROUGH) { return TextDecoration.STRIKETHROUGH; }
    		else if (this == UNDERLINE) { return TextDecoration.UNDERLINED; }
    		else if (this == ITALIC) { return TextDecoration.ITALIC; }
    		else if (this == MAGIC) { return TextDecoration.OBFUSCATED; }
    		else if (this == RESET) { return null; }
    		return null;
    	}
    	public String toClosestLegacy() { return getClosestLegacy(this.color).toString(); }
    	@Override
    	public String toString() { return this.toString; }
    	public String serialize() { return this.toSaveableString(); }
    	public static CompChatColor getByChar(final char code) { return BY_CHAR.get(code); }
    	public static CompChatColor fromColor(final Color color) { return fromString("#"+Integer.toHexString(color.getRGB()).substring(2)); }
    	public static CompChatColor fromString(@NonNull String string) {
    		if ("MAGENTA".equals(string.toUpperCase())) { return LIGHT_PURPLE; }
    		if (string.charAt(0) == '<' && string.charAt(string.length() - 1) == '>') { string = string.substring(1, string.length() - 1); }
    		if (string.charAt(0) == '#' && string.length() == 7) {
    			if (MinecraftVersion.hasVersion() && MinecraftVersion.olderThan(V.v1_16)) {
    				final Color color = getColorFromHex(string);
    				return getClosestLegacy(color);
    			}
    			int rgb;
    			try { rgb = Integer.parseInt(string.substring(1), 16); } catch (final NumberFormatException ex) { throw new IllegalArgumentException("Illegal hex string "+string); }
    			return new CompChatColor(string, "<"+string+">", rgb);
    		}
    		if (string.length() == 2) {
    			if (string.charAt(0) != '&') { throw new IllegalArgumentException("Invalid syntax, please use & + color code. Got: "+string); }
    			final CompChatColor byChar = BY_CHAR.get(string.charAt(1));
    			if (byChar != null) { return byChar; }
    		} else {
    			final CompChatColor byName = BY_NAME.get(string.toUpperCase(Locale.ROOT));
    			if (byName != null) { return byName; }
    			if (string.equalsIgnoreCase("magic")) { return MAGIC; }
    		}
    		throw new IllegalArgumentException("Could not parse CompChatColor "+string);
    	}
    	public static CompChatColor fromTextColor(final TextColor color) { return fromString(color.asHexString()); }
    	public static CompChatColor fromTextDecoration(final TextDecoration decoration) {
    		if (decoration == TextDecoration.BOLD) { return BOLD; }
    		else if (decoration == TextDecoration.STRIKETHROUGH) { return STRIKETHROUGH; }
    		else if (decoration == TextDecoration.UNDERLINED) { return UNDERLINE; }
    		else if (decoration == TextDecoration.ITALIC) { return ITALIC; }
    		else if (decoration == TextDecoration.OBFUSCATED) { return MAGIC; }
    		else { throw new IllegalArgumentException("Unknown decoration: "+decoration); }
    	}
    	private static Color getColorFromHex(final String hex) { return new Color(Integer.parseInt(hex.substring(1, 3), 16), Integer.parseInt(hex.substring(3, 5), 16), Integer.parseInt(hex.substring(5, 7), 16)); }
    	public static CompChatColor getClosestLegacy(final Color color) {
    		if (color.getAlpha() < 128) { return null; }
    		int index = 0;
    		double best = -1;
    		for (int i = 0; i < LEGACY_COLORS.length; i++) { if (areSimilar(LEGACY_COLORS[i], color)) { return CompChatColor.getColors().get(i); } }

    		for (int i = 0; i < LEGACY_COLORS.length; i++) {
    			final double distance = getDistance(color, LEGACY_COLORS[i]);
    			if (distance < best || best == -1) {
    				best = distance;
    				index = i;
    			}
    		}
    		return CompChatColor.getColors().get(index);
    	}
    	private static boolean areSimilar(final Color first, final Color second) { return Math.abs(first.getRed() - second.getRed()) <= 5 && Math.abs(first.getGreen() - second.getGreen()) <= 5 && Math.abs(first.getBlue() - second.getBlue()) <= 5; }
    	private static double getDistance(final Color first, final Color second) {
    		final double rmean = (first.getRed() + second.getRed()) / 2.0;
    		final double r = first.getRed() - second.getRed();
    		final double g = first.getGreen() - second.getGreen();
    		final int b = first.getBlue() - second.getBlue();
    		final double weightR = 2 + rmean / 256.0;
    		final double weightG = 4.0;
    		final double weightB = 2 + (255 - rmean) / 256.0;
    		return weightR * r * r + weightG * g * g + weightB * b * b;
    	}
    	public static String translateColorCodes(final String message) {
    		final StringBuilder result = new StringBuilder();
    		for (int i = 0; i < message.length(); i++) {
    			if (message.charAt(i) == '<') {
    				final int endIndex = message.indexOf('>', i);
    				if (endIndex != -1) {
    					final String code = message.substring(i, endIndex + 1);
    					if (MINI_TO_LEGACY.containsKey(code)) {
    						result.append(MINI_TO_LEGACY.get(code));
    						i = endIndex;
    						continue;
    					}
    					if (code.matches("<#[0-9a-fA-F]{6}>")) {
    						appendHex(result, code.substring(1, code.length() - 1));
    						i = endIndex;
    						continue;
    					}
    				}
    			} else if (i + 6 < message.length() && message.charAt(i) == '#' && message.substring(i + 1, i + 7).matches("[0-9a-fA-F]{6}")) {
    				appendHex(result, message.substring(i, i + 7));
    				i += 6;
    				continue;

    			} else if (message.charAt(i) == '&' && i + 1 < message.length() && ALL_CODES.indexOf(message.charAt(i + 1)) > -1) {
    				result.append(CompChatColor.COLOR_CHAR).append(Character.toLowerCase(message.charAt(i + 1)));
    				i += 1;
    				continue;
    			}
    			result.append(message.charAt(i));
    		}
    		return result.toString();
    	}
    	public static boolean hasLegacyColors(final String message) { return LEGACY_COLOR_MATCH.matcher(message.toLowerCase()).find(); }
    	private static void appendHex(final StringBuilder result, final String code) {
    		if (MinecraftVersion.hasVersion() && MinecraftVersion.olderThan(V.v1_16)) { result.append(getClosestLegacy(getColorFromHex(code))); }
    		else { result.append(COLOR_CHAR).append("x").append(COLOR_CHAR).append(code.charAt(1)).append(COLOR_CHAR).append(code.charAt(2)).append(COLOR_CHAR).append(code.charAt(3)).append(COLOR_CHAR).append(code.charAt(4)).append(COLOR_CHAR).append(code.charAt(5)).append(COLOR_CHAR).append(code.charAt(6)); }
    	}
    	public static String stripColorCodes(final String message) { return stripColorCodes(message, true); }
    	public static String stripColorCodes(final String message, final boolean ampersand) {
    		final int messageLength = message.length();
    		final char[] strippedMessage = new char[messageLength];
    		int resultIndex = 0;
    		for (int i = 0; i < messageLength; i++) {
    			final char currentChar = message.charAt(i);
    			if ((currentChar == '§' || (ampersand && currentChar == '&')) && i + 1 < messageLength) {
    				final char nextChar = message.charAt(i + 1);
    				if ((nextChar >= '0' && nextChar <= '9') || (nextChar >= 'a' && nextChar <= 'f') || (nextChar >= 'A' && nextChar <= 'F') || (nextChar >= 'k' && nextChar <= 'o') || (nextChar >= 'K' && nextChar <= 'O') || nextChar == 'r' || nextChar == 'R' || nextChar == 'x') { i++; }
    				else { strippedMessage[resultIndex++] = currentChar; }
    			} else { strippedMessage[resultIndex++] = currentChar; }
    		}
    		return new String(strippedMessage, 0, resultIndex);
    	}
    	public static String getLastColors(final String input) {
    		if (input == null) { return ""; }
    		String result = "";
    		final int length = input.length();
    		for (int index = length - 1; index > -1; index--) {
    			final char section = input.charAt(index);
    			if (section == COLOR_CHAR && index < length - 1) {
    				final String hexColor = getHexColor(input, index);
    				if (hexColor != null) {
    					result = hexColor + result;
    					break;
    				}
    				final char c = input.charAt(index + 1);
    				final CompChatColor color = getByChar(c);
    				if (color != null) {
    					result = color.toString() + result;
    					if (color.isColor() || color.equals(RESET)) { break; }
    				}
    			}
    		}
    		return result;
    	}
    	private static String getHexColor(final String input, final int index) {
    		if (index < 12) { return null; }
    		if (input.charAt(index - 11) != 'x' || input.charAt(index - 12) != COLOR_CHAR) { return null; }
    		for (int i = index - 10; i <= index; i += 2) { if (input.charAt(i) != COLOR_CHAR) { return null; } }
    		for (int i = index - 9; i <= (index + 1); i += 2) {
    			final char toCheck = input.charAt(i);
    			if (toCheck < '0' || toCheck > 'f') { return null; }
    			if (toCheck > '9' && toCheck < 'A') { return null; }
    			if (toCheck > 'F' && toCheck < 'a') { return null; }
    		}
    		return input.substring(index - 12, index + 2);
    	}
    	public static String convertLegacyToMini(final String message, final boolean supportAmpersand) {
    		final StringBuilder result = new StringBuilder();
    		final String[] parts = message.split(" ", -1);
    		for (int idx = 0; idx < parts.length; idx++) {
    			final String part = parts[idx];
    			if (GENERIC_DOMAIN_PATTERN.matcher(part).find()) {
    				if (part.startsWith("<") && part.endsWith(">")) {
    					result.append(part);
    					if (idx < parts.length - 1) result.append(' ');
    					continue;
    				}
    				if (!part.isEmpty() && ((part.startsWith("§") || part.startsWith("&")) || part.startsWith("<"))) {
    					final Matcher matcher = COLOR_CODE_PATTERN.matcher(part);
    					String lastMatch = null;
    					String startingMatch = null;
    					while (matcher.find()) {
    						final String g = matcher.group();
    						if (part.startsWith(g)) {
    							startingMatch = g;
    							break;
    						}
    						lastMatch = g;
    					}
    					final String color = (startingMatch != null) ? startingMatch : lastMatch;
    					if (color != null) {
    						result.append(CompChatColor.convertLegacyToMini(color, true)).append(part.replaceFirst(Pattern.quote(color), ""));
    						if (idx < parts.length - 1) result.append(' ');
    						continue;
    					}
    				} else {
    					result.append(part);
    					if (idx < parts.length - 1) result.append(' ');
    					continue;
    				}

    				if(!part.startsWith("<")) continue;
    			}
    			for (int i = 0; i < part.length(); i++) {
    				if (i + 13 < part.length() && part.charAt(i) == '§' && part.charAt(i + 1) == 'x') {
    					final StringBuilder hex = new StringBuilder("#");
    					boolean isValidHexSequence = true;
    					for (int j = 2; j <= 12; j += 2) {
    						if (part.charAt(i + j) == '§')
    							hex.append(part.charAt(i + j + 1));
    						else {
    							isValidHexSequence = false;
    							break;
    						}
    					}
    					if (isValidHexSequence) {
    						result.append('<').append(hex).append('>');
    						i += 13;
    						continue;
    					}
    				}
    				if (i + 1 < part.length() && ((part.charAt(i) == '&' && supportAmpersand) || part.charAt(i) == '§')) {
    					final String code = part.substring(i, i + 2);
    					if (LEGACY_TO_MINI.containsKey(code)) {
    						result.append(LEGACY_TO_MINI.get(code));
    						i++;
    						continue;
    					}
    				}
    				result.append(part.charAt(i));
    			}
    			if (idx < parts.length - 1) result.append(" ");
    		}
    		return result.toString();
    	}
    	public static String convertMiniToLegacy(final String minimessage) {
    		final StringBuilder filteredMessage = new StringBuilder();
    		final Deque<String> tagStack = new ArrayDeque<>();
    		final int length = minimessage.length();
    		for (int i = 0; i < length; i++) {
    			final char currentChar = minimessage.charAt(i);
    			if (currentChar == '\\' && i + 1 < length && minimessage.charAt(i + 1) == '<') {
    				filteredMessage.append('\\').append('<');
    				i++;
    				continue;
    			}
    			if (currentChar == '<') {
    				final int closeIndex = minimessage.indexOf('>', i);
    				if (closeIndex == -1 || minimessage.substring(i + 1, closeIndex).contains("<")) {
    					filteredMessage.append(currentChar);
    					continue;
    				}
    				final String tagContent = minimessage.substring(i + 1, closeIndex).toLowerCase();
    				if (tagContent.charAt(0) == '/') {
    					final String endTag = tagContent.substring(1);
    					if (!isValidTag(endTag)) {
    						i = closeIndex;
    						continue;
    					}
    					if (!tagStack.isEmpty() && tagStack.peek().equals(endTag)) {
    						tagStack.pop();
    						String colorCode;
    						if (!tagStack.isEmpty()) {
    							final String currentTag = tagStack.peek();
    							colorCode = CompChatColor.MINI_TO_LEGACY.get("<" + currentTag + ">");
    							if (currentTag.charAt(0) == '#' && currentTag.length() == 7) { colorCode = CompChatColor.getClosestLegacy(getColorFromHex(currentTag)).toString(); } 
    							if (colorCode != null) { filteredMessage.append(colorCode); }
    						} else { filteredMessage.append(CompChatColor.RESET.toString()); }
    					}
    					i = closeIndex;
    					continue;
    				} else {
    					String tagName;
    					if (tagContent.startsWith("color:")) { tagName = tagContent.substring(6); }
    					else if (tagContent.startsWith("colour:")) { tagName = tagContent.substring(7); }
    					else if (tagContent.startsWith("c:")) { tagName = tagContent.substring(2); }
    					else { tagName = tagContent; }
    					if (!isValidTag(tagName)) {
    						i = closeIndex;
    						continue;
    					}
    					tagStack.push(tagName);
    					String colorCode = CompChatColor.MINI_TO_LEGACY.get("<" + tagName + ">");
    					if (tagName.charAt(0) == '#' && tagName.length() == 7) { colorCode = CompChatColor.getClosestLegacy(getColorFromHex(tagName)).toString(); } 
    					if (colorCode != null) { filteredMessage.append(colorCode); }
    					i = closeIndex;
    					continue;
    				}
    			} else { filteredMessage.append(currentChar); }
    		}
    		while (!tagStack.isEmpty()) {
    			tagStack.pop();
    			filteredMessage.append(CompChatColor.RESET.toString());
    		}
    		return filteredMessage.toString();
    	}
    	private static boolean isValidTag(String tag) {
    		if (tag.isEmpty()) { return true; }
    		if (tag.charAt(0) == '#') {
    			if (tag.length() == 7) { return true; }
    			return false;
    		}
    		tag = tag.toLowerCase();
    		switch (tag) {
    			case "grey":
    				tag = "gray";
    				break;
    			case "dark_grey":
    				tag = "dark_gray";
    				break;
    			case "insert":
    				tag = "insertion";
    				break;
    			default:
    				if (tag.contains(":")) { tag = tag.split(":", 2)[0]; }
    				break;
    		}
    		return "reset".equals(tag) || "b".equals(tag) || "bold".equals(tag) || "i".equals(tag) || "italic".equals(tag) || "u".equals(tag) || "underlined".equals(tag) || "st".equals(tag) || "strikethrough".equals(tag) || "obf".equals(tag) || "obfuscated".equals(tag) || NamedTextColor.NAMES.value(tag) != null;
    	}
    	public static CompChatColor[] values() { return BY_CHAR.values().toArray(new CompChatColor[BY_CHAR.size()]); }
    	public static List<CompChatColor> getColors() { return Arrays.asList(BLACK, DARK_BLUE, DARK_GREEN, DARK_AQUA, DARK_RED, DARK_PURPLE, GOLD, GRAY, DARK_GRAY, BLUE, GREEN, AQUA, RED, LIGHT_PURPLE, YELLOW, WHITE); }
    	public static List<CompChatColor> getDecorations() { return Arrays.asList(MAGIC, BOLD, STRIKETHROUGH, UNDERLINE, ITALIC); }
    	static {
    		MINI_TO_LEGACY.put("<black>", "§0");
    		MINI_TO_LEGACY.put("<dark_blue>", "§1");
    		MINI_TO_LEGACY.put("<dark_green>", "§2");
    		MINI_TO_LEGACY.put("<dark_aqua>", "§3");
    		MINI_TO_LEGACY.put("<dark_red>", "§4");
    		MINI_TO_LEGACY.put("<dark_purple>", "§5");
    		MINI_TO_LEGACY.put("<gold>", "§6");
    		MINI_TO_LEGACY.put("<gray>", "§7");
    		MINI_TO_LEGACY.put("<dark_gray>", "§8");
    		MINI_TO_LEGACY.put("<blue>", "§9");
    		MINI_TO_LEGACY.put("<green>", "§a");
    		MINI_TO_LEGACY.put("<aqua>", "§b");
    		MINI_TO_LEGACY.put("<red>", "§c");
    		MINI_TO_LEGACY.put("<light_purple>", "§d");
    		MINI_TO_LEGACY.put("<yellow>", "§e");
    		MINI_TO_LEGACY.put("<white>", "§f");
    		MINI_TO_LEGACY.put("<u>", "§n");
    		MINI_TO_LEGACY.put("<underlined>", "§n");
    		MINI_TO_LEGACY.put("<st>", "§m");
    		MINI_TO_LEGACY.put("<strikethrough>", "§m");
    		MINI_TO_LEGACY.put("<obf>", "§k");
    		MINI_TO_LEGACY.put("<obfuscated>", "§k");
    		MINI_TO_LEGACY.put("<i>", "§o");
    		MINI_TO_LEGACY.put("<italic>", "§o");
    		MINI_TO_LEGACY.put("<b>", "§l");
    		MINI_TO_LEGACY.put("<bold>", "§l");
    		MINI_TO_LEGACY.put("<r>", "§r");
    		MINI_TO_LEGACY.put("<reset>", "§r");
    		MINI_TO_COLOR.put("<black>", NamedTextColor.BLACK);
    		MINI_TO_COLOR.put("<dark_blue>", NamedTextColor.DARK_BLUE);
    		MINI_TO_COLOR.put("<dark_green>", NamedTextColor.DARK_GREEN);
    		MINI_TO_COLOR.put("<dark_aqua>", NamedTextColor.DARK_AQUA);
    		MINI_TO_COLOR.put("<dark_red>", NamedTextColor.DARK_RED);
    		MINI_TO_COLOR.put("<dark_purple>", NamedTextColor.DARK_PURPLE);
    		MINI_TO_COLOR.put("<gold>", NamedTextColor.GOLD);
    		MINI_TO_COLOR.put("<gray>", NamedTextColor.GRAY);
    		MINI_TO_COLOR.put("<dark_gray>", NamedTextColor.DARK_GRAY);
    		MINI_TO_COLOR.put("<blue>", NamedTextColor.BLUE);
    		MINI_TO_COLOR.put("<green>", NamedTextColor.GREEN);
    		MINI_TO_COLOR.put("<aqua>", NamedTextColor.AQUA);
    		MINI_TO_COLOR.put("<red>", NamedTextColor.RED);
    		MINI_TO_COLOR.put("<light_purple>", NamedTextColor.LIGHT_PURPLE);
    		MINI_TO_COLOR.put("<yellow>", NamedTextColor.YELLOW);
    		MINI_TO_COLOR.put("<white>", NamedTextColor.WHITE);
    		MINI_TO_DECORATION.put("<u>", TextDecoration.UNDERLINED);
    		MINI_TO_DECORATION.put("<underlined>", TextDecoration.UNDERLINED);
    		MINI_TO_DECORATION.put("<st>", TextDecoration.STRIKETHROUGH);
    		MINI_TO_DECORATION.put("<strikethrough>", TextDecoration.STRIKETHROUGH);
    		MINI_TO_DECORATION.put("<obf>", TextDecoration.OBFUSCATED);
    		MINI_TO_DECORATION.put("<obfuscated>", TextDecoration.OBFUSCATED);
    		MINI_TO_DECORATION.put("<i>", TextDecoration.ITALIC);
    		MINI_TO_DECORATION.put("<italic>", TextDecoration.ITALIC);
    		MINI_TO_DECORATION.put("<b>", TextDecoration.BOLD);
    		MINI_TO_DECORATION.put("<bold>", TextDecoration.BOLD);
    		MINI_TO_DECORATION.put("<r>", null);
    		MINI_TO_DECORATION.put("<reset>", null);
    		LEGACY_TO_MINI.put("&0", "<black>");
    		LEGACY_TO_MINI.put("&1", "<dark_blue>");
    		LEGACY_TO_MINI.put("&2", "<dark_green>");
    		LEGACY_TO_MINI.put("&3", "<dark_aqua>");
    		LEGACY_TO_MINI.put("&4", "<dark_red>");
    		LEGACY_TO_MINI.put("&5", "<dark_purple>");
    		LEGACY_TO_MINI.put("&6", "<gold>");
    		LEGACY_TO_MINI.put("&7", "<gray>");
    		LEGACY_TO_MINI.put("&8", "<dark_gray>");
    		LEGACY_TO_MINI.put("&9", "<blue>");
    		LEGACY_TO_MINI.put("§0", "<black>");
    		LEGACY_TO_MINI.put("§1", "<dark_blue>");
    		LEGACY_TO_MINI.put("§2", "<dark_green>");
    		LEGACY_TO_MINI.put("§3", "<dark_aqua>");
    		LEGACY_TO_MINI.put("§4", "<dark_red>");
    		LEGACY_TO_MINI.put("§5", "<dark_purple>");
    		LEGACY_TO_MINI.put("§6", "<gold>");
    		LEGACY_TO_MINI.put("§7", "<gray>");
    		LEGACY_TO_MINI.put("§8", "<dark_gray>");
    		LEGACY_TO_MINI.put("§9", "<blue>");
    		LEGACY_TO_MINI.put("&a", "<green>");
    		LEGACY_TO_MINI.put("&b", "<aqua>");
    		LEGACY_TO_MINI.put("&c", "<red>");
    		LEGACY_TO_MINI.put("&d", "<light_purple>");
    		LEGACY_TO_MINI.put("&e", "<yellow>");
    		LEGACY_TO_MINI.put("&f", "<white>");
    		LEGACY_TO_MINI.put("&A", "<green>");
    		LEGACY_TO_MINI.put("&B", "<aqua>");
    		LEGACY_TO_MINI.put("&C", "<red>");
    		LEGACY_TO_MINI.put("&D", "<light_purple>");
    		LEGACY_TO_MINI.put("&E", "<yellow>");
    		LEGACY_TO_MINI.put("&F", "<white>");
    		LEGACY_TO_MINI.put("§a", "<green>");
    		LEGACY_TO_MINI.put("§b", "<aqua>");
    		LEGACY_TO_MINI.put("§c", "<red>");
    		LEGACY_TO_MINI.put("§d", "<light_purple>");
    		LEGACY_TO_MINI.put("§e", "<yellow>");
    		LEGACY_TO_MINI.put("§f", "<white>");
    		LEGACY_TO_MINI.put("§A", "<green>");
    		LEGACY_TO_MINI.put("§B", "<aqua>");
    		LEGACY_TO_MINI.put("§C", "<red>");
    		LEGACY_TO_MINI.put("§D", "<light_purple>");
    		LEGACY_TO_MINI.put("§E", "<yellow>");
    		LEGACY_TO_MINI.put("§F", "<white>");
    		LEGACY_TO_MINI.put("&n", "<u>");
    		LEGACY_TO_MINI.put("&m", "<st>");
    		LEGACY_TO_MINI.put("&k", "<obf>");
    		LEGACY_TO_MINI.put("&o", "<i>");
    		LEGACY_TO_MINI.put("&l", "<b>");
    		LEGACY_TO_MINI.put("&r", "<reset>");
    		LEGACY_TO_MINI.put("&N", "<u>");
    		LEGACY_TO_MINI.put("&M", "<st>");
    		LEGACY_TO_MINI.put("&K", "<obf>");
    		LEGACY_TO_MINI.put("&O", "<i>");
    		LEGACY_TO_MINI.put("&L", "<b>");
    		LEGACY_TO_MINI.put("&R", "<reset>");
    		LEGACY_TO_MINI.put("§n", "<u>");
    		LEGACY_TO_MINI.put("§m", "<st>");
    		LEGACY_TO_MINI.put("§k", "<obf>");
    		LEGACY_TO_MINI.put("§o", "<i>");
    		LEGACY_TO_MINI.put("§l", "<b>");
    		LEGACY_TO_MINI.put("§r", "<reset>");
    		LEGACY_TO_MINI.put("§N", "<u>");
    		LEGACY_TO_MINI.put("§M", "<st>");
    		LEGACY_TO_MINI.put("§K", "<obf>");
    		LEGACY_TO_MINI.put("§O", "<i>");
    		LEGACY_TO_MINI.put("§L", "<b>");
    		LEGACY_TO_MINI.put("§R", "<reset>");
    	}

    	@Override
    	public int value() { return this.color.getRGB(); }
    }
}