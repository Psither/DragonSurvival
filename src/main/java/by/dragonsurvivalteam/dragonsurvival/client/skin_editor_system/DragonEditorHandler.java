package by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system;

import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.objects.DragonEditorObject.DragonTextureMetadata;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.objects.LayerSettings;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.objects.SkinPreset.SkinAgeGroup;
import by.dragonsurvivalteam.dragonsurvival.client.util.RenderingUtils;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.capability.subcapabilities.SkinCap;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.AbstractDragonType;
import by.dragonsurvivalteam.dragonsurvival.common.entity.DragonEntity;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.DragonBody;
import by.dragonsurvivalteam.dragonsurvival.util.DragonUtils;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.GlConst;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.datafixers.util.Pair;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.GlStateBackup;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import static by.dragonsurvivalteam.dragonsurvival.DragonSurvival.MODID;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class DragonEditorHandler {
    private static ShaderInstance skinGenerationShader;

    private static ResourceLocation getSkinTextureResourceLocation(Player player, EnumSkinLayer layer, String key, AbstractDragonType type) {
        if (Objects.equals(layer.name, "Extra") && layer != EnumSkinLayer.EXTRA) {
            return getSkinTextureResourceLocation(player, EnumSkinLayer.EXTRA, key, type);
        }

        if (layer == EnumSkinLayer.BASE && (key.equalsIgnoreCase("Skin") || key.equalsIgnoreCase(SkinCap.defaultSkinValue))) {
            DragonStateHandler handler = DragonStateProvider.getData(player);
            return getSkinTextureResourceLocation(player, layer, type.getSubtypeNameLowerCase() + "_base_" + handler.getLevel().ordinal(), type);
        }

        DragonTextureMetadata[] texts = DragonEditorRegistry.CUSTOMIZATIONS.getOrDefault(type.getTypeNameUpperCase(), new HashMap<>()).getOrDefault(layer, new DragonTextureMetadata[0]);

        for (DragonTextureMetadata texture : texts) {
            if (Objects.equals(texture.key, key)) {
                return ResourceLocation.parse(texture.texture);
            }
        }

        return null;
    }

    public static DragonTextureMetadata getSkinTextureMetadata(Player player, EnumSkinLayer layer, String key, AbstractDragonType type) {
        if (Objects.equals(layer.name, "Extra") && layer != EnumSkinLayer.EXTRA) {
            return getSkinTextureMetadata(player, EnumSkinLayer.EXTRA, key, type);
        }

        if (layer == EnumSkinLayer.BASE && (key.equalsIgnoreCase("Skin") || key.equalsIgnoreCase(SkinCap.defaultSkinValue))) {
            return getSkinTextureMetadata(player, layer, type.getTypeNameLowerCase() + "_base_" + DragonUtils.getLevel(player).ordinal(), type);
        }

        DragonTextureMetadata[] texts = DragonEditorRegistry.CUSTOMIZATIONS.getOrDefault(type.getTypeNameUpperCase(), new HashMap<>()).getOrDefault(layer, new DragonTextureMetadata[0]);

        for (DragonTextureMetadata texture : texts) {
            if (Objects.equals(texture.key, key)) {
                return texture;
            }
        }

        return null;
    }

    public static ArrayList<String> getKeys(AbstractDragonType type, Holder<DragonBody> body, EnumSkinLayer layers) {
        if (Objects.equals(layers.name, "Extra") && layers != EnumSkinLayer.EXTRA) {
            return getKeys(type, body, EnumSkinLayer.EXTRA);
        }

        ArrayList<String> list = new ArrayList<>();

        DragonTextureMetadata[] texts = DragonEditorRegistry.CUSTOMIZATIONS.getOrDefault(type.getTypeNameUpperCase(), new HashMap<>()).getOrDefault(layers, new DragonTextureMetadata[0]);
        for (DragonTextureMetadata texture : texts) {
            // TODO :: change how body is checked
            if (texture.bodies == null || Arrays.asList(texture.bodies).contains(body.getKey().location().getPath())) {
                list.add(texture.key);
            }
        }

        return list;
    }

    public static ArrayList<String> getKeys(Player player, EnumSkinLayer layers) {
        return getKeys(DragonUtils.getType(player), DragonUtils.getBody(player), layers);
    }

    public static CompletableFuture<List<Pair<NativeImage, ResourceLocation>>> generateSkinTextures(final DragonEntity dragon) {
        return CompletableFuture.supplyAsync(() -> genTextures(dragon), Util.backgroundExecutor());
    }

    public static void generateSkinTexturesGPU(final DragonEntity dragon) {
        genTexturesGPU(dragon);
    }

    private static void genTexturesGPU(final DragonEntity dragon) {
        Player player = dragon.getPlayer();

        if (player == null) {
            return;
        }

        DragonStateHandler handler = DragonStateProvider.getData(player);
        GlStateBackup state = new GlStateBackup();
        RenderSystem.backupGlState(state);
        RenderSystem.backupProjectionMatrix();
        int currentFrameBuffer = GlStateManager.getBoundFramebuffer();
        int currentViewportX = GlStateManager.Viewport.x();
        int currentViewportY = GlStateManager.Viewport.y();
        int currentViewportWidth = GlStateManager.Viewport.width();
        int currentViewportHeight = GlStateManager.Viewport.height();

        RenderTarget normalTarget = new TextureTarget(512, 512, false, Minecraft.ON_OSX);
        RenderTarget glowTarget = new TextureTarget(512, 512, false, Minecraft.ON_OSX);
        normalTarget.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
        glowTarget.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
        normalTarget.clear(true);
        glowTarget.clear(true);

        SkinAgeGroup skinAgeGroup = handler.getSkinData().skinPreset.skinAges.get(handler.getLevel()).get();

        String uuid = player.getStringUUID();
        ResourceLocation dynamicNormalKey = ResourceLocation.fromNamespaceAndPath(MODID, "dynamic_normal_" + uuid + "_" + handler.getLevel().name);
        ResourceLocation dynamicGlowKey = ResourceLocation.fromNamespaceAndPath(MODID, "dynamic_glow_" + uuid + "_" + handler.getLevel().name);

        for (EnumSkinLayer layer : EnumSkinLayer.values()) {
            LayerSettings settings = skinAgeGroup.layerSettings.get(layer).get();
            String selectedSkin = settings.selectedSkin;

            if (selectedSkin != null) {
                DragonTextureMetadata skinTexture = getSkinTextureMetadata(player, layer, selectedSkin, handler.getType());

                if (skinTexture != null) {
                    float hueVal = settings.hue - skinTexture.average_hue;
                    float satVal = settings.saturation;
                    float brightVal = settings.brightness;

                    ResourceLocation textureLocation = getSkinTextureResourceLocation(player, layer, selectedSkin, handler.getType());
                    AbstractTexture texture = Minecraft.getInstance().getTextureManager().getTexture(textureLocation);

                    if (settings.glowing) {
                        glowTarget.bindWrite(true);
                    } else {
                        normalTarget.bindWrite(true);
                    }
                    RenderSystem.enableBlend();
                    RenderSystem.colorMask(true, true, true, true);
                    RenderSystem.blendEquation(GlConst.GL_FUNC_ADD);
                    RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
                    RenderSystem.disableDepthTest();
                    RenderSystem.depthMask(false);
                    skinGenerationShader.setSampler("SkinTexture", texture);
                    skinGenerationShader.getUniform("HueVal").set(hueVal);
                    skinGenerationShader.getUniform("SatVal").set(satVal);
                    skinGenerationShader.getUniform("BrightVal").set(brightVal);
                    skinGenerationShader.getUniform("Colorable").set(skinTexture.colorable ? 1.0f : 0.0f);
                    skinGenerationShader.getUniform("Glowing").set(settings.glowing ? 1.0f : 0.0f);
                    skinGenerationShader.apply();

                    BufferBuilder bufferbuilder = RenderSystem.renderThreadTesselator().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLIT_SCREEN);
                    bufferbuilder.addVertex(0.0F, 0.0F, 0.0F);
                    bufferbuilder.addVertex(1.0F, 0.0F, 0.0F);
                    bufferbuilder.addVertex(1.0F, 1.0F, 0.0F);
                    bufferbuilder.addVertex(0.0F, 1.0F, 0.0F);
                    BufferUploader.draw(bufferbuilder.buildOrThrow());

                    if (settings.glowing && layer == EnumSkinLayer.BASE) {
                        normalTarget.bindWrite(true);
                        bufferbuilder = RenderSystem.renderThreadTesselator().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLIT_SCREEN);
                        bufferbuilder.addVertex(0.0F, 0.0F, 0.0F);
                        bufferbuilder.addVertex(1.0F, 0.0F, 0.0F);
                        bufferbuilder.addVertex(1.0F, 1.0F, 0.0F);
                        bufferbuilder.addVertex(0.0F, 1.0F, 0.0F);
                        BufferUploader.draw(bufferbuilder.buildOrThrow());
                        normalTarget.unbindWrite();
                    }

                    skinGenerationShader.clear();
                    if (settings.glowing) {
                        glowTarget.unbindWrite();
                    } else {
                        normalTarget.unbindWrite();
                    }
                }
            }
        }

        RenderingUtils.copyTextureFromRenderTarget(glowTarget, dynamicGlowKey);
        RenderingUtils.copyTextureFromRenderTarget(normalTarget, dynamicNormalKey);
        glowTarget.destroyBuffers();
        normalTarget.destroyBuffers();
        RenderSystem.restoreGlState(state);
        RenderSystem.restoreProjectionMatrix();
        GlStateManager._glBindFramebuffer(GlConst.GL_FRAMEBUFFER, currentFrameBuffer);
        GlStateManager._viewport(currentViewportX, currentViewportY, currentViewportWidth, currentViewportHeight);
    }

    private static List<Pair<NativeImage, ResourceLocation>> genTextures(final DragonEntity dragon) {
        Player player = dragon.getPlayer();

        if (player == null) {
            return List.of();
        }

        DragonStateHandler handler = DragonStateProvider.getData(player);
        List<Pair<NativeImage, ResourceLocation>> texturesToRegister = new ArrayList<>();
        SkinAgeGroup skinAgeGroup = handler.getSkinData().skinPreset.skinAges.get(handler.getLevel()).get();
        NativeImage normal = new NativeImage(512, 512, true);
        NativeImage glow = new NativeImage(512, 512, true);

        for (EnumSkinLayer layer : EnumSkinLayer.values()) {
            LayerSettings settings = skinAgeGroup.layerSettings.get(layer).get();
            String selectedSkin = settings.selectedSkin;

            if (selectedSkin != null) {
                DragonTextureMetadata skinTexture = getSkinTextureMetadata(player, layer, selectedSkin, handler.getType());

                if (skinTexture != null) {
                    float hue = settings.hue - skinTexture.average_hue;
                    float saturation = settings.saturation;
                    float brightness = settings.brightness;

                    ResourceLocation textureLocation = getSkinTextureResourceLocation(player, layer, selectedSkin, handler.getType());
                    NativeImage skinImage = RenderingUtils.getImageFromResource(textureLocation);

                    for (int x = 0; x < skinImage.getWidth(); x++) {
                        for (int y = 0; y < skinImage.getHeight(); y++) {
                            Color baseColor = new Color(skinImage.getPixelRGBA(x, y), true);
                            Color hueAdjustedColor = getHueAdjustedColor(settings.glowing, skinTexture.colorable, hue, saturation, brightness, baseColor);

                            if (hueAdjustedColor == null) {
                                continue;
                            }

                            if (hueAdjustedColor.getAlpha() != 0) {
                                Supplier<NativeImage> target = settings.glowing ? () -> glow : () -> normal;
                                target.get().setPixelRGBA(x, y, hueAdjustedColor.getRGB());

                                if (settings.glowing && layer == EnumSkinLayer.BASE) {
                                    normal.setPixelRGBA(x, y, hueAdjustedColor.getRGB());
                                }
                            }
                        }
                    }

                    skinImage.close();
                }
            }
        }

        String uuid = player.getStringUUID();
        ResourceLocation dynamicNormalKey = ResourceLocation.fromNamespaceAndPath(MODID, "dynamic_normal_" + uuid + "_" + handler.getLevel().name);
        ResourceLocation dynamicGlowKey = ResourceLocation.fromNamespaceAndPath(MODID, "dynamic_glow_" + uuid + "_" + handler.getLevel().name);

        texturesToRegister.add(new Pair<>(normal, dynamicNormalKey));
        texturesToRegister.add(new Pair<>(glow, dynamicGlowKey));

        return texturesToRegister;
    }

    @Nullable private static Color getHueAdjustedColor(boolean glowing, boolean colorable, float hue, float saturation, float brightness, Color baseColor) {
        if (!colorable) {
            return baseColor;
        }

        float[] hsb = new float[3];
        Color.RGBtoHSB(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), hsb);

        if (glowing && hsb[0] == 0.5f && hsb[1] == 0.5f) {
            return null;
        }

        hsb[0] = hsb[0] - hue;
        hsb[1] = Mth.lerp(Math.abs(saturation - 0.5f) * 2.0f, hsb[1], saturation > 0.5f ? 1.0f : 0.0f);
        hsb[2] = Mth.lerp(Math.abs(brightness - 0.5f) * 2.0f, hsb[2], brightness > 0.5f ? 1.0f : 0.0f);

        Color adjustedColor = new Color(Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]));
        return new Color(adjustedColor.getRed(), adjustedColor.getGreen(), adjustedColor.getBlue(), baseColor.getAlpha());
    }

    @SubscribeEvent
    public static void registerShaders(RegisterShadersEvent event) throws IOException {
        event.registerShader(new ShaderInstance(event.getResourceProvider(), ResourceLocation.fromNamespaceAndPath("dragonsurvival", "skin_generation"), DefaultVertexFormat.BLIT_SCREEN), (instance) -> {
            skinGenerationShader = instance;
        });
    }
}