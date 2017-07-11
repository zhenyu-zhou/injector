package app;

public class RouteFlowSwActivateFault extends RouteFlow
{
	@Override
	public void switchActivated(long switchId)
	{
		final String errMsg = "Panicking on receiving a switch_activate notification!";
		throw new Error(errMsg);
	}
}
