package solo.soloportal;

import cn.nukkit.plugin.PluginBase;
import solo.solobasepackage.util.ArrayUtil;
import solo.solobasepackage.util.Message;
import solo.solobasepackage.util.ParticleUtil;
import cn.nukkit.Player;
import cn.nukkit.event.Listener;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.block.SignChangeEvent;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.event.player.PlayerJoinEvent;
import cn.nukkit.event.player.PlayerTeleportEvent;
import cn.nukkit.event.player.PlayerToggleSneakEvent;
import cn.nukkit.event.block.BlockBreakEvent;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.level.particle.Particle;
import cn.nukkit.level.Position;
import cn.nukkit.level.Level;
import cn.nukkit.block.Block;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class Main extends PluginBase implements Listener {

	private static Main instance = null;

	public WarpManager warpManager;
	public StuffWarpManager stuffWarpManager;
	public PortalManager portalManager;

	@Override
	public void onEnable() {
		if(instance == null){
			instance = this;
		}
		this.getDataFolder().mkdirs();
		this.warpManager = new WarpManager();
		this.stuffWarpManager = new StuffWarpManager();
		this.portalManager = new PortalManager();

		this.getServer().getScheduler().scheduleRepeatingTask(new PortalUpdateTask(this), 4);
		this.getServer().getPluginManager().registerEvents(this, this);
	}

	@Override
	public void onDisable() {
		this.warpManager.save();
		this.stuffWarpManager.save();
		this.portalManager.save();
	}

	public static Main getInstance(){
		return instance;
	}

	public boolean warpTo(Player player, String warpName){
		Position pos = this.warpManager.get(warpName);
		String[] test = warpName.split("월드:");
		if(test.length == 2){
			Level level = this.getServer().getLevelByName(test[1]);
			if(level == null){
				Message.alert(player, test[1] + " 월드는 존재하지 않습니다.");
				return false;
			}
			pos = level.getSpawnLocation();
		}
		if(pos == null){
			Message.alert(player, warpName + " 워프는 존재하지 않습니다.");
			return false;
		}
		if(!player.isOp() && this.warpManager.isLocked(warpName)){
			Message.alert(player, warpName + " 워프는 잠겨있습니다.");
			return false;
		}
		player.teleport(pos);
		Message.normal(player, warpName + " (으)로 이동하였습니다.");
		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
		String cmd = command.getName();
		if(cmd.equals("워프") || cmd.toLowerCase().equals("warp")) {
			if(!(sender instanceof Player)){
				sender.sendMessage("§c인게임에서만 사용가능합니다.");
				return true;
			}
			Player player = (Player) sender;

			int page = 1;
			boolean showHelp = false;
			try{
				page = Integer.parseInt(args[0]);
				showHelp = true;
			}catch(Exception e){
				if(args.length < 1){
					showHelp = true;
				}
			}
			if(showHelp){
				ArrayList<String> information = new ArrayList<String>();
				information.add("/워프 [워프 이름] 또는 /[워프 이름] - 해당 워프로 이동합니다.");
				if(player.isOp()){
					information.add("/워프 월드:[월드 이름] 으로 입력하세면 해당 월드로 이동합니다. 예) /워프 월드:spawn");
				}
				information.add("/워프 목록 - 워프목록을 표시합니다.");
				if(player.isOp()) {
					information.add("/워프 생성 [워프 이름] - 워프를 생성합니다.");
					information.add("/워프 제거 [워프 이름] - 워프를 제거합니다.");
					information.add("/워프 잠금 [워프 이름] - 워프를 잠급니다.");
					information.add("/워프 잠금해제 [워프 이름] - 워프를 잠금 해제합니다.");
				}
				Message.page(sender, "워프 명령어 목록", information, page);
				return true;
			}

			StringBuilder sb;
			String warpName;
			boolean f;
			boolean s;

			switch(args[0]) {
				case "생성":
				case "추가":
					if(!player.isOp()) {
						Message.normal(player, "권한이 없습니다.");
						return true;
					}
					if(args.length < 2) {
						Message.normal(player, "사용법 : /" + cmd + " " + args[0] + " " + "[워프 이름]");
						return true;
					}
					switch(args[1]) {
						case "생성":
						case "제거":
						case "추가":
						case "삭제":
						case "목록":
						case "잠금":
						case "잠금해제":
							Message.alert(player, "그 이름으로 워프를 생성할 수 없습니다.");
							return true;
						default:
							break;
					}
					f = true;
					s = true;
					sb = new StringBuilder();
					for(String arg : args){
						if(f){
							f = false;
							continue;
						}
						if(s){
							s = false;
						}else{
							sb.append(" ");
						}
						sb.append(arg);
					}
					warpName = sb.toString();
					if(this.warpManager.get(warpName) != null){
						Message.alert(player, warpName + " 은(는) 이미 존재하는 워프입니다.");
						return true;
					}
					this.warpManager.create(new Position(player.x, player.y, player.z, player.getLevel()), warpName);
					Message.normal(player, "성공적으로 워프를 생성하였습니다.");
					return true;

				case "제거":
				case "삭제":
					if(!player.isOp()) {
						Message.alert(player, "권한이 없습니다.");
						return true;
					}
					if(args.length < 2){
						Message.usage(player, "/" + cmd + " " + args[0] + " " + "[워프 이름]");
						return true;
					}
					f = true;
					s = true;
					sb = new StringBuilder();
					for(String arg : args){
						if(f){
							f = false;
							continue;
						}
						if(s){
							s = false;
						}else{
							sb.append(" ");
						}
						sb.append(arg);
					}
					warpName = sb.toString();
					if(this.warpManager.get(warpName) == null){
						Message.alert(player, "해당 워프는 존재하지 않습니다.");
						return true;
					}
					this.warpManager.remove(warpName);
					Message.normal(player, "성공적으로 워프를 삭제하였습니다.");
					return true;

				case "목록":
					f = true;
					sb = new StringBuilder();
					LinkedHashMap<String, Object> data = this.warpManager.getAll();
					for(String wName : data.keySet()){
						if(f){
							f = false;
						}else{
							sb.append(", ");
						}
						sb.append(wName);
						LinkedHashMap<String, Object> dat = (LinkedHashMap<String, Object>) data.get(wName);
						if((boolean)dat.get("lock")){
							sb.append("(잠김)");
						}
					}
					Message.normal(player, "워프 목록 : " + sb.toString());
					return true;

				case "잠금":
					if(!player.isOp()) {
						Message.alert(player, "권한이 없습니다.");
						return true;
					}
					if(args.length < 2){
						Message.usage(player, "/" + cmd + " " + args[0] + " " + "[워프 이름]");
						return true;
					}
					f = true;
					s = true;
					sb = new StringBuilder();
					for(String arg : args){
						if(f){
							f = false;
							continue;
						}
						if(s){
							s = false;
						}else{
							sb.append(" ");
						}
						sb.append(arg);
					}
					warpName = sb.toString();
					if(this.warpManager.get(warpName) == null){
						Message.alert(player, "해당 워프는 존재하지 않습니다.");
						return true;
					}
					if(this.warpManager.isLocked(warpName)){
						Message.alert(player, "해당 워프는 이미 잠겨있습니다.");
						return true;
					}
					this.warpManager.lock(warpName);
					Message.normal(player, "성공적으로 워프를 잠궜습니다.");
					return true;

				case "잠금해제":
					if(!player.isOp()) {
						Message.alert(player, "권한이 없습니다.");
						return true;
					}
					if(args.length < 2){
						Message.usage(player, "/" + cmd + " " + args[0] + " " + "[워프 이름]");
						return true;
					}
					f = true;
					s = true;
					sb = new StringBuilder();
					for(String arg : args){
						if(f){
							f = false;
							continue;
						}
						if(s){
							s = false;
						}else{
							sb.append(" ");
						}
						sb.append(arg);
					}
					warpName = sb.toString();
					if(this.warpManager.get(warpName) == null){
						Message.alert(player, "해당 워프는 존재하지 않습니다.");
						return true;
					}
					if(!this.warpManager.isLocked(warpName)){
						Message.alert(player, "해당 워프는 잠겨있지 않습니다.");
						return true;
					}
					this.warpManager.unlock(warpName);
					Message.normal(player, "성공적으로 워프를 잠금 해제하였습니다.");
					return true;

				default:
					s = true;
					sb = new StringBuilder();
					for(String arg : args){
						if(s){
							s = false;
						}else{
							sb.append(" ");
						}
						sb.append(arg);
					}
					warpName = sb.toString();
					this.warpTo(player, warpName);
					return true;
			}
		}
		return true;
	}//함수 끝


	@EventHandler
	public void onSignChange(SignChangeEvent event){
		Player player = event.getPlayer();
		if(player.isOp()){
			String line1 = event.getLine(0);
			String line2 = event.getLine(1);
			String line3 = event.getLine(2);
			String line4 = event.getLine(3);
			
			ArrayList<String> information;
			
			if(line1.equals("워프생성")){
				event.setCancelled();
				if(line2.equals("")) {
					information = new ArrayList<String>();
					information.add("1줄 : 워프생성");
					information.add("2줄 : [워프 이름]");
					information.add("3줄 : [워프 블럭] (표지판, 나무버튼, 돌버튼, 레버) > 비워두셔도 됩니다.");
					Message.info(player, "워프 생성법", information);
					return;
				}
				if(this.warpManager.get(line2) == null) {
					Message.alert(player, "존재하지 않는 워프입니다.");
					event.setCancelled();
					return;
				}
				int stuff = StuffWarpManager.SIGN;
				switch(line3){
					case "나무버튼":
						stuff = StuffWarpManager.WOODEN_BUTTON;
						break;
					case "돌버튼":
					case "버튼":
						stuff = StuffWarpManager.STONE_BUTTON;
						break;
					case "레버":
						stuff = StuffWarpManager.LEVER;
				}
				this.stuffWarpManager.create(event.getBlock(), line2, stuff);
				Message.normal(player, "성공적으로 워프를 생성하였습니다.");
	
			}else if(line1.equals("포탈생성")){
				event.setCancelled();
				if(line2.equals("")) {
					information = new ArrayList<String>();
					information.add("1줄 : 포탈생성");
					information.add("2줄 : [워프 이름]");
					information.add("3줄 : [파티클] " + ArrayUtil.implode(", ", ParticleUtil.getAvailable()));
					information.add("4줄 : [포탈 색깔(떨어지는 먼지 파티클에만 적용됨)] (빨강, 초록, 파랑, 노랑, 분홍, 하늘색, 흰색, 회색, 진한회색, 검정)");
					Message.info(player, "포탈 생성법", information);
					return;
				}
				if(this.warpManager.get(line2) == null) {
					Message.normal(player, "존재하지 않는 워프입니다.");
					event.setCancelled();
					return;
				}
				int type = ParticleUtil.fromString(line3);
				if(type == 0){
					type = Particle.TYPE_FALLING_DUST;
				}
				int color = Portal.COLOR_SKY_BLUE;
				switch(line4){
					case "빨강": color = Portal.COLOR_RED; break;
					case "초록": color = Portal.COLOR_GREEN; break;
					case "파랑": color = Portal.COLOR_BLUE; break;
					case "노랑": color = Portal.COLOR_YELLOW; break;
					case "분홍": color = Portal.COLOR_PINK; break;
					case "하늘색": color = Portal.COLOR_SKY_BLUE; break;
					case "흰색": color = Portal.COLOR_WHITE; break;
					case "진한회색": color = Portal.COLOR_DARK_GRAY; break;
					case "검정": color = Portal.COLOR_DARK; break;
				}
				this.portalManager.create(event.getBlock(), line2, type, color);
				Message.normal(player, "성공적으로 포탈을 생성하였습니다.");
			}
		}
	}

	@EventHandler
	public void onBreak(BlockBreakEvent event) {
		Block block = event.getBlock();
		Player player = event.getPlayer();
		if(player.isOp()){
			if(this.stuffWarpManager.get(block) != null){
				this.stuffWarpManager.remove(block);
				Message.normal(player, "성공적으로 워프를 삭제하였습니다.");
			}else if(this.portalManager.get(block) != null){
				this.portalManager.remove(block);
				Message.normal(player, "성공적으로 포탈을 삭제하였습니다.");
			}
		}
	}

	@EventHandler
	public void onInteract(PlayerInteractEvent event) {
		Block block = event.getBlock();
		Player player = event.getPlayer();
		switch(block.getId()) {
			case 77:
			case 69:
			case 68:
			case 63:
			case 143:
				break;
			default:
				return;
		}
		String warpName = this.stuffWarpManager.get(block);
		if(warpName == null){
			return;
		}
		Position pos = this.warpManager.get(warpName);
		if(pos == null){
			this.stuffWarpManager.remove(block);
			return;
		}
		this.warpTo(player, warpName);
		event.setCancelled();
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onTeleport(PlayerTeleportEvent event){
		this.portalManager.portals.values().forEach((k) -> k.spawnTo(event.getPlayer(), event.getTo().getLevel()));
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onJoin(PlayerJoinEvent event){
		this.portalManager.portals.values().forEach((k) -> k.spawnTo(event.getPlayer()));;
	}

	@EventHandler
	public void onSneak(PlayerToggleSneakEvent event){
		if(!event.isSneaking()){
			return;
		}
		Player player = event.getPlayer();
		Position portalPos = new Position(Math.floor(player.x), Math.floor(player.y), Math.floor(player.z), player.getLevel());
		String warpName = this.portalManager.get(portalPos);
		if(warpName == null){
			return;
		}
		Position warpPos = this.warpManager.get(warpName);
		if(warpPos == null){
			this.portalManager.remove(portalPos);
			return;
		}
		this.warpTo(player, warpName);
		event.setCancelled();
	}
}