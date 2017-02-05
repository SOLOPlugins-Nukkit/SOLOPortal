package solo.soloportal;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.data.EntityMetadata;
import cn.nukkit.level.Level;
import cn.nukkit.level.particle.Particle;
import cn.nukkit.level.particle.DustParticle;
import cn.nukkit.level.particle.SmokeParticle;
import cn.nukkit.level.particle.RedstoneParticle;
import cn.nukkit.level.particle.GenericParticle;
import cn.nukkit.math.Vector3;
import cn.nukkit.network.protocol.AddEntityPacket;
import cn.nukkit.network.protocol.MoveEntityPacket;
import cn.nukkit.network.protocol.RemoveEntityPacket;

import java.util.Random;
import java.util.HashMap;

public class Portal{

	public static final int COLOR_RED = 100;
	public static final int COLOR_GREEN = 101;
	public static final int COLOR_BLUE = 102;

	public static final int COLOR_YELLOW = 103;
	public static final int COLOR_PINK = 104;
	public static final int COLOR_SKY_BLUE = 105;

	public static final int COLOR_WHITE = 106;
	public static final int COLOR_GRAY = 107;
	public static final int COLOR_DARK_GRAY = 108;
	public static final int COLOR_DARK = 109;

	@SuppressWarnings("serial")
	public HashMap<Integer, int[]> colors = new HashMap<Integer, int[]>(){{
		put(COLOR_RED, new int[]{255, 0, 0});
		put(COLOR_GREEN, new int[]{0, 255, 0});
		put(COLOR_BLUE, new int[]{0, 0, 255});
		
		put(COLOR_YELLOW, new int[]{255, 255, 0});
		put(COLOR_PINK, new int[]{255, 0, 255});
		put(COLOR_SKY_BLUE, new int[]{0, 255, 255});
		
		put(COLOR_WHITE, new int[]{255, 255, 255});
		put(COLOR_GRAY, new int[]{192, 192, 192});
		put(COLOR_DARK_GRAY, new int[]{128, 128, 128});
		put(COLOR_DARK, new int[]{0, 0, 0});
	}};
	
	public String levelName;
	public Level level;
	public int x, y, z;

	public String warpName;
	public int type;
	public int color;
	
	public AddEntityPacket addPk;
	public MoveEntityPacket movePk;
	public RemoveEntityPacket removePk;
	
	public int stopTry = 0;

	public Portal(String levelName, int x, int y, int z, String warpName){
		this(levelName, x, y, z, warpName, Particle.TYPE_FALLING_DUST);
	}
	public Portal(String levelName, int x, int y, int z, String warpName, int type){
		this(levelName, x, y, z, warpName, type, Portal.COLOR_SKY_BLUE);
	}
	public Portal(String levelName, int x, int y, int z, String warpName, int type, int color){
		this.levelName = levelName;
		this.level = Server.getInstance().getLevelByName(levelName);
		this.x = x;
		this.y = y;
		this.z = z;
		
		this.warpName = warpName;
		this.type = type;
		this.color = color;
		
		this.init();
	}
	
	public void init(){
		long eid = Entity.entityCount++;
		
		this.addPk = new AddEntityPacket();
		this.addPk.entityUniqueId = eid;
		this.addPk.entityRuntimeId = eid;
		this.addPk.type = 15; //villager
		this.addPk.x = (float) (this.x + 0.5);
		this.addPk.y = (float) (this.y + 2.3);
		this.addPk.z = (float) (this.z + 0.5);
		this.addPk.speedX = 0;
		this.addPk.speedY = 0;
		this.addPk.speedZ = 0;
		this.addPk.yaw = 0;
		this.addPk.pitch = 0;
		
		long flags = 0;
		flags |= 1 << Entity.DATA_FLAG_INVISIBLE;
		flags |= 1 << Entity.DATA_FLAG_CAN_SHOW_NAMETAG;
		//flags |= 1 << Entity.DATA_FLAG_ALWAYS_SHOW_NAMETAG;
		flags |= 1 << Entity.DATA_FLAG_NO_AI;
		EntityMetadata metadata = new EntityMetadata()
				.putLong(Entity.DATA_FLAGS, flags)
				.putShort(Entity.DATA_AIR, 400)
				.putShort(Entity.DATA_MAX_AIR, 400)
				.putString(Entity.DATA_NAMETAG, "§b§l" + this.warpName)
				.putLong(Entity.DATA_LEAD_HOLDER_EID, -1)
				.putFloat(Entity.DATA_SCALE, 0.0001f);
		
		this.addPk.metadata = metadata;
		
		this.movePk = new MoveEntityPacket();
		this.movePk.eid = eid;
		this.movePk.x = (float) (this.x + 0.5);
		this.movePk.y = (float) (this.y + 2.3);
		this.movePk.z = (float) (this.z + 0.5);
		
		this.removePk = new RemoveEntityPacket();
		this.removePk.eid = eid;
		
		this.spawnToAll();
	}
	
	public void spawnTo(Player player){
		this.spawnTo(player, player.getLevel());
	}
	
	public void spawnTo(Player player, Level level){
		if(level == null || !levelName.equals(level.getFolderName())){
			this.despawnTo(player);
			return;
		}
		player.dataPacket(this.addPk);
		player.dataPacket(this.movePk);
	}
	
	public void spawnToAll(){
		for(Player player : Server.getInstance().getOnlinePlayers().values()){
			this.spawnTo(player);
		}
	}
	
	public void despawnTo(Player player){
		player.dataPacket(this.removePk);
	}
	
	public void despawnToAll(){
		for(Player player : Server.getInstance().getOnlinePlayers().values()){
			this.despawnTo(player);;
		}
	}

	public void update(){
		if(this.level == null){
			if(this.stopTry++ > 40){
				this.level = Server.getInstance().getLevelByName(this.levelName);
			}
			return;
		}
		Random random = new Random();
		int[] c = this.colors.get(this.color);
		Vector3 vec = new Vector3(this.x, this.y, this.z);
		Particle particle;

		switch(this.type){
			case Particle.TYPE_SMOKE:
				particle = new SmokeParticle(vec, 2); //second parameter is scale
				break;

			case Particle.TYPE_REDSTONE:
				particle = new RedstoneParticle(vec, 1); //second parameter is lifetime
				break;

			case Particle.TYPE_FALLING_DUST:
				particle = new DustParticle(vec, c[0] & 0xff, c[1] & 0xff, c[2] & 0xff, 255);
				break;

			default:
				particle = new GenericParticle(vec, this.type);
		}

		for(int i = 0; i < 6; i++){
			particle.setComponents(
				vec.x + random.nextFloat(),
	 			vec.y + random.nextFloat() * 3,
				vec.z + random.nextFloat()
			);
			this.level.addParticle(particle);
		}
	}
}