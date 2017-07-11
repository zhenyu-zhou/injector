package floodlight_core;

public class LinkInfo
{
	protected Long firstSeenTime;
	protected Long lastLldpReceivedTime; /* Standard LLLDP received time */
	protected Long lastBddpReceivedTime; /* Modified LLDP received time  */

	public LinkInfo(Long firstSeenTime, Long lastLldpReceivedTime,
			Long lastBddpReceivedTime)
	{
		this.firstSeenTime = firstSeenTime;
		this.lastLldpReceivedTime = lastLldpReceivedTime;
		this.lastBddpReceivedTime = lastBddpReceivedTime;
	}
}
