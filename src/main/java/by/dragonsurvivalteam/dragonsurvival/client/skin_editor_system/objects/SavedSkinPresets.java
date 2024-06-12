package by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.objects;

import by.dragonsurvivalteam.dragonsurvival.util.DragonLevel;

import java.util.HashMap;

public class SavedSkinPresets{
	public HashMap<String, HashMap<Integer, SkinPreset>> skinPresets = new HashMap<>();
	public HashMap<String, HashMap<DragonLevel, Integer>> current = new HashMap<>();
	public int version = 0;
}