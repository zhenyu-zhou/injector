package app;

import floodlight_core.*;

public class RouteFlowPortDownFault extends RouteFlow
{
    @Override
    public void switchPortChanged(long switchId, ImmutablePort port, IOFSwitch.PortChangeType type) {
        switch (type) {
            case DOWN:
                final String errMsg = "Panicking on receiving a PORT_DOWN notification!";
                System.err.println(errMsg);
                // System.exit(1);
                // Redundant exception, just for testing.
                throw new Error(errMsg);
            default:
                break;
        }
        super.switchPortChanged(switchId, port, type);
    }
}
