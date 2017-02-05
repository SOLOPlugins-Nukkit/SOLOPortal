package solo.soloportal;

import cn.nukkit.level.Position;
import cn.nukkit.level.particle.Particle;
import cn.nukkit.block.Block;

import java.util.LinkedHashMap;
import java.util.HashMap;

public class PortalManager extends Manager{

	public HashMap<String, Portal> portals = new HashMap<String, Portal>();

	@SuppressWarnings("unchecked")
	public PortalManager(){
		super();
		for(String key : this.data.keySet()){
			LinkedHashMap<String, Object> dat = (LinkedHashMap<String, Object>) (this.data.get(key));
			this.portals.put(key, new Portal(
					key.split(":")[0],
					Integer.parseInt(key.split(":")[1]),
					Integer.parseInt(key.split(":")[2]),
					Integer.parseInt(key.split(":")[3]),
					(String) dat.get("warpName"),
					(int) dat.get("type"),
					(int) dat.get("color")
				)
			);
		}
	}

	@Override
	public String getDataName(){
		return "portal";
	}

	public void create(Position pos, String warpName){
		this.create(pos, warpName, Particle.TYPE_FALLING_DUST);
	}
	
	public void create(Position pos, String warpName, int type){
		this.create(pos, warpName, type, Portal.COLOR_SKY_BLUE);
	}
	
	@SuppressWarnings("serial")
	public void create(Position pos, String warpName, int type, int color){
		String str = this.toString(pos);
		this.data.put(str, new LinkedHashMap<String, Object>(){{
			put("warpName", warpName);
			put("type", type);
			put("color", color);
		}});
		this.portals.put(str, new Portal(
				pos.level.getFolderName(),
				pos.getFloorX(),
				pos.getFloorY(),
				pos.getFloorZ(),
				warpName,
				type,
				color
			)
		);
		pos.getLevel().setBlock(pos, Block.get(0));
	}

	public boolean remove(Position pos){
		String str = this.toString(pos);
		if(this.data.containsKey(str)){
			this.data.remove(str);
			if(this.portals.containsKey(str)){
				this.portals.remove(str);
			}
			return true;
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	public String get(Position pos){
		String str = this.toString(pos);
		if(this.data.containsKey(str)){
			LinkedHashMap<String, Object> map = (LinkedHashMap<String, Object>) (this.data.get(str));
			return (String) map.get("warpName");
		}
		return null;
	}

	public void update(){
		for(Portal portal : this.portals.values()){
			portal.update();
		}
	}

}