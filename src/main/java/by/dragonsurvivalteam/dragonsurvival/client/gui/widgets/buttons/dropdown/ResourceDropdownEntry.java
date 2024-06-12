package by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.dropdown;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.TooltipFlag;
import net.minecraftforge.client.gui.widget.ExtendedButton;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class ResourceDropdownEntry extends DropdownEntry {
	private final int num;
	private final ResourceEntry entry;
	private final Consumer<ResourceEntry> setter;
	private final ResourceTextField source;
	private ExtendedButton button;

	public ResourceDropdownEntry(ResourceTextField source, int num, ResourceEntry entry, Consumer<ResourceEntry> setter){
		this.num = num;
		this.entry = entry;
		this.setter = setter;
		this.source = source;
	}

	@Override
	public @NotNull List<? extends GuiEventListener> children() {
		return button != null ? ImmutableList.of(button) : new ArrayList<>();
	}

	@Override
    public void render(@NotNull final GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isMouseOver, float partialTicks) {
        if (button == null) {
            button = new ExtendedButton(left + 3, 0, width - 12, height, null, null) {
                private int tick = 0;

                @Override
                public @NotNull Component getMessage() {
                    return Component.empty();
                }

                @Override
                public void onPress() {
                    if (!source.isFocused()) {
                        return;
                    }

                    setter.accept(entry);
                }

                @Override
                public void renderWidget(@NotNull final GuiGraphics guiGraphics , int mouseX, int mouseY, float partialTicks) {
                    if (!source.isFocused()) {
                        return;
                    }

                    // TODO: Figure out how to get rid of list here
                    if (getY() + height > list.getBottom() - 3 || getY() < list.getTop() + 3) {
                        return;
                    }

                    guiGraphics.pose().pushPose();
                    RenderSystem.enableDepthTest();
                    // Make sure it gets rendered above the other resource text fields
//                    guiGraphics.pose().translate(0, 0, 200);

                    if (entry != null) {
                        if (tick >= 1) {
                            entry.tick();
                            tick = 0;
                        } else {
                            tick++;
                        }

                        int color;

                        if (num % 2 == 0) {
                            color = new Color(0.2F, 0.2F, 0.2F, 1F).getRGB();
                        } else {
                             color = new Color(0.1F, 0.1F, 0.1F, 1F).getRGB();
                        }

                        if (isHovered) {
                            color = new Color(color).brighter().getRGB();
                        }

                        guiGraphics.pose().pushPose();
//                        guiGraphics.pose().translate(0, 0, 400);

                        // Draws the background per entry
                        guiGraphics.fill(getX(), getY(), getX() + width, getY() + height, color);
                        guiGraphics.drawString(Minecraft.getInstance().font, Component.empty().append(Minecraft.getInstance().font.substrByWidth(Component.empty().append(entry.id), width - 20).getString()), getX() + 25, getY() + 5, DyeColor.WHITE.getTextColor());

                        if (!entry.isEmpty()) {
                            guiGraphics.renderItem(entry.getDisplayItem(), getX() + 3, getY() + 1);

                            if (entry.tag) {
                                guiGraphics.drawString(Minecraft.getInstance().font, Component.empty().append("#"), getX() + 14, getY() + 10, DyeColor.WHITE.getTextColor(), true);
                            }

                            if (isHovered) {
                                guiGraphics.pose().pushPose();
//                                guiGraphics.pose().translate(0, 0, 450);
                                List<Component> lines = entry.getDisplayItem().getTooltipLines(Minecraft.getInstance().player, TooltipFlag.Default.NORMAL);
                                guiGraphics.renderComponentTooltip(Minecraft.getInstance().font, lines, mouseX, mouseY);
                                guiGraphics.pose().popPose();
                            }
                        }

                        RenderSystem.disableDepthTest();
                    }

                    guiGraphics.pose().popPose();
                }
            };
        } else {
            button.setY(top);
            button.visible = source.visible;
            button.render(guiGraphics, mouseX, mouseY, partialTicks);
        }
    }

	@Override
	public @NotNull List<? extends NarratableEntry> narratables(){
		return Collections.emptyList();
	}
}