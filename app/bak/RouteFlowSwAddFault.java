package app.bak;

import app.RouteFlow;

public class RouteFlowSwAddFault extends RouteFlow
{
	@Override
	public void switchAdded(long switchId)
	{
		final String errMsg = "Panicking on receiving a switch_add notification!";
		throw new Error(errMsg);
	}
}
