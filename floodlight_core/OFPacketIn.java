package floodlight_core;

public class OFPacketIn extends OFMessage
{
	protected int bufferId;
	protected short inPort;
	protected byte[] packetData;
	
    public int getBufferId() {
        return this.bufferId;
    }
    
    public short getInPort() {
        return this.inPort;
    }
    
    public void setInPort(short p) {
        this.inPort = p;
    }
    
    public byte[] getPacketData() {
        return this.packetData;
    }
}
