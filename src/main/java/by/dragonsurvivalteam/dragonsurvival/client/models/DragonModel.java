package by.dragonsurvivalteam.dragonsurvival.client.models;

import by.dragonsurvivalteam.dragonsurvival.client.render.ClientDragonRenderer;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.DragonEditorHandler;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.objects.SkinPreset.SkinAgeGroup;
import by.dragonsurvivalteam.dragonsurvival.client.util.RenderingUtils;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.capability.objects.DragonMovementData;
import by.dragonsurvivalteam.dragonsurvival.common.entity.DragonEntity;
import by.dragonsurvivalteam.dragonsurvival.config.ClientConfig;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.DragonBody;
import by.dragonsurvivalteam.dragonsurvival.util.AnimationUtils;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.loading.math.MathParser;
import software.bernie.geckolib.model.GeoModel;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static by.dragonsurvivalteam.dragonsurvival.DragonSurvival.MODID;

public class DragonModel extends GeoModel<DragonEntity> {
    private final ResourceLocation defaultTexture = ResourceLocation.fromNamespaceAndPath(MODID, "textures/dragon/cave_newborn.png");
    private final ResourceLocation model = ResourceLocation.fromNamespaceAndPath(MODID, "geo/dragon_model.geo.json");
    private ResourceLocation overrideTexture;
    private CompletableFuture<Void> textureRegisterFuture = CompletableFuture.completedFuture(null);

    /** Factor to multiply the delta yaw and pitch by, needed for scaling for the animations */
    private static final double DELTA_YAW_PITCH_FACTOR = 0.2;

    /** Factor to multiply the delta movement by, needed for scaling for the animations */
    private static final double DELTA_MOVEMENT_FACTOR = 10;

    /**
     * TODO Body Types Update
     * Required:
     * - tips for body types like for magic abilities
     * <p>
     * Extras:
     * - customization.json - Ability to disallow some details in the editor for some Body Types (for example, wing details are not required for wingless).
     * - emotes.json - Ability to disallow some emotions for certain Body Types.
     */

    @Override
    public void applyMolangQueries(final AnimationState<DragonEntity> animationState, double currentTick) {
        super.applyMolangQueries(animationState, currentTick);
        DragonEntity dragon = animationState.getAnimatable();
        Player player = dragon.getPlayer();

        if (player == null) {
            return;
        }

        float deltaTick = Minecraft.getInstance().getTimer().getRealtimeDeltaTicks();
        float partialDeltaTick = Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(false);
        DragonStateHandler handler = DragonStateProvider.getData(player);
        DragonMovementData md = handler.getMovementData();

        MathParser.setVariable("query.head_yaw", () -> md.headYaw);
        MathParser.setVariable("query.head_pitch", () -> md.headPitch);

        double gravity = player.getAttributeValue(Attributes.GRAVITY);
        MathParser.setVariable("query.gravity", () -> gravity);


        double bodyYawAvg;
        double headYawAvg;
        double headPitchAvg;
        double verticalVelocityAvg;
        if (!ClientDragonRenderer.isOverridingMovementData) {
            double bodyYawChange = Functions.angleDifference(md.bodyYaw, md.bodyYawLastFrame) / deltaTick * DELTA_YAW_PITCH_FACTOR;
            double headYawChange = Functions.angleDifference(md.headYaw, md.headYawLastFrame) / deltaTick * DELTA_YAW_PITCH_FACTOR;
            double headPitchChange = Functions.angleDifference(md.headPitch, md.headPitchLastFrame) / deltaTick * DELTA_YAW_PITCH_FACTOR;

            double verticalVelocity = Mth.lerp(partialDeltaTick, md.deltaMovementLastFrame.y, md.deltaMovement.y) * DELTA_MOVEMENT_FACTOR;
            // Factor in the vertical angle of the dragon so that the vertical velocity is scaled down when the dragon is looking up or down
            // Ideally, we would just use more precise data (factor in the full rotation of the player in our animations)
            // but this works pretty well in most situations the player will encounter
            verticalVelocity *= 1 - Mth.abs(Mth.clampedMap(md.prevXRot, -90, 90, -1, 1));

            float deltaTickFor60FPS = AnimationUtils.getDeltaTickFor60FPS();
            // Accumulate them in the history
            while(dragon.bodyYawHistory.size() > 10 / deltaTickFor60FPS ) {
                dragon.bodyYawHistory.removeFirst();
            }
            dragon.bodyYawHistory.add(bodyYawChange);

            while(dragon.headYawHistory.size() > 10 / deltaTickFor60FPS ) {
                dragon.headYawHistory.removeFirst();
            }
            dragon.headYawHistory.add(headYawChange);

            while(dragon.headPitchHistory.size() > 10 / deltaTickFor60FPS ) {
                dragon.headPitchHistory.removeFirst();
            }
            dragon.headPitchHistory.add(headPitchChange);

            // Handle the clear case (see DragonEntity.java)
            if(dragon.clearVerticalVelocity) {
                dragon.verticalVelocityHistory.clear();
                while(dragon.verticalVelocityHistory.size() < 10 / deltaTickFor60FPS) {
                    dragon.verticalVelocityHistory.add(0.);
                }
            }

            while(dragon.verticalVelocityHistory.size() > 10 / deltaTickFor60FPS ) {
                dragon.verticalVelocityHistory.removeFirst();
            }
            dragon.verticalVelocityHistory.add(verticalVelocity);

            bodyYawAvg = dragon.bodyYawHistory.stream().mapToDouble(Double::doubleValue).average().orElse(0);
            headYawAvg = dragon.headYawHistory.stream().mapToDouble(Double::doubleValue).average().orElse(0);
            headPitchAvg = dragon.headPitchHistory.stream().mapToDouble(Double::doubleValue).average().orElse(0);
            verticalVelocityAvg = dragon.verticalVelocityHistory.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        } else {
            bodyYawAvg = 0;
            headYawAvg = 0;
            headPitchAvg = 0;
            verticalVelocityAvg = 0;
        }

        double currentBodyYawChange = MathParser.getVariableFor("query.body_yaw_change").get();
        double currentHeadPitchChange = MathParser.getVariableFor("query.head_pitch_change").get();
        double currentHeadYawChange = MathParser.getVariableFor("query.head_yaw_change").get();

        // Handle the clear case (see DragonEntity.java)
        double currentTailMotionUp;
        if(dragon.clearVerticalVelocity) {
            currentTailMotionUp = 0;
            dragon.clearVerticalVelocity = false;
        } else {
            currentTailMotionUp = MathParser.getVariableFor("query.tail_motion_up").get();
        }

        double lerpRate = Math.min(1., deltaTick);
        MathParser.setVariable("query.body_yaw_change", () -> Mth.lerp(lerpRate, currentBodyYawChange, bodyYawAvg));
        MathParser.setVariable("query.head_yaw_change", () -> Mth.lerp(lerpRate, currentHeadPitchChange, headYawAvg));
        MathParser.setVariable("query.head_pitch_change", () -> Mth.lerp(lerpRate, currentHeadYawChange, headPitchAvg));
        MathParser.setVariable("query.tail_motion_up", () -> Mth.lerp(lerpRate, currentTailMotionUp, -verticalVelocityAvg));
    }

    @Override
    public ResourceLocation getModelResource(final DragonEntity dragon) {
        return model;
    }

    @Override
    public ResourceLocation getTextureResource(final DragonEntity dragon) {
        if (overrideTexture != null) {
            return overrideTexture;
        }

        Player player;

        if (dragon.overrideUUIDWithLocalPlayerForTextureFetch) {
            player = Minecraft.getInstance().player;
        } else {
            player = dragon.getPlayer();
        }

        if (player == null) {
            return defaultTexture;
        }

        DragonStateHandler handler = DragonStateProvider.getData(player);
        SkinAgeGroup ageGroup = handler.getSkinData().skinPreset.skinAges.get(handler.getLevel()).get();

        if (handler.getSkinData().blankSkin) {
            return ResourceLocation.fromNamespaceAndPath(MODID, "textures/dragon/blank_skin_" + handler.getTypeNameLowerCase() + ".png");
        }

        if (handler.getSkinData().recompileSkin.get(handler.getLevel())) {
            if (ClientConfig.forceCPUSkinGeneration) {
                if (textureRegisterFuture.isDone()) {
                    CompletableFuture<List<Pair<NativeImage, ResourceLocation>>> imageGenerationFuture = DragonEditorHandler.generateSkinTextures(dragon);
                    textureRegisterFuture = imageGenerationFuture.thenRunAsync(() -> {
                        handler.getSkinData().isCompiled.put(handler.getLevel(), true);
                        handler.getSkinData().recompileSkin.put(handler.getLevel(), false);
                        for (Pair<NativeImage, ResourceLocation> pair : imageGenerationFuture.join()) {
                            RenderingUtils.uploadTexture(pair.getFirst(), pair.getSecond());
                        }
                    }, Minecraft.getInstance());
                }
            } else {
                DragonEditorHandler.generateSkinTexturesGPU(dragon);
                handler.getSkinData().isCompiled.put(handler.getLevel(), true);
                handler.getSkinData().recompileSkin.put(handler.getLevel(), false);
            }
        }

        // Show the default skin while we are compiling if we haven't already compiled the skin
        if (ageGroup.defaultSkin || !handler.getSkinData().isCompiled.get(handler.getLevel())) {
            return ResourceLocation.fromNamespaceAndPath(MODID, "textures/dragon/" + handler.getTypeNameLowerCase() + "_" + handler.getLevel().getRawName() + ".png");
        }

        String uuid = player.getStringUUID();
        return ResourceLocation.fromNamespaceAndPath(MODID, "dynamic_normal_" + uuid + "_" + handler.getLevel().name);
    }

    @Override
    public ResourceLocation getAnimationResource(final DragonEntity dragon) {
        Player player = dragon.getPlayer();
        return getAnimationResource(player);
    }

    @Override
    public RenderType getRenderType(final DragonEntity animatable, final ResourceLocation texture) {
        Player player = animatable.getPlayer();

        if (player != null) {
            DragonStateHandler data = DragonStateProvider.getData(player);

            if (data.hasHunterStacks() && !data.isBeingRenderedInInventory) {
                return RenderType.itemEntityTranslucentCull(texture);
            }
        }

        return RenderType.entityCutout(texture);
    }

    public void setOverrideTexture(final ResourceLocation overrideTexture) {
        this.overrideTexture = overrideTexture;
    }

    public static ResourceLocation getAnimationResource(final Player player) {
        if (player != null) {
            DragonStateHandler handler = DragonStateProvider.getData(player);
            Holder<DragonBody> body = handler.getBody();

            if (body != null) {
                //noinspection DataFlowIssue -> key is present
                ResourceLocation location = body.getKey().location();
                return ResourceLocation.fromNamespaceAndPath(location.getNamespace(), String.format("animations/dragon_%s.json", location.getPath()));
            }
        }

        return ResourceLocation.fromNamespaceAndPath(MODID, "animations/dragon_center.json");
    }
}