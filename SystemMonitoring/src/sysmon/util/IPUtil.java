package sysmon.util;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * Retrieve the available IP address of local machine.
 * @author Yexi Jiang (http://users.cs.fiu.edu/~yjian004)
 *
 */
public class IPUtil {

	/**
	 * Retrieve all the available IP address, and only returns the first one
	 * @return
	 */
	public static String getFirstAvailableIP() {
		try {
			List<String> ipAddresses = retrieveIPAddresses();
			return ipAddresses.get(0);
		} catch (SocketException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Retrieve all the IPv4 non-loop-back addresses.
	 * 
	 * @return
	 * @throws SocketException
	 */
	public static List<String> retrieveIPAddresses() throws SocketException {
		List<String> ipAddressList = new ArrayList<String>();
		Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
		while (interfaces.hasMoreElements()) {
			NetworkInterface currentInterface = interfaces.nextElement();
			if (!currentInterface.isUp() || currentInterface.isLoopback() || currentInterface.isVirtual())
				continue;
			
			String interfaceName = currentInterface.toString().split(":")[1];
			if(false == (interfaceName.startsWith("eth") || interfaceName.startsWith("wlan")))
				continue;
			Enumeration<InetAddress> addresses = currentInterface.getInetAddresses();
			while (addresses.hasMoreElements()) {
				InetAddress current_addr = addresses.nextElement();
				if (current_addr.isLoopbackAddress() || !(current_addr instanceof Inet4Address))
					continue;
				ipAddressList.add(current_addr.getHostAddress());
			}
		}
		return ipAddressList;
	}
}
