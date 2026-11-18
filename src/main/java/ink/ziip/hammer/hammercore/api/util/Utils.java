package ink.ziip.hammer.hammercore.api.util;

import lombok.experimental.UtilityClass;
import org.bukkit.ChatColor;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@UtilityClass
public class Utils {

    private static final PotionEffectType[] badEffects = new PotionEffectType[]{
            PotionEffectType.SLOWNESS,
            PotionEffectType.WITHER,
            PotionEffectType.WEAKNESS,
            PotionEffectType.BLINDNESS,
            PotionEffectType.HUNGER,
            PotionEffectType.INVISIBILITY,
            PotionEffectType.INVISIBILITY,
            PotionEffectType.POISON,
            PotionEffectType.LEVITATION,
            PotionEffectType.LEVITATION,
            PotionEffectType.GLOWING,
            PotionEffectType.GLOWING,
    };

    private static final String[] badEffectKeys = new String[]{
            "SLOWNESS", "WITHER", "WEAKNESS", "BLINDNESS", "HUNGER", "INVISIBILITY",
            "INVISIBILITY", "POISON", "LEVITATION", "LEVITATION", "GLOWING", "GLOWING"
    };

    public static String translateColorCodes(String message) {
        Pattern pattern = Pattern.compile("#[a-fA-F0-9]{6}");
        Matcher matcher = pattern.matcher(message);
        while (matcher.find()) {
            String hexCode = message.substring(matcher.start(), matcher.end());
            String replaceSharp = hexCode.replace('#', 'x');

            char[] ch = replaceSharp.toCharArray();
            StringBuilder builder = new StringBuilder();
            for (char c : ch) {
                builder.append("&").append(c);
            }

            message = message.replace(hexCode, builder.toString());
            matcher = pattern.matcher(message);
        }
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static HashMap<String, String> splitList(List<String> list) {
        HashMap<String, String> map = new HashMap<>();

        list.forEach(
                content -> {
                    String[] splits = content.split(":", 2);
                    map.put(splits[0], splits[1]);
                });

        return map;
    }

    public List<Object> getRandomPotionEffect() {
        Random random = new Random();
        int num = random.nextInt(0, badEffects.length);
        String display = ink.ziip.hammer.hammercore.HammerCore.getInstance().getMessageManager()
                .raw("admin.effect-names." + badEffectKeys[num]);
        if (display == null || display.isBlank()) display = badEffectKeys[num];
        return Arrays.asList(badEffects[num], display);
    }

    public static boolean isTeleportationItem(String lore) {
        String regex = ink.ziip.hammer.hammercore.manager.ConfigManager.SOUVENIR_TELEPORT_PATTERN;
        lore = ChatColor.stripColor(lore);
        return Pattern.matches(regex, lore);
    }
}
