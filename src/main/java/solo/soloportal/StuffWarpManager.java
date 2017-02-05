package solo.soloportal;

import cn.nukkit.level.Position;
import cn.nukkit.block.Block;
import cn.nukkit.blockentity.BlockEntitySign;

public class StuffWarpManager extends Manager{

	public static final int SIGN = 68; //[0 => 남, 2 => 북, 4 => 서, 5 => 동]
	public static final int STONE_BUTTON = 77; //[0 => 천장, 1 => 바닥, 2 => 북, 3 => 남, 4 => 서, 5 => 동]
	public static final int WOODEN_BUTTON = 143;
	public static final int LEVER = 69; //[0 => 천장, 1 => 동, 2 => 서, 3 => 남, 4 => 북, 5 => 바닥]

	@Override
	public String getDataName(){
		return "stuff";
	}

	public void create(Block block, String warpName){
		this.create(block, warpName, SIGN);
	}
	public void create(Block block, String warpName, int stuff){
		this.data.put(this.toString(block), warpName);
		String mode = "floor";
		int damage = 0;
		if(block.getId() == 68){
			switch(block.getDamage()){
				case 2:
					mode = "north";
					break;
				case 4:
					mode = "west";
					break;
				case 5:
					mode = "east";
					break;
				default:
					mode = "south";
			}
		}
		switch(stuff){
			case SIGN:
				BlockEntitySign sign = (BlockEntitySign) block.getLevel().getBlockEntity(block);
				sign.setText("§b[ 워프 ]", "§f" + warpName);
				return;

			case STONE_BUTTON:
			case WOODEN_BUTTON:
				switch(mode){
					case "north":
						damage = 2;
						break;
					case "west":
						damage = 4;
						break;
					case "east":
						damage = 5;
						break;
					case "south":
						damage = 3;
						break;
					default:
						damage = 1;
				}
				break;
			case LEVER:
				switch(mode){
					case "north":
						damage = 4;
						break;
					case "west":
						damage = 2;
						break;
					case "east":
						damage = 1;
						break;
					case "south":
						damage = 3;
						break;
					default:
						damage = 5;
				}
				break;
		}
		block.getLevel().setBlock(block, Block.get(stuff, damage));
	}

	public boolean remove(Position pos){
		String str = this.toString(pos);
		if(this.data.containsKey(str)){
			this.data.remove(str);
			return true;
		}
		return false;
	}

	public String get(Position pos){
		String str = this.toString(pos);
		if(this.data.containsKey(str)){
			return (String) this.data.get(str);
		}
		return null;
	}
}
