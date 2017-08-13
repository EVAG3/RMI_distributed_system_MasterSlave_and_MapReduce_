package server.client;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.Remote;

import utility.Task;

/**
 * ClientService - The client will build the task and send to master
 * 
 * @author yu
 *
 */
public interface ClientService extends Remote {

	/**
	 * Build the task
	 * 
	 * @return a complete task
	 */
	Task buildTask();

	/**
	 * Send the task to master
	 * 
	 * @param taskRequest
	 *            the task request
	 * @return the task with the result
	 * @throws NotBoundException
	 *             if not found
	 * @throws IOException
	 */
	Task sendToMaster(Task taskRequest) throws NotBoundException, IOException;
}
