package floodlight_core;

public class HexString
{
	public static byte[] fromHexString(String values) throws NumberFormatException {
        String[] octets = values.split(":");
        byte[] ret = new byte[octets.length];
        
        for(int i = 0; i < octets.length; i++) {
            if (octets[i].length() > 2)
                throw new NumberFormatException("Invalid octet length");
            ret[i] = Integer.valueOf(octets[i], 16).byteValue();
        }
        return ret;
    }
	
    public static String toHexString(long val, int padTo) {
        char arr[] = Long.toHexString(val).toCharArray();
        String ret = "";
        // prepend the right number of leading zeros
        int i = 0;
        for (; i < (padTo * 2 - arr.length); i++) {
            ret += "0";
            if ((i % 2) != 0)
                ret += ":";
        }
        for (int j = 0; j < arr.length; j++) {
            ret += arr[j];
            if ((((i + j) % 2) != 0) && (j < (arr.length - 1)))
                ret += ":";
        }
        return ret;        
    }
   
    public static String toHexString(long val) {
        return toHexString(val, 8);
    }
}
