<<<<<<< HEAD:src/main/java/by/jackraidenph/dragonsurvival/client/handlers/ClientGrowthHudHandler.java
package by.jackraidenph.dragonsurvival.client.handlers;

import by.jackraidenph.dragonsurvival.DragonSurvivalMod;
import by.jackraidenph.dragonsurvival.client.util.RenderUtils;
import by.jackraidenph.dragonsurvival.common.capability.caps.DragonStateHandler;
import by.jackraidenph.dragonsurvival.common.capability.provider.DragonStateProvider;
import by.jackraidenph.dragonsurvival.common.handlers.DragonGrowthHandler;
import by.jackraidenph.dragonsurvival.config.ConfigHandler;
import by.jackraidenph.dragonsurvival.misc.DragonLevel;
import by.jackraidenph.dragonsurvival.util.DragonUtils;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.gui.ForgeIngameGui;

import java.awt.Color;

public class ClientGrowthHudHandler
{
	public static void renderGrowth(ForgeIngameGui gui, PoseStack mStack, float partialTicks, int width, int height) {
		Player playerEntity = Minecraft.getInstance().player;
		if (playerEntity == null || !DragonUtils.isDragon(playerEntity) || playerEntity.isSpectator())
=======
package by.dragonsurvivalteam.dragonsurvival.client.handlers;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvivalMod;
import by.dragonsurvivalteam.dragonsurvival.client.gui.DragonScreen;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.provider.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.DragonGrowthHandler;
import by.dragonsurvivalteam.dragonsurvival.common.util.DragonUtils;
import by.dragonsurvivalteam.dragonsurvival.config.ConfigHandler;
import by.dragonsurvivalteam.dragonsurvival.misc.DragonLevel;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.awt.Color;

@Mod.EventBusSubscriber( Dist.CLIENT )
public class ClientGrowthHudHandler{
	@SubscribeEvent
	public static void renderAbilityHud(RenderGameOverlayEvent.Post event){
		PlayerEntity playerEntity = Minecraft.getInstance().player;

		if(playerEntity == null || !DragonUtils.isDragon(playerEntity) || playerEntity.isSpectator()){
>>>>>>> v1.16.x:src/main/java/by/dragonsurvivalteam/dragonsurvival/client/handlers/ClientGrowthHudHandler.java
			return;
		}

		DragonStateHandler handler = DragonStateProvider.getCap(playerEntity).orElse(null);
		ItemStack stack = playerEntity.getMainHandItem();
<<<<<<< HEAD:src/main/java/by/jackraidenph/dragonsurvival/client/handlers/ClientGrowthHudHandler.java
		
		if(handler == null || stack.isEmpty()) return;
		
		int increment = DragonGrowthHandler.getIncrement(stack.getItem(), handler.getLevel());
		
		if(increment != 0 && (handler.getSize() < ConfigHandler.SERVER.maxGrowthSize.get() && increment > 0 || increment < 0 && handler.getSize() >= (DragonLevel.BABY.size + 1))){
			float curSize = (float)handler.getSize();
			float nextSize = (float)(handler.getSize() + increment);
			float progress = 0;
			float nextProgess = 0;
			
			if (handler.getLevel() == DragonLevel.BABY) {
				progress = (curSize - DragonLevel.BABY.size) / (DragonLevel.YOUNG.size - DragonLevel.BABY.size);
				nextProgess = (nextSize - DragonLevel.BABY.size) / (DragonLevel.YOUNG.size - DragonLevel.BABY.size);
				
			} else if (handler.getLevel() == DragonLevel.YOUNG) {
				progress = (curSize - DragonLevel.YOUNG.size) / (DragonLevel.ADULT.size - DragonLevel.YOUNG.size);
				nextProgess = (nextSize - DragonLevel.YOUNG.size) / (DragonLevel.ADULT.size - DragonLevel.YOUNG.size);
				
			} else if (handler.getLevel() == DragonLevel.ADULT && handler.getSize() < 40) {
				progress = (curSize - DragonLevel.ADULT.size) / (40 - DragonLevel.ADULT.size);
				nextProgess = (nextSize - DragonLevel.ADULT.size) / (40 - DragonLevel.ADULT.size);
				
			} else if (handler.getLevel() == DragonLevel.ADULT && handler.getSize() >= 40) {
				progress = (float)((curSize - 40) / (ConfigHandler.SERVER.maxGrowthSize.get() - 40));
				nextProgess = (float)((nextSize - 40) / (ConfigHandler.SERVER.maxGrowthSize.get() - 40));
			}
			
			progress = Math.min(1.0f, progress);
			nextProgess = Math.min(1.0f, nextProgess);
			
			int radius = 17;
			int thickness = 5;
			int circleX = (width / 2) - (radius);
			int circleY = height - 90;
			
			circleX += ConfigHandler.CLIENT.growthXOffset.get();
			circleY += ConfigHandler.CLIENT.growthYOffset.get();
			
			double zLevel = 0;
			Matrix4f matrix4f = mStack.last().pose();
			
			RenderSystem.setShaderColor(0f, 0f, 0f, 1f);
			Color c = new Color(99, 99, 99);
			
			RenderSystem.setShaderColor(c.brighter().getRed()  / 255.0f, c.brighter().getGreen() / 255.0f, c.brighter().getBlue() / 255.0f, 1.0f);
			if(!handler.growing) RenderSystem.setShaderColor(76 / 255F, 0F, 0F, 1F);
			RenderUtils.drawSmoothCircle(matrix4f, circleX + radius, circleY + radius, zLevel, radius + 1, 6, 1, 0);
			
			RenderSystem.setShaderColor(c.getRed()  / 255.0f, c.getRed()  / 255.0f, c.getRed()  / 255.0f, 1.0f);
			RenderUtils.drawSmoothCircle(matrix4f, circleX + radius, circleY + radius, zLevel, radius, 6, 1, 0);
			RenderSystem.setShaderColor(0f, 0f, 0f, 1f);
			
			if(nextProgess > progress) {
				int num = 1;
				double perSide = 1.0 / 6.0;
				if(nextProgess < progress + perSide){
					nextProgess = (float)(progress + perSide);
					num = 2;
				}
				RenderSystem.enableTexture();
				RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
				RenderSystem.setShaderTexture(0,new ResourceLocation(DragonSurvivalMod.MODID, "textures/gui/growth/circle_" + num + ".png"));
				RenderUtils.drawTexturedCircle(matrix4f,circleX + radius, circleY + radius, zLevel, radius, 0.5, 0.5, 0.5, 6, nextProgess, -0.5);
				
				RenderSystem.setShaderTexture(0,new ResourceLocation(DragonSurvivalMod.MODID, "textures/gui/growth/circle_" + handler.getType().name().toLowerCase() + ".png"));
				RenderUtils.drawTexturedCircle(matrix4f,circleX + radius, circleY + radius, zLevel, radius, 0.5, 0.5, 0.5, 6, progress, -0.5);
				
			}else if(increment < 0){
				RenderSystem.enableTexture();
				RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
				RenderSystem.setShaderTexture(0,new ResourceLocation(DragonSurvivalMod.MODID, "textures/gui/growth/circle_3.png"));
				RenderUtils.drawTexturedCircle(matrix4f,circleX + radius, circleY + radius, zLevel, radius, 0.5, 0.5, 0.5, 6, progress, -0.5);
				
				RenderSystem.setShaderTexture(0,new ResourceLocation(DragonSurvivalMod.MODID, "textures/gui/growth/circle_" + handler.getType().name().toLowerCase() + ".png"));
				RenderUtils.drawTexturedCircle(matrix4f,circleX + radius, circleY + radius, zLevel, radius, 0.5, 0.5, 0.5, 6, nextProgess, -0.5);
			}
			
			RenderSystem.setShaderColor(c.getRed()  / 255.0f, c.getRed()  / 255.0f, c.getRed()  / 255.0f, 1.0f);
			RenderUtils.drawSmoothCircle(matrix4f, circleX + radius, circleY + radius, zLevel, radius - thickness, 6, 1, 0);
			RenderSystem.setShaderColor(c.brighter().getRed()  / 255.0f, c.brighter().getGreen() / 255.0f, c.brighter().getBlue() / 255.0f, 1.0f);
			RenderUtils.drawSmoothCircle(matrix4f, circleX + radius, circleY + radius, zLevel, radius - thickness, 6, 1, 0);
			
			RenderSystem.setShaderColor(1F, 1F, 1F, 1.0f);
			RenderSystem.setShaderTexture(0,new ResourceLocation(DragonSurvivalMod.MODID, "textures/gui/growth/growth_" + handler.getType().name().toLowerCase() + "_" + (handler.getLevel().ordinal() + 1) + ".png"));
			Screen.blit(mStack, circleX + 6, circleY + 6, 0, 0, 20, 20, 20, 20);
=======

		if(handler == null || stack == null || stack.isEmpty()) return;

		if (event.getType() == ElementType.HOTBAR) {
			RenderSystem.pushMatrix();

			TextureManager textureManager = Minecraft.getInstance().getTextureManager();
			MainWindow window = Minecraft.getInstance().getWindow();

			int increment = DragonGrowthHandler.getIncrement(stack.getItem(), handler.getLevel());

			if(increment != 0 && (handler.getSize() < ConfigHandler.SERVER.maxGrowthSize.get() && increment > 0 || increment < 0 && handler.getSize() >= (DragonLevel.BABY.size + 1))){
				float curSize = (float)handler.getSize();
				float nextSize = (float)(handler.getSize() + increment);
				float progress = 0;
				float nextProgess = 0;

				if(handler.getLevel() == DragonLevel.BABY){
					progress = (curSize - DragonLevel.BABY.size) / (DragonLevel.YOUNG.size - DragonLevel.BABY.size);
					nextProgess = (nextSize - DragonLevel.BABY.size) / (DragonLevel.YOUNG.size - DragonLevel.BABY.size);
				}else if(handler.getLevel() == DragonLevel.YOUNG){
					progress = (curSize - DragonLevel.YOUNG.size) / (DragonLevel.ADULT.size - DragonLevel.YOUNG.size);
					nextProgess = (nextSize - DragonLevel.YOUNG.size) / (DragonLevel.ADULT.size - DragonLevel.YOUNG.size);
				}else if(handler.getLevel() == DragonLevel.ADULT && handler.getSize() < 40){
					progress = (curSize - DragonLevel.ADULT.size) / (40 - DragonLevel.ADULT.size);
					nextProgess = (nextSize - DragonLevel.ADULT.size) / (40 - DragonLevel.ADULT.size);
				}else if(handler.getLevel() == DragonLevel.ADULT && handler.getSize() >= 40){
					progress = (float)((curSize - 40) / (ConfigHandler.SERVER.maxGrowthSize.get() - 40));
					nextProgess = (float)((nextSize - 40) / (ConfigHandler.SERVER.maxGrowthSize.get() - 40));
				}

				progress = Math.min(1.0f, progress);
				nextProgess = Math.min(1.0f, nextProgess);

				int radius = 17;
				int thickness = 5;
				int circleX = (window.getGuiScaledWidth() / 2) - (radius);
				int circleY = window.getGuiScaledHeight() - 90;

				circleX += ConfigHandler.CLIENT.growthXOffset.get();
				circleY += ConfigHandler.CLIENT.growthYOffset.get();

				RenderSystem.disableTexture();
				Color c = new Color(99, 99, 99);
				RenderSystem.color4f(c.getRed() / 255.0f, c.getGreen() / 255.0f, c.getBlue() / 255.0f, 1.0f);
				DragonScreen.drawTexturedRing(circleX + radius, circleY + radius, radius - thickness, radius, 0, 0, 0, 128, 6, 1, 0);
				RenderSystem.enableTexture();
				RenderSystem.color4f(1F, 1F, 1F, 1.0f);

				if(nextProgess > progress){
					int num = 1;
					double perSide = 1.0 / 6.0;
					if(nextProgess < progress + perSide){
						nextProgess = (float)(progress + perSide);
						num = 2;
					}

					textureManager.bind(new ResourceLocation(DragonSurvivalMod.MODID, "textures/gui/growth/circle_" + num + ".png"));
					DragonScreen.drawTexturedCircle(circleX + radius, circleY + radius, radius, 0.5, 0.5, 0.5, 6, nextProgess, -0.5);

					textureManager.bind(new ResourceLocation(DragonSurvivalMod.MODID, "textures/gui/growth/circle_" + handler.getType().name().toLowerCase() + ".png"));
					DragonScreen.drawTexturedCircle(circleX + radius, circleY + radius, radius, 0.5, 0.5, 0.5, 6, progress, -0.5);
				}else if(increment < 0){
					textureManager.bind(new ResourceLocation(DragonSurvivalMod.MODID, "textures/gui/growth/circle_3.png"));
					DragonScreen.drawTexturedCircle(circleX + radius, circleY + radius, radius, 0.5, 0.5, 0.5, 6, progress, -0.5);

					textureManager.bind(new ResourceLocation(DragonSurvivalMod.MODID, "textures/gui/growth/circle_" + handler.getType().name().toLowerCase() + ".png"));
					DragonScreen.drawTexturedCircle(circleX + radius, circleY + radius, radius, 0.5, 0.5, 0.5, 6, nextProgess, -0.5);
				}

				RenderSystem.disableTexture();
				RenderSystem.lineWidth(4.0f);
				if(handler.growing){
					RenderSystem.color4f(0F, 0F, 0F, 1F);
				}else{
					RenderSystem.color4f(76 / 255F, 0F, 0F, 1F);
				}
				DragonScreen.drawSmoothCircle(circleX + radius, circleY + radius, radius, 6, 1, 0);

				RenderSystem.color4f(c.getRed() / 255.0f, c.getGreen() / 255.0f, c.getBlue() / 255.0f, 1.0f);
				DragonScreen.drawSmoothCircle(circleX + radius, circleY + radius, radius - thickness, 6, 1, 0);
				RenderSystem.lineWidth(1.0F);

				c = c.brighter();
				RenderSystem.color4f(c.getRed() / 255.0f, c.getGreen() / 255.0f, c.getBlue() / 255.0f, 1.0f);
				DragonScreen.drawTexturedRing(circleX + radius, circleY + radius, 0, radius - thickness, 0, 0, 0, 0, 6, 1, 0);

				RenderSystem.enableTexture();
				RenderSystem.color4f(1F, 1F, 1F, 1.0f);

				textureManager.bind(new ResourceLocation(DragonSurvivalMod.MODID, "textures/gui/growth/growth_" + handler.getType().name().toLowerCase() + "_" + (handler.getLevel().ordinal() + 1) + ".png"));
				Screen.blit(event.getMatrixStack(), circleX + 6, circleY + 6, 0, 0, 20, 20, 20, 20);
			}

			RenderSystem.popMatrix();
>>>>>>> v1.16.x:src/main/java/by/dragonsurvivalteam/dragonsurvival/client/handlers/ClientGrowthHudHandler.java
		}
	}
}