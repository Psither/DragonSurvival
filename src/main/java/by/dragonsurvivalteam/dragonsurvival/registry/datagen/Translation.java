package by.dragonsurvivalteam.dragonsurvival.registry.datagen;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.magic.common.DragonAbility;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import java.lang.annotation.*;
import java.util.Locale;

/**
 * The following field types have special behaviour when no {@link Translation#key()} is supplied: <br>
 * - {@link Enum} will use {@link Enum#toString()} -> {@link String#toLowerCase(Locale)} to determine the wrapped value <br>
 * - {@link String} annotated with the type {@link Type#MISC} will use its stored value, not wrapping anything <br>
 * - {@link Holder} will use {@link Holder#getKey()} -> {@link ResourceKey#location()} -> {@link ResourceLocation#getPath()} to determine the wrapped value <br>
 * - {@link ResourceKey} will use {@link ResourceKey#location()} -> {@link ResourceLocation#getPath()} to determine the wrapped value <br>
 * - {@link DragonAbility} will use {@link DragonAbility#getName()} to determine the wrapped value
 */
@Target({ElementType.FIELD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(Translation.Translations.class)
public @interface Translation {
    /** If it's empty the key will be derived from the field (behaviour depends on the field type) */
    String key() default "";

    Type type();

    String locale() default "en_us";

    /**
     * Translation for the key <br>
     * Comment entries will be separated by a newline (\n)
     */
    String[] comments();

    // To allow multiple translations (potentially of differing types) to be set on one field
    @Target({ElementType.FIELD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @interface Translations {
        Translation[] value();
    }

    enum Type {
        ITEM("item." + DragonSurvival.MODID + ".", ""),
        BLOCK("block." + DragonSurvival.MODID + ".", ""),
        ENTITY("entity." + DragonSurvival.MODID + ".", ""),
        ATTRIBUTE("attribute." + DragonSurvival.MODID + ".", ""),
        ATTRIBUTE_DESCRIPTION("attribute." + DragonSurvival.MODID + ".", ".desc"),
        EFFECT("effect." + DragonSurvival.MODID + ".", ""),
        EFFECT_DESCRIPTION("effect." + DragonSurvival.MODID + ".", ".desc"),
        ENCHANTMENT("enchantment." + DragonSurvival.MODID + ".", ""),
        ENCHANTMENT_DESCRIPTION("enchantment." + DragonSurvival.MODID + ".", ".desc"),
        DESCRIPTION(DragonSurvival.MODID + ".description.", ""),
        DESCRIPTION_ADDITION(DragonSurvival.MODID + ".description.addition.", ""),
        CONFIGURATION(DragonSurvival.MODID + ".configuration.", ".tooltip"),
        ABILITY(DragonSurvival.MODID + ".ability.", ""),
        ABILITY_DESCRIPTION(DragonSurvival.MODID + ".ability.", ".desc"),
        ADVANCEMENT(DragonSurvival.MODID + ".advancement.", ""),
        ADVANCEMENT_DESCRIPTION(DragonSurvival.MODID + ".advancement.", ".desc"),
        SKIN_PART(DragonSurvival.MODID + ".skin_part.", ""),
        GUI(DragonSurvival.MODID + ".gui.", ""),
        KEYBIND(DragonSurvival.MODID + ".keybind.", ""),
        EMOTE(DragonSurvival.MODID + ".emote.", ""),
        BODY(DragonSurvival.MODID + ".body.", ""),
        BODY_DESCRIPTION(DragonSurvival.MODID + ".body.", ".desc"),
        /** When used on {@link String} and no specified key it's expected that the string contains the translation key */
        MISC("", "");

        public final String prefix;
        public final String suffix;

        Type(final String prefix, final String suffix) {
            this.prefix = prefix;
            this.suffix = suffix;
        }

        public String wrap(final String key) {
            return prefix + key + suffix;
        }

        /** To replace the default {@link DragonSurvival#MODID} with an external one */
        public String wrap(final String modid, final String key) {
            return prefix.replace(DragonSurvival.MODID, modid) + key + suffix;
        }

        /** Expects the key in the format of {@link Translation.Type#wrap(String)} */
        public String unwrap(final String key) {
            return key.substring(prefix.length(), key.length() - suffix.length());
        }
    }
}
