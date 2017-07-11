package floodlight_core;

public class OFMessage
{
	protected short length;
	protected OFType type;

    public OFMessage setLength(short length) {
        this.length = length;
        return this;
    }
    
    public OFType getType() {
        return type;
    }
}
