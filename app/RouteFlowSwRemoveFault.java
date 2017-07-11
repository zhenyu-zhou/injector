package app;

public class RouteFlowSwRemoveFault extends RouteFlow
{
	@Override
	public void switchRemoved(long switchId)
	{
		final String errMsg = "Panicking on receiving a switch_remove notification!";
		throw new Error(errMsg);
	}
}
