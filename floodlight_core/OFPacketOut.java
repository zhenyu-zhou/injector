package floodlight_core;

import java.util.List;

public class OFPacketOut extends OFMessage
{
	public static int BUFFER_ID_NONE = 0xffffffff;
	public static int MINIMUM_LENGTH = 8;
	
	protected int bufferId;
	protected short inPort;
	protected short actionsLength;
	protected byte[] packetData;
	protected List<OFAction> actions;
	
    public OFPacketOut setBufferId(int bufferId) {
        this.bufferId = bufferId;
        return this;
    }
    
    public OFPacketOut setInPort(short inPort) {
        this.inPort = inPort;
        return this;
    }
    
    public OFPacketOut setActions(List<OFAction> actions) {
        this.actions = actions;
        return this;
    }
    
    public OFPacketOut setActionsLength(short actionsLength) {
        this.actionsLength = actionsLength;
        return this;
    }
    
    public short getActionsLength() {
        return this.actionsLength;
    }
    
    public OFPacketOut setPacketData(byte[] packetData) {
        this.packetData = packetData;
        return this;
    }
}
