package by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons;

import by.dragonsurvivalteam.dragonsurvival.client.gui.AbilityScreen;
import by.dragonsurvivalteam.dragonsurvival.client.gui.DragonScreen;
import by.dragonsurvivalteam.dragonsurvival.client.gui.SkinsScreen;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.MouseTooltipPositioner;
import by.dragonsurvivalteam.dragonsurvival.client.handlers.ClientEvents;
import by.dragonsurvivalteam.dragonsurvival.client.handlers.magic.ClientMagicHUDHandler;
import by.dragonsurvivalteam.dragonsurvival.network.NetworkHandler;
import by.dragonsurvivalteam.dragonsurvival.network.container.OpenDragonInventory;
import by.dragonsurvivalteam.dragonsurvival.network.container.OpenInventory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import static by.dragonsurvivalteam.dragonsurvival.network.container.OpenDragonInventory.SendOpenDragonInventoryAndMaintainCursorPosition;


public class TabButton extends Button {
	public enum TabType {
		INVENTORY,
		ABILITY,
		GITHUB_REMINDER,
		SKINS
	}

	private final TabType tabType;
	private final Screen parent;

	public TabButton(int x, int y, TabType tabType, Screen parent){
		super(x, y, 28, 32, Component.empty(), button -> {}, DEFAULT_NARRATION);
		this.tabType = tabType;
		this.parent = parent;

		setTooltip(Tooltip.create(Component.translatable("ds.gui.tab_button." + tabType.ordinal())));
	}

	private boolean setInventoryScreen(Screen sourceScreen) {
		if (sourceScreen instanceof InventoryScreen) {
			Minecraft.getInstance().setScreen(new InventoryScreen(Minecraft.getInstance().player));
			NetworkHandler.CHANNEL.sendToServer(new OpenInventory());
			return true;
		} else if (sourceScreen instanceof DragonScreen) {
			SendOpenDragonInventoryAndMaintainCursorPosition();
			return true;
		}

		return false;
	}

	@Override
	public void onPress(){
		if(!isCurrent())
			switch(tabType){
				case INVENTORY -> {
					boolean setSuccessfully = false;
					if(parent instanceof AbilityScreen){
						if(((AbilityScreen)parent).sourceScreen != null){
							setSuccessfully = setInventoryScreen(((AbilityScreen)parent).sourceScreen);
						}
					} else if(parent instanceof SkinsScreen){
						if(((SkinsScreen)parent).sourceScreen != null){
							setSuccessfully = setInventoryScreen(((SkinsScreen)parent).sourceScreen);
						}
					}

					if(!setSuccessfully) {
						if(ClientEvents.dragonInventory){
							SendOpenDragonInventoryAndMaintainCursorPosition();
						} else {
							Minecraft.getInstance().setScreen(new InventoryScreen(Minecraft.getInstance().player));
							NetworkHandler.CHANNEL.sendToServer(new OpenInventory());
						}
					}
				}
				case ABILITY -> Minecraft.getInstance().setScreen(new AbilityScreen(parent));
				case SKINS -> Minecraft.getInstance().setScreen(new SkinsScreen(parent));
			}
	}

	public boolean isCurrent(){
		return switch(tabType){
			case INVENTORY -> parent instanceof DragonScreen || parent instanceof InventoryScreen;
			case ABILITY -> parent instanceof AbilityScreen;
			case SKINS -> parent instanceof SkinsScreen;
			default -> false;
		};
	}

	@Override
	public void renderWidget(@NotNull final GuiGraphics guiGraphics, int mouseX, int mouseY, float p_230431_4_){
		if (isCurrent()) {
			guiGraphics.blit(ClientMagicHUDHandler.widgetTextures, getX(), getY(), tabType == TabType.INVENTORY ? 0 : 28, 0, 28, 32);
		} else if (isHovered()) {
			guiGraphics.blit(ClientMagicHUDHandler.widgetTextures, getX(), getY(), 84, 0, 28, 32);
		} else {
			guiGraphics.blit(ClientMagicHUDHandler.widgetTextures, getX(), getY(), 56, 0, 28, 32);
		}

		if (isHovered() || isCurrent()) {
			guiGraphics.blit(ClientMagicHUDHandler.widgetTextures, getX() + 2, getY() + 2 + (isCurrent() ? 2 : 0), tabType.ordinal() * 24, 67, 24, 24);
		} else {
			guiGraphics.blit(ClientMagicHUDHandler.widgetTextures, getX() + 2, getY() + 2 + (isCurrent() ? 2 : 0), tabType.ordinal() * 24, 41, 24, 24);
		}
	}

	@Override
	protected @NotNull ClientTooltipPositioner createTooltipPositioner() {
		return new MouseTooltipPositioner(this);
	}
}