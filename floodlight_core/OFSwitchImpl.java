package floodlight_core;

public class OFSwitchImpl implements IOFSwitch
{
	protected long datapathId;
	
	public long getId() {
        return this.datapathId;
    }
}
