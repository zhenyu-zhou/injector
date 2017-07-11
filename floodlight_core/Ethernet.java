package floodlight_core;

public class Ethernet
{
	protected MACAddress sourceMACAddress;

	public static Long toLong(byte[] address)
	{
		long mac = 0;
        for (int i = 0; i < 6; i++) {
            long t = (address[i] & 0xffL) << ((5 - i) * 8);
            mac |= t;
        }
        return mac;
	}
	
	public byte[] getSourceMACAddress() {
        return sourceMACAddress.toBytes();
    }

}
