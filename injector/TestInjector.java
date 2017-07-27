package injector;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.Assert;
import org.junit.Test;

import util.Pair;
import util.Util;
import app.*;
import floodlight_core.*;
import floodlight_core.IOFSwitch.PortChangeType;

public class TestInjector
{
	// @Test(expected = RuntimeException.class)
	public void testHubException()
	{
		Hub hub = new Hub(1);
		OFSwitchImpl sw = new OFSwitchImpl();
		FloodlightContext ctx = new FloodlightContext();

		// seq: 212221
		ArrayList<OFMessage> msgs = new ArrayList<OFMessage>();
		OFPacketIn temp = new OFPacketIn();
		temp.setInPort((short) 2);
		msgs.add(temp);

		temp = new OFPacketIn();
		temp.setInPort((short) 1);
		msgs.add(temp);

		for (int i = 0; i < 3; i++)
		{
			temp = new OFPacketIn();
			temp.setInPort((short) 2);
			msgs.add(temp);
		}

		temp = new OFPacketIn();
		temp.setInPort((short) 1);
		msgs.add(temp);

		for (OFMessage m : msgs)
		{
			hub.receive(sw, m, ctx);
		}
	}

	// @Test
	public void testHub()
	{
		Hub good_hub = new Hub(-1);
		OFSwitchImpl sw = new OFSwitchImpl();
		FloodlightContext ctx = new FloodlightContext();

		// seq: 212221
		ArrayList<OFMessage> msgs = new ArrayList<OFMessage>();
		OFPacketIn temp = new OFPacketIn();
		temp.setInPort((short) 2);
		msgs.add(temp);

		temp = new OFPacketIn();
		temp.setInPort((short) 1);
		msgs.add(temp);

		for (int i = 0; i < 3; i++)
		{
			temp = new OFPacketIn();
			temp.setInPort((short) 2);
			msgs.add(temp);
		}

		temp = new OFPacketIn();
		temp.setInPort((short) 1);
		msgs.add(temp);

		Hub hub = new Hub(1);
		// seq: 2322212 (e1 - e7)
		// transform: e2
		// converge: e6
		// dist: 5
		ArrayList<OFMessage> msgs2 = new ArrayList<OFMessage>();
		temp = new OFPacketIn();
		temp.setInPort((short) 2);
		msgs2.add(temp);

		temp = new OFPacketIn();
		temp.setInPort((short) 3);
		msgs2.add(temp);

		for (int i = 0; i < 3; i++)
		{
			temp = new OFPacketIn();
			temp.setInPort((short) 2);
			msgs2.add(temp);
		}

		temp = new OFPacketIn();
		temp.setInPort((short) 1);
		msgs2.add(temp);

		Assert.assertEquals(msgs.size(), msgs2.size());
		for (int i = 0; i < msgs.size(); i++)
		{
			System.out.println("i: " + i);
			OFMessage m = msgs.get(i);
			OFMessage m2 = msgs2.get(i);
			good_hub.receive(sw, m, ctx);
			hub.receive(sw, m2, ctx);
			if (i > 0 && i < 5)
			{
				Assert.assertFalse(good_hub.isHas_port1() == hub.isHas_port1());
			}
			else
			{
				Assert.assertTrue(good_hub.isHas_port1() == hub.isHas_port1());
			}
			Assert.assertEquals(good_hub.getCount_port2(), hub.getCount_port2());
		}
	}

	// @Test
	public void testHubRandom()
	{
		Hub good_hub = new Hub(-1);
		OFSwitchImpl sw = new OFSwitchImpl();
		FloodlightContext ctx = new FloodlightContext();
		List<OFMessage> msgs = new ArrayList<OFMessage>();

		Hub hub = new Hub(1);
		// List<OFMessage> msgs2 = new ArrayList<OFMessage>();

		int count = 0;

		List<Pair<Short, Double>> dist = new ArrayList<Pair<Short, Double>>();
		dist.add(new Pair<Short, Double>((short) 1, 0.5));
		dist.add(new Pair<Short, Double>((short) 2, 0.5));

		for (int i = 0; i < 10; i++)
		{
			OFPacketIn temp = new OFPacketIn();
			short p = Util.randomGet(dist);
			System.out.println("Random port: " + p);
			temp.setInPort(p);
			msgs.add(temp);
			// msgs2.add(temp);
		}

		for (int i = 0; i < msgs.size(); i++)
		{
			OFMessage m = msgs.get(i);
			good_hub.receive(sw, m, ctx);
			try
			{
				hub.receive(sw, m, ctx);
			} catch (Exception e)
			{
				System.err.println("Crash at pkt in from port "
						+ ((OFPacketIn) m).getInPort());
				// Transform all port 1 to 3
				hub.unsetHas_port1();
				Assert.assertFalse(good_hub.isHas_port1() == hub.isHas_port1());
				Assert.assertEquals(good_hub.getCount_port2(),
						hub.getCount_port2());
				count = 0;
			}

			System.out.println("good_hub.isHas_port1(): "
					+ good_hub.isHas_port1());
			System.out.println("hub.isHas_port1(): " + hub.isHas_port1());
			System.out.println("good_hub.getCount_port2(): "
					+ good_hub.getCount_port2());
			System.out.println("hub.getCount_port2(): " + hub.getCount_port2());
			if (good_hub.isHas_port1() != hub.isHas_port1())
			{
				count++;
			}
			else if (count > 0)
			{
				System.err.println("dist: " + count);
				count = 0;
			}
		}
	}

	// @Test(expected = Error.class)
	public void testRouteFlowException()
	{
		RouteFlowPortDownFault rf = new RouteFlowPortDownFault();
		rf.switchPortChanged(-1, null, IOFSwitch.PortChangeType.DOWN);
	}

	// l1 down -> s1 down, s1 up, l2 up
	// @Test
	public void testRouteFlowDownFault()
	{
		RouteFlow rf = new RouteFlow();
		RouteFlowPortDownFault rfp = new RouteFlowPortDownFault();

		/*         l1
		 *  s1(1) ----(1) s2
		 *   (2)       (2)
		 *     \       /
		 *   l2 \     / l3
		 *      (1) (2)
		 *        s3
		 */
		for (int i = 1; i <= 3; i++)
		{
			rf.switchAdded(i);
			rf.switchActivated(i);
			rfp.switchAdded(i);
			rfp.switchActivated(i);
		}
		Assert.assertTrue(rf.getActiveSws().equals(rfp.getActiveSws()));
		Assert.assertEquals(rf.getActiveSws().size(), 3);

		rf.addLink(1, 1, 2, 1); // l1
		rf.addLink(1, 2, 3, 1); // l2
		rf.addLink(2, 2, 3, 2); // l3
		rfp.addLink(1, 1, 2, 1);
		rfp.addLink(1, 2, 3, 1);
		rfp.addLink(2, 2, 3, 2);

		Assert.assertTrue(rf.links.keySet().equals(rfp.links.keySet()));
		Assert.assertEquals(rf.links.keySet().size(), 3);

		// event: (l1) up down up
		List<IOFSwitch.PortChangeType> events = new ArrayList<IOFSwitch.PortChangeType>();
		events.add(IOFSwitch.PortChangeType.UP);
		events.add(IOFSwitch.PortChangeType.DOWN);
		events.add(IOFSwitch.PortChangeType.UP);

		for (IOFSwitch.PortChangeType t : events)
		{
			switch (t)
			{
			case DOWN:
				rf.removeLink(1, 1, 2, 1);
				rf.switchPortChanged(-1, null, t);

				// s1 down
				rfp.switchRemoved(1);
				rfp.removeLink(1, 1, 2, 1);
				rfp.removeLink(1, 2, 3, 1);
				Assert.assertFalse(rf.links.keySet().equals(rfp.links.keySet()));
				Assert.assertEquals(rf.links.keySet().size(), 2);
				Assert.assertEquals(rf.links.keySet().size(), rfp.links
						.keySet().size() + 1);
				Assert.assertFalse(rf.getActiveSws().equals(rfp.getActiveSws()));
				Assert.assertEquals(rf.getActiveSws().size(), 3);
				Assert.assertEquals(rf.getActiveSws().size(), rfp
						.getActiveSws().size() + 1);

				// s1 up
				rfp.switchAdded(1);
				rfp.switchActivated(1);
				Assert.assertTrue(rf.getActiveSws().equals(rfp.getActiveSws()));

				// l2 up
				rfp.addLink(1, 2, 3, 1);
				Assert.assertTrue(rf.links.keySet().equals(rfp.links.keySet()));

				break;
			case UP:
				rf.addLink(1, 1, 2, 1);
				rf.switchPortChanged(-1, null, t);
				rfp.addLink(1, 1, 2, 1);
				rfp.switchPortChanged(-1, null, t);
				break;
			default:
				rf.switchPortChanged(-1, null, t);
				rfp.switchPortChanged(-1, null, t);
				break;
			}
		}
	}

	// @Test
	public void testRouteFlowDownFaultRandom()
	{
		RouteFlow rf = new RouteFlow();
		RouteFlowPortDownFault rfp = new RouteFlowPortDownFault();

		/*         l1
		 *  s1(1) ----(1) s2
		 *   (2)       (2)
		 *     \       /
		 *   l2 \     / l3
		 *      (1) (2)
		 *        s3
		 */
		for (int i = 1; i <= 3; i++)
		{
			rf.switchAdded(i);
			rf.switchActivated(i);
			rfp.switchAdded(i);
			rfp.switchActivated(i);
		}
		Assert.assertTrue(rf.getActiveSws().equals(rfp.getActiveSws()));
		Assert.assertEquals(rf.getActiveSws().size(), 3);

		rf.addLink(1, 1, 2, 1); // l1
		rf.addLink(1, 2, 3, 1); // l2
		rf.addLink(2, 2, 3, 2); // l3
		rfp.addLink(1, 1, 2, 1);
		rfp.addLink(1, 2, 3, 1);
		rfp.addLink(2, 2, 3, 2);

		Assert.assertTrue(rf.links.keySet().equals(rfp.links.keySet()));
		Assert.assertEquals(rf.links.keySet().size(), 3);

		// random events
		List<Pair<IOFSwitch.PortChangeType, Double>> dist = new ArrayList<Pair<IOFSwitch.PortChangeType, Double>>();
		for (IOFSwitch.PortChangeType t : IOFSwitch.PortChangeType.values())
		{
			dist.add(new Pair<IOFSwitch.PortChangeType, Double>(t,
					1.0 / IOFSwitch.PortChangeType.values().length));
		}

		List<IOFSwitch.PortChangeType> events = new ArrayList<IOFSwitch.PortChangeType>();
		for (int i = 0; i < 10; i++)
		{
			IOFSwitch.PortChangeType t = Util.randomGet(dist);
			System.out.println("Random type: " + t);
			events.add(t);
		}
		System.out.println("---------------");

		for (IOFSwitch.PortChangeType t : events)
		{
			switch (t)
			{
			case DOWN:
				rf.removeLink(1, 1, 2, 1);
				rf.switchPortChanged(-1, null, t);

				// l1 down -> s1 down + s1 up + l2 up
				// dist: 3
				// s1 down
				rfp.switchRemoved(1);
				rfp.removeLink(1, 1, 2, 1);
				rfp.removeLink(1, 2, 3, 1);
				Assert.assertFalse(rf.links.keySet().equals(rfp.links.keySet()));
				Assert.assertEquals(rf.links.keySet().size(), 2);
				Assert.assertEquals(rf.links.keySet().size(), rfp.links
						.keySet().size() + 1);
				Assert.assertFalse(rf.getActiveSws().equals(rfp.getActiveSws()));
				Assert.assertEquals(rf.getActiveSws().size(), 3);
				Assert.assertEquals(rf.getActiveSws().size(), rfp
						.getActiveSws().size() + 1);

				// s1 up
				rfp.switchAdded(1);
				rfp.switchActivated(1);
				Assert.assertTrue(rf.getActiveSws().equals(rfp.getActiveSws()));

				// l2 up
				rfp.addLink(1, 2, 3, 1);
				Assert.assertTrue(rf.links.keySet().equals(rfp.links.keySet()));

				break;
			case UP:
				rf.addLink(1, 1, 2, 1);
				rf.switchPortChanged(-1, null, t);
				rfp.addLink(1, 1, 2, 1);
				rfp.switchPortChanged(-1, null, t);
				break;
			default:
				rf.switchPortChanged(-1, null, t);
				rfp.switchPortChanged(-1, null, t);
				break;
			}
		}
	}

	// @Test
	public void testRouteFlowUpFaultRandom()
	{
		RouteFlow rf = new RouteFlow();
		RouteFlowPortUpFault rfp = new RouteFlowPortUpFault();

		/*         l1
		 *  s1(1) ----(1) s2
		 *   (2)       (2)
		 *     \       /
		 *   l2 \     / l3
		 *      (1) (2)
		 *        s3
		 */
		for (int i = 1; i <= 3; i++)
		{
			rf.switchAdded(i);
			rf.switchActivated(i);
			rfp.switchAdded(i);
			rfp.switchActivated(i);
		}
		Assert.assertTrue(rf.getActiveSws().equals(rfp.getActiveSws()));
		Assert.assertEquals(rf.getActiveSws().size(), 3);

		rf.addLink(1, 1, 2, 1); // l1
		rf.addLink(1, 2, 3, 1); // l2
		rf.addLink(2, 2, 3, 2); // l3
		rfp.addLink(1, 1, 2, 1);
		rfp.addLink(1, 2, 3, 1);
		rfp.addLink(2, 2, 3, 2);

		Assert.assertTrue(rf.links.keySet().equals(rfp.links.keySet()));
		Assert.assertEquals(rf.links.keySet().size(), 3);

		// random events
		List<Pair<IOFSwitch.PortChangeType, Double>> dist = new ArrayList<Pair<IOFSwitch.PortChangeType, Double>>();
		for (IOFSwitch.PortChangeType t : IOFSwitch.PortChangeType.values())
		{
			dist.add(new Pair<IOFSwitch.PortChangeType, Double>(t,
					1.0 / IOFSwitch.PortChangeType.values().length));
		}

		List<IOFSwitch.PortChangeType> events = new ArrayList<IOFSwitch.PortChangeType>();
		for (int i = 0; i < 10; i++)
		{
			IOFSwitch.PortChangeType t = Util.randomGet(dist);
			System.out.println("Random type: " + t);
			events.add(t);
		}
		System.out.println("---------------");

		for (IOFSwitch.PortChangeType t : events)
		{
			switch (t)
			{
			case DOWN:
				rf.removeLink(1, 1, 2, 1);
				rf.switchPortChanged(-1, null, t);
				rfp.removeLink(1, 1, 2, 1);
				rfp.switchPortChanged(-1, null, t);
				break;
			case UP:
				rf.addLink(1, 1, 2, 1);
				rf.switchPortChanged(-1, null, t);

				// Toggle: link up -> link down, link up
				rfp.removeLink(1, 1, 2, 1);
				rfp.switchPortChanged(-1, null, PortChangeType.DOWN);
				Assert.assertEquals(rf.links.size(), rfp.links.size() + 1);
				ImmutablePort port = new ImmutablePort(1, 1, 2, 1);
				rfp.switchPortChanged(-1, port, t);
				rfp.addLink(1, 1, 2, 1);

				Assert.assertTrue(rf.links.keySet().equals(rfp.links.keySet()));
				break;
			default:
				rf.switchPortChanged(-1, null, t);
				rfp.switchPortChanged(-1, null, t);
				break;
			}
		}
	}

	// single transform: l1 down -> s1 down (s1 up is assumed to follow)
	// @Test
	public void testRouteFlowDownFaultSingle()
	{
		RouteFlow rf = new RouteFlow();
		RouteFlowPortDownFault rfp = new RouteFlowPortDownFault();
		int count = 0;

		/*         l1
		 *  s1(1) ----(1) s2
		 *   (2)       (2)
		 *     \       /
		 *   l2 \     / l3
		 *      (1) (2)
		 *        s3
		 */
		for (int i = 1; i <= 3; i++)
		{
			rf.switchAdded(i);
			rf.switchActivated(i);
			rfp.switchAdded(i);
			rfp.switchActivated(i);
		}
		Assert.assertTrue(rf.getActiveSws().equals(rfp.getActiveSws()));
		Assert.assertEquals(rf.getActiveSws().size(), 3);

		rf.addLink(1, 1, 2, 1); // l1
		rf.addLink(1, 2, 3, 1); // l2
		rf.addLink(2, 2, 3, 2); // l3
		rfp.addLink(1, 1, 2, 1);
		rfp.addLink(1, 2, 3, 1);
		rfp.addLink(2, 2, 3, 2);

		Assert.assertTrue(rf.links.keySet().equals(rfp.links.keySet()));
		Assert.assertEquals(rf.links.keySet().size(), 3);

		// random events
		List<Pair<IOFSwitch.PortChangeType, Double>> dist = new ArrayList<Pair<IOFSwitch.PortChangeType, Double>>();
		for (IOFSwitch.PortChangeType t : IOFSwitch.PortChangeType.values())
		{
			dist.add(new Pair<IOFSwitch.PortChangeType, Double>(t,
					1.0 / IOFSwitch.PortChangeType.values().length));
		}

		List<IOFSwitch.PortChangeType> events = new ArrayList<IOFSwitch.PortChangeType>();
		for (int i = 0; i < 10; i++)
		{
			IOFSwitch.PortChangeType t = Util.randomGet(dist);
			System.out.println("Random type: " + t);
			events.add(t);
		}
		System.out.println("---------------");

		for (IOFSwitch.PortChangeType t : events)
		{
			switch (t)
			{
			case DOWN:
				rf.removeLink(1, 1, 2, 1);
				rf.switchPortChanged(-1, null, t);

				// l1 down -> s1 down + s1 up
				// dist: 3
				// s1 down
				rfp.switchRemoved(1);
				rfp.removeLink(1, 1, 2, 1);
				rfp.removeLink(1, 2, 3, 1);
				Assert.assertFalse(rf.links.keySet().equals(rfp.links.keySet()));
				Assert.assertEquals(rf.links.keySet().size(), 2);
				Assert.assertEquals(rf.links.keySet().size(), rfp.links
						.keySet().size() + 1);
				Assert.assertFalse(rf.getActiveSws().equals(rfp.getActiveSws()));
				Assert.assertEquals(rf.getActiveSws().size(), 3);
				Assert.assertEquals(rf.getActiveSws().size(), rfp
						.getActiveSws().size() + 1);

				// s1 up
				rfp.switchAdded(1);
				rfp.switchActivated(1);
				Assert.assertTrue(rf.getActiveSws().equals(rfp.getActiveSws()));

				count = 1;
				break;
			case UP:
				rf.addLink(1, 1, 2, 1);
				rf.switchPortChanged(-1, null, t);
				rfp.addLink(1, 1, 2, 1);
				rfp.switchPortChanged(-1, null, t);
				if (count > 0)
					count++;
				break;
			// borrow for l2 up
			case ADD:
				rf.addLink(1, 2, 3, 1);
				rf.switchPortChanged(-1, null, IOFSwitch.PortChangeType.UP);
				rfp.addLink(1, 2, 3, 1);
				rfp.switchPortChanged(-1, null, IOFSwitch.PortChangeType.UP);
				Assert.assertTrue(rf.links.keySet().equals(rfp.links.keySet()));
				if (count > 0)
				{
					count++;
					System.err.println("dist: " + count);
					count = 0;
				}
				break;
			default:
				rf.switchPortChanged(-1, null, t);
				rfp.switchPortChanged(-1, null, t);
				if (count > 0)
					count++;
				break;
			}
		}
	}

	// @Test
	public void testRouteFlowReorderFaultRandom()
	{
		RouteFlow rf = new RouteFlow();
		RouteFlowPortUpFault rfp = new RouteFlowPortUpFault();
		int count = 1;

		/*         l1
		 *  s1(1) ----(1) s2
		 *   (2)       (2)
		 *     \       /
		 *   l2 \     / l3
		 *      (1) (2)
		 *        s3
		 */
		for (int i = 2; i <= 3; i++)
		{
			rf.switchAdded(i);
			rf.switchActivated(i);
			rfp.switchAdded(i);
			rfp.switchActivated(i);
		}
		Assert.assertTrue(rf.getActiveSws().equals(rfp.getActiveSws()));
		Assert.assertEquals(rf.getActiveSws().size(), 2);

		// rf.addLink(1, 1, 2, 1); // l1
		// rf.addLink(1, 2, 3, 1); // l2
		rf.addLink(2, 2, 3, 2); // l3
		// rfp.addLink(1, 1, 2, 1);
		// rfp.addLink(1, 2, 3, 1);
		rfp.addLink(2, 2, 3, 2);

		Assert.assertTrue(rf.links.keySet().equals(rfp.links.keySet()));
		Assert.assertEquals(rf.links.keySet().size(), 1);

		// l1 up without s1 up
		rf.addLink(1, 1, 2, 1);
		try
		{
			rfp.addLink(1, 1, 2, 1);
		} catch (Error e)
		{
			System.out.println("Caught the exception");
			Assert.assertFalse(rf.links.keySet().equals(rfp.links.keySet()));
			Assert.assertEquals(rf.links.keySet().size(), 2);
			Assert.assertEquals(rfp.links.keySet().size(), 1);
		}

		double p = 0.5;
		Random random = new Random();
		double r = random.nextDouble();
		while (r < p)
		{
			count++;
			System.out.println("count: " + count);
			r = random.nextDouble();
		}
		// E[D] = 1/(1-p)
		System.err.println("dist: " + count);

		// s1 up
		rf.switchAdded(1);
		rf.switchActivated(1);
		rfp.switchAdded(1);
		rfp.switchActivated(1);

		// l1 up
		rfp.addLink(1, 1, 2, 1);

		Assert.assertTrue(rf.getActiveSws().equals(rfp.getActiveSws()));
		Assert.assertTrue(rf.links.keySet().equals(rfp.links.keySet()));
	}

	// @Test
	public int testModel(int n, int target)
	{
		List<Pair<Integer, Double>> dist = new ArrayList<Pair<Integer, Double>>();
		int temp = -1;
		int count = 1;
		
		System.out.println("n: " + n);
		System.out.println("target: " + target);

		for (int i = 1; i <= n; i++)
		{
			dist.add(new Pair<Integer, Double>(i, 1.0 / n));
		}
		
		temp = Util.randomGet(dist);
		// System.out.println("temp: " + temp);

		// p = target / n
		while (temp > target)
		{
			temp = Util.randomGet(dist);
			// System.out.println("temp: " + temp);
			count++;
		}

		// System.out.println("dist: " + count);
		return count;
	}

	@Test
	public void testMulModel()
	{
		int T = 5;
		int[] n = { 8, 20 };
		int[] target = { 1, 1 };
		assert (n.length == target.length);

		for (int i = 0; i < T; i++)
		{
			int dist = 0;
			for (int j = 0; j < n.length; j++)
			{
				dist += testModel(n[j], target[j]);
			}
			System.out.println("dist: " + dist);
			System.out.println("-------------");
		}
	}

}
