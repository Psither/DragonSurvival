package by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.objects;

import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.EnumSkinLayer;
import by.dragonsurvivalteam.dragonsurvival.util.DragonLevel;

import java.util.HashMap;

public class DragonEditorObject{
	public HashMap<String, HashMap<DragonLevel, HashMap<EnumSkinLayer, String>>> defaults = new HashMap<>();

	public Dragon sea_dragon;
	public Dragon forest_dragon;
	public Dragon cave_dragon;

	public static class Dragon{
		public HashMap<EnumSkinLayer, Texture[]> layers;
	}

	public static class Texture{
		public String key;
		public String texture;
		public boolean colorable = true;
		public String defaultColor;
		public Float average_hue;

		public boolean random = true;
		public boolean randomHue = true;
	}
}