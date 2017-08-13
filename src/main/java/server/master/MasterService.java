package server.master;

import java.io.IOException;
import java.rmi.Remote;

import utility.Task;

/**
 * MasterService - The The master service will receive the task and assign the
 * work to slaves
 * 
 * @author yu
 *
 */
public interface MasterService extends Remote {
	/**
	 * Submit the task to the master server
	 * 
	 * @param task
	 *            the task
	 * @return
	 * @throws IOException
	 */
	Task submitTask(Task task) throws IOException;
}
