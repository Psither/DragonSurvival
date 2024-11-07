package by.dragonsurvivalteam.dragonsurvival.magic.abilities.VoltaicDragon;

import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.AbstractDragonType;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.DragonTypes;
import by.dragonsurvivalteam.dragonsurvival.magic.abilities.SeaDragon.active.BallLightningAbility;
import by.dragonsurvivalteam.dragonsurvival.magic.common.RegisterDragonAbility;

@RegisterDragonAbility
public class FulminationAbility extends BallLightningAbility {
    @Override
    public AbstractDragonType getDragonType() { return DragonTypes.VOLTAIC; }
}
