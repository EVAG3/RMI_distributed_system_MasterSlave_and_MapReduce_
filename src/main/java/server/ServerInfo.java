package server;

import java.io.Serializable;

/**
 * A configuration class that describes the IP and port of a server
 *
 */
public class ServerInfo implements Serializable {
	/** version UID by default */
	private static final long serialVersionUID = 1L;

	/** hash for hash code function */
	private static final int HASH = 31;

	/** service name */
	private final String serviceName;

	/** the host name */
	private final String hostName;

	/** the port */
	private final int port;

	/**
	 * Construct a server info
	 * 
	 * @param serviceName
	 *            the service name
	 * @param hostName
	 *            the host name
	 * @param port
	 *            the port
	 */
	public ServerInfo(String serviceName, String hostName, int port) {
		this.serviceName = serviceName;
		this.hostName = hostName;
		this.port = port;
	}

	/**
	 * Getter for service name
	 * 
	 * @return service name
	 */
	public String getServiceName() {
		return serviceName;
	}

	/**
	 * Getter for host name
	 * 
	 * @return host name
	 */
	public String getHostName() {
		return hostName;
	}

	/**
	 * Getter for port
	 * 
	 * @return port
	 */
	public int getPort() {
		return port;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		ServerInfo that = (ServerInfo) o;

		return port == that.port && (hostName != null ? hostName.equals(that.hostName) : that.hostName == null)
				&& (serviceName != null ? serviceName.equals(that.serviceName) : that.serviceName == null);
	}

	@Override
	public int hashCode() {
		int result = hostName != null ? hostName.hashCode() : 0;
		result = HASH * result + (serviceName != null ? serviceName.hashCode() : 0);
		result = HASH * result + port;
		return result;
	}
}
