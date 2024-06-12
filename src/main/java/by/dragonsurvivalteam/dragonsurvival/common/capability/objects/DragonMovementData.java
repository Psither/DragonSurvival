package by.dragonsurvivalteam.dragonsurvival.common.capability.objects;

public class DragonMovementData{
	public double bodyYaw;
	public double headYaw;
	public double headPitch;

	public double headYawLastTick;
	public double headPitchLastTick;
	public double bodyYawLastTick;
	
	public float prevXRot;
	public float prevZRot;
	public float rotLastTick;

	public boolean bite;
	public boolean dig;

	public boolean spinLearned;
	public int spinCooldown;
	public int spinAttack;

	public DragonMovementData(double bodyYaw, double headYaw, double headPitch, boolean bite){
		this.bodyYaw = bodyYaw;
		this.headYaw = headYaw;
		this.headPitch = headPitch;
		this.bite = bite;
	}
}