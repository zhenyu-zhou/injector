package app;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import floodlight_core.*;

public class LearningSwitch implements IOFMessageListener
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
	
	public LearningSwitch(int bug)
	{
		BUG = bug;
		if (BUG == 9)
		{
			Thread worker = new Thread(new Worker());
			worker.start();
		}
	}
	
	protected Map<IOFSwitch, Map<MacVlanPair, Short>> macVlanToSwitchPortMap = new HashMap<IOFSwitch, Map<MacVlanPair, Short>>();

	protected void addToPortMap(IOFSwitch sw, long mac, short vlan,
			short portVal)
	{
		Map<MacVlanPair, Short> swMap = macVlanToSwitchPortMap.get(sw);

		if (vlan == (short) 0xffff)
		{
			vlan = 0;
		}

		if (swMap == null)
		{
			swMap = new HashMap<MacVlanPair, Short>(10);
			macVlanToSwitchPortMap.put(sw, swMap);
		}
		swMap.put(new MacVlanPair(mac, vlan), portVal);
	}
	
    public Short getFromPortMap(IOFSwitch sw, long mac, short vlan) {
        if (vlan == (short) 0xffff) {
            vlan = 0;
        }
        Map<MacVlanPair,Short> swMap = macVlanToSwitchPortMap.get(sw);
        if (swMap != null)
            return swMap.get(new MacVlanPair(mac, vlan));

        // if none found
        return null;
    }

	private Command processPacketInMessage(IOFSwitch sw, OFPacketIn pi,
			FloodlightContext cntx)
	{
		// Read in packet data headers by using OFMatch
		OFMatch match = new OFMatch();
		match.loadFromPacket(pi.getPacketData(), pi.getInPort());
		Long sourceMac = Ethernet.toLong(match.getDataLayerSource());
		Long destMac = Ethernet.toLong(match.getDataLayerDestination());
		Short vlan = match.getDataLayerVirtualLan();
		if ((destMac & 0xfffffffffff0L) == 0x0180c2000000L)
		{
			return Command.STOP;
		}
		if ((sourceMac & 0x010000000000L) == 0)
		{
			// If source MAC is a unicast address, learn the port for this
			// MAC/VLAN
			this.addToPortMap(sw, sourceMac, vlan, pi.getInPort());
		}

		// Now output flow-mod and/or packet
		Short outPort = getFromPortMap(sw, destMac, vlan);
		if (outPort == null)
		{
			// If we haven't learned the port for the dest MAC/VLAN, flood it
			// Don't flood broadcast packets if the broadcast is disabled.
			// XXX For LearningSwitch this doesn't do much. The sourceMac is
			// removed
			// from port map whenever a flow expires, so you would still see
			// a lot of floods.
			this.writePacketOutForPacketIn(sw, pi, OFPort.OFPP_FLOOD.getValue());
		}
		else if (outPort != match.getInputPort())
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
			
			// BUG 7, 8: No write actions anymore
			/* match.setWildcards(((Integer) sw
					.getAttribute(IOFSwitch.PROP_FASTWILDCARDS)).intValue()
					& ~OFMatch.OFPFW_IN_PORT
					& ~OFMatch.OFPFW_DL_VLAN
					& ~OFMatch.OFPFW_DL_SRC
					& ~OFMatch.OFPFW_DL_DST
					& ~OFMatch.OFPFW_NW_SRC_MASK & ~OFMatch.OFPFW_NW_DST_MASK);
			// We write FlowMods with Buffer ID none then explicitly PacketOut
			// the buffered packet
			this.pushPacket(sw, match, pi, outPort);
			this.writeFlowMod(sw, OFFlowMod.OFPFC_ADD,
					OFPacketOut.BUFFER_ID_NONE, match, outPort);
			if (LEARNING_SWITCH_REVERSE_FLOW)
			{
				this.writeFlowMod(sw, OFFlowMod.OFPFC_ADD, -1, match.clone()
						.setDataLayerSource(match.getDataLayerDestination())
						.setDataLayerDestination(match.getDataLayerSource())
						.setNetworkSource(match.getNetworkDestination())
						.setNetworkDestination(match.getNetworkSource())
						.setTransportSource(match.getTransportDestination())
						.setTransportDestination(match.getTransportSource())
						.setInputPort(outPort), match.getInputPort());
			} */
		}
		return Command.CONTINUE;
	}

	private void writePacketOutForPacketIn(IOFSwitch sw, OFPacketIn pi,
			short value)
	{
		// TODO Auto-generated method stub
	}

	@Override
	public Command receive(IOFSwitch sw, OFMessage msg, FloodlightContext cntx)
	{
		switch (msg.getType())
		{
		case PACKET_IN:
			return this.processPacketInMessage(sw, (OFPacketIn) msg, cntx);
		// case FLOW_REMOVED:
		//	return this.processFlowRemovedMessage(sw, (OFFlowRemoved) msg);
		case ERROR:
			return Command.CONTINUE;
		default:
			break;
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
