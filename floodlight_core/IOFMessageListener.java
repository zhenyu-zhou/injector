package floodlight_core;

public interface IOFMessageListener
{
	Command receive(IOFSwitch sw, OFMessage msg, FloodlightContext cntx);
}
