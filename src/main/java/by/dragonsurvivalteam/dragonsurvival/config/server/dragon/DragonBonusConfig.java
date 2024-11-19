package by.dragonsurvivalteam.dragonsurvival.config.server.dragon;

import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigOption;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigRange;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigSide;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.util.DragonLevel;

public class DragonBonusConfig {
    // FIXME :: remove this config - doubt it's even correctly checked in all places
    @Translation(key = "bonuses_enabled", type = Translation.Type.CONFIGURATION, comments = "If enabled all dragon bonuses are disabled")
    @ConfigOption(side = ConfigSide.SERVER, category = "bonuses", key = "bonuses_enabled")
    public static Boolean bonusesEnabled = true;

    @Translation(key = "health_adjustments", type = Translation.Type.CONFIGURATION, comments = "If enabled health from dragons will scale with their size")
    @ConfigOption(side = ConfigSide.SERVER, category = "bonuses", key = "health_adjustments")
    public static Boolean healthAdjustments = true;

    @Translation(key = "damage_bonus", type = Translation.Type.CONFIGURATION, comments = "If enabled dragons will gain a damage modifier")
    @ConfigOption(side = ConfigSide.SERVER, category = "bonuses", key = "damage_bonus")
    public static Boolean isDamageBonusEnabled = true;

    // FIXME :: i don't think this gets checked at all the points it would be relevant
    // FIXME :: was the original intention for this only affect block breaking if no tools are in the claw slot or mainhand (i.e. mining with paw)?
    @Translation(key = "claws_as_tools", type = Translation.Type.CONFIGURATION, comments = "N/A")
    @ConfigOption(side = ConfigSide.SERVER, category = "bonuses", key = "claws_as_tools")
    public static Boolean clawsAreTools = true;

    @Translation(key = "bonus_unlocks_at", type = Translation.Type.CONFIGURATION, comments = "Determines at which level the harvest and break speed bonuses are unlocked")
    @ConfigOption(side = ConfigSide.SERVER, category = "bonuses", key = "bonus_unlocks_at")
    public static DragonLevel bonusUnlockedAt = DragonLevel.YOUNG;

    @ConfigRange(min = 0, max = 100)
    @Translation(key = "speed_up_effect_level", type = Translation.Type.CONFIGURATION, comments = "Determines the level of the speed effect gained when a dragon is on its speed up block type - disabled if set to 0")
    @ConfigOption(side = ConfigSide.SERVER, category = "bonuses", key = "speed_up_effect_level")
    public static Integer speedupEffectLevel = 2;

    // --- Damage bonus --- //

    @ConfigRange(min = 0.0, max = 100.0)
    @Translation(key = "newborn_damage_bonus", type = Translation.Type.CONFIGURATION, comments = "Damage bonus for newborn dragons")
    @ConfigOption(side = ConfigSide.SERVER, category = "bonuses", key = "newborn_damage_bonus")
    public static Double newbornBonusDamage = 1.0;

    @ConfigRange(min = 0.0, max = 100.0)
    @Translation(key = "young_damage_bonus", type = Translation.Type.CONFIGURATION, comments = "Damage bonus for young dragons")
    @ConfigOption(side = ConfigSide.SERVER, category = "bonuses", key = "young_damage_bonus")
    public static Double youngBonusDamage = 2.0;

    @ConfigRange(min = 0.0, max = 100.0)
    @Translation(key = "adult_damage_bonus", type = Translation.Type.CONFIGURATION, comments = "Damage bonus for adult dragons")
    @ConfigOption(side = ConfigSide.SERVER, category = "bonuses", key = "adult_damage_bonus")
    public static Double adultBonusDamage = 3.0;

    // --- Jump height bonus --- //

    @ConfigRange(min = 0.0, max = 0.9)
    @Translation(key = "newborn_jump_bonus", type = Translation.Type.CONFIGURATION, comments = "Jump height bonus for newborn dragons")
    @ConfigOption(side = ConfigSide.SERVER, category = "bonuses", key = "newborn_jump_bonus")
    public static Double newbornJump = 0.025;

    @ConfigRange(min = 0.0, max = 0.9)
    @Translation(key = "young_jump_bonus", type = Translation.Type.CONFIGURATION, comments = "Jump height bonus for young dragons")
    @ConfigOption(side = ConfigSide.SERVER, category = "bonuses", key = "young_jump_bonus")
    public static Double youngJump = 0.05;

    @ConfigRange(min = 0.0, max = 0.9)
    @Translation(key = "adult_jump_bonus", type = Translation.Type.CONFIGURATION, comments = "Jump height bonus for adult dragons")
    @ConfigOption(side = ConfigSide.SERVER, category = "bonuses", key = "adult_jump_bonus")
    public static Double adultJump = 0.1;

    // --- Step height bonus --- //

    @ConfigRange(min = 0.0, max = 10.0)
    @Translation(key = "newborn_step_height_bonus", type = Translation.Type.CONFIGURATION, comments = "Step height bonus for newborn dragons")
    @ConfigOption(side = ConfigSide.SERVER, category = "bonuses", key = "newborn_step_height_bonus")
    public static Double newbornStepHeight = 0.0;

    @ConfigRange(min = 0.0, max = 10.0)
    @Translation(key = "young_step_height_bonus", type = Translation.Type.CONFIGURATION, comments = "Step height bonus for young dragons")
    @ConfigOption(side = ConfigSide.SERVER, category = "bonuses", key = "young_step_height_bonus")
    public static Double youngStepHeight = 0.25;

    @ConfigRange(min = 0.0, max = 10.0)
    @Translation(key = "adult_step_height_bonus", type = Translation.Type.CONFIGURATION, comments = "Step height bonus for adult dragons")
    @ConfigOption(side = ConfigSide.SERVER, category = "bonuses", key = "adult_step_height_bonus")
    public static Double adultStepHeight = 0.5;

    // --- Harvest level bonus --- //

    @ConfigRange(min = 0, max = 4)
    @Translation(key = "base_harvest_level", type = Translation.Type.CONFIGURATION, comments = "Harvest level bonus for dragons (independent of the required tool for the block)")
    @ConfigOption(side = ConfigSide.SERVER, category = "bonuses", key = "base_harvest_level")
    public static Integer baseHarvestLevel = 0;

    @ConfigRange(min = 0, max = 4)
    @Translation(key = "harvest_level_bonus", type = Translation.Type.CONFIGURATION, comments = {
            "Harvest level bonus for dragons (only applicable to blocks the dragon is effective against)",
            "This only applies if the required size for harvest and break speed bonuses is reached"
    })
    @ConfigOption(side = ConfigSide.SERVER, category = "bonuses", key = "harvest_level_bonus")
    public static Integer bonusHarvestLevel = 1;

    // --- Break speed bonus --- //

    @ConfigRange(min = 1, max = 10)
    @Translation(key = "break_speed_multiplier", type = Translation.Type.CONFIGURATION, comments = {
            "Break speed bonus for blocks the dragon is effective against",
            "This only applies if the required size for harvest and break speed bonuses is reached",
            "Adult dragons have a separate config entry for their bonus"
    })
    @ConfigOption(side = ConfigSide.SERVER, category = "bonuses", key = "break_speed_multiplier")
    public static Float bonusBreakSpeed = 2f;

    @ConfigRange(min = 1, max = 10) // FIXME :: put the stage at the start (e.g. adult_break_speed_multiplier)
    @Translation(key = "break_speed_multiplier_adult", type = Translation.Type.CONFIGURATION, comments = {
            "Break speed bonus for blocks the dragon is effective against",
            "This config is only applicable to adult dragons"
    })
    @ConfigOption(side = ConfigSide.SERVER, category = "bonuses", key = "break_speed_multiplier_adult")
    public static Float bonusBreakSpeedAdult = 2.5f;

    @ConfigRange(min = 1, max = 10)
    @Translation(key = "base_break_speed_multiplier_adult", type = Translation.Type.CONFIGURATION, comments = {
            "Break speed bonus for all blocks - for effective blocks the break speed of the bonus break speed config will be used",
            "This config is only applicable to adult dragons"
    })
    @ConfigOption(side = ConfigSide.SERVER, category = "bonuses", key = "base_break_speed_multiplier_adult")
    public static Float baseBreakSpeedAdult = 1.5f;

    @ConfigRange(min = 1, max = 10)
    @Translation(key = "break_speed_reduction", type = Translation.Type.CONFIGURATION, comments = {"The base break speed / bonus break speed will be divided by this value if an effective claw tool is present for the block"})
    @ConfigOption(side = ConfigSide.SERVER, category = "bonuses", key = "break_speed_reduction")
    public static Float bonusBreakSpeedReduction = 2f;
}
