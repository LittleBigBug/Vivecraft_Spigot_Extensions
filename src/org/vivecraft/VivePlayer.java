package org.vivecraft;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.vivecraft.listeners.VivecraftNetworkListener;
import org.vivecraft.utils.Quaternion;
import org.vivecraft.utils.Vector3;

import net.minecraft.server.v1_12_R1.Vec3D;

public class VivePlayer {

	public byte[] hmdData;
	public byte[] controller0data;
	public byte[] controller1data;
	public byte[] draw;
	boolean isTeleportMode;
	boolean isReverseHands;
	boolean isSeated;
	float worldScale;
	boolean isVR;

	public Player player;

	public VivePlayer(Player player) {
		this.player = player;	
	}

	public float getDraw(){
		try {
			if(draw != null){
				ByteArrayInputStream byin = new ByteArrayInputStream(draw);
				DataInputStream da = new DataInputStream(byin);
		
				float draw= da.readFloat();
				
				da.close(); //needed?
				return draw;	
			}else{
			}
		} catch (IOException e) {

		}
	 
		return 0;
	}
	
	@SuppressWarnings("unused")
	public Vec3D getHMDDir(){
		try {
			if(hmdData != null){
				
				ByteArrayInputStream byin = new ByteArrayInputStream(hmdData);
				DataInputStream da = new DataInputStream(byin);
		
				this.isSeated = da.readBoolean();
				float lx = da.readFloat();
				float ly = da.readFloat();
				float lz = da.readFloat();
				
				float w = da.readFloat();
				float x = da.readFloat();
				float y = da.readFloat();
				float z = da.readFloat();
			    Vector3 forward = new Vector3(0,0,-1);
				Quaternion q = new Quaternion(w, x, y, z);
				Vector3 out = q.multiply(forward);
	
				
				//System.out.println("("+out.getX()+","+out.getY()+","+out.getZ()+")" + " : W:" + w + " X: "+x + " Y:" + y+ " Z:" + z);
				da.close(); //needed?
				return new Vec3D(out.getX(), out.getY(), out.getZ());
			}else{
			}
		} catch (IOException e) {

		}
	 
		return ((CraftPlayer)player).getHandle().f(1.0f);
	}
	
	@SuppressWarnings("unused")
	public Vec3D getControllerDir(int controller){
		byte[] data = controller0data;
		if(controller == 1) data = controller1data;
		if(this.isSeated) controller = 0;
		if(data != null){

			ByteArrayInputStream byin = new ByteArrayInputStream(data);
			DataInputStream da = new DataInputStream(byin);

			try {

				this.isReverseHands = da.readBoolean();

				float lx = da.readFloat();
				float ly = da.readFloat();
				float lz = da.readFloat();

				float w = da.readFloat();
				float x = da.readFloat();
				float y = da.readFloat();
				float z = da.readFloat();
				Vector3 forward = new Vector3(0,0,-1);
				Quaternion q = new Quaternion(w, x, y, z);
				Vector3 out = q.multiply(forward);

				da.close(); //needed?
				return new Vec3D(out.getX(), out.getY(), out.getZ());
			} catch (IOException e) {
			}
		}else{
		}
		
		return ((CraftPlayer)player).getHandle().f(1.0f);

	}
	public Location getHMDPos() {
		try {
			if(hmdData != null ){
				
				ByteArrayInputStream byin = new ByteArrayInputStream(hmdData);
				DataInputStream da = new DataInputStream(byin);
		
				this.isSeated= da.readBoolean();
				float lx = da.readFloat();
				float ly = da.readFloat();
				float lz = da.readFloat();
				
				da.close(); //needed?
								
				return new Location(player.getWorld(), lx, ly, lz);
			}else{
			}
		} catch (IOException e) {

		}
	 
		return player.getLocation(); //why

	}
	
	public Location getControllerPos(int c) {
		try {
			if(controller0data != null && controller0data != null){
				
				ByteArrayInputStream byin = new ByteArrayInputStream(c==0?controller0data:controller1data);
				DataInputStream da = new DataInputStream(byin);
		
				this.isReverseHands = da.readBoolean();
				float x = da.readFloat();
				float y = da.readFloat();
				float z = da.readFloat();
				
				da.close(); //needed?
				
				if (this.isSeated){
					Vec3D dir = this.getHMDDir();
					dir = dir.b((float) Math.toRadians(c==0?-35:35));
					dir = new Vec3D(dir.x, 0, dir.z);
					dir = dir.a();
					Location out = this.getHMDPos().add(dir.x * 0.3 * worldScale, -0.4* worldScale ,dir.z*0.3* worldScale);
					x = (float) out.getX();
					y = (float) out.getY();
					z = (float) out.getZ();
				}
				
				return new Location(player.getWorld(), x, y, z);
			}else{
			}
		} catch (IOException e) {

		}
	 
		return player.getLocation(); //why

	}

	public boolean isVR(){
		return this.isVR;
	}
	
	public void setVR(boolean vr){
		this.isVR = vr;
	}
	
	public boolean isSeated(){
		try {
			if(hmdData == null) return false;
			if(hmdData.length <29) return false;//old client.
			
			ByteArrayInputStream byin = new ByteArrayInputStream(hmdData);
			DataInputStream da = new DataInputStream(byin);
	
			boolean seated= da.readBoolean();
			
			da.close(); //needed?
			return seated;
				
		} catch (IOException e) {

		}
	 
		return false;
	}

	public byte[] getUberPacket() {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		try {
			output.write((byte) VivecraftNetworkListener.PacketDiscriminators.UBERPACKET.ordinal());
			output.write(java.nio.ByteBuffer.allocate(8).putLong(player.getUniqueId().getMostSignificantBits()).array());
			output.write(java.nio.ByteBuffer.allocate(8).putLong(player.getUniqueId().getLeastSignificantBits()).array());
			if(hmdData.length < 29) output.write(0);
			output.write(hmdData);
			output.write(controller0data);
			output.write(controller1data);
		} catch (IOException e) {

		}

		return output.toByteArray();

	}

}