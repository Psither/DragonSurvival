package by.dragonsurvivalteam.dragonsurvival.common.capability;

import by.dragonsurvivalteam.dragonsurvival.commands.DragonCommand;
import by.dragonsurvivalteam.dragonsurvival.common.capability.objects.DragonMovementData;
import by.dragonsurvivalteam.dragonsurvival.common.capability.subcapabilities.*;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.AbstractDragonBody;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.AbstractDragonType;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.DragonBodies;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.DragonTypes;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.magic.HunterHandler;
import by.dragonsurvivalteam.dragonsurvival.config.ServerConfig;
import by.dragonsurvivalteam.dragonsurvival.config.server.dragon.DragonBonusConfig;
import by.dragonsurvivalteam.dragonsurvival.mixins.PlayerEndMixin;
import by.dragonsurvivalteam.dragonsurvival.mixins.PlayerStartMixin;
import by.dragonsurvivalteam.dragonsurvival.network.client.ClientProxy;
import by.dragonsurvivalteam.dragonsurvival.registry.DSModifiers;
import by.dragonsurvivalteam.dragonsurvival.util.DragonLevel;
import by.dragonsurvivalteam.dragonsurvival.util.DragonUtils;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import by.dragonsurvivalteam.dragonsurvival.util.ToolUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import javax.annotation.Nullable;

public class DragonStateHandler extends EntityStateHandler {
    @SuppressWarnings("unchecked")
    public final Supplier<SubCap>[] caps = new Supplier[]{this::getSkinData, this::getMagicData, this::getEmoteData, this::getClawToolData};

    // Weapon / tool swap data - START
    /** Used in {@link PlayerStartMixin} and {@link PlayerEndMixin} */
    public ItemStack storedMainHandWeapon = ItemStack.EMPTY;
    public boolean switchedWeapon;

    public ItemStack storedMainHandTool = ItemStack.EMPTY;
    public boolean switchedTool;
    public int switchedToolSlot = -1;
    /** To track the state if a tool swap is triggered within a tool swap (should only swap back if the last tool swap finishes) */
    public int toolSwapLayer;
    // Weapon / tool swap data - END

    /** Translucent rendering in the inventory screen leads to issues (invisible model) */
    public boolean isBeingRenderedInInventory;
    /** Only needs to be updated on effect removal (server -> client) */
    private int hunterStacks;

    public boolean growing = true;

    public boolean treasureResting;
    public int treasureRestTimer;
    public int treasureSleepTimer;

    public int altarCooldown;
    public boolean hasUsedAltar;
    public boolean isInAltar;
    public boolean refreshBody;
    public boolean isJumping;

    /** Last timestamp the server synchronized the player */
    public int lastSync = 0;

    private final DragonMovementData movementData = new DragonMovementData();
    private final ClawInventory clawToolData = new ClawInventory(this);
    private final EmoteCap emoteData = new EmoteCap(this);
    private final MagicCap magicData = new MagicCap(this);
    private final SkinCap skinData = new SkinCap(this);
    private final Map<String, Double> savedDragonSize = new ConcurrentHashMap<>();

    private AbstractDragonType dragonType;
    private AbstractDragonBody dragonBody;

    private int passengerId = -1;
    private boolean isHiding;
    private boolean hasFlight;
    private boolean areWingsSpread;
    private double size;
    private boolean destructionEnabled;

    // Needed to calculate collision damage correctly when flying. See ServerFlightHandler.
    public Vec3 preCollisionDeltaMovement = Vec3.ZERO;

    /** Sets the size, health and base damage */
    public void setSize(double size, Player player) {
        setSize(size);
        DSModifiers.updateSizeModifiers(player);
    }

    private void checkAndRemoveModifier(@Nullable final AttributeInstance attribute, @Nullable final ResourceLocation modifier) {
        if (attribute != null && modifier != null) {
            attribute.removeModifier(modifier);
        }
    }

    public void setFreeLook(boolean isFreeLook) {
        movementData.wasFreeLook = movementData.isFreeLook;
        movementData.isFreeLook = isFreeLook;
    }

    public void setFirstPerson(boolean isFirstPerson) {
        movementData.isFirstPerson = isFirstPerson;
    }

    public void setBite(boolean bite) {
        movementData.bite = bite;
    }

    public void setDesiredMoveVec(Vec2 desiredMoveVec) {
        movementData.desiredMoveVec = desiredMoveVec;
    }

    public void setMovementData(double bodyYaw, double headYaw, double headPitch, Vec3 deltaMovement) {
        movementData.headYawLastFrame = movementData.headYaw;
        movementData.bodyYawLastFrame = movementData.bodyYaw;
        movementData.headPitchLastFrame = movementData.headPitch;
        movementData.deltaMovementLastFrame = movementData.deltaMovement;

        movementData.bodyYaw = bodyYaw;
        movementData.headYaw = headYaw;
        movementData.headPitch = headPitch;
        movementData.deltaMovement = deltaMovement;
    }

    /** Only used for rendering related code - to properly set the size (and update modifiers) use {@link DragonStateHandler#setSize(double, Player)} */
    public void setSize(double size) {
        if (size != this.size) {
            DragonLevel oldLevel = getLevel();
            this.size = size;

            if (oldLevel != getLevel()) {
                if (FMLEnvironment.dist == Dist.CLIENT) {
                    ClientProxy.sendClientData();
                }
            }

            if (dragonType != null) {
                setSavedDragonSize(dragonType.getTypeName(), size);
            }
        }
    }

    public double getSavedDragonSize(final String type) {
        Double value = savedDragonSize.get(type);
        value = value == null ? 0 : value;

        return value;
    }

    public void setSavedDragonSize(final String type, double size) {
        Double value = savedDragonSize.get(type);

        if (size == 0 || (value != null && value == size)) {
            return;
        }

        savedDragonSize.put(type, size);
    }

    public AbstractDragonType getType() {
        return dragonType;
    }

    public AbstractDragonBody getBody() {
        return dragonBody;
    }

    public String getTypeName() {
        if (dragonType == null) {
            return "human";
        }

        return dragonType.getTypeName();
    }

    public String getTypeNameLowerCase() {
        if (dragonType == null) {
            return "human";
        }

        return dragonType.getTypeNameLowerCase();
    }

    public String getSubtypeName() {
        if (dragonType == null) {
            return "human";
        }

        return dragonType.getSubtypeName();
    }

    public void setType(final AbstractDragonType type, Player player) {
        AbstractDragonType oldType = dragonType;
        setType(type);

        if (!DragonUtils.isDragonType(oldType, dragonType)) {
            DSModifiers.updateTypeModifiers(player);
            skinData.skinPreset.initDefaults(dragonType);
        }
    }

    /** Only used for rendering related code - to properly set the type (and update modifiers) use {@link DragonStateHandler#setType(AbstractDragonType, Player)} */
    public void setType(final AbstractDragonType type) {
        if (type == null) {
            dragonType = null;
            return;
        }

        if (DragonUtils.isDragonType(dragonType, type)) {
            return;
        }

        growing = true;
        getMagicData().initAbilities(type);
        dragonType = DragonTypes.newDragonTypeInstance(type.getSubtypeName());
    }

    public void setBody(final AbstractDragonBody body, Player player) {
        AbstractDragonBody oldBody = dragonBody;
        setBody(body);

        if (!DragonUtils.isBodyType(oldBody, dragonBody)) {
            DSModifiers.updateBodyModifiers(player);
        }
    }

    /** Only used for rendering related code - to properly set the body (and update modifiers) use {@link DragonStateHandler#setBody(AbstractDragonBody, Player)} */
    public void setBody(final AbstractDragonBody body) {
        if (body == null) {
            dragonType = null;
            return;
        }

        if (dragonBody == null || !DragonUtils.isBodyType(body, dragonBody)) {
            dragonBody = DragonBodies.newDragonBodyInstance(body.getBodyName());
            refreshBody = true;
        }
    }

    public static DragonLevel getLevel(double size) {
        if (size < 20) {
            return DragonLevel.NEWBORN;
        } else if (size < 30) {
            return DragonLevel.YOUNG;
        } else {
            return DragonLevel.ADULT;
        }
    }

    public DragonLevel getLevel() {
        return getLevel(size);
    }

    /** Determines if the current dragon type can harvest the supplied block (with or without tools) (configured harvest bonuses are taken into account) */
    public boolean canHarvestWithPaw(final BlockState state) {
        if (!isDragon()) {
            return false;
        }

        for (int i = 0; i < 4; i++) {
            ItemStack stack = getClawToolData().getClawsInventory().getItem(i);

            if (stack.isCorrectToolForDrops(state)) {
                return true;
            }
        }

        return canHarvestWithPawNoTools(state);
    }

    /** Determines if the current dragon type can harvest the supplied block without a tool (configured harvest bonuses are taken into account) */
    public boolean canHarvestWithPawNoTools(final BlockState state) {
        if (!isDragon()) {
            return false;
        }

        return getDragonHarvestLevel(state) >= ToolUtils.getRequiredHarvestLevel(state);
    }

    /**
     * Returns the calculated harvest level (based on the unlocked bonuses) (-1 for non-dragons) <br>
     * The unlockable harvest level bonus is only considered if the supplied state is part of {@link AbstractDragonType#harvestableBlocks()} <br>
     * If the supplied state is 'null' then the unlockable bonuses are also considered
     */
    public int getDragonHarvestLevel(@Nullable final BlockState state) {
        if (getType() == null) {
            return -1;
        }

        int bonusLevel = 0;

        if (state == null || state.is(getType().harvestableBlocks())) {
            if (getLevel() == DragonLevel.NEWBORN && DragonBonusConfig.bonusUnlockedAt == DragonLevel.NEWBORN) {
                bonusLevel = DragonBonusConfig.bonusHarvestLevel;
            } else if (getLevel() == DragonLevel.YOUNG && /* valid for NEWBORN & YOUNG */ DragonBonusConfig.bonusUnlockedAt != DragonLevel.ADULT) {
                bonusLevel = DragonBonusConfig.bonusHarvestLevel;
            } else if (getLevel() == DragonLevel.ADULT) {
                bonusLevel = DragonBonusConfig.bonusHarvestLevel;
            }
        }

        return DragonBonusConfig.baseHarvestLevel + bonusLevel;
    }

    public void setPassengerId(int passengerId) {
        this.passengerId = passengerId;
    }

    public void setWingsSpread(boolean areWingsSpread) {
        this.areWingsSpread = areWingsSpread;
    }

    public void setDestructionEnabled(boolean destructionEnabled) {
        this.destructionEnabled = destructionEnabled;
    }

    public void setHasFlight(boolean hasFlight) {
        this.hasFlight = hasFlight;
    }

    public void setIsHiding(boolean isHiding) {
        this.isHiding = isHiding;
    }

    public MagicCap getMagicData() {
        return magicData;
    }

    public DragonMovementData getMovementData() {
        return movementData;
    }

    public double getSize() {
        return size;
    }

    public boolean getDestructionEnabled() {
        return destructionEnabled;
    }

    public boolean isDragon() {
        return dragonType != null;
    }

    public int getPassengerId() {
        return passengerId;
    }

    public EmoteCap getEmoteData() {
        return emoteData;
    }

    public SkinCap getSkinData() {
        return skinData;
    }

    public boolean hasFlight() {
        return hasFlight;
    }

    public boolean isWingsSpread() {
        return hasFlight && areWingsSpread;
    }

    public boolean isHiding() {
        return isHiding;
    }

    public ClawInventory getClawToolData() {
        return clawToolData;
    }

    public CompoundTag serializeNBT(HolderLookup.Provider provider, boolean isSavingForSoul) {
        CompoundTag tag = new CompoundTag();
        tag.putString("type", dragonType != null ? dragonType.getTypeName() : "none");
        tag.putString("subtype", dragonType != null ? dragonType.getSubtypeName() : "none");
        tag.putString("dragonBody", dragonBody != null ? dragonBody.getBodyName() : "none");

        if (isDragon()) {
            tag.put("typeData", dragonType.writeNBT());
            if (dragonBody != null) {
                tag.put("bodyData", dragonBody.writeNBT());
            }

            //Rendering
            DragonMovementData movementData = getMovementData();
            tag.putBoolean("bite", movementData.bite);
            tag.putBoolean("dig", movementData.dig);

            tag.putBoolean("isHiding", isHiding());

            //Spin attack
            tag.putInt("spinCooldown", movementData.spinCooldown);
            tag.putInt("spinAttack", movementData.spinAttack);

            tag.putDouble("size", getSize());
            tag.putBoolean("destructionEnabled", getDestructionEnabled());
            tag.putBoolean("growing", growing);

            tag.putBoolean("isFlying", isWingsSpread());

            tag.putBoolean("resting", treasureResting);
            tag.putInt("restingTimer", treasureRestTimer);
        }

        if (isDragon() || ServerConfig.saveAllAbilities) {
            tag.putBoolean("spinLearned", getMovementData().spinLearned);
            tag.putBoolean("hasWings", hasFlight());
        }

        // Only store the size of the dragon the player is currently in if we are saving for the soul
        if (isSavingForSoul) {
            switch (getTypeName()) {
                case "sea" -> tag.putDouble("seaSize", getSavedDragonSize(DragonTypes.SEA.getTypeName()));
                case "cave" -> tag.putDouble("caveSize", getSavedDragonSize(DragonTypes.CAVE.getTypeName()));
                case "forest" -> tag.putDouble("forestSize", getSavedDragonSize(DragonTypes.FOREST.getTypeName()));
            }
        } else {
            tag.putDouble("seaSize", getSavedDragonSize(DragonTypes.SEA.getTypeName()));
            tag.putDouble("caveSize", getSavedDragonSize(DragonTypes.CAVE.getTypeName()));
            tag.putDouble("forestSize", getSavedDragonSize(DragonTypes.FOREST.getTypeName()));
        }

        for (int i = 0; i < caps.length; i++) {
            if (isSavingForSoul) {
                if (/* Emote Data */ i == 2 || /* Claw Tool Data */ i == 3) {
                    continue;
                }
            }

            tag.put("cap_" + i, caps[i].get().serializeNBT(provider));
        }

        tag.putInt("altarCooldown", altarCooldown);
        tag.putBoolean("usedAltar", hasUsedAltar);

        if (lastPos != null) {
            tag.put("lastPos", Functions.newDoubleList(lastPos.x, lastPos.y, lastPos.z));
        }

        tag.putInt("lastAfflicted", lastAfflicted);
        tag.putBoolean("isJumping", isJumping);

        return tag;
    }

    @Override
    public @UnknownNullability CompoundTag serializeNBT(@NotNull HolderLookup.Provider provider) {
        return serializeNBT(provider, false);
    }

    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag, boolean isLoadingForSoul) {
        if (tag.getAllKeys().contains("subtype"))
            dragonType = DragonTypes.newDragonTypeInstance(tag.getString("subtype"));
        else
            dragonType = DragonTypes.newDragonTypeInstance(tag.getString("type"));

        if (dragonType != null) {
            if (tag.contains("typeData")) {
                dragonType.readNBT(tag.getCompound("typeData"));
            }
        }

        dragonBody = DragonBodies.newDragonBodyInstance(tag.getString("dragonBody"));
        if (dragonBody != null) {
            if (tag.contains("bodyData")) {
                dragonBody.readNBT(tag.getCompound("bodyData"));
            }
        }

        if (isDragon()) {
            setBite(tag.getBoolean("bite"));
            getMovementData().headYawLastFrame = getMovementData().headYaw;
            getMovementData().bodyYawLastFrame = getMovementData().bodyYaw;
            getMovementData().headPitchLastFrame = getMovementData().headPitch;
            setIsHiding(tag.getBoolean("isHiding"));
            getMovementData().dig = tag.getBoolean("dig");

            setWingsSpread(tag.getBoolean("isFlying"));

            getMovementData().spinCooldown = tag.getInt("spinCooldown");
            getMovementData().spinAttack = tag.getInt("spinAttack");

            setSize(tag.getDouble("size"));
            setDestructionEnabled(tag.getBoolean("destructionEnabled"));
            growing = !tag.contains("growing") || tag.getBoolean("growing");

            treasureResting = tag.getBoolean("resting");
            treasureRestTimer = tag.getInt("restingTimer");

            if (getSize() == 0) {
                setSize(DragonLevel.NEWBORN.size);
            }
        }

        if (isDragon() || ServerConfig.saveAllAbilities) {
            getMovementData().spinLearned = tag.getBoolean("spinLearned");
            setHasFlight(tag.getBoolean("hasWings"));
        }

        // Only load the size of the dragon the player is currently in if we are loading for the soul
        if (isLoadingForSoul) {
            switch (getTypeName()) {
                case "sea" -> setSavedDragonSize(DragonTypes.SEA.getTypeName(), tag.getDouble("seaSize"));
                case "cave" -> setSavedDragonSize(DragonTypes.CAVE.getTypeName(), tag.getDouble("caveSize"));
                case "forest" -> setSavedDragonSize(DragonTypes.FOREST.getTypeName(), tag.getDouble("forestSize"));
            }
        } else {
            setSavedDragonSize(DragonTypes.SEA.getTypeName(), tag.getDouble("seaSize"));
            setSavedDragonSize(DragonTypes.CAVE.getTypeName(), tag.getDouble("caveSize"));
            setSavedDragonSize(DragonTypes.FOREST.getTypeName(), tag.getDouble("forestSize"));
        }

        for (int i = 0; i < caps.length; i++) {
            if (isLoadingForSoul) {
                if (i == 2) {
                    continue;
                }
            }

            if (isLoadingForSoul) {
                if (i == 3) {
                    continue;
                }
            }

            if (tag.contains("cap_" + i)) {
                caps[i].get().deserializeNBT(provider, (CompoundTag) tag.get("cap_" + i));
            }
        }

        altarCooldown = tag.getInt("altarCooldown");
        hasUsedAltar = tag.getBoolean("usedAltar");

        if (tag.contains("lastPos")) {
            ListTag listnbt = tag.getList("lastPos", 6);
            lastPos = new Vec3(listnbt.getDouble(0), listnbt.getDouble(1), listnbt.getDouble(2));
        }

        lastAfflicted = tag.getInt("lastAfflicted");
        refreshBody = true;
        isJumping = tag.getBoolean("isJumping");

        getSkinData().compileSkin();
    }

    @Override
    public void deserializeNBT(@NotNull HolderLookup.Provider provider, CompoundTag tag) {
        deserializeNBT(provider, tag, false);
    }

    public void revertToHumanForm(Player player, boolean isRevertingFromSoul) {
        // Don't set the saved dragon size if we are reverting from a soul, as we already are storing the size of the dragon in the soul
        if (ServerConfig.saveGrowthStage && !isRevertingFromSoul) {
            this.setSavedDragonSize(this.getTypeName(), this.getSize());
        }

        // Drop everything in your claw slots
        DragonCommand.reInsertClawTools(player, this);

        this.setType(null);
        this.setBody(null, player);
        this.setSize(20F, player);
        this.setIsHiding(false);

        if (!ServerConfig.saveAllAbilities) {
            this.getMovementData().spinLearned = false;
            this.setHasFlight(false);
        }

        this.altarCooldown = Functions.secondsToTicks(ServerConfig.altarUsageCooldown);
        this.hasUsedAltar = true;
        this.isJumping = false;
    }

    // --- Hunter handler --- //

    public void modifyHunterStacks(int modification) {
        hunterStacks = Math.clamp(hunterStacks + modification, 0, HunterHandler.MAX_HUNTER_STACKS);
    }

    public boolean hasMaxHunterStacks() {
        return hunterStacks == HunterHandler.MAX_HUNTER_STACKS;
    }

    public boolean hasHunterStacks() {
        return hunterStacks > 0;
    }

    public void clearHunterStacks() {
        hunterStacks = 0;
    }

    public int getHunterStacks() {
        return hunterStacks;
    }
}