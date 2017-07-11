package floodlight_core;

public interface IOFSwitch
{
	public enum PortChangeType {
        ADD, OTHER_UPDATE, DELETE, UP, DOWN,
    }

	long getId();

}
