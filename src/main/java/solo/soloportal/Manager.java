package solo.soloportal;

import cn.nukkit.utils.Config;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.Server;

import java.util.LinkedHashMap;
import java.io.File;

public abstract class Manager{

	public LinkedHashMap<String, Object> data = new LinkedHashMap<String, Object>();
	public Config config;

	@SuppressWarnings("deprecation")
	public Manager(){
		this.config = new Config(new File(Main.getInstance().getDataFolder(), this.getDataName() + ".yml"), Config.YAML, this.data);
		this.data = (LinkedHashMap<String, Object>) this.config.getAll();
	}

	public String getDataName(){
		return "data";
	}

	public void save(){
		this.config.setAll(this.data);
		this.config.save();
	}

	public LinkedHashMap<String, Object> getAll(){
		return this.data;
	}

	protected String toString(Position pos){
		return pos.getLevel().getFolderName() + ":" + Double.toString(pos.x) + ":" + Double.toString(pos.y) + ":" + Double.toString(pos.z) + ":";
	}

	protected Position toPosition(String string){
		String[] starr = string.split(":");
		Level level = Server.getInstance().getLevelByName(starr[0]);
		if(level == null){
			level = Server.getInstance().getDefaultLevel();
		}
		double x = Double.parseDouble(starr[1]);
		double y = Double.parseDouble(starr[2]);
		double z = Double.parseDouble(starr[3]);
		return new Position(x, y, z, level);
	}
}