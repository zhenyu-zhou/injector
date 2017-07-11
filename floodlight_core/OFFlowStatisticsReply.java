package floodlight_core;

public class OFFlowStatisticsReply implements OFStatistics
{
	protected OFMatch match;
	
	public OFMatch getMatch() {
        return match;
    }
}
