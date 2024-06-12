package by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.objects;

import by.dragonsurvivalteam.dragonsurvival.common.capability.NBTInterface;
import by.dragonsurvivalteam.dragonsurvival.common.capability.subcapabilities.SkinCap;
import net.minecraft.nbt.CompoundTag;

public class LayerSettings implements NBTInterface{
	public String selectedSkin = SkinCap.defaultSkinValue;

	public float hue = 0.5f, saturation = 0.5f, brightness = 0.5f;
	public boolean modifiedColor = false;

	public boolean glowing = false;

	public LayerSettings(){}

	public LayerSettings(String selectedSkin, float defaultHue){
		this.selectedSkin = selectedSkin;
		this.hue = defaultHue;
	}

	@Override
	public CompoundTag writeNBT(){
		CompoundTag nbt = new CompoundTag();
		nbt.putString("skin", selectedSkin);

		nbt.putFloat("hue", hue);
		nbt.putFloat("saturation", saturation);
		nbt.putFloat("brightness", brightness);

		nbt.putBoolean("modifiedColor", modifiedColor);
		nbt.putBoolean("glowing", glowing);
		return nbt;
	}

	@Override
	public void readNBT(CompoundTag base){
		selectedSkin = base.getString("skin");

		hue = base.getFloat("hue");
		saturation = base.getFloat("saturation");
		brightness = base.getFloat("brightness");

		modifiedColor = base.getBoolean("modifiedColor");
		glowing = base.getBoolean("glowing");
	}
}