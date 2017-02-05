package solo.soloportal;

import cn.nukkit.scheduler.PluginTask;

public class PortalUpdateTask extends PluginTask<Main>{

	public PortalUpdateTask(Main owner){
		super(owner);
	}

	@Override
	public void onRun(int currentTick){
		this.owner.portalManager.update();
	}
}