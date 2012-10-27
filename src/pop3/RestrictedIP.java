package pop3;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class RestrictedIP {

	public static boolean isBanned(InetAddress inetAddress) throws UnknownHostException {
		System.out.println(inetAddress);
		if ( inetAddress.equals(InetAddress.getByName("127.0.0.1")) ) {
			return false;
		}
		return false;
	}

}
