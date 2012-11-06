package pop3;

import java.net.InetAddress;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.net.util.SubnetUtils;

public class RestrictedIPs {
	private static Set<InetAddress> ipsRestricted = new HashSet<InetAddress>();
	private static Set<SubnetUtils> subNetsRestricted = new HashSet<SubnetUtils>();

	public static boolean isBanned(InetAddress ip) {
		if (ipsRestricted.contains(ip)) {
			return true;
		}
		for (SubnetUtils subNet : subNetsRestricted) {
			if (subNet.getInfo().isInRange(ip.getHostAddress())) {
				return true;
			}
		}
		return false;
	}

	public static boolean banIPByAddress(String ip) {
		try {
			ipsRestricted.add(InetAddress.getByName(ip));
			return true;
		} catch (Exception e) {
			// TODO loguear que no se pudo bannear ...
		}
		return false;
	}

	public static boolean banIPByName(String name) {
		return banIPByAddress(name);
	}

	public static boolean banIPBySubnet(String cidrNotation) {
		try {
			subNetsRestricted.add(new SubnetUtils(cidrNotation));
			return true;
		} catch (Exception e) {

			// TODO loguear que no se pudo bannear ...
		}
		return false;
	}

	public static boolean banIPBySubnet(String address, String mask) {
		try {
			subNetsRestricted.add(new SubnetUtils(address, mask));
			return true;
		} catch (Exception e) {
			// TODO loguear que no se pudo bannear ...
		}
		return false;
	}
}
