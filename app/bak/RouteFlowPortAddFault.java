package app.bak;

import app.RouteFlow;
import floodlight_core.IOFSwitch;
import floodlight_core.ImmutablePort;

public class RouteFlowPortAddFault extends RouteFlow
{
    @Override
    public void switchPortChanged(long switchId, ImmutablePort port, IOFSwitch.PortChangeType type) {
        switch (type) {
            case ADD:
                final String errMsg = "Panicking on receiving a PORT_ADD notification!";
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
