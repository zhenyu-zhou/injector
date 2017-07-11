package app;

import java.util.Random;

import floodlight_core.*;

public class LoadBalancer implements IOFMessageListener
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

	public LoadBalancer(int bug)
	{
		BUG = bug;
		if (BUG == 9)
		{
			Thread worker = new Thread(new Worker());
			worker.start();
		}
	}
	
	@Override
	public Command receive(IOFSwitch sw, OFMessage msg, FloodlightContext cntx)
	{
		if (msg.getType().equals(OFType.PACKET_IN))
		{
			handle(sw, (OFPacketIn) msg, cntx);
		}
		
        /* Ethernet eth = IFloodlightProviderService.bcStore.get(cntx,
                                                              IFloodlightProviderService.CONTEXT_PI_PAYLOAD);
        IPacket pkt = eth.getPayload();
 
        if (eth.isBroadcast() || eth.isMulticast()) {
            // handle ARP for VIP
            if (pkt instanceof ARP) {
                // retrieve arp to determine target IP address                                                       
                ARP arpRequest = (ARP) eth.getPayload();

                int targetProtocolAddress = IPv4.toIPv4Address(arpRequest
                                                               .getTargetProtocolAddress());

                if (vipIpToId.containsKey(targetProtocolAddress)) {
                    String vipId = vipIpToId.get(targetProtocolAddress);
                    vipProxyArpReply(sw, pi, cntx, vipId);
                    return Command.STOP;
                }
            }
        } else {
            // currently only load balance IPv4 packets - no-op for other traffic 
            if (pkt instanceof IPv4) {
                IPv4 ip_pkt = (IPv4) pkt;
                
                // If match Vip and port, check pool and choose member
                int destIpAddress = ip_pkt.getDestinationAddress();
                
                if (vipIpToId.containsKey(destIpAddress)){
                    IPClient client = new IPClient();
                    client.ipAddress = ip_pkt.getSourceAddress();
                    client.nw_proto = ip_pkt.getProtocol();
                    if (ip_pkt.getPayload() instanceof TCP) {
                        TCP tcp_pkt = (TCP) ip_pkt.getPayload();
                        client.srcPort = tcp_pkt.getSourcePort();
                        client.targetPort = tcp_pkt.getDestinationPort();
                    }
                    if (ip_pkt.getPayload() instanceof UDP) {
                        UDP udp_pkt = (UDP) ip_pkt.getPayload();
                        client.srcPort = udp_pkt.getSourcePort();
                        client.targetPort = udp_pkt.getDestinationPort();
                    }
                    if (ip_pkt.getPayload() instanceof ICMP) {
                        client.srcPort = 8; 
                        client.targetPort = 0; 
                    }
                    
                    LBVip vip = vips.get(vipIpToId.get(destIpAddress));
                    LBPool pool = pools.get(vip.pickPool(client));
                    LBMember member = members.get(pool.pickMember(client));

                    // for chosen member, check device manager and find and push routes, in both directions                    
                    pushBidirectionalVipRoutes(sw, pi, cntx, client, member);
                   
                    // packet out based on table rule
                    pushPacket(pkt, sw, pi.getBufferId(), pi.getInPort(), OFPort.OFPP_TABLE.getValue(),
                                cntx, true);

                    return Command.STOP;
                }
            }
        } */
        return Command.CONTINUE; 
	}
	
	private void handle(IOFSwitch sw, OFPacketIn pi, FloodlightContext cntx)
	{
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
