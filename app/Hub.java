package app;

import java.util.Collections;
import java.util.Random;

import floodlight_core.Command;
import floodlight_core.FloodlightContext;
import floodlight_core.IOFMessageListener;
import floodlight_core.IOFSwitch;
import floodlight_core.OFAction;
import floodlight_core.OFActionOutput;
import floodlight_core.OFMessage;
import floodlight_core.OFPacketIn;
import floodlight_core.OFPacketOut;
import floodlight_core.OFPort;
import util.U16;

public class Hub implements IOFMessageListener
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
	
	public void unsetHas_port1()
	{
		has_port1 = false;
	}

	public int getCount_port2()
	{
		return count_port2;
	}

	public Hub(int bug)
	{
		BUG = bug;
		if (BUG == 9)
		{
			Thread worker = new Thread(new Worker());
			worker.start();
		}
	}

	private void print_states()
	{
		System.out.println("---------------------------");
		System.out.println("has_port1: " + has_port1);
		System.out.println("count_port2: " + count_port2);
	}

	public Command receive(IOFSwitch sw, OFMessage msg, FloodlightContext cntx)
	{
		OFPacketIn pi = (OFPacketIn) msg;
		// OFPacketOut po = (OFPacketOut)
		// floodlightProvider.getOFMessageFactory()
		// .getMessage(OFType.PACKET_OUT);
		OFPacketOut po = new OFPacketOut();
		po.setBufferId(pi.getBufferId()).setInPort(pi.getInPort());

		if (BUG != 7)
		{
			// set actions
			OFActionOutput action = new OFActionOutput()
					.setPort(OFPort.OFPP_FLOOD.getValue());
			po.setActions(Collections.singletonList((OFAction) action));
			po.setActionsLength((short) OFActionOutput.MINIMUM_LENGTH);
		}

		// set data if is is included in the packetin
		if (pi.getBufferId() == OFPacketOut.BUFFER_ID_NONE)
		{
			byte[] packetData = pi.getPacketData();
			po.setLength(U16.t(OFPacketOut.MINIMUM_LENGTH
					+ po.getActionsLength() + packetData.length));
			po.setPacketData(packetData);
		}
		else
		{
			po.setLength(U16.t(OFPacketOut.MINIMUM_LENGTH
					+ po.getActionsLength()));
		}

		// BUG8: Don't have data plane to push the packet out
		/* try {
		    sw.write(po, cntx);
		} catch (IOException e) {
		    log.error("Failure writing PacketOut", e);
		} */

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
		else if (BUG == -1)
		{
			if (pi.getInPort() == 1)
			{
				has_port1 = true;
			}
			else if (pi.getInPort() == 2)
			{
				count_port2++;
			}
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
