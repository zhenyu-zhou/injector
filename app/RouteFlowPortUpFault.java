package app;

import floodlight_core.IOFSwitch;
import floodlight_core.ImmutablePort;
import floodlight_core.Link;

public class RouteFlowPortUpFault extends RouteFlow
{
	@Override
	public void switchPortChanged(long switchId, ImmutablePort port,
			IOFSwitch.PortChangeType type)
	{
		switch (type)
		{
		case UP:
			if (port != null)
			{
				Link l = new Link(port.srcId, port.srcPort, port.dstId,
						port.dstPort);
				System.out.println("Insert link: " + l);
				// Shouldn't take a link up twice
				if (links.containsKey(l))
				{
					final String errMsg = "Panicking on receiving a PORT_UP notification!";
					System.err.println(errMsg);
					// System.exit(1);
					// Redundant exception, just for testing.
					throw new Error(errMsg);
				}
			}
		default:
			break;
		}
		super.switchPortChanged(switchId, port, type);
	}
}
