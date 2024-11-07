package by.dragonsurvivalteam.dragonsurvival.common.entity.goals;

import by.dragonsurvivalteam.dragonsurvival.registry.DragonEffects;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public class FreezeSolidGoal extends Goal {
    Mob mob;
    public FreezeSolidGoal(Mob mob) {
        this.mob = mob;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP, Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        return mob.hasEffect(DragonEffects.FULLY_FROZEN);
    }
}
