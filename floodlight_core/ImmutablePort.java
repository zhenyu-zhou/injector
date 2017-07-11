package floodlight_core;

public class ImmutablePort
{
	public final int srcId, dstId, srcPort, dstPort;
	
	public ImmutablePort(int s, int sp, int d, int dp)
	{
		srcId = s;
		dstId = d;
		srcPort = sp;
		dstPort = dp;
	}
}
