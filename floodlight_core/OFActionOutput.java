package floodlight_core;

public class OFActionOutput extends OFAction
{
	public static int MINIMUM_LENGTH = 8;
	
	protected short port;

    public OFActionOutput setPort(short port) {
        this.port = port;
        return this;
    }
}
