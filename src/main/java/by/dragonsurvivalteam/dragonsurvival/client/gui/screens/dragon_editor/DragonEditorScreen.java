package by.dragonsurvivalteam.dragonsurvival.client.gui.screens.dragon_editor;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.client.gui.hud.MagicHUD;
import by.dragonsurvivalteam.dragonsurvival.client.gui.screens.DragonAltarScreen;
import by.dragonsurvivalteam.dragonsurvival.client.gui.screens.SkinsScreen;
import by.dragonsurvivalteam.dragonsurvival.client.gui.screens.dragon_editor.buttons.*;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.ColorSelectorButton;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.UndoRedoButton;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.generic.ArrowButton;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.generic.DropDownButton;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.generic.ExtendedCheckbox;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.generic.HelpButton;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.components.DragonEditorConfirmComponent;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.components.DragonUIRenderComponent;
import by.dragonsurvivalteam.dragonsurvival.client.render.ClientDragonRenderer;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.DragonEditorHandler;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.DragonEditorRegistry;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.EnumSkinLayer;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.objects.DragonEditorObject.DragonTextureMetadata;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.objects.LayerSettings;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.objects.SkinPreset;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.objects.SkinPreset.SkinAgeGroup;
import by.dragonsurvivalteam.dragonsurvival.client.util.FakeClientPlayerUtils;
import by.dragonsurvivalteam.dragonsurvival.client.util.TextRenderUtil;
import by.dragonsurvivalteam.dragonsurvival.commands.DragonCommand;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.capability.subcapabilities.SkinCap;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.AbstractDragonBody;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.AbstractDragonType;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.DragonBodies;
import by.dragonsurvivalteam.dragonsurvival.config.ServerConfig;
import by.dragonsurvivalteam.dragonsurvival.mixins.client.ScreenAccessor;
import by.dragonsurvivalteam.dragonsurvival.network.dragon_editor.SyncPlayerSkinPreset;
import by.dragonsurvivalteam.dragonsurvival.network.syncing.SyncComplete;
import by.dragonsurvivalteam.dragonsurvival.server.handlers.ServerFlightHandler;
import by.dragonsurvivalteam.dragonsurvival.util.DragonLevel;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import by.dragonsurvivalteam.dragonsurvival.util.GsonFactory;
import com.google.gson.Gson;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.DyeColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.gui.widget.ExtendedButton;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.neoforge.network.PacketDistributor;
import org.apache.commons.lang3.text.WordUtils;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.function.Supplier;

import static by.dragonsurvivalteam.dragonsurvival.DragonSurvival.MODID;

@EventBusSubscriber(Dist.CLIENT)
public class DragonEditorScreen extends Screen {
    private static final ResourceLocation backgroundTexture = ResourceLocation.withDefaultNamespace("textures/block/black_concrete.png");
    private static final ResourceLocation SAVE = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/save_icon.png");
    private static final ResourceLocation RANDOM = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/random_icon.png");
    private static final ResourceLocation RESET = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/reset_button.png");
    private final Screen source;
    private final String[] animations = {"sit_dentist",
            "sit_head_locked",
            "idle_head_locked",
            "fly_head_locked",
            "swim_fast_head_locked",
            "run_head_locked",
            "spinning_on_back"};
    public int guiLeft;
    public int guiTop;
    public boolean confirmation = false;
    public boolean showUi = true;

    public DragonUIRenderComponent dragonRender;

    public static final DragonStateHandler HANDLER = new DragonStateHandler();

    public DragonLevel level;
    public AbstractDragonType dragonType;
    public AbstractDragonBody dragonBody;
    public SkinPreset preset;
    public int currentSelected;
    private final HashMap<DragonLevel, Integer> presetSelections = new HashMap<>();

    private final List<ColorSelectorButton> colorButtons = new ArrayList<>();
    public ExtendedCheckbox defaultSkinCheckbox;
    private ExtendedCheckbox showUiCheckbox;

    public int backgroundColor = -804253680;
    float tick;
    private int curAnimation;
    private int lastSelected;
    private boolean hasInit;
    private DragonEditorConfirmComponent conf;
    private boolean isEditor;
    private final Map<EnumSkinLayer, DropDownButton> dropdownButtons = new HashMap<>();
    private final Map<EnumSkinLayer, ColorSelectorButton> colorSelectorButtons = new HashMap<>();
    private ExtendedCheckbox wingsCheckbox;

    public DragonEditorScreen(Screen source) {
        this(source, null);
        this.isEditor = true;
    }

    public DragonEditorScreen(Screen source, AbstractDragonType dragonType) {
        super(Component.translatable("ds.gui.dragon_editor"));
        this.source = source;
        this.dragonType = dragonType;
    }

    public record EditorAction<T>(Function<T, T> action, T value) {

        public T run() {
            return action.apply(value);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof EditorAction<?> action) {
                if (action.action != null && action.value != null) {
                    return action.action.equals(this.action) && action.value.equals(this.value);
                }
            }
            return false;
        }
    }

    private float zoomToSetForDragonLevel(DragonLevel level) {
        return switch (level) {
            case NEWBORN -> level.size * 3 - 5;
            case YOUNG, ADULT -> level.size * 2;
        };
    }

    public final Function<DragonLevel, DragonLevel> selectLevelAction = (newLevel) -> {
        DragonLevel prevLevel = level;
        level = newLevel;
        dragonRender.zoom = zoomToSetForDragonLevel(level);
        HANDLER.getSkinData().compileSkin();
        update();

        return prevLevel;
    };

    // setHueAction, setSaturationAction, setBrightnessAction in HueSelectorComponent.Java

    // setDragonSlotAction in DragonEditorSlotButton.Java

    public final Function<CompoundTag, CompoundTag> setSkinPresetAction = (tag) -> {
        CompoundTag prevTag = HANDLER.getSkinData().skinPreset.serializeNBT(Minecraft.getInstance().player.registryAccess());
        HANDLER.getSkinData().skinPreset.deserializeNBT(Minecraft.getInstance().player.registryAccess(), tag);
        HANDLER.getSkinData().compileSkin();
        update();
        return prevTag;
    };

    public final Function<AbstractDragonBody, AbstractDragonBody> dragonBodySelectAction = (body) -> {
        AbstractDragonBody prevBody = dragonBody;
        dragonBody = body;
        update();
        return prevBody;
    };

    public final Function<Pair<EnumSkinLayer, String>, Pair<EnumSkinLayer, String>> dragonPartSelectAction = (pair) -> {
        Pair<EnumSkinLayer, String> prevPair = new Pair<>(pair.getFirst(), preset.skinAges.get(level).get().layerSettings.get(pair.getFirst()).get().selectedSkin);

        EnumSkinLayer layer = pair.getFirst();
        String value = pair.getSecond();
        dropdownButtons.get(layer).current = value;
        dropdownButtons.get(layer).updateMessage();
        preset.skinAges.get(level).get().layerSettings.get(layer).get().selectedSkin = DragonEditorScreen.partToTechnical(value);

        // Make sure that when we change a part, the color is properly updated to the default color of the new part
        LayerSettings settings = preset.skinAges.get(level).get().layerSettings.get(layer).get();
        DragonTextureMetadata text = DragonEditorHandler.getSkinTextureMetadata(FakeClientPlayerUtils.getFakePlayer(0, DragonEditorScreen.HANDLER), layer, settings.selectedSkin, dragonType);
        if (text != null && !settings.modifiedColor) {
            settings.hue = text.average_hue;
        }

        HANDLER.getSkinData().compileSkin();
        update();

        return prevPair;
    };

    public final Function<Boolean, Boolean> checkWingsButtonAction = (selected) -> {
        boolean prevSelected = !wingsCheckbox.selected;
        wingsCheckbox.selected = selected;
        preset.skinAges.get(level).get().wings = selected;
        HANDLER.getSkinData().compileSkin();
        update();
        return prevSelected;
    };

    public final Function<Boolean, Boolean> checkDefaultSkinAction = (selected) -> {
        boolean prevSelected = !defaultSkinCheckbox.selected;
        defaultSkinCheckbox.selected = selected;
        preset.skinAges.get(level).get().defaultSkin = selected;
        HANDLER.getSkinData().compileSkin();
        update();
        return prevSelected;
    };

    public static class UndoRedoList {
        private record UndoRedoPair(EditorAction<?> undo, EditorAction<?> redo) {
        }

        private final List<UndoRedoPair> delegate = new ArrayList<>();
        private final int maxSize;
        private int selectedIndex = 0;

        public UndoRedoList(int maxSize) {
            this.maxSize = maxSize;
        }

        public <T> void add(EditorAction<T> action) {
            // Run the action here instead of elsewhere, so that we make sure whatever is being undone is actually done
            T previousState = action.run();

            if (selectedIndex > 0 && action.equals(delegate.get(selectedIndex - 1).redo)) {
                return;
            }

            delegate.subList(selectedIndex, delegate.size()).clear();

            EditorAction<T> undoAction = new EditorAction<>(action.action, previousState);

            delegate.add(new UndoRedoPair(undoAction, action));

            if (delegate.size() > maxSize) {
                delegate.removeFirst();
            } else {
                selectedIndex++;
            }
        }

        public void undo() {
            if (selectedIndex > 0) {
                selectedIndex--;
                delegate.get(selectedIndex).undo.run();
            }
        }

        public void redo() {
            if (selectedIndex < delegate.size()) {
                delegate.get(selectedIndex).redo.run();
                selectedIndex++;
            }
        }

        public void clear() {
            delegate.clear();
            selectedIndex = 0;
        }
    }

    public final UndoRedoList actionHistory = new UndoRedoList(200);

    @Override
    public void render(@NotNull final GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTicks) {
        if (dragonRender == null) {
            // TODO :: Can happen with the dragon-editor command before using the altar first
            init();
        }

        tick += pPartialTicks;
        if (tick >= 60 * 20) {
            save();
            tick = 0;
        }

        if (showUi) {
            dragonRender.x = width / 2 - 70;
            dragonRender.y = guiTop;
            dragonRender.width = 140;
            dragonRender.height = 125;
        } else {
            dragonRender.x = 0;
            dragonRender.width = width;
        }

        FakeClientPlayerUtils.getFakePlayer(0, HANDLER).animationSupplier = () -> animations[curAnimation];

        renderBackground(guiGraphics, pMouseX, pMouseY, pPartialTicks);
        children().stream().filter(DragonUIRenderComponent.class::isInstance).toList().forEach(s -> ((DragonUIRenderComponent) s).render(guiGraphics, pMouseX, pMouseY, pPartialTicks));

        DragonAltarScreen.renderBorders(guiGraphics, backgroundTexture, 0, width, 32, height - 32, width, height);

        TextRenderUtil.drawCenteredScaledText(guiGraphics, width / 2, 10, 2f, title.getString(), DyeColor.WHITE.getTextColor());

        if (showUi) {
            int i = 0;
            for (EnumSkinLayer layers : EnumSkinLayer.values()) {
                String name = layers.name;
                SkinsScreen.drawNonShadowLineBreak(guiGraphics, font, Component.translatable("ds.gui.dragon_editor.part." + name.toLowerCase(Locale.ENGLISH)), (i < 5 ? width / 2 - 100 - 100 : width / 2 + 83) + 45, guiTop + 10 + (i >= 5 ? (i - 5) * 30 : i * 30) - 12, DyeColor.WHITE.getTextColor());
                i++;
            }
        }

        if (showUi) {
            SkinsScreen.drawNonShadowLineBreak(guiGraphics, font, Component.empty().append(WordUtils.capitalize(animations[curAnimation].replace("_", " "))), width / 2, height / 2 + 75 - 22, DyeColor.GRAY.getTextColor());
        }

        for (Renderable widget : new CopyOnWriteArrayList<>(renderables)) {
            widget.render(guiGraphics, pMouseX, pMouseY, pPartialTicks);
        }

        for (GuiEventListener child : children()) {
            if (!(child instanceof DragonUIRenderComponent)) {
                ((Renderable) child).render(guiGraphics, pMouseX, pMouseY, pPartialTicks);
            }
        }

        if (!showUi) {
            for (Renderable renderable : renderables) {
                if (renderable instanceof AbstractWidget widget) {
                    widget.visible = false;
                }
            }
        } else {
            for (Renderable renderable : renderables) {
                if (renderable instanceof AbstractWidget widget) {
                    widget.visible = true;
                }
            }

            for (ColorSelectorButton colorSelectorButton : colorSelectorButtons.values()) {
                DragonTextureMetadata text = DragonEditorHandler.getSkinTextureMetadata(FakeClientPlayerUtils.getFakePlayer(0, HANDLER), colorSelectorButton.layer, this.preset.skinAges.get(this.level).get().layerSettings.get(colorSelectorButton.layer).get().selectedSkin, HANDLER.getType());

                colorSelectorButton.visible = (text != null && text.colorable) && !defaultSkinCheckbox.selected;
            }
        }

        defaultSkinCheckbox.selected = preset.skinAges.get(level).get().defaultSkin;
        showUiCheckbox.visible = true;
    }

    public SkinPreset save() {
        SkinPreset newPreset = new SkinPreset();
        newPreset.deserializeNBT(Minecraft.getInstance().player.registryAccess(), preset.serializeNBT(Minecraft.getInstance().player.registryAccess()));
        String type = dragonType != null ? dragonType.getTypeNameUpperCase() : null;

        DragonEditorRegistry.getSavedCustomizations().skinPresets.computeIfAbsent(type, key -> new HashMap<>());
        DragonEditorRegistry.getSavedCustomizations().skinPresets.get(type).put(currentSelected, newPreset);
        for (DragonLevel dl : presetSelections.keySet()) {
            DragonEditorRegistry.getSavedCustomizations().current.get(type).put(dl, presetSelections.get(dl));
        }

        try {
            Gson gson = GsonFactory.newBuilder().setPrettyPrinting().create();
            FileWriter writer = new FileWriter(DragonEditorRegistry.savedFile);
            gson.toJson(DragonEditorRegistry.getSavedCustomizations(), writer);
            writer.close();
        } catch (IOException e) {
            DragonSurvival.LOGGER.error("An error occurred while trying to save the dragon skin", e);
        }

        return newPreset;
    }

    @Override
    public void renderBackground(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        pGuiGraphics.fill(0, 0, width, height, -350, backgroundColor);
    }

    private void initDummyDragon(final DragonStateHandler localHandler) {
        if (dragonType == null && localHandler.isDragon()) {
            level = localHandler.getLevel();
            dragonType = localHandler.getType();
        }

        if (dragonType == null) {
            return;
        }

        if (dragonBody == null) {
            dragonBody = localHandler.getBody();
            if (dragonBody == null) {
                dragonBody = DragonBodies.getStatic("center");
            }
        }

        if (level == null) {
            level = DragonLevel.NEWBORN;
        }

        String type = dragonType.getTypeNameUpperCase();

        DragonEditorRegistry.getSavedCustomizations().current.computeIfAbsent(type, key -> new HashMap<>());
        DragonEditorRegistry.getSavedCustomizations().current.get(type).putIfAbsent(level, 0);

        currentSelected = DragonEditorRegistry.getSavedCustomizations().current.get(type).get(level);

        DragonEditorRegistry.getSavedCustomizations().skinPresets.computeIfAbsent(type, key -> new HashMap<>());
        DragonEditorRegistry.getSavedCustomizations().skinPresets.get(type).computeIfAbsent(currentSelected, key -> {
            SkinPreset newPreset = new SkinPreset();
            newPreset.initDefaults(dragonType);
            return newPreset;
        });

        SkinPreset curPreset = DragonEditorRegistry.getSavedCustomizations().skinPresets.get(type).get(currentSelected);
        preset = new SkinPreset();
        preset.deserializeNBT(Minecraft.getInstance().player.registryAccess(), curPreset.serializeNBT(Minecraft.getInstance().player.registryAccess()));

        HANDLER.getSkinData().skinPreset = preset;
        HANDLER.getSkinData().compileSkin();

        dragonRender.zoom = (float) (level.size * 4 - 5);

        HANDLER.setHasFlight(true);
        HANDLER.setType(dragonType);
        HANDLER.setBody(dragonBody);
    }

    private boolean dragonTypeWouldChange(DragonStateHandler handler) {
        return handler.getType() != null && !handler.getType().equals(dragonType);
    }

    private boolean dragonBodyWouldChange(DragonStateHandler handler) {
        return handler.getBody() != null && !handler.getBody().equals(dragonBody);
    }

    public boolean dragonWouldChange(DragonStateHandler handler) {
        return (handler.getType() != null && !handler.getType().equals(dragonType)) || (handler.getBody() != null && !handler.getBody().equals(dragonBody));
    }

    @Override
    public void init() {
        super.init();

        guiLeft = (width - 256) / 2;
        guiTop = (height - 120) / 2;

        conf = new DragonEditorConfirmComponent(this, width / 2 - 130 / 2, height / 2 - 181 / 2, 130, 154);
        initDragonRender();

        Minecraft minecraft = getMinecraft();
        if (!hasInit) {
            DragonStateHandler dshandler = DragonStateProvider.getData(minecraft.player);

            initDummyDragon(dshandler);
            update();

            hasInit = true;
        }

        addRenderableWidget(new NewbornEditorButton(this));
        addRenderableWidget(new YoungEditorButton(this));
        addRenderableWidget(new AdultEditorButton(this));

        for (int i1 = 0; i1 < DragonBodies.ORDER.length; i1++) {
            addRenderableWidget(new DragonBodyButton(this, width / 2 - 71 + (i1 * 27), height / 2 + 69, 25, 25, DragonBodies.getStatic(DragonBodies.ORDER[i1]), i1, isEditor));
        }

        int maxWidth = -1;

        for (EnumSkinLayer layer : EnumSkinLayer.values()) {
            String name = layer.getNameUpperCase().charAt(0) + layer.getNameLowerCase().substring(1).replace("_", " ");
            maxWidth = (int) Math.max(maxWidth, font.width(name) * 1.45F);
        }

        int i = 0;
        for (EnumSkinLayer layers : EnumSkinLayer.values()) {
            ArrayList<String> valueList = DragonEditorHandler.getKeys(dragonType, dragonBody, layers);

            if (layers != EnumSkinLayer.BASE) {
                valueList.addFirst(SkinCap.defaultSkinValue);
            }

            String[] values = valueList.toArray(new String[0]);
            String curValue = partToTranslation(preset.skinAges.get(level).get().layerSettings.get(layers).get().selectedSkin);

            DropDownButton btn = new DragonEditorDropdownButton(this, i < 8 ? width / 2 - 210 : width / 2 + 80, guiTop - 5 + (i >= 8 ? (i - 8) * 20 : i * 20), 100, 15, curValue, values, layers);
            dropdownButtons.put(layers, btn);
            addRenderableWidget(btn);
            addRenderableWidget(new ArrowButton(btn.getX() - 15, btn.getY() + 1, 16, 16, false, s -> {
                int index = 0;

                for (int i1 = 0; i1 < btn.values.length; i1++) {
                    if (Objects.equals(btn.values[i1], btn.current)) {
                        index = i1;
                        break;
                    }
                }

                index = Functions.wrap(index - 1, 0, btn.values.length - 1);
                btn.current = btn.values[index];
                btn.setter.accept(btn.current);
                btn.updateMessage();

                LayerSettings settings = preset.skinAges.get(level).get().layerSettings.get(layers).get();
                DragonTextureMetadata text = DragonEditorHandler.getSkinTextureMetadata(FakeClientPlayerUtils.getFakePlayer(0, HANDLER), layers, settings.selectedSkin, dragonType);
                if (text != null && !settings.modifiedColor) {
                    settings.hue = text.average_hue;
                }
            }));

            addRenderableWidget(new ArrowButton(btn.getX() + btn.getWidth() - 1, btn.getY() + 1, 16, 16, true, s -> {
                int index = 0;

                for (int i1 = 0; i1 < btn.values.length; i1++) {
                    if (Objects.equals(btn.values[i1], btn.current)) {
                        index = i1;
                        break;
                    }
                }

                index = Functions.wrap(index + 1, 0, btn.values.length - 1);
                btn.current = btn.values[index];
                btn.setter.accept(btn.current);
                btn.updateMessage();

                LayerSettings settings = preset.skinAges.get(level).get().layerSettings.get(layers).get();
                DragonTextureMetadata text = DragonEditorHandler.getSkinTextureMetadata(FakeClientPlayerUtils.getFakePlayer(0, HANDLER), layers, settings.selectedSkin, dragonType);
                if (text != null && !settings.modifiedColor) {
                    settings.hue = text.average_hue;
                }
            }));

            ColorSelectorButton colorButton = new ColorSelectorButton(this, layers, btn.getX() + 14 + btn.getWidth() + 2, btn.getY(), btn.getHeight(), btn.getHeight(), s -> {
            });
            colorSelectorButtons.put(layers, colorButton);
            addRenderableWidget(colorButton);
            i++;
        }

        addRenderableWidget(new Button(width / 2 + 45, height / 2 + 75 - 27, 15, 15, Component.empty(), btn -> {
            curAnimation += 1;

            if (curAnimation >= animations.length) {
                curAnimation = 0;
            }
        }, Supplier::get) {
            @Override
            public void renderWidget(@NotNull final GuiGraphics guiGraphics, int p_230431_2_, int p_230431_3_, float p_230431_4_) {
                if (isHoveredOrFocused()) {
                    guiGraphics.blit(MagicHUD.widgetTextures, getX(), getY(), (float) 66 / 2, (float) 222 / 2, 11, 17, 128, 128);
                } else {
                    guiGraphics.blit(MagicHUD.widgetTextures, getX(), getY(), (float) 44 / 2, (float) 222 / 2, 11, 17, 128, 128);
                }
            }
        });

        addRenderableWidget(new Button(width / 2 - 45 - 20, height / 2 + 75 - 27, 15, 15, Component.empty(), btn -> {
            curAnimation -= 1;

            if (curAnimation < 0) {
                curAnimation = animations.length - 1;
            }
        }, Supplier::get) {
            @Override
            public void renderWidget(@NotNull final GuiGraphics guiGraphics, int p_230431_2_, int p_230431_3_, float p_230431_4_) {
                RenderSystem.setShaderTexture(0, MagicHUD.widgetTextures);

                if (isHoveredOrFocused()) {
                    guiGraphics.blit(MagicHUD.widgetTextures, getX(), getY(), (float) 22 / 2, (float) 222 / 2, 11, 17, 128, 128);
                } else {
                    guiGraphics.blit(MagicHUD.widgetTextures, getX(), getY(), 0, (float) 222 / 2, 11, 17, 128, 128);
                }
            }
        });

        for (int num = 1; num <= 9; num++) {
            addRenderableWidget(new DragonEditorSlotButton(width / 2 + 200 + 15, guiTop + (num - 1) * 12 + 5 + 30, num, this));
        }


        wingsCheckbox = new ExtendedCheckbox(width / 2 - 220, height - 25, 120, 17, 17, Component.translatable("ds.gui.dragon_editor.wings"), preset.skinAges.get(level).get().wings, p -> actionHistory.add(new EditorAction<>(checkWingsButtonAction, p.selected())));
        wingsCheckbox.setTooltip(Tooltip.create(Component.translatable("ds.gui.dragon_editor.wings.tooltip")));
        wingsCheckbox.selected = preset.skinAges.get(level).get().wings;
        addRenderableWidget(wingsCheckbox);

        defaultSkinCheckbox = new ExtendedCheckbox(width / 2 + 100, height - 25, 120, 17, 17, Component.translatable("ds.gui.dragon_editor.default_skin"), preset.skinAges.get(level).get().defaultSkin, p -> actionHistory.add(new EditorAction<>(checkDefaultSkinAction, p.selected())));
        defaultSkinCheckbox.setTooltip(Tooltip.create(Component.translatable("ds.gui.dragon_editor.default_skin.tooltip")));
        addRenderableWidget(defaultSkinCheckbox);

        ExtendedButton saveButton = new ExtendedButton(width / 2 - 75 - 10, height - 25, 75, 20, Component.translatable("ds.gui.dragon_editor.save"), pButton -> {
        }) {
            Renderable renderButton;
            boolean toggled;

            @Override
            public void renderWidget(@NotNull final GuiGraphics guiGraphics, int mouseX, int mouseY, float partial) {
                super.renderWidget(guiGraphics, mouseX, mouseY, partial);
                if (toggled && (!visible || !confirmation)) {
                    toggled = false;
                    Screen screen = Minecraft.getInstance().screen;
                    screen.children().removeIf(s -> s == conf);
                    screen.renderables.removeIf(s -> s == renderButton);
                }
            }

            @Override
            public void onPress() {
                DragonStateHandler handler = DragonStateProvider.getData(minecraft.player);
                minecraft.player.level().playSound(minecraft.player, minecraft.player.blockPosition(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 1, 0.7f);

                boolean dragonDataIsPreserved = ServerConfig.saveAllAbilities && ServerConfig.saveGrowthStage;
                if (handler.isDragon() && dragonWouldChange(handler) && !dragonDataIsPreserved) {
                    confirmation = true;
                    showUi = false;
                    conf.isBodyTypeChange = dragonBodyWouldChange(handler) && !dragonTypeWouldChange(handler);
                } else {
                    confirm();
                }

                if (confirmation) {
                    if (!toggled) {
                        renderButton = new ExtendedButton(0, 0, 0, 0, Component.empty(), b -> {
                        }) {
                            @Override
                            public void renderWidget(@NotNull final GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
                                if (conf != null && confirmation) {
                                    conf.render(guiGraphics, pMouseX, pMouseY, pPartialTick);
                                }

                                super.renderWidget(guiGraphics, pMouseX, pMouseY, pPartialTick);
                            }
                        };
                        ((ScreenAccessor) DragonEditorScreen.this).dragonSurvival$children().add(conf);
                        renderables.add(renderButton);
                    }
                    toggled = !toggled;
                } else {
                    children().removeIf(s -> s == conf);
                    renderables.removeIf(s -> s == renderButton);
                }
            }
        };
        saveButton.setTooltip(Tooltip.create(Component.translatable("ds.gui.dragon_editor.tooltip.done")));
        addRenderableWidget(saveButton);

        ExtendedButton discardButton = new ExtendedButton(width / 2 + 10, height - 25, 75, 20, Component.translatable("ds.gui.dragon_editor.back"), btn -> Minecraft.getInstance().setScreen(source));
        discardButton.setTooltip(Tooltip.create(Component.translatable("ds.gui.dragon_editor.tooltip.back")));
        addRenderableWidget(discardButton);

        ExtendedButton resetButton = new ExtendedButton(guiLeft + 290, 11, 18, 18, Component.empty(), btn -> {
            // Don't actually modify the skin preset here, do it inside of setSkinPresetAction
            SkinPreset preset = new SkinPreset();
            preset.deserializeNBT(Minecraft.getInstance().player.registryAccess(), this.preset.serializeNBT(Minecraft.getInstance().player.registryAccess()));
            preset.skinAges.put(level, Lazy.of(() -> new SkinAgeGroup(level, dragonType)));
            wingsCheckbox.selected = true;
            actionHistory.add(new EditorAction<>(setSkinPresetAction, preset.serializeNBT(Minecraft.getInstance().player.registryAccess())));
        }) {
            @Override
            public void renderWidget(@NotNull final GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
                guiGraphics.blit(RESET, getX(), getY(), 0, 0, width, height, width, height);
            }
        };
        resetButton.setTooltip(Tooltip.create(Component.translatable("ds.gui.dragon_editor.reset")));
        addRenderableWidget(resetButton);


        ExtendedButton randomButton = new ExtendedButton(guiLeft + 260, 11, 18, 18, Component.empty(), btn -> {
            ArrayList<String> extraKeys = DragonEditorHandler.getKeys(FakeClientPlayerUtils.getFakePlayer(0, HANDLER), EnumSkinLayer.EXTRA);

            extraKeys.removeIf(s -> {
                DragonTextureMetadata text = DragonEditorHandler.getSkinTextureMetadata(FakeClientPlayerUtils.getFakePlayer(0, HANDLER), EnumSkinLayer.EXTRA, s, dragonType);
                if (text == null) {
                    DragonSurvival.LOGGER.error("Key " + s + " not found!");
                    return true;
                }
                return !text.random;
            });

            // Don't actually modify the skin preset here, do it inside of setSkinPresetAction
            SkinPreset preset = new SkinPreset();
            preset.deserializeNBT(Minecraft.getInstance().player.registryAccess(), this.preset.serializeNBT(Minecraft.getInstance().player.registryAccess()));
            for (EnumSkinLayer layer : EnumSkinLayer.values()) {
                ArrayList<String> keys = DragonEditorHandler.getKeys(FakeClientPlayerUtils.getFakePlayer(0, HANDLER), layer);

                if (Objects.equals(layer.name, "Extra")) {
                    keys = extraKeys;
                }

                if (layer != EnumSkinLayer.BASE) {
                    keys.add(SkinCap.defaultSkinValue);
                }

                if (!keys.isEmpty()) {
                    String key = keys.get(minecraft.player.getRandom().nextInt(keys.size()));
                    if (Objects.equals(layer.name, "Extra")) {
                        extraKeys.remove(key);
                    }

                    LayerSettings settings = preset.skinAges.get(level).get().layerSettings.get(layer).get();
                    settings.selectedSkin = key;
                    DragonTextureMetadata text = DragonEditorHandler.getSkinTextureMetadata(FakeClientPlayerUtils.getFakePlayer(0, HANDLER), layer, key, dragonType);

                    if (text != null && text.randomHue) {
                        settings.hue = minecraft.player.getRandom().nextFloat();
                        settings.saturation = 0.25f + minecraft.player.getRandom().nextFloat() * 0.5f;
                        settings.brightness = 0.3f + minecraft.player.getRandom().nextFloat() * 0.3f;
                        settings.modifiedColor = true;
                    } else {
                        if (text != null) {
                            settings.hue = text.average_hue;
                        } else {
                            settings.hue = 0.0f;
                        }
                        settings.saturation = 0.5f;
                        settings.brightness = 0.5f;
                        settings.modifiedColor = true;
                    }
                }
            }

            actionHistory.add(new EditorAction<>(setSkinPresetAction, preset.serializeNBT(Minecraft.getInstance().player.registryAccess())));
        }) {
            @Override
            public void renderWidget(@NotNull final GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
                guiGraphics.blit(RANDOM, getX(), getY(), 0, 0, 16, 16, 16, 16);
            }
        };
        randomButton.setTooltip(Tooltip.create(Component.translatable("ds.gui.dragon_editor.random")));
        addRenderableWidget(randomButton);

        UndoRedoButton undoButton = new UndoRedoButton(guiLeft + 318, 11, 18, 18, false, button -> actionHistory.undo());
        undoButton.setTooltip(Tooltip.create(Component.translatable("ds.gui.dragon_editor.undo")));
        addRenderableWidget(undoButton);

        UndoRedoButton redoButton = new UndoRedoButton(guiLeft + 340, 11, 18, 18, true, button -> actionHistory.redo());
        redoButton.setTooltip(Tooltip.create(Component.translatable("ds.gui.dragon_editor.redo")));
        addRenderableWidget(redoButton);

        ExtendedButton saveSlotButton = new ExtendedButton(width / 2 + 213, guiTop + 10, 18, 18, Component.empty(), button -> { /* Nothing to do */ }) {
            @Override
            public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
                guiGraphics.blit(SAVE, getX(), getY(), 0, 0, 16, 16, 16, 16);
            }
        };
        saveSlotButton.setTooltip(Tooltip.create(Component.translatable("ds.gui.dragon_editor.save_slot")));
        addRenderableWidget(saveSlotButton);

        addRenderableWidget(new CopySettingsButton(this, guiLeft + 230, 11, 18, 18, Component.translatable("ds.gui.dragon_editor.copy"), button -> { /* Nothing to do */ }));

        showUiCheckbox = new ExtendedCheckbox(guiLeft - 15, 11, 40, 18, 18, Component.translatable("ds.gui.dragon_editor.show_ui"), showUi, p -> showUi = p.selected());
        addRenderableWidget(showUiCheckbox);
        addRenderableWidget(new BackgroundColorButton(guiLeft - 45, 11, 18, 18, Component.empty(), s -> {
        }, this));
        addRenderableWidget(new HelpButton(dragonType, guiLeft - 75, 11, 15, 15, "ds.help.customization", 1));
    }

    public void update() {
        if (dragonType != null) {
            HANDLER.setType(dragonType);
        }

        HANDLER.setBody(dragonBody);
        HANDLER.getSkinData().skinPreset = preset;
        HANDLER.setSize(level.size);
        HANDLER.setHasFlight(true);

        if (currentSelected != lastSelected) {
            preset = new SkinPreset();

            if (DragonEditorRegistry.getSavedCustomizations().skinPresets.containsKey(dragonType.getTypeNameUpperCase())) {
                preset.deserializeNBT(Minecraft.getInstance().player.registryAccess(), DragonEditorRegistry.getSavedCustomizations().skinPresets.get(dragonType.getTypeNameUpperCase()).get(currentSelected).serializeNBT(Minecraft.getInstance().player.registryAccess()));
            }
            HANDLER.getSkinData().skinPreset = preset;
        }
        presetSelections.put(level, currentSelected);

        lastSelected = currentSelected;
    }

    private void initDragonRender() {
        children().removeIf(DragonUIRenderComponent.class::isInstance);

        float yRot = -3, xRot = -5, zoom = 0, xOffset = 0, yOffset = 0;
        if (dragonRender != null) {
            yRot = dragonRender.yRot;
            xRot = dragonRender.xRot;
            zoom = dragonRender.zoom;
            xOffset = dragonRender.xOffset;
            yOffset = dragonRender.yOffset;
        }

        dragonRender = new DragonUIRenderComponent(this, width / 2 - 70, guiTop, 140, 125, () -> FakeClientPlayerUtils.getFakeDragon(0, HANDLER));
        dragonRender.xRot = xRot;
        dragonRender.yRot = yRot;
        dragonRender.zoom = zoom;
        dragonRender.xOffset = xOffset;
        dragonRender.yOffset = yOffset;

        ((ScreenAccessor) this).dragonSurvival$children().addFirst(dragonRender);
    }

    public void confirm() {
        DragonStateProvider.getOptional(minecraft.player).ifPresent(cap -> {
            minecraft.player.level().playSound(minecraft.player, minecraft.player.blockPosition(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 1, 0.7f);

            if (!cap.isDragon() || dragonWouldChange(cap)) {
                minecraft.player.sendSystemMessage(Component.translatable("ds." + dragonType.getTypeNameLowerCase() + "_dragon_choice"));

                if (dragonType == null && cap.getType() != null) {
                    DragonCommand.reInsertClawTools(minecraft.player, cap);
                }

                cap.setType(dragonType, minecraft.player);
                cap.setBody(dragonBody, minecraft.player);

                double size = cap.getSavedDragonSize(cap.getTypeName());

                if (!ServerConfig.saveGrowthStage || size == 0) {
                    cap.setSize(DragonLevel.NEWBORN.size, minecraft.player);
                } else {
                    cap.setSize(size, minecraft.player);
                }

                cap.setHasFlight(ServerConfig.saveGrowthStage ? cap.hasFlight() || ServerFlightHandler.startWithLevitation : ServerFlightHandler.startWithLevitation);
                cap.setIsDiving(false);
                cap.getMovementData().spinLearned = ServerConfig.saveGrowthStage && cap.getMovementData().spinLearned;

                HANDLER.getSkinData().skinPreset = save();
                cap.getSkinData().renderAdult = ClientDragonRenderer.renderAdultSkin;
                cap.getSkinData().renderYoung = ClientDragonRenderer.renderYoungSkin;
                cap.getSkinData().renderNewborn = ClientDragonRenderer.renderNewbornSkin;

                cap.altarCooldown = Functions.secondsToTicks(ServerConfig.altarUsageCooldown);
                cap.hasUsedAltar = true;

                PacketDistributor.sendToServer(new SyncComplete.Data(minecraft.player.getId(), cap.serializeNBT(minecraft.player.registryAccess())));
            } else {
                PacketDistributor.sendToServer(new SyncPlayerSkinPreset.Data(minecraft.player.getId(), save().serializeNBT(minecraft.player.registryAccess())));
            }
        });

        minecraft.player.closeContainer();
    }

    @Override
    public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
        if (dragonRender != null && dragonRender.isMouseOver(pMouseX, pMouseY)) {
            return dragonRender.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);
        }

        return super.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);
    }

    public static String partToTranslation(final String part) {
        String text = "ds.skin_part." + DragonEditorScreen.HANDLER.getTypeNameLowerCase() + "." + part.toLowerCase(Locale.ENGLISH);

        if (I18n.exists(text)) {
            return text;
        }

        return part;
    }

    public static String partToTechnical(final String part) {
        return part.replace("ds.skin_part.", "").replace(DragonEditorScreen.HANDLER.getTypeNameLowerCase() + ".", "");
    }

    @SubscribeEvent
    public static void undoRedoKeybinds(ScreenEvent.KeyPressed.Post event) {
        if (event.getScreen() instanceof DragonEditorScreen screen) {
            if (event.getKeyCode() == GLFW.GLFW_KEY_Z && event.getModifiers() == GLFW.GLFW_MOD_CONTROL) {
                screen.actionHistory.undo();
            } else if (event.getKeyCode() == GLFW.GLFW_KEY_Y && event.getModifiers() == GLFW.GLFW_MOD_CONTROL) {
                screen.actionHistory.redo();
            }
        }
    }
}
