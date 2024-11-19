package by.dragonsurvivalteam.dragonsurvival.client.render;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.client.models.DragonModel;
import by.dragonsurvivalteam.dragonsurvival.client.skins.DragonSkins;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.capability.objects.DragonMovementData;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.AbstractDragonType;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.DragonTypes;
import by.dragonsurvivalteam.dragonsurvival.common.entity.DragonEntity;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.DragonSizeHandler;
import by.dragonsurvivalteam.dragonsurvival.config.ClientConfig;
import by.dragonsurvivalteam.dragonsurvival.config.ServerConfig;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigOption;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigSide;
import by.dragonsurvivalteam.dragonsurvival.input.Keybind;
import by.dragonsurvivalteam.dragonsurvival.magic.DragonAbilities;
import by.dragonsurvivalteam.dragonsurvival.magic.common.active.BreathAbility;
import by.dragonsurvivalteam.dragonsurvival.mixins.client.EntityRendererAccessor;
import by.dragonsurvivalteam.dragonsurvival.mixins.client.LivingRendererAccessor;
import by.dragonsurvivalteam.dragonsurvival.network.flight.SyncDeltaMovement;
import by.dragonsurvivalteam.dragonsurvival.network.player.SyncDragonMovement;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEffects;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEntities;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.server.handlers.ServerFlightHandler;
import by.dragonsurvivalteam.dragonsurvival.util.DragonLevel;
import by.dragonsurvivalteam.dragonsurvival.util.DragonUtils;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.ParrotOnShoulderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.joml.Vector3f;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.util.RenderUtil;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

@EventBusSubscriber(Dist.CLIENT)
public class ClientDragonRenderer {
    public static DragonModel dragonModel = new DragonModel();

    /**
     * Used for inventory rendering - when set to true changed movement data will not be tracked <br>
     * See {@link ClientDragonRenderer#setDragonMovementData(Player, float)} and {@link DragonModel#applyMolangQueries(AnimationState, double)}
     */
    public static boolean isOverridingMovementData = false;

    /** Instances used for rendering third-person dragon models */
    public static ConcurrentHashMap<Integer, AtomicReference<DragonEntity>> playerDragonHashMap = new ConcurrentHashMap<>(20);

    @Translation(key = "render_dragon_in_first_person", type = Translation.Type.CONFIGURATION, comments = "If enabled the dragon body will be visible in first person")
    @ConfigOption(side = ConfigSide.CLIENT, category = "rendering", key = "render_dragon_in_first_person")
    public static Boolean renderInFirstPerson = true;

    @Translation(key = "render_first_person_flight", type = Translation.Type.CONFIGURATION, comments = "If enabled the dragon body will be visible in first person while flying")
    @ConfigOption(side = ConfigSide.CLIENT, category = "rendering", key = "render_first_person_flight")
    public static Boolean renderFirstPersonFlight = false;

    @Translation(key = "render_items_in_mouth", type = Translation.Type.CONFIGURATION, comments = {"If enabled held items will be rendered neat the mouth of the dragon", "If disabled held items will be displayed on the side of the dragon"})
    @ConfigOption(side = ConfigSide.CLIENT, category = "rendering", key = "render_items_in_mouth")
    public static Boolean renderItemsInMouth = false;

    @Translation(key = "render_held_item", type = Translation.Type.CONFIGURATION, comments = "If enabled items will be rendered for dragons while in third person mode")
    @ConfigOption(side = ConfigSide.CLIENT, category = "rendering", key = "render_held_item")
    public static boolean renderHeldItem = true;

    @Translation(key = "render_dragon_claws", type = Translation.Type.CONFIGURATION, comments = "If enabled dragon claws and teeth will have an overlay depending on the items in the claw slots")
    @ConfigOption(side = ConfigSide.CLIENT, category = "rendering", key = "render_dragon_claws")
    public static Boolean renderDragonClaws = true;

    @Translation(key = "render_newborn_skin", type = Translation.Type.CONFIGURATION, comments = "If enabled your custom newborn dragon skin will be rendered")
    @ConfigOption(side = ConfigSide.CLIENT, category = "rendering", key = "render_newborn_skin")
    public static Boolean renderNewbornSkin = true;

    @Translation(key = "render_young_skin", type = Translation.Type.CONFIGURATION, comments = "If enabled your custom young dragon skin will be rendered")
    @ConfigOption(side = ConfigSide.CLIENT, category = "rendering", key = "render_young_skin")
    public static Boolean renderYoungSkin = true;

    @Translation(key = "render_adult_skin", type = Translation.Type.CONFIGURATION, comments = "If enabled your custom adult dragon skin will be rendered")
    @ConfigOption(side = ConfigSide.CLIENT, category = "rendering", key = "render_adult_skin")
    public static Boolean renderAdultSkin = true;

    @Translation(key = "render_other_players_custom_skins", type = Translation.Type.CONFIGURATION, comments = "If enabled custom skins of other players will be rendered")
    @ConfigOption(side = ConfigSide.CLIENT, category = "rendering", key = "render_other_players_custom_skins")
    public static Boolean renderOtherPlayerSkins = true;

    @Translation(key = "dragon_name_tags", type = Translation.Type.CONFIGURATION, comments = "If enabled name tags will be shown for dragons")
    @ConfigOption(side = ConfigSide.CLIENT, category = "rendering", key = "dragon_name_tags")
    public static Boolean dragonNameTags = false;

    /** Show breath hit range when hitboxes are being rendered */
    @SubscribeEvent
    public static void renderBreathHitBox(final RenderLevelStageEvent event) {
        if (ClientConfig.renderBreathRange && event.getStage() == RenderLevelStageEvent.Stage.AFTER_SOLID_BLOCKS && Minecraft.getInstance().getEntityRenderDispatcher().shouldRenderHitBoxes()) {
            LocalPlayer localPlayer = Minecraft.getInstance().player;

            if (localPlayer == null) {
                return;
            }

            DragonStateHandler handler = DragonStateProvider.getData(localPlayer);

            if (!handler.isDragon()) {
                return;
            }

            VertexConsumer buffer = Minecraft.getInstance().renderBuffers().bufferSource().getBuffer(RenderType.LINES);
            Vec3 camera = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();

            PoseStack poseStack = event.getPoseStack();
            poseStack.pushPose();
            poseStack.translate(-camera.x(), -camera.y(), -camera.z());

            int range = BreathAbility.calculateCurrentBreathRange(handler.getSize());
            AbstractDragonType dragonType = handler.getType();

            int red = DragonUtils.isType(dragonType, DragonTypes.CAVE) ? 1 : 0;
            int green = DragonUtils.isType(dragonType, DragonTypes.FOREST) ? 1 : 0;
            int blue = DragonUtils.isType(dragonType, DragonTypes.SEA) ? 1 : 0;

            LevelRenderer.renderLineBox(poseStack, buffer, DragonAbilities.calculateBreathArea(localPlayer, handler, range), red, green, blue, 1);

            /* Draw the area which will affect blocks
            Pair<BlockPos, Direction> data = DragonAbilities.breathStartPosition(localPlayer, red == 1 ? new NetherBreathAbility() : green == 1 ? new ForestBreathAbility() : new StormBreathAbility(), range);
            BlockPos startPosition = data.getFirst();

            if (startPosition != null) {
                AABB blockRange = new AABB(
                        startPosition.getX() - (double) range / 2,
                        startPosition.getY() - (double) range / 2,
                        startPosition.getZ() - (double) range / 2,
                        startPosition.getX() + (double) range / 2,
                        startPosition.getY() + (double) range / 2,
                        startPosition.getZ() + (double) range / 2
                );

                LevelRenderer.renderLineBox(poseStack, buffer, blockRange, 1, 1, 1, 1);
            }
            */

            poseStack.popPose();
        }
    }

    /**
     * Amount of client ticks the player model will not be rendered if the player was recently a dragon (to avoid player model pop-up after respawning)
     */
    private static final int MAX_DELAY = 10;
    private static int renderDelay;

    @SubscribeEvent(receiveCanceled = true)
    public static void cancelNameplatesFromDummyEntities(RenderNameTagEvent renderNameplateEvent) {
        Entity entity = renderNameplateEvent.getEntity();
        if (entity.getType() == DSEntities.DRAGON.get()) {
            renderNameplateEvent.setCanRender(TriState.FALSE);
        }
    }

    /**
     * Called for every player
     */
    @SubscribeEvent
    public static void thirdPersonPreRender(final RenderPlayerEvent.Pre renderPlayerEvent) {
        if (!(renderPlayerEvent.getEntity() instanceof AbstractClientPlayer player)) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        DragonStateHandler handler = DragonStateProvider.getData(player);

        if (!playerDragonHashMap.containsKey(player.getId())) {
            DragonEntity dummyDragon = DSEntities.DRAGON.get().create(player.level());
            dummyDragon.playerId = player.getId();
            playerDragonHashMap.put(player.getId(), new AtomicReference<>(dummyDragon));
        }

        if (handler.isDragon()) {
            if (player == minecraft.player) {
                renderDelay = MAX_DELAY;
            }

            renderPlayerEvent.setCanceled(true);
            if (!isOverridingMovementData) {
                setDragonMovementData(player, Minecraft.getInstance().getTimer().getRealtimeDeltaTicks());
            }
            float partialRenderTick = renderPlayerEvent.getPartialTick();
            float yaw = player.getViewYRot(partialRenderTick);

            DragonLevel dragonStage = handler.getLevel();
            ResourceLocation texture = DragonSkins.getPlayerSkin(player, handler.getType(), dragonStage);
            PoseStack poseStack = renderPlayerEvent.getPoseStack();

            try {
                poseStack.pushPose();

                Vector3f lookVector = Functions.getDragonCameraOffset(player);
                poseStack.translate(-lookVector.x(), lookVector.y(), -lookVector.z());

                double size = handler.getSize();
                EntityRenderer<? extends Player> playerRenderer = renderPlayerEvent.getRenderer();
                int eventLight = renderPlayerEvent.getPackedLight();
                final MultiBufferSource renderTypeBuffer = renderPlayerEvent.getMultiBufferSource();

                if (dragonNameTags && player != minecraft.player) {
                    RenderNameTagEvent renderNameplateEvent = new RenderNameTagEvent(player, player.getDisplayName(), playerRenderer, poseStack, renderTypeBuffer, eventLight, partialRenderTick);
                    NeoForge.EVENT_BUS.post(renderNameplateEvent);

                    // TODO: Test this, we might not need shouldShowName
                    if (renderNameplateEvent.canRender().isTrue() && ((LivingRendererAccessor) playerRenderer).dragonSurvival$callShouldShowName(player)) {
                        ((EntityRendererAccessor) playerRenderer).dragonSurvival$renderNameTag(player, renderNameplateEvent.getContent(), poseStack, renderTypeBuffer, eventLight, partialRenderTick);
                    }
                }

                poseStack.mulPose(Axis.YN.rotationDegrees((float) handler.getMovementData().bodyYaw));

                // This is some arbitrary scaling that was created back when the maximum size was hard capped at 40. Touching it will cause the render to desync from the hitbox.
                AttributeInstance attributeInstance = player.getAttribute(Attributes.SCALE);
                float scale = (float) (Math.max(size / 40.0D, 0.4D) * (attributeInstance != null ? attributeInstance.getValue() : 1.0D));
                poseStack.scale(scale, scale, scale);

                ((EntityRendererAccessor) renderPlayerEvent.getRenderer()).dragonSurvival$setShadowRadius((float) ((3.0F * size + 62.0F) / 260.0F));
                DragonEntity playerAsDragon = playerDragonHashMap.get(player.getId()).get(); // What will be rendered in place of the human player model
                EntityRenderer<? super DragonEntity> dragonRenderer = minecraft.getEntityRenderDispatcher().getRenderer(playerAsDragon);
                dragonModel.setOverrideTexture(texture);

                if (player.isCrouching() && handler.isWingsSpread() && !player.onGround()) {
                    poseStack.translate(0, -0.15, 0);
                } else if (player.isCrouching()) {
                    if (size > ServerConfig.DEFAULT_MAX_GROWTH_SIZE) {
                        poseStack.translate(0, 0.045, 0);
                    } else {
                        poseStack.translate(0, 0.325 - size / DragonLevel.ADULT.size * 0.140, 0);
                    }
                } else if (player.isSwimming() || player.isAutoSpinAttack() || handler.isWingsSpread() && !player.onGround() && !player.isInWater() && !player.isInLava()) {
                    if (size > ServerConfig.DEFAULT_MAX_GROWTH_SIZE) {
                        poseStack.translate(0, -0.55, 0);
                    } else {
                        poseStack.translate(0, -0.15 - size / DragonLevel.ADULT.size * 0.2, 0);
                    }
                }

                if (!player.isInvisible()) {
                    boolean isPlayerGliding = ServerFlightHandler.isGliding(player);
                    Entity playerVehicle = player.getVehicle();

                    if (isPlayerGliding || (player.isPassenger() && DragonStateProvider.isDragon(playerVehicle) && ServerFlightHandler.isGliding((Player) playerVehicle))) {
                        float upRot;

                        if (isPlayerGliding) {
                            upRot = Mth.clamp((float) (player.getDeltaMovement().y * 20), -80, 80);
                        } else {
                            upRot = Mth.clamp((float) (playerVehicle.getDeltaMovement().y * 20), -80, 80);
                        }

                        playerAsDragon.prevXRot = Mth.lerp(0.1F, playerAsDragon.prevXRot, upRot);
                        playerAsDragon.prevXRot = Mth.clamp(playerAsDragon.prevXRot, -80, 80);

                        handler.getMovementData().prevXRot = playerAsDragon.prevXRot;

                        if (Float.isNaN(playerAsDragon.prevXRot)) {
                            playerAsDragon.prevXRot = upRot;
                        }

                        if (Float.isNaN(playerAsDragon.prevXRot)) {
                            playerAsDragon.prevXRot = 0;
                        }

                        poseStack.mulPose(Axis.XN.rotationDegrees(playerAsDragon.prevXRot));

                        float yRot;
                        Vec3 deltaVel;
                        Vec3 viewDir;
                        if (isPlayerGliding) {
                            yRot = player.getViewYRot(1f);
                            deltaVel = player.getDeltaMovement();
                            viewDir = player.getViewVector(1f);
                        } else {
                            yRot = playerVehicle.getViewYRot(1f);
                            deltaVel = playerVehicle.getDeltaMovement();
                            viewDir = playerVehicle.getViewVector(1f);
                        }

                        // Factor for interpolating to the target bank angle
                        final float ROLL_VEL_LERP_FACTOR = 0.1F;
                        // Minimum velocity to begin banking
                        final double ROLL_VEL_INFLUENCE_MIN = 0.5D;
                        // Maximum velocity at which point the bank angle has full effect
                        final double ROLL_VEL_INFLUENCE_MAX = 2.0D;

                        // Minimum view-velocity delta to start rolling
                        final float ROLL_MIN_DELTA_DEG = 5;
                        // Equivalent maximum, after which the bank angle is maximized
                        final float ROLL_MAX_DELTA_DEG = 90;
                        // Maximum roll angle
                        final float ROLL_MAX_DEG = 60;
                        // Exponent for targetRollNormalized (applied after normalizing relative to ROLL_MAX_DEG)
                        // > 1: soft, starts banking slowly, increases rapidly with higher delta
                        // < 1: sensitive, starts banking even when the difference is tiny, softer towards the limits
                        final double ROLL_EXP = 0.7;

                        float targetRollNormalized;

                        // Note that we're working with the HORIZONTAL move delta
                        if (deltaVel.horizontalDistanceSqr() > ROLL_VEL_INFLUENCE_MIN * ROLL_VEL_INFLUENCE_MIN) {
                            float velAngle = (float) Math.atan2(-deltaVel.x, deltaVel.z) * Mth.RAD_TO_DEG;

                            float viewToVelDeltaDeg = Mth.degreesDifference(velAngle, yRot);

                            // Raw target roll, normalized
                            targetRollNormalized = (float) Functions.deadzoneNormalized(viewToVelDeltaDeg, ROLL_MIN_DELTA_DEG, ROLL_MAX_DELTA_DEG);
//                            if (player instanceof LocalPlayer localPlayer) {
//                                localPlayer.sendSystemMessage(Component.literal("Raw target angle: %.2f".formatted(targetRollNormalized)));
//                            }
                            // Scale via exponent (still normalized)
                            targetRollNormalized = Math.copySign((float) Math.pow(Math.abs(targetRollNormalized), ROLL_EXP), targetRollNormalized);
//                            if (player instanceof LocalPlayer localPlayer) {
//                                localPlayer.sendSystemMessage(Component.literal("Scaled via exponent: %.2f".formatted(targetRollNormalized)));
//                            }
                            // Scale by velocity influence
                            float velInfluence = (float) Functions.inverseLerpClamped(
                                    deltaVel.horizontalDistance(),
                                    ROLL_VEL_INFLUENCE_MIN,
                                    ROLL_VEL_INFLUENCE_MAX
                            );
                            targetRollNormalized *= velInfluence;
//                            if (player instanceof LocalPlayer localPlayer) {
//                                localPlayer.sendSystemMessage(Component.literal("Scaled by velocity: %.2f".formatted(targetRollNormalized)));
//                            }
                        } else {
                            targetRollNormalized = 0;
                        }

                        float targetRollDeg = targetRollNormalized * ROLL_MAX_DEG * Mth.DEG_TO_RAD;

                        // NaN/Inf prevention - snap directly
                        if (!Double.isFinite(playerAsDragon.prevZRot)) {
                            playerAsDragon.prevZRot = targetRollDeg;
                        } else {
                            playerAsDragon.prevZRot = Mth.lerp(ROLL_VEL_LERP_FACTOR, playerAsDragon.prevZRot, targetRollDeg);
                        }

                        handler.getMovementData().prevXRot = playerAsDragon.prevXRot;
                        handler.getMovementData().prevZRot = playerAsDragon.prevZRot;

                        poseStack.mulPose(Axis.ZP.rotation(playerAsDragon.prevZRot));
                    } else {
                        handler.getMovementData().prevZRot = 0;
                        handler.getMovementData().prevXRot = 0;
                    }
                    if (player != minecraft.player || !Minecraft.getInstance().options.getCameraType().isFirstPerson() || !isPlayerGliding || renderFirstPersonFlight) {
                        dragonRenderer.render(playerAsDragon, yaw, partialRenderTick, poseStack, renderTypeBuffer, eventLight);
                    }
                }

                if (!player.isSpectator()) {
                    // Render the parrot on the players shoulder
                    ((LivingRendererAccessor) playerRenderer).dragonSurvival$getRenderLayers().stream().filter(ParrotOnShoulderLayer.class::isInstance).findAny().ifPresent(renderLayer -> {
                        poseStack.scale(1.0F / scale, 1.0F / scale, 1.0F / scale);
                        poseStack.mulPose(Axis.XN.rotationDegrees(180.0F));
                        double height = 1.3 * scale;
                        double forward = 0.3 * scale;
                        float parrotHeadYaw = Mth.clamp(-1.0F * ((float) handler.getMovementData().bodyYaw - (float) handler.getMovementData().headYaw), -75.0F, 75.0F);
                        poseStack.translate(0, -height, -forward);
                        renderLayer.render(poseStack, renderTypeBuffer, eventLight, player, 0.0F, 0.0F, partialRenderTick, (float) player.tickCount + partialRenderTick, parrotHeadYaw, (float) handler.getMovementData().headPitch);
                        poseStack.translate(0, height, forward);
                        poseStack.mulPose(Axis.XN.rotationDegrees(-180.0F));
                        poseStack.scale(scale, scale, scale);
                    });

                    int combinedOverlayIn = LivingEntityRenderer.getOverlayCoords(player, 0);
                    if (player.hasEffect(DSEffects.TRAPPED)) {
                        float bolasScale = player.getEyeHeight();
                        if (handler.isDragon()) {
                            bolasScale = (float) DragonSizeHandler.calculateDragonEyeHeight(handler, player);
                        }
                        BolasOnPlayerRenderer.renderBolas(eventLight, combinedOverlayIn, renderTypeBuffer, poseStack, bolasScale);
                    }
                }
            } catch (Throwable throwable) {
                DragonSurvival.LOGGER.error("A problem occurred while rendering a dragon in third person", throwable);
            } finally {
                poseStack.popPose();
            }
        } else {
            if (renderDelay > 0 && player == minecraft.player) {
                renderDelay--;
                renderPlayerEvent.setCanceled(true);
            } else {
                ((EntityRendererAccessor) renderPlayerEvent.getRenderer()).dragonSurvival$setShadowRadius(0.5F);
            }
        }
        dragonModel.setOverrideTexture(null);
    }

    @SubscribeEvent
    public static void spin(InputEvent.InteractionKeyMappingTriggered keyInputEvent) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }

        DragonStateHandler handler = DragonStateProvider.getData(player);
        if (!handler.isDragon()) {
            return;
        }

        if (keyInputEvent.isAttack() && keyInputEvent.shouldSwingHand() && !handler.getMovementData().dig) {
            handler.getMovementData().bite = true;
        }
    }

    public static void setDragonMovementData(Player player, float realtimeDeltaTick) {
        if (player == null) return;

        DragonStateProvider.getOptional(player).ifPresent(playerStateHandler -> {
            if (!playerStateHandler.isDragon()) return;

            Vec3 moveVector;
            if (!ServerFlightHandler.isFlying(player)) {
                moveVector = player.getDeltaMovement();
            } else {
                moveVector = new Vec3(player.getX() - player.xo, player.getY() - player.yo, player.getZ() - player.zo);
            }

            // Get new body yaw & head angles
            var newAngles = BodyAngles.calculateNext(player, playerStateHandler, realtimeDeltaTick);

            // Update the movement data
            playerStateHandler.setMovementData(newAngles.bodyYaw, newAngles.headYaw, newAngles.headPitch, moveVector);
        });
    }

    @SubscribeEvent
    public static void updateFirstPersonDataAndSendMovementData(ClientTickEvent.Pre event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            DragonStateProvider.getOptional(player).ifPresent(playerStateHandler -> {
                if (playerStateHandler.isDragon()) {
                    Input input = player.input;
                    playerStateHandler.setFirstPerson(Minecraft.getInstance().options.getCameraType().isFirstPerson());
                    playerStateHandler.setFreeLook(Keybind.FREE_LOOK.isDown());
                    playerStateHandler.setDesiredMoveVec(new Vec2(input.leftImpulse, input.forwardImpulse));
                    if (player.isPassenger()) {
                        // Prevent animation jank while we are riding an entity
                        PacketDistributor.sendToServer(new SyncDeltaMovement.Data(player.getId(), 0, 0, 0));
                    } else {
                        PacketDistributor.sendToServer(new SyncDeltaMovement.Data(player.getId(), player.getDeltaMovement().x, player.getDeltaMovement().y, player.getDeltaMovement().z));
                    }

                    DragonMovementData md = playerStateHandler.getMovementData();
                    PacketDistributor.sendToServer(
                            new SyncDragonMovement.Data(
                                    player.getId(),
                                    md.isFirstPerson,
                                    md.bite,
                                    md.isFreeLook,
                                    md.desiredMoveVec.x,
                                    md.desiredMoveVec.y
                            )
                    );
                }
            });
        }
    }

    /**
     * Don't render fire overlay for cave dragons
     */
    @SubscribeEvent
    public static void removeFireOverlay(RenderBlockScreenEffectEvent event) {
        if (event.getOverlayType() != RenderBlockScreenEffectEvent.OverlayType.FIRE) {
            return;
        }

        DragonStateProvider.getOptional(Minecraft.getInstance().player).ifPresent(handler -> {
            if (DragonUtils.isType(handler, DragonTypes.CAVE)) {
                event.setCanceled(true);
            }
        });
    }

    @SubscribeEvent
    public static void unloadWorld(LevelEvent.Unload worldEvent) {
        playerDragonHashMap.clear();
    }

    private record BodyAngles(double bodyYaw, double headPitch, double headYaw) {

        /// Minimum magnitude for player input to consider the player to be moving
        /// This is used for deliberate movement, i.e. player input
        /// Forced movement (mid-air momentum etc.) relies on MOVE_DELTA_EPSILON for the world-space move delta vector
        static final double INPUT_EPSILON = 0.0000001D;

        /// Minimum magnitude to consider the player to be moving (horizontally)
        /// Applies to world-space horizontal movement (as opposed to raw player input)
        static final double MOVE_DELTA_EPSILON = 0.0001D;

        /// When moving (without input) too slower, the body aligns to the move direction slower too.
        /// This constant determines the move vector magnitude below which it begins to slow down.
        /// The body stops aligning below MOVE_DELTA_EPSILON,
        /// and aligns at full speed above MOVE_DELTA_FULL_EFFECT_MIN_MAG.
        static final double MOVE_DELTA_FULL_EFFECT_MIN_MAG = 0.3D;

        /// Factor to align the body to the move vector
        static final double MOVE_ALIGN_FACTOR = 0.3D;
        /// Multiplier for MOVE_ALIGN_FACTOR when in the air
        static final double MOVE_ALIGN_FACTOR_AIR = 0.12D;
        /// Multiplier for MOVE_ALIGN_FACTOR * MOVE_ALIGN_FACTOR_AIR when there's no player input
        static final double MOVE_ALIGN_FACTOR_AIR_PASSIVE_MUL = 0.75D; // Multiplier for the above

        // Body angle limits in various circumstances
        // 0 is straight ahead, 180 is no restriction

        /// Body angle limits: Third person
        static final double BODY_ANGLE_LIMIT_TP = 180D - 30D;
        /// Body angle limit softness: Third person
        static final double BODY_ANGLE_LIMIT_TP_SOFTNESS = 0.9D;
        /// Body angle limit softness, multiplier when in the air: Third person
        static final double BODY_ANGLE_LIMIT_TP_SOFTNESS_AIR_MUL = 0.15D;

        // Third person + free look is unrestricted
        /// Body angle limits: Third person, free look
        static final double BODY_ANGLE_LIMIT_TP_FREE = 180D;
        /// Body angle limit softness: Third person, free look
        static final double BODY_ANGLE_LIMIT_TP_SOFTNESS_FREE = 0D;
        /// Body angle limit softness, multiplier when in the air: Third person, free look
        static final double BODY_ANGLE_LIMIT_TP_SOFTNESS_AIR_MUL_FREE = 0D;

        /// Body angle limits: First person
        static final double BODY_ANGLE_LIMIT_FP = 10D;
        /// Body angle limit softness: First person
        static final double BODY_ANGLE_LIMIT_FP_SOFTNESS = 0.75D;
        /// Body angle limit softness, multiplier when in the air: First person
        static final double BODY_ANGLE_LIMIT_FP_SOFTNESS_AIR_MUL = 0.4D;

        /// Body angle limits: First person, free look
        static final double BODY_ANGLE_LIMIT_FP_FREE = 60D;
        /// Body angle limit softness: First person, free look
        static final double BODY_ANGLE_LIMIT_FP_FREE_SOFTNESS = 0.85D;
        /// Body angle limit softness, multiplier when in the air: First person, free look
        static final double BODY_ANGLE_LIMIT_FP_FREE_SOFTNESS_AIR_MUL = 0.4D;

        // Head angle values
        // Head yaw has no angle limits defined here, but avoids passing through 180 (behind the player)

        /// Head yaw lerp factor
        static final double HEAD_YAW_FACTOR = 0.3D;
        /// Head yaw pitch factor
        static final double HEAD_PITCH_FACTOR = 0.3D;

        public static BodyAngles calculateNext(Player player, DragonStateHandler dragonStateHandler, float realtimeDeltaTick) {
            // Handle headYaw
            float viewYRot = player.getViewYRot(realtimeDeltaTick);
            float viewXRot = player.getViewXRot(realtimeDeltaTick);
            // Head yaw is relative to body
            DragonMovementData movementData = dragonStateHandler.getMovementData();

            // Get pos delta since last tick - not scaled by realtimeDeltaTick
            var posDelta = new Vec3(player.getX() - player.xo, player.getY() - player.yo, player.getZ() - player.zo);

            var headAngles = calculateNextHeadAngles(realtimeDeltaTick, movementData, viewXRot, viewYRot);

            return new BodyAngles(
                    calculateNextBodyYaw(realtimeDeltaTick, player, movementData, posDelta, viewYRot),
                    headAngles.getA(),
                    headAngles.getB()
            );
        }

        private static double calculateNextBodyYaw(
                float realtimeDeltaTick,
                Player player,
                DragonMovementData movementData,
                Vec3 posDelta,
                float viewYRot) {

            // Handle bodyYaw
            double bodyYaw = movementData.bodyYaw;
            boolean isFreeLook = movementData.isFreeLook;
            boolean wasFreeLook = movementData.wasFreeLook; // TODO: what's this for?
            boolean isFirstPerson = movementData.isFirstPerson;
            boolean hasPosDelta = posDelta.horizontalDistanceSqr() > MOVE_DELTA_EPSILON * MOVE_DELTA_EPSILON;

            var rawInput = movementData.desiredMoveVec;
            var hasMoveInput = rawInput.lengthSquared() > INPUT_EPSILON * INPUT_EPSILON;
            boolean isInputBack = rawInput.y < 0;

            if (hasMoveInput) {

                // When providing move input, turn the body towards the input direction
                var targetAngle = Math.toDegrees(Math.atan2(-rawInput.x, rawInput.y)) + viewYRot;

                // If in first person and moving back when not flying, flip the target angle
                // Checks dragon flight or creative/spectator flight
                var isFlying = ServerFlightHandler.isFlying(player) || player.getAbilities().flying;
                if (isFirstPerson && !isFreeLook && isInputBack && !isFlying) {
                    targetAngle += 180;
                }

                var factor = player.onGround() ? MOVE_ALIGN_FACTOR : MOVE_ALIGN_FACTOR_AIR;

                // In first person, force the body to turn away from the view direction if possible
                // This prevents issues with the body yaw and angle limit fighting, never letting the body
                // pass through the area in front of the player when that's the shorter path for the body yaw
                if (isFirstPerson) {
                    bodyYaw = Functions.lerpAngleAwayFrom(realtimeDeltaTick * factor, bodyYaw, targetAngle, viewYRot + 180);
                } else {
                    bodyYaw = RenderUtil.lerpYaw(realtimeDeltaTick * factor, bodyYaw, targetAngle);
                }
            } else if (hasPosDelta && !player.onGround()) {
                // When moving without input and in the air, slowly align to the move vector

                // +Z: 0 deg; -X: 90 deg
                // Move angle that the body will try to align to
                var posDeltaAngle = Math.toDegrees(Math.atan2(-posDelta.x, posDelta.z));

                var factor = MOVE_ALIGN_FACTOR_AIR * MOVE_ALIGN_FACTOR_AIR_PASSIVE_MUL;
                double deltaMagFactor = Math.min(
                        1,
                        (posDelta.horizontalDistance() - MOVE_DELTA_EPSILON) / MOVE_DELTA_FULL_EFFECT_MIN_MAG
                );
                factor *= deltaMagFactor;

                bodyYaw = RenderUtil.lerpYaw(
                        realtimeDeltaTick * factor,
                        bodyYaw,
                        posDeltaAngle);
            }

            {
                // Limit body angle based on view direction and PoV
                var angleLimit = 0D;
                var factor = 0D;
                var airMul = 1D;
                if (isFirstPerson) {
                    if (isFreeLook) {
                        angleLimit = BODY_ANGLE_LIMIT_FP_FREE;
                        factor = BODY_ANGLE_LIMIT_FP_FREE_SOFTNESS;
                        airMul = BODY_ANGLE_LIMIT_FP_FREE_SOFTNESS_AIR_MUL;
                    } else {
                        angleLimit = BODY_ANGLE_LIMIT_FP;
                        factor = BODY_ANGLE_LIMIT_FP_SOFTNESS;
                        airMul = BODY_ANGLE_LIMIT_FP_SOFTNESS_AIR_MUL;
                    }
                } else {
                    if (isFreeLook) {
                        angleLimit = BODY_ANGLE_LIMIT_TP_FREE;
                        factor = BODY_ANGLE_LIMIT_TP_SOFTNESS_FREE;
                        airMul = BODY_ANGLE_LIMIT_TP_SOFTNESS_AIR_MUL_FREE;
                    } else {
                        angleLimit = BODY_ANGLE_LIMIT_TP;
                        factor = BODY_ANGLE_LIMIT_TP_SOFTNESS;
                        airMul = BODY_ANGLE_LIMIT_TP_SOFTNESS_AIR_MUL;
                    }
                }
                if (!player.onGround()) {
                    factor *= airMul;
                }
                bodyYaw = Functions.limitAngleDeltaSoft(bodyYaw, viewYRot, angleLimit, realtimeDeltaTick * factor);
            }
            return bodyYaw;
        }

        private static Tuple<Double, Double> calculateNextHeadAngles(
                float realtimeDeltaTick,
                DragonMovementData movementData,
                float viewXRot,
                float viewYRot) {
            // Yaw is relative to the body
            double headYawTarget = Functions.angleDifference(
                    viewYRot,
                    movementData.bodyYaw
            );
            double headYaw = Functions.lerpAngleAwayFrom(
                    realtimeDeltaTick * HEAD_YAW_FACTOR,
                    movementData.headYaw,
                    headYawTarget,
                    180
            );

            // Pitch is also technically relative, since the body doesn't have pitch
            double headPitch = Mth.lerp(
                    realtimeDeltaTick * HEAD_PITCH_FACTOR,
                    movementData.headPitch,
                    viewXRot
            );

            return new Tuple<>(headPitch, headYaw);
        }
    }

}
