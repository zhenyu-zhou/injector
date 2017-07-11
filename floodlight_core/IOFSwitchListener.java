package floodlight_core;

public interface IOFSwitchListener
{
	public void switchAdded(long switchId);

    public void switchRemoved(long switchId);

    public void switchActivated(long switchId);

    public void switchPortChanged(long switchId,
                                  ImmutablePort port,
                                  IOFSwitch.PortChangeType type);

    public void switchChanged(long switchId);
}
