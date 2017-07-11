package app;

public class RouteFlowSwChangeFault extends RouteFlow
{
	@Override
	public void switchChanged(long switchId)
	{
		final String errMsg = "Panicking on receiving a switch_change notification!";
		throw new Error(errMsg);
	}
}
