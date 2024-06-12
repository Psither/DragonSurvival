package by.dragonsurvivalteam.dragonsurvival.client.gui;


import by.dragonsurvivalteam.dragonsurvival.DragonSurvivalMod;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.*;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.generic.HelpButton;
import by.dragonsurvivalteam.dragonsurvival.client.handlers.magic.ClientMagicHUDHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.capability.subcapabilities.MagicCap;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.AbstractDragonType;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.DragonTypes;
import by.dragonsurvivalteam.dragonsurvival.magic.DragonAbilities;
import by.dragonsurvivalteam.dragonsurvival.magic.common.active.ActiveDragonAbility;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.jline.reader.Widget;

import com.mojang.blaze3d.systems.RenderSystem;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class AbilityScreen extends Screen{
	private static final ResourceLocation BACKGROUND_TEXTURE = new ResourceLocation(DragonSurvivalMod.MODID, "textures/gui/magic_interface.png");

	private final int xSize = 256;
	private final int ySize = 256;
	public Screen sourceScreen;
	public ArrayList<ActiveDragonAbility> unlockAbleSkills = new ArrayList<>();
	private int guiLeft;
	private int guiTop;
	private AbstractDragonType type;

	public AbilityScreen(Screen sourceScreen){
		super(Component.empty().append("AbilityScreen"));
		this.sourceScreen = sourceScreen;
	}

	public List<GuiEventListener> widgetList(){
		return children;
	}

	@Override
	public void render(@NotNull final GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		if(minecraft == null){
			return;
		}

		renderBackground(guiGraphics);

		int startX = guiLeft + 10;
		int startY = guiTop - 30;

		guiGraphics.blit(BACKGROUND_TEXTURE, startX, startY, 0, 0, 256, 256);

		if(type != null){
			int barYPos = Objects.equals(type, DragonTypes.SEA) ? 198 : Objects.equals(type, DragonTypes.FOREST) ? 186 : 192;

			float progress = Mth.clamp(minecraft.player.experienceLevel / 50F, 0, 1);
			float progress1 = Math.min(1F, Math.min(0.5F, progress) * 2F);
			float progress2 = Math.min(1F, Math.min(0.5F, progress - 0.5F) * 2F);

			startX += 0.5F;
			startY += 0.75F;
			guiGraphics.blit(ClientMagicHUDHandler.widgetTextures, startX + 23 / 2, startY + 28, 0, (float) 180 / 2, 105, 3, 128, 128);
			guiGraphics.blit(ClientMagicHUDHandler.widgetTextures, startX + 254 / 2, startY + 28, 0, (float) 180 / 2, 105, 3, 128, 128);
			guiGraphics.blit(ClientMagicHUDHandler.widgetTextures, startX + 23 / 2, startY + 28, 0, (float) barYPos / 2, (int)(105 * progress1), 3, 128, 128);

			if(progress > 0.5){
				guiGraphics.blit(ClientMagicHUDHandler.widgetTextures, startX + 254 / 2, startY + 28, 0, (float) barYPos / 2, (int)(105 * progress2), 3, 128, 128);
			}

			int expChange = -1;

			for(GuiEventListener btn : children){
				if(!(btn instanceof AbstractWidget) || !((AbstractWidget)btn).isHoveredOrFocused()){
					continue;
				}

				if(btn instanceof IncreaseLevelButton){
					expChange = ((IncreaseLevelButton)btn).skillCost;
					break;
				}
			}

			if(expChange != -1){
				float Changeprogress = Mth.clamp(expChange / 50F, 0, 1); //Total exp required to hit level 50
				float Changeprogress1 = Math.min(1F, Math.min(0.5F, Changeprogress) * 2F);
				float Changeprogress2 = Math.min(1F, Math.min(0.5F, Changeprogress - 0.5F) * 2F);

				guiGraphics.blit(ClientMagicHUDHandler.widgetTextures, startX + 23 / 2, startY + 28, 0, (float) 174 / 2, (int)(105 * Changeprogress1), 3, 128, 128);

				if(Changeprogress2 > 0.5){
					guiGraphics.blit(ClientMagicHUDHandler.widgetTextures, startX + 254 / 2, startY + 28, 0, (float) 174 / 2, (int)(105 * Changeprogress2), 3, 128, 128);
				}
			}

			Component textComponent = Component.empty().append(Integer.toString(minecraft.player.experienceLevel)).withStyle(ChatFormatting.DARK_GRAY);
			int xPos = startX + 117 + 1;
			int finalXPos = (xPos - minecraft.font.width(textComponent) / 2);
			guiGraphics.drawString(minecraft.font, textComponent, finalXPos, startY + 26, 0, false);
		}

		super.render(guiGraphics, mouseX, mouseY, partialTick);

		renderables.forEach(s-> {
			if(s instanceof AbilityButton btn){
				if(btn.skillType == 0 && btn.dragging && btn.ability != null){
					RenderSystem.setShaderTexture(0, btn.ability.getIcon());
					guiGraphics.blit(btn.ability.getIcon(), mouseX, mouseY, 0, 0, 32, 32, 32, 32);
				}
			}
		});
	}

	@Override
	public void init(){
		guiLeft = (width - xSize) / 2;
		guiTop = (height - ySize / 2) / 2;

		int startX = guiLeft;
		int startY = guiTop;

		//Inventory
		addRenderableWidget(new TabButton(startX + 5 + 10, startY - 26 - 30, TabButton.TabType.INVENTORY, this));
		addRenderableWidget(new TabButton(startX + 34 + 10, startY - 28 - 30, TabButton.TabType.ABILITY, this));
		addRenderableWidget(new TabButton(startX + 62 + 10, startY - 26 - 30, TabButton.TabType.GITHUB_REMINDER, this));
		addRenderableWidget(new TabButton(startX + 91 + 10, startY - 26 - 30, TabButton.TabType.SKINS, this));

		addRenderableWidget(new SkillProgressButton(guiLeft + 10 + (int)(219 / 2F), startY + 8 - 30, 4, this));

		for(int i = 1; i <= 4; i++){
			addRenderableWidget(new SkillProgressButton(guiLeft + 10 + (int)(219 / 2F) - i * 23, startY + 8 - 30, 4 - i, this));
			addRenderableWidget(new SkillProgressButton(guiLeft + 10 + (int)(219 / 2F) + i * 23, startY + 8 - 30, 4 + i, this));
		}

		DragonStateProvider.getCap(Minecraft.getInstance().player).ifPresent(cap -> {
			for(int num = 0; num < MagicCap.activeAbilitySlots; num++){
				addRenderableWidget(new AbilityButton((int)(guiLeft + (90+20) / 2.0), guiTop + 40 - 25 + num * 35, 0, num, this));
			}

			for(int num = 0; num < MagicCap.passiveAbilitySlots; num++){
				addRenderableWidget(new AbilityButton(guiLeft + (int)((215 + 10) / 2F), guiTop + 40 - 25 + num * 35, 1, num, this));
				addRenderableWidget(new IncreaseLevelButton(guiLeft + (int)(219 / 2F) + 35, guiTop + 40  - 17 + num * 35, num));
				addRenderableWidget(new DecreaseLevelButton(guiLeft + (int)(219 / 2F) - 13, guiTop + 40  - 17 + num * 35, num));
			}

			for(int num = 0; num < MagicCap.innateAbilitySlots; num++){
				addRenderableWidget(new AbilityButton(guiLeft + (int)(340 / 2F), guiTop + 40 - 25 + num * 35, 2, num, this));
			}
		});

		addRenderableWidget(new HelpButton(startX + 218 / 2 + 3 + 10 - 55, startY + 263 / 2 + 28, 9, 9, "ds.skill.help_1", 0));
		addRenderableWidget(new HelpButton(startX + 218 / 2 + 3 + 10 + 2, startY + 263 / 2 + 28, 9, 9, "ds.skill.help_2", 0));
		addRenderableWidget(new HelpButton(startX + 218 / 2 + 3 + 10 + 60, startY + 263 / 2 + 28, 9, 9, "ds.skill.help_3", 0));
	}


	@Override
	public void tick(){
		DragonStateProvider.getCap(Minecraft.getInstance().player).ifPresent(cap -> {
			type = cap.getType();
			unlockAbleSkills.clear();

			for(ActiveDragonAbility ab : cap.getMagicData().getActiveAbilities()){
				ActiveDragonAbility ability = DragonAbilities.getSelfAbility(minecraft.player, ab.getClass());
				ActiveDragonAbility db = ability != null ? ability : ab;

				for(int i = db.getLevel(); i < db.getMaxLevel(); i++){
					try{
						ActiveDragonAbility newActivty = db.getClass().newInstance();
						newActivty.setLevel(i + 1);
						unlockAbleSkills.add(newActivty);
					}catch(InstantiationException | IllegalAccessException e){
						throw new RuntimeException(e);
					}
				}
			}

			unlockAbleSkills.sort(Comparator.comparingInt(ActiveDragonAbility::getCurrentRequiredLevel));
		});
	}

	@Override
	public boolean isPauseScreen(){
		return false;
	}
}