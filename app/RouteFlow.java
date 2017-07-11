package app;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import floodlight_core.*;
import floodlight_core.IOFSwitch.PortChangeType;

public class RouteFlow implements IOFMessageListener, IOFSwitchListener,
		ILinkDiscoveryListener
{
    private static final int DEF_NUM_SWS   = 16;
    private static final int DEF_NUM_LINKS = DEF_NUM_SWS * 2;

    // Hosts are always assumed to be at the first port of a switch.
    public static final short SW_HOST_PORT = 1;
	
	protected final AtomicInteger numSws;
	protected final Set<Long> activeSws;
	public Set<Long> getActiveSws()
	{
		return activeSws;
	}

	// Mapping from host-ip to sw-port
    protected final Map<Integer, Long>          hostToSw;
    // Network links.
    protected final Map<Link, LinkInfo>         netwLinks;
    // Network routes.
    protected final Map<Long, Map<Long, Short>> netwRoutes;
    
    // Update links without service
    public Map<Link, LinkInfo> links;
	
	public RouteFlow()
	{
		this.numSws = new AtomicInteger(0);
		this.activeSws = new HashSet<Long>(DEF_NUM_SWS);
		this.hostToSw = new HashMap<Integer, Long>(DEF_NUM_SWS);
        this.netwLinks = new HashMap<Link, LinkInfo>(DEF_NUM_LINKS);
        this.netwRoutes = new HashMap<Long, Map<Long, Short>>(DEF_NUM_SWS);
        
        this.links = new HashMap<Link, LinkInfo>(DEF_NUM_LINKS);
	}

	@Override
	public Command receive(IOFSwitch sw, OFMessage msg, FloodlightContext cntx)
	{
		// System.out.println(String.format("receive> %s => %s", sw.getId(),
		// 		msg.getType()));
		return Command.CONTINUE;
	}

	@Override
	public void switchAdded(long switchId)
	{
		final int numSws = this.numSws.incrementAndGet();
		final int numActiveSws = this.activeSws.size();
		// System.out.println(String.format(
		// 		"switchAdded> [%d] %s; #switches: %d, #active: %d", switchId,
		// 		Long.toHexString(switchId), numSws, numActiveSws));
	}

	@Override
	public void switchRemoved(long switchId)
	{
		synchronized (this.activeSws)
		{
			this.activeSws.remove(switchId);
		}
		final int numActiveSws = this.activeSws.size();
		final int numSws = this.numSws.decrementAndGet();
		// System.out.println(String.format(
		// 		"switchRemoved> [%d] %s; #switches: %d, #active: %s", switchId,
		// 		Long.toHexString(switchId), numSws, numActiveSws));

		synchronized (this.activeSws)
		{
			if (this.processLinkUpdates())
			{
				this.computeRoutes();
			}
		}
	}

	private void computeRoutes()
	{
		// TODO Auto-generated method stub
	}

	private boolean processLinkUpdates()
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void switchActivated(long switchId)
	{
		synchronized (this.activeSws)
		{
			this.activeSws.add(switchId);
			this.floodARP(switchId);
		}
		final int numActiveSws = this.activeSws.size();
		final int numSws = this.numSws.get();
		// System.out.println(String.format(
		// 		"switchActivated> [%d] %s; #switches: %d, #active: %d",
		// 		switchId, Long.toHexString(switchId), numSws, numActiveSws));

		synchronized (this.activeSws)
		{
			if (this.processLinkUpdates())
			{
				this.computeRoutes();
			}
		}
	}

	private void floodARP(long switchId)
	{
		// TODO Auto-generated method stub
	}

	@Override
	public void switchPortChanged(long switchId, ImmutablePort port,
			PortChangeType type)
	{
		// System.out.println(String.format("switchPortChanged> [%d] %s: %s (%s)",
		// 		switchId, Long.toHexString(switchId), port.toString(),
		// 		type.toString()));
		synchronized (this.activeSws)
		{
			if (this.processLinkUpdates())
			{
				this.computeRoutes();
			}
		}
	}

	@Override
	public void switchChanged(long switchId)
	{
		// System.out.println(String.format("switchChanged> [%d] %s", switchId,
		// 		Long.toHexString(switchId)));
	}
	
	public void addLink(int src, int srcPort, int dst, int dstPort)
	{
		addLink(new Link(src, srcPort, dst, dstPort));
	}

	public void removeLink(int src, int srcPort, int dst, int dstPort)
	{
		removeLink(new Link(src, srcPort, dst, dstPort));
	}
	
	public void addLink(Link l)
	{
		links.put(l, null);
	}
	
	public void removeLink(Link l)
	{
		links.remove(l);
	}
}
