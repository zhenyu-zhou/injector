package floodlight_core;

import java.util.List;

public class OFStatisticsReply extends OFMessage
{
	protected List<? extends OFStatistics> statistics;
	
	public List<? extends OFStatistics> getStatistics() {
        return statistics;
    }
}
