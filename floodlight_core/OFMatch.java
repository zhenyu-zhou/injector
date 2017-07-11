package floodlight_core;

public class OFMatch
{
	protected byte[] dataLayerSource;
    protected byte[] dataLayerDestination;
    protected short dataLayerVirtualLan;
    protected short inputPort;

	public OFMatch loadFromPacket(byte[] packetData, short inPort)
	{
		return this;
	}

	public byte[] getDataLayerDestination() {
        return this.dataLayerDestination;
    }
	
	public byte[] getDataLayerSource() {
        return this.dataLayerSource;
    }
	
	public short getDataLayerVirtualLan() {
        return this.dataLayerVirtualLan;
    }

	public Short getInputPort()
	{
		return inputPort;
	}

	public OFMatch clone() {
        try {
            OFMatch ret = (OFMatch) super.clone();
            ret.dataLayerDestination = this.dataLayerDestination.clone();
            ret.dataLayerSource = this.dataLayerSource.clone();
            return ret;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
	
	public OFMatch setInputPort(short inputPort) {
        this.inputPort = inputPort;
        return this;
    }
}
