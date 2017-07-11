package floodlight_core;

import java.util.Arrays;

public class MACAddress
{
	public static final int MAC_ADDRESS_LENGTH = 6;
    private byte[] address = new byte[MAC_ADDRESS_LENGTH];
	
	public long toLong() {
        long mac = 0;
        for (int i = 0; i < 6; i++) {
            long t = (address[i] & 0xffL) << ((5 - i) * 8);
            mac |= t;
        }
        return mac;
    }
	
	public byte[] toBytes() {
        return Arrays.copyOf(address, address.length);
    }
}
