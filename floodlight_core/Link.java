package floodlight_core;

public class Link
{
	private long src;
	private short srcPort;
	private long dst;
	private short dstPort;

	public Link(long srcId, short srcPort, long dstId, short dstPort)
	{
		this.src = srcId;
		this.srcPort = srcPort;
		this.dst = dstId;
		this.dstPort = dstPort;
	}

	// Convenience method
	public Link(long srcId, int srcPort, long dstId, int dstPort)
	{
		this.src = srcId;
		this.srcPort = (short) srcPort;
		this.dst = dstId;
		this.dstPort = (short) dstPort;
	}

	@Override
	public boolean equals(Object o)
	{
		if (!(o instanceof Link))
		{
			return false;
		}
		Link l = (Link) o;
		return this.src == l.src && this.srcPort == l.srcPort
				&& this.dst == l.dst && this.dstPort == l.dstPort;
	}

	@Override
	public String toString()
	{
		return "src: " + this.src + ", srcPort: " + this.srcPort + ", dst: "
				+ dst + ", dstPort: " + dstPort;
	}

	// zzy: assume each one has only one digit, ie. \in [1, 9]
	@Override
	public int hashCode()
	{
		return (int) (src * 1000 + srcPort * 100 + dst * 10 + dstPort);
	}
}
