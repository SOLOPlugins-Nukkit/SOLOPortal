package solo.soloportal;

import cn.nukkit.level.Position;

import java.util.LinkedHashMap;

public class WarpManager extends Manager{

	public WarpManager(){
		super();
	}

	@SuppressWarnings("serial")
	public void create(Position pos, String warpName){
		String stpos = this.toString(pos);
		this.data.put(warpName, new LinkedHashMap<String, Object>(){{
			put("position", stpos);
			put("lock", false);
		}});
	}

	@Override
	public String getDataName(){
		return "warp";
	}

	public boolean remove(String warpName){
		if(this.data.containsKey(warpName)){
			this.data.remove(warpName);
			return true;
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	public boolean isLocked(String warpName){
		if(this.data.containsKey(warpName)){
			LinkedHashMap<String, Object> map = (LinkedHashMap<String, Object>) this.data.get(warpName);
			return (boolean) map.get("lock");
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	public void lock(String warpName){
		if(this.data.containsKey(warpName)){
			LinkedHashMap<String, Object> map = (LinkedHashMap<String, Object>) this.data.get(warpName);
			map.put("lock", true);
			this.data.put(warpName, map);
		}
	}

	@SuppressWarnings("unchecked")
	public void unlock(String warpName){
		if(this.data.containsKey(warpName)){
			LinkedHashMap<String, Object> map = (LinkedHashMap<String, Object>) this.data.get(warpName);
			map.put("lock", false);
			this.data.put(warpName, map);
		}
	}

	@SuppressWarnings("unchecked")
	public Position get(String warpName){
		if(this.data.containsKey(warpName)){
			LinkedHashMap<String, Object> map = (LinkedHashMap<String, Object>) this.data.get(warpName);
			return this.toPosition((String) map.get("position"));
		}
		return null;
	}

}