package app;

import java.util.LinkedList;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import floodlight_core.*;

public class Hedera implements IOFMessageListener
{
	// 1: has_port_1; 2: buffer overflow; 3: cache; 4: random;
	// 5: blackhole; 6: copy & paste; 7: wrong action; 8: forgot packet;
	// 9: race condition
	private final int BUG;

	private boolean has_port1 = false;
	private int count_port2 = 0;
	private int idx_bug = -1;

	public boolean isHas_port1()
	{
		return has_port1;
	}

	public int getCount_port2()
	{
		return count_port2;
	}
	
	public Hedera(int bug)
	{
		BUG = bug;
		if (BUG == 9)
		{
			Thread worker = new Thread(new Worker());
			worker.start();
		}
	}
	
	private static final byte[] MULTICAST_DNS = HexString
			.fromHexString("33:33:00:00:00:fb");
	static final byte[] FAKE_DST_ETH = HexString
			.fromHexString("ee:ee:ee:ee:ee:ee");

	static final short SIGNAL_VLAN_ID = Short.MAX_VALUE;
	static final String FAKE_SRC_IP = "255.255.255.255";
	static final int FAKE_DST_IP = Integer.MAX_VALUE;

	private final ConcurrentMap<Long, LinkedList<OFMatch>> swMatchMap = new ConcurrentHashMap<Long, LinkedList<OFMatch>>();

	private Command handlePacketIn(IOFSwitch sw, OFPacketIn pi,
			FloodlightContext cntx)
	{
		System.out.println("hedera: in handle pkt in!!");
		
		if (BUG == 1)
		{
			if (pi.getInPort() == 1)
			{
				has_port1 = true;
			}
			else if (pi.getInPort() == 2)
			{
				count_port2++;
				if (has_port1 && count_port2 == 3)
					throw new RuntimeException("Port 2 Exception");
			}
			// print_states();
		}
		else if (BUG == 2)
		{
			if (pi.getInPort() == 1)
			{
				has_port1 = true;
			}
			else
			{
				if (has_port1)
				{
					byte[] buf = pi.getPacketData();
					byte b = buf[99];
				}
			}
		}
		else if (BUG == 3)
		{
			if (pi.getInPort() > 1)
			{
				throw new RuntimeException("Invalid port!");
			}
		}
		else if (BUG == 4)
		{
			if (pi.getInPort() == 1)
			{
				has_port1 = true;
			}
			else if (pi.getInPort() == 2)
			{
				count_port2++;
				Random r = new Random();
				double rd = r.nextDouble();
				if (has_port1 && count_port2 == 3 && rd > 0.8)
					throw new RuntimeException("Port 2 Exception");
			}
		}
		else if (BUG == 5)
		{
			if (pi.getInPort() == 1)
			{
				has_port1 = true;
			}
			else if (pi.getInPort() == 2)
			{
				count_port2++;
				if (has_port1 && count_port2 == 3)
					// TODO: no data plane in the injector system!
					// Therefore this is the same as bug1
					throw new RuntimeException("Blackhole");
			}
		}
		else if (BUG == 6)
		{
			if (pi.getInPort() == 1)
			{
				has_port1 = true;
			}
			else
			{
				if (has_port1)
				{
					byte[] buf = new byte[100];
					byte b = buf[99];
					buf = pi.getPacketData();
					// copy & paste
					b = buf[99];
				}
			}
		}
		else if (BUG == 9)
		{
			if (pi.getInPort() == 1)
			{
				has_port1 = true;
			}
			else
			{
				if (has_port1)
				{
					try
					{
						byte[] buf = pi.getPacketData();
						idx_bug = 99;
						Thread.sleep(100);
						byte b = buf[idx_bug];
						idx_bug = -1;
					} catch (InterruptedException e)
					{
						e.printStackTrace();
					}
				}
			}
		}

		final OFMatch pktMatch = new OFMatch();
		pktMatch.loadFromPacket(pi.getPacketData(), pi.getInPort());

		final Long dstLong = Ethernet
				.toLong(pktMatch.getDataLayerDestination());
		if (dstLong.equals(Ethernet.toLong(MULTICAST_DNS)))
		{
			System.out.println("hedera: multicast dns");
			return Command.CONTINUE;
		}

		final Ethernet eth = new Ethernet();
		// IFloodlightProviderService.bcStore.get(cntx,
		// IFloodlightProviderService.CONTEXT_PI_PAYLOAD);
		final Long sourceMACHash = Ethernet.toLong(eth.getSourceMACAddress());
		if (dstLong.equals(Ethernet.toLong(FAKE_DST_ETH)))
		{
			System.out.println("hedera: FAKE_DST_ETH");
			String peerPortMac = HexString.toHexString(dstLong);
			/* if (!this.netTopo.addSwToSwLink(peerPortMac, sw.getId(), pi.getInPort())) {
			  // Not a new link!
			  this.knownMacs.add(peerPortMac);
			} */
			return Command.CONTINUE;
		}
		if (!(this.swMatchMap.containsKey(sw.getId()) && this.swMatchMap.get(
				sw.getId()).contains(pktMatch)))
		{
			System.out.println("hedera: swMatchMap has match "
					+ this.swMatchMap.containsKey(sw.getId()));
			return Command.CONTINUE;
		}

		// BUG 7, 8: No write actions anymore
		/* System.out.println("hedera: update table");
		final String srcMac = HexString.toHexString(sourceMACHash);
		this.addHostToSw(srcMac, sw.getId(), pi.getInPort());

		final OFMatch flowMatch = pktMatch.clone()
				.setInputPort(Short.MAX_VALUE);
		this.flowStatMap.putIfAbsent(flowMatch, 0L);

		final Path p = this.pathUtil.findPath(flowMatch);
		try
		{
			this.pathUtil.installPath(p, false);
		} catch (IOException e)
		{
			e.printStackTrace();
		} */
		return Command.CONTINUE;
	}

	  private Command handleStatsReply(IOFSwitch sw, OFStatisticsReply pi, FloodlightContext cntx) {
		  System.out.println("in handleStatsReply!!");
	    /* if (!this.statSrcs.add(sw.getId())) {
	      // WARNING: Assume that when we receive a second STATS_REPLY from a switch,
	      //  it implies that a new stat. collection cycle has started.

	      // Clear prior stat. computations.
	      this.flowStatMap.clear();
	    } */

	    for (OFStatistics stat : pi.getStatistics()) {
	      // Only interested in flow statistics.
	      if (!(stat instanceof OFFlowStatisticsReply)) {
	        continue;
	      }

	      final OFFlowStatisticsReply flowStat = (OFFlowStatisticsReply) stat;
	      final OFMatch match = flowStat.getMatch().clone().setInputPort(Short.MAX_VALUE);
	      // this.flowStatMap.put(match, flowStat.getByteCount());
	    }

	    /* final Map<String, ConcurrentMap<String, FlowStatsWrapper>> flowBytesMap =
	        DemandEstimator.getFlowBytes(pi.getStatistics());
	    this.perFlowStats.putAll(flowBytesMap);

	    try {
	      this.pathUtil.bcastPortMac(sw.getId());
	    } catch (IOException e) {
	      e.printStackTrace();
	      LOG.error("receive> failed to broadcast port mac on all ports; {}", e.getLocalizedMessage());
	    }
	    this.loadBalanceFlows(); */
	    return Command.CONTINUE;
	  }
	
	public Command receive(IOFSwitch sw, OFMessage msg, FloodlightContext cntx)
	{
		if (BUG == 9)
		{
			Thread worker = new Thread(new Worker());
			worker.start();
		}

		if (msg.getType().equals(OFType.PACKET_IN))
		{
			return this.handlePacketIn(sw, (OFPacketIn) msg, cntx);
		}

		if (msg.getType().equals(OFType.STATS_REPLY))
		{
			return this.handleStatsReply(sw, (OFStatisticsReply) msg, cntx);
		}
		return Command.CONTINUE;
	}
	
	class Worker implements Runnable
	{
		@Override
		public void run()
		{
			while (true)
			{
				System.out.println("worker");
				try
				{
					Thread.sleep(200);
				} catch (InterruptedException e)
				{
					e.printStackTrace();
				}
				if (idx_bug > 0)
				{
					idx_bug = 0;
				}
			}
		}
	}
}
