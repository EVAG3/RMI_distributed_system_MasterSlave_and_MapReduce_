package server.slave;

import java.io.IOException;
import java.rmi.Remote;

import utility.Task;

/**
 * The interface for slave service
 * 
 * @author yu
 *
 */
public interface SlaveService extends Remote {
	/**
	 * Execute a request
	 * 
	 * @param request
	 * @return
	 * @throws IOException
	 */
	Task execute(Task Task) throws IOException;

}
