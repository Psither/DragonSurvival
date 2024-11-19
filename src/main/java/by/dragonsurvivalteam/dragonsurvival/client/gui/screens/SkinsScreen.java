package by.dragonsurvivalteam.dragonsurvival.client.gui.screens;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.client.gui.hud.MagicHUD;
import by.dragonsurvivalteam.dragonsurvival.client.gui.screens.dragon_editor.buttons.DragonSkinBodyButton;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.TabButton;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.generic.ArrowButton;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.generic.HelpButton;
import by.dragonsurvivalteam.dragonsurvival.client.render.ClientDragonRenderer;
import by.dragonsurvivalteam.dragonsurvival.client.render.entity.dragon.DragonRenderer;
import by.dragonsurvivalteam.dragonsurvival.client.skins.DragonSkins;
import by.dragonsurvivalteam.dragonsurvival.client.skins.SkinObject;
import by.dragonsurvivalteam.dragonsurvival.client.util.FakeClientPlayerUtils;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.capability.subcapabilities.SkinCap;
import by.dragonsurvivalteam.dragonsurvival.common.entity.DragonEntity;
import by.dragonsurvivalteam.dragonsurvival.config.ConfigHandler;
import by.dragonsurvivalteam.dragonsurvival.network.dragon_editor.SyncDragonSkinSettings;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.tags.DSBodyTags;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.DragonBody;
import by.dragonsurvivalteam.dragonsurvival.util.DragonLevel;
import by.dragonsurvivalteam.dragonsurvival.util.DragonUtils;
import com.ibm.icu.impl.Pair;
import com.mojang.math.Axis;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.core.Holder;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import software.bernie.geckolib.cache.object.GeoBone;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.function.Supplier;

public class SkinsScreen extends Screen {
    @Translation(type = Translation.Type.MISC, comments = "Skin Settings")
    private static final String SETTINGS = Translation.Type.GUI.wrap("skin_screen.settings");

    @Translation(type = Translation.Type.MISC, comments = "Enable / disable skins")
    private static final String TOGGLE = Translation.Type.GUI.wrap("skin_screen.toggle");

    @Translation(type = Translation.Type.MISC, comments = "This author does not have a skin for this stage.")
    private static final String NO_SKIN = Translation.Type.GUI.wrap("skin_screen.no_skin");

    @Translation(type = Translation.Type.MISC, comments = "You do not have a skin for this stage.")
    private static final String NO_SKIN_SELF = Translation.Type.GUI.wrap("skin_screen.no_skin_self");

    @Translation(type = Translation.Type.MISC, comments = "■ Join our §6discord server§r!§7 Read the Rules, FAQ and Wiki before you ask anything.")
    private static final String DISCORD = Translation.Type.GUI.wrap("skin_screen.discord");

    @Translation(type = Translation.Type.MISC, comments = "■ This is a link to our §6Wiki§r dedicated to making your own skin!§7 Remember that this will be very difficult, and requires knowledge of graphic editors.")
    private static final String WIKI = Translation.Type.GUI.wrap("skin_screen.wiki");

    @Translation(type = Translation.Type.MISC, comments = {
            "■ A §6Skin§r is a self-made texture for your dragon. In this tab, you can disable the display of your skin for your dragon at different stages, as well as skins of other players.",
            "■ Skins are not built in the Dragon Editor, but created using outside tools.",
            "§7■ If you are interested in how to make your own skin or take a commission, use the buttons on the right."
    })
    private static final String HELP = Translation.Type.GUI.wrap("skin_screen.help");

    @Translation(type = Translation.Type.MISC, comments = "Your dragon")
    private static final String SELF = Translation.Type.GUI.wrap("skin_screen.self");

    @Translation(type = Translation.Type.MISC, comments = "■ This is what §6you§r look like now.")
    private static final String SELF_INFO = Translation.Type.GUI.wrap("skin_screen.self_info");

    @Translation(type = Translation.Type.MISC, comments = "Random")
    private static final String RANDOM = Translation.Type.GUI.wrap("skin_screen.random");

    @Translation(type = Translation.Type.MISC, comments = "■ Shows a randomly selected §6other player§r§f who uploaded a skin. You §ccan't use§r§f their appearance for yourself!§7 Only look and admire! >:D")
    private static final String RANDOM_INFO = Translation.Type.GUI.wrap("skin_screen.random_info");

    @Translation(type = Translation.Type.MISC, comments = "Show player skins")
    private static final String OTHERS = Translation.Type.GUI.wrap("skin_screen.others");

    private static final ResourceLocation BACKGROUND_TEXTURE = ResourceLocation.fromNamespaceAndPath(DragonSurvival.MODID, "textures/gui/skin_interface.png");
    private static final ResourceLocation DISCORD_TEXTURE = ResourceLocation.fromNamespaceAndPath(DragonSurvival.MODID, "textures/gui/discord_button.png");
    private static final ResourceLocation WIKI_TEXTURE = ResourceLocation.fromNamespaceAndPath(DragonSurvival.MODID, "textures/gui/wiki_button.png");
    private static final ResourceLocation UNCHECKED = ResourceLocation.fromNamespaceAndPath(DragonSurvival.MODID, "textures/gui/unchecked.png");
    private static final ResourceLocation CHECKED = ResourceLocation.fromNamespaceAndPath(DragonSurvival.MODID, "textures/gui/dragon_claws_checked.png");

    private static final String DISCORD_URL = "https://discord.gg/8SsB8ar";
    private static final String WIKI_URL = "https://github.com/DragonSurvivalTeam/DragonSurvival/wiki/3.-Customization";
    private static final ArrayList<String> SEEN_SKINS = new ArrayList<>();

    private static DragonLevel level = DragonLevel.ADULT;
    private static ResourceLocation skinTexture;
    private static ResourceLocation glowTexture;
    private static String playerName;
    private static String lastPlayerName;
    private static boolean noSkin;
    private static boolean loading;

    public final DragonStateHandler handler = new DragonStateHandler();
    public Screen sourceScreen;

    /**
     * Widgets which belong to the dragon body logic <br>
     * (they are stored to properly reference (and remove) them when using the arrow buttons to navigate through the bodies)
     */
    private final List<AbstractWidget> dragonBodyWidgets = new ArrayList<>();
    private int bodySelectionOffset;

    private final int imageWidth = 164;
    private final int imageHeight = 128;
    private int guiLeft;
    private int guiTop;
    private float yRot = -3;
    private float xRot = -5;
    private float zoom = 0;
    private URI clickedLink;

    // To avoid having to retrieve the player capabilities every render tick
    private boolean renderNewborn;
    private boolean renderYoung;
    private boolean renderAdult;

    public SkinsScreen(Screen sourceScreen) {
        super(Component.empty());
        this.sourceScreen = sourceScreen;

        LocalPlayer localPlayer = sourceScreen.getMinecraft().player;
        //noinspection DataFlowIssue -> player should not be null
        SkinCap skinData = DragonStateProvider.getData(localPlayer).getSkinData();
        renderNewborn = skinData.renderNewborn;
        renderYoung = skinData.renderYoung;
        renderAdult = skinData.renderAdult;
    }

    @Override
    @SuppressWarnings("DataFlowIssue") // player is present
    public void render(@NotNull final GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (minecraft == null) {
            return;
        }

        this.renderBlurredBackground(partialTick);

        int startX = guiLeft;
        int startY = guiTop;

        final GeoBone neckandHead = ClientDragonRenderer.dragonModel.getAnimationProcessor().getBone("Neck");

        if (neckandHead != null) {
            neckandHead.setHidden(false);
        }

        DragonEntity dragon = FakeClientPlayerUtils.getFakeDragon(0, handler);
        EntityRenderer<? super DragonEntity> dragonRenderer = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(dragon);

        if (noSkin && Objects.equals(playerName, minecraft.player.getGameProfile().getName())) {
            ClientDragonRenderer.dragonModel.setOverrideTexture(null);
            ((DragonRenderer) dragonRenderer).glowTexture = null;
        } else {
            ClientDragonRenderer.dragonModel.setOverrideTexture(skinTexture);
            ((DragonRenderer) dragonRenderer).glowTexture = glowTexture;
        }

        float scale = zoom;

        if (!loading) {
            DragonStateHandler playerData = DragonStateProvider.getData(minecraft.player);

            handler.setHasFlight(true);
            handler.setSize(level.size);

            if (!DragonUtils.isType(handler, playerData.getType())) {
                handler.setType(playerData.getType());
            }

            if (handler.getBody() == null) {
                handler.setBody(playerData.getBody());
            }

            handler.getSkinData().skinPreset.initDefaults(handler);

            if (noSkin && Objects.equals(playerName, minecraft.player.getGameProfile().getName())) {
                handler.getSkinData().skinPreset.deserializeNBT(minecraft.player.registryAccess(), playerData.getSkinData().skinPreset.serializeNBT(Minecraft.getInstance().player.registryAccess()));
            } else {
                handler.getSkinData().skinPreset.skinAges.get(level).get().defaultSkin = true;
            }

            FakeClientPlayerUtils.getFakePlayer(0, handler).animationSupplier = () -> "fly_head_locked_magic";

            Quaternionf quaternion = Axis.ZP.rotationDegrees(180.0F);
            quaternion.mul(Axis.XP.rotationDegrees(yRot * 10.0F));
            quaternion.rotateY((float) Math.toRadians(180 - xRot * 10));
            InventoryScreen.renderEntityInInventory(guiGraphics, startX + 15, startY + 70, (int) scale, new Vector3f(0, 0, 100), quaternion, null, dragon);
        }

        ((DragonRenderer) dragonRenderer).glowTexture = null;

        guiGraphics.blit(BACKGROUND_TEXTURE, startX + 128, startY, 0, 0, 164, 256);
        drawNonShadowString(guiGraphics, minecraft.font, Component.translatable(SETTINGS).withStyle(ChatFormatting.BLACK), startX + 128 + imageWidth / 2, startY + 7, -1);
        guiGraphics.drawCenteredString(minecraft.font, Component.translatable(TOGGLE), startX + 128 + imageWidth / 2, startY + 30, -1);
        drawNonShadowString(guiGraphics, minecraft.font, Component.empty().append(playerName + " - ").append(level.translatableName()).withStyle(ChatFormatting.GRAY), startX + 15, startY - 15, -1);

        if (!loading && noSkin) {
            if (playerName.equals(minecraft.player.getGameProfile().getName())) {
                drawNonShadowLineBreak(guiGraphics, minecraft.font, Component.translatable(NO_SKIN_SELF).withStyle(ChatFormatting.WHITE), startX + 40, startY + imageHeight - 20, -1);
            } else {
                drawNonShadowLineBreak(guiGraphics, minecraft.font, Component.translatable(NO_SKIN).withStyle(ChatFormatting.WHITE), startX + 65, startY + imageHeight - 20, -1);
            }
        }

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override // We override this to not blur the background
    public void renderBackground(@NotNull GuiGraphics graphics, int pMouseX, int pMouseY, float pPartialTick) {
        // Don't render the vanilla background, it darkens the UI in an undesirable way
    }

    public static void drawNonShadowString(@NotNull final GuiGraphics guiGraphics, final Font font, final Component component, int x, int y, int color) {
        guiGraphics.drawString(font, Language.getInstance().getVisualOrder(component), x - font.width(component) / 2, y, color, false);
    }

    public static void drawNonShadowLineBreak(@NotNull final GuiGraphics guiGraphics, final Font font, final Component component, int x, int y, int color) {
        List<FormattedText> wrappedLine = font.getSplitter().splitLines(component, 150, Style.EMPTY);

        for (int i = 0; i < wrappedLine.size(); i++) {
            FormattedText properties = wrappedLine.get(i);
            guiGraphics.drawString(font, Language.getInstance().getVisualOrder(properties), x - font.width(component.getVisualOrderText()) / 2, y + i * 9, color, false);
        }
    }

    @Override
    public void init() {
        Minecraft minecraft = getMinecraft();
        LocalPlayer player = minecraft.player;
        super.init();

        guiLeft = (width - 256) / 2;
        guiTop = (height - 128) / 2;

        int startX = guiLeft;
        int startY = guiTop;

        if (playerName == null) {
            playerName = minecraft.player.getGameProfile().getName();
        }

        setTextures();

        addRenderableWidget(new TabButton(startX + 128 + 4, startY - 26, TabButton.Type.INVENTORY_TAB, this));
        addRenderableWidget(new TabButton(startX + 128 + 33, startY - 26, TabButton.Type.ABILITY_TAB, this));
        addRenderableWidget(new TabButton(startX + 128 + 62, startY - 26, TabButton.Type.GITHUB_REMINDER_TAB, this));
        addRenderableWidget(new TabButton(startX + 128 + 91, startY - 28, TabButton.Type.SKINS_TAB, this));

        addDragonBodyWidgets();

        // Button to enable / disable rendering of the newborn dragon skin
        addRenderableWidget(new Button(startX + 128, startY + 45, imageWidth, 20, DragonLevel.NEWBORN.translatableName(), button -> {
            DragonStateHandler handler = DragonStateProvider.getData(player);
            boolean newValue = !handler.getSkinData().renderNewborn;

            handler.getSkinData().renderNewborn = newValue;
            renderNewborn = newValue;
            ConfigHandler.updateConfigValue("render_newborn_skin", handler.getSkinData().renderNewborn);
            PacketDistributor.sendToServer(new SyncDragonSkinSettings.Data(player.getId(), handler.getSkinData().renderNewborn, handler.getSkinData().renderYoung, handler.getSkinData().renderAdult));
            setTextures();
        }, Supplier::get) {
            @Override
            public void renderWidget(GuiGraphics guiGraphics, int p_230431_2_, int p_230431_3_, float p_230431_4_) {
                super.renderWidget(guiGraphics, p_230431_2_, p_230431_3_, p_230431_4_);
                guiGraphics.blit(renderNewborn ? CHECKED : UNCHECKED, getX() + 3, getY() + 3, 0, 0, 13, 13, 13, 13);
            }
        });

        // Button to enable / disable rendering of the young dragon skin
        addRenderableWidget(new Button(startX + 128, startY + 45 + 23, imageWidth, 20, DragonLevel.YOUNG.translatableName(), button -> {
            DragonStateHandler handler = DragonStateProvider.getData(player);
            boolean newValue = !handler.getSkinData().renderYoung;

            handler.getSkinData().renderYoung = newValue;
            renderYoung = newValue;
            ConfigHandler.updateConfigValue("render_young_skin", handler.getSkinData().renderYoung);
            PacketDistributor.sendToServer(new SyncDragonSkinSettings.Data(player.getId(), handler.getSkinData().renderNewborn, handler.getSkinData().renderYoung, handler.getSkinData().renderAdult));
            setTextures();
        }, Supplier::get) {
            @Override
            protected void renderWidget(@NotNull final GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
                super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
                guiGraphics.blit(renderYoung ? CHECKED : UNCHECKED, getX() + 3, getY() + 3, 0, 0, 13, 13, 13, 13);
            }
        });

        // Button to enable / disable rendering of the adult dragon skin
        addRenderableWidget(new Button(startX + 128, startY + 45 + 46, imageWidth, 20, DragonLevel.ADULT.translatableName(), button -> {
            DragonStateHandler handler = DragonStateProvider.getData(getMinecraft().player);
            boolean newValue = !handler.getSkinData().renderAdult;

            handler.getSkinData().renderAdult = newValue;
            renderAdult = newValue;
            ConfigHandler.updateConfigValue("render_adult_skin", handler.getSkinData().renderAdult);
            PacketDistributor.sendToServer(new SyncDragonSkinSettings.Data(getMinecraft().player.getId(), handler.getSkinData().renderNewborn, handler.getSkinData().renderYoung, handler.getSkinData().renderAdult));
            setTextures();
        }, Supplier::get) {
            @Override
            protected void renderWidget(@NotNull final GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
                super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
                guiGraphics.blit(renderAdult ? CHECKED : UNCHECKED, getX() + 3, getY() + 3, 0, 0, 13, 13, 13, 13);
            }
        });

        // Button to enable / disable the rendering of customized skins (of other players)
        addRenderableWidget(new Button(startX + 128, startY + 128, imageWidth, 20, Component.translatable(OTHERS), button -> {
            ClientDragonRenderer.renderOtherPlayerSkins = !ClientDragonRenderer.renderOtherPlayerSkins;
            ConfigHandler.updateConfigValue("render_other_players_custom_skins", ClientDragonRenderer.renderOtherPlayerSkins);
            setTextures();
        }, Supplier::get) {
            @Override
            protected void renderWidget(@NotNull final GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
                super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
                guiGraphics.blit(ClientDragonRenderer.renderOtherPlayerSkins ? CHECKED : UNCHECKED, getX() + 3, getY() + 3, 0, 0, 13, 13, 13, 13);
            }
        });

        Button discordURLButton = new Button(startX + 128 + imageWidth / 2 - 8 - 25, startY + 128 + 30, 16, 16, Component.empty(), button -> {
            try {
                clickedLink = new URI(DISCORD_URL);
                minecraft.setScreen(new ConfirmLinkScreen(this::confirmLink, DISCORD_URL, false));
            } catch (URISyntaxException exception) {
                DragonSurvival.LOGGER.error(exception);
            }
        }, Supplier::get) {
            @Override
            protected void renderWidget(@NotNull final GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
                guiGraphics.blit(DISCORD_TEXTURE, getX(), getY(), 0, 0, 16, 16, 16, 16);
            }
        };
        discordURLButton.setTooltip(Tooltip.create(Component.translatable(DISCORD)));
        addRenderableWidget(discordURLButton);

        Button wikiButton = new Button(startX + 128 + imageWidth / 2 - 8 + 25, startY + 128 + 30, 16, 16, Component.empty(), button -> {
            try {
                URI uri = new URI(WIKI_URL);
                clickedLink = uri;
                minecraft.setScreen(new ConfirmLinkScreen(this::confirmLink, WIKI_URL, false));
            } catch (URISyntaxException exception) {
                DragonSurvival.LOGGER.error(exception);
            }
        }, Supplier::get) {
            @Override
            protected void renderWidget(@NotNull final GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
                guiGraphics.blit(WIKI_TEXTURE, getX(), getY(), 0, 0, 16, 16, 16, 16);
            }
        };
        wikiButton.setTooltip(Tooltip.create(Component.translatable(WIKI)));
        addRenderableWidget(wikiButton);

        addRenderableWidget(new HelpButton(startX + 128 + imageWidth / 2 - 8, startY + 128 + 30, 16, 16, HELP, 1));

        addRenderableWidget(Button.builder(Component.translatable(SELF), button -> {
            playerName = minecraft.player.getGameProfile().getName();
            setTextures();
        }).bounds(startX - 60, startY + 128, 90, 20).tooltip(Tooltip.create(Component.translatable(SELF_INFO))).build());

        addRenderableWidget(Button.builder(Component.translatable(RANDOM), button -> {
            ArrayList<Pair<DragonLevel, String>> skins = new ArrayList<>();
            HashSet<String> users = new HashSet<>();
            Random random = new Random();

            for (Map.Entry<DragonLevel, HashMap<String, SkinObject>> ent : DragonSkins.SKIN_USERS.entrySet()) {
                for (Map.Entry<String, SkinObject> user : ent.getValue().entrySet()) {
                    if (!user.getValue().glow) {
                        skins.add(Pair.of(ent.getKey(), user.getKey()));
                        users.add(user.getKey());
                    }
                }
            }

            skins.removeIf(c -> SEEN_SKINS.contains(c.second));
            if (!skins.isEmpty()) {
                Pair<DragonLevel, String> skin = skins.get(random.nextInt(skins.size()));

                if (skin != null) {
                    level = skin.first;
                    playerName = skin.second;

                    SEEN_SKINS.add(skin.second);

                    if (SEEN_SKINS.size() >= users.size() / 2) {
                        SEEN_SKINS.remove(0);
                    }

                    setTextures();
                }
            }
        }).bounds(startX + 35, startY + 128, 60, 20).tooltip(Tooltip.create(Component.translatable(RANDOM_INFO))).build());

        addRenderableWidget(new Button(startX + 90, startY - 20, 11, 17, Component.empty(), button -> {
            int pos = level.ordinal() + 1;
            if (pos == DragonLevel.values().length) {
                pos = 0;
            }
            level = DragonLevel.values()[pos];
            setTextures();
        }, Supplier::get) {
            @Override
            protected void renderWidget(@NotNull final GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
                if (isHovered()) {
                    guiGraphics.blit(MagicHUD.WIDGET_TEXTURES, getX(), getY(), (float) 66 / 2, (float) 222 / 2, 11, 17, 128, 128);
                } else {
                    guiGraphics.blit(MagicHUD.WIDGET_TEXTURES, getX(), getY(), (float) 44 / 2, (float) 222 / 2, 11, 17, 128, 128);
                }
            }
        });

        addRenderableWidget(new Button(startX - 70, startY - 20, 11, 17, Component.empty(), button -> {
            int pos = level.ordinal() - 1;
            if (pos == -1) {
                pos = DragonLevel.values().length - 1;
            }
            level = DragonLevel.values()[pos];
            setTextures();
        }, Supplier::get) {
            @Override
            protected void renderWidget(@NotNull final GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
                if (isHovered()) {
                    guiGraphics.blit(MagicHUD.WIDGET_TEXTURES, getX(), getY(), (float) 22 / 2, (float) 222 / 2, 11, 17, 128, 128);
                } else {
                    guiGraphics.blit(MagicHUD.WIDGET_TEXTURES, getX(), getY(), 0, (float) 222 / 2, 11, 17, 128, 128);
                }
            }
        });
    }

    private void addDragonBodyWidgets() {
        List<Holder<DragonBody>> bodies = DSBodyTags.getOrdered(null);

        int buttonWidth = 25;
        int gap = 3;

        boolean cannotFit = bodies.size() > 5;
        int elements = Math.min(5, bodies.size());
        int requiredWidth = elements * buttonWidth + (elements - 1) * gap;

        for (int index = 0; index < 5; index++) {
            // To make sure the buttons are centered if there are less than 5 elements (max. supported by the current GUI)
            int x = (width - requiredWidth - /* offset to the left */ 225) / 2 + (index * (buttonWidth + gap));
            int y = height / 2 + 90;

            AbstractWidget widget;

            if (cannotFit && /* leftmost element */ index == 0) {
                widget = new ArrowButton(x, y, 25, 25, false, button -> {
                    if (bodySelectionOffset > 0) {
                        bodySelectionOffset--;
                        removeDragonBodyWidgets();
                        addDragonBodyWidgets();
                    }
                });
            } else if (cannotFit && /* rightmost element */ index == 4) {
                widget = new ArrowButton(x, y, 25, 25, true, button -> {
                    // If there are 5 bodies we can navigate next two times, showing 0 - 2,  1 - 3 and 2 - 4
                    if (bodySelectionOffset < bodies.size() - /* shown elements */ 3) {
                        bodySelectionOffset++;
                        removeDragonBodyWidgets();
                        addDragonBodyWidgets();
                    }
                });
            } else {
                // Subtract 1 since index 0 is an arrow button (otherwise we would skip the first body)
                int selectionIndex = index + bodySelectionOffset - (cannotFit ? 1 : 0);
                Holder<DragonBody> body = bodies.get(selectionIndex);
                widget = new DragonSkinBodyButton(this, x, y, 25, 25, body);
            }

            dragonBodyWidgets.add(widget);
            addRenderableWidget(widget);
        }
    }

    private void removeDragonBodyWidgets() {
        dragonBodyWidgets.forEach(this::removeWidget);
        dragonBodyWidgets.clear();
    }

    public void setTextures() {
        loading = true;

        ResourceLocation skinTexture = DragonSkins.getPlayerSkin(playerName, level);
        ResourceLocation glowTexture = null;
        boolean defaultSkin = false;

        if (!DragonSkins.renderStage(minecraft.player, level) && playerName.equals(minecraft.player.getGameProfile().getName()) || skinTexture == null) {
            skinTexture = null;
            defaultSkin = true;
        }

        if (skinTexture != null) {
            glowTexture = DragonSkins.getPlayerGlow(playerName, level);
        }

        SkinsScreen.glowTexture = glowTexture;
        SkinsScreen.skinTexture = skinTexture;

        if (Objects.equals(lastPlayerName, playerName) || lastPlayerName == null) {
            zoom = level.size;
        }

        noSkin = defaultSkin;
        loading = false;
        lastPlayerName = playerName;
    }

    private void confirmLink(boolean p_231162_1_) {
        if (p_231162_1_) {
            openLink(clickedLink);
        }

        clickedLink = null;
        minecraft.setScreen(this);
    }

    private void openLink(URI uri) {
        Util.getPlatform().openUri(uri);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean mouseDragged(double x1, double y1, int p_231045_5_, double x2, double y2) {
        xRot -= (float) (x2 / 5);
        yRot -= (float) (y2 / 5);

        return super.mouseDragged(x1, y1, p_231045_5_, x2, y2);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        zoom += (float) scrollY;
        zoom = Mth.clamp(zoom, 10, 80);

        return true;
    }
}