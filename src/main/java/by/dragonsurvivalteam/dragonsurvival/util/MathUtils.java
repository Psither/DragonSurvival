package by.dragonsurvivalteam.dragonsurvival.util;

import com.mojang.math.Vector3f;

import net.minecraft.util.RandomSource;

public class MathUtils {
	public static Vector3f randomPointInSphere (float radius, RandomSource random) {

		float randomRadius = radius * random.nextFloat();
		// Get a sphere of uniformly random size.
		float x = 0, y = 0, z = 0;
		while (x == y && x == z && x == 0) {
			// Get random gaussian-distributed numbers to evenly distribute points.
			x = (float) random.nextGaussian();
			y = (float) random.nextGaussian();
			z = (float) random.nextGaussian();
		}
		// Normalize and multiply by radius of sphere.
		double multfactor = (1/(Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2)))) * randomRadius;
		x *= (float) multfactor;
		y *= (float) multfactor;
		z *= (float) multfactor;
		return new Vector3f(x, y, z);
	}
}
