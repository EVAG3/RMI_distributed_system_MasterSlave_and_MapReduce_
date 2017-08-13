package server.client;

import java.io.*;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.util.Map;

import server.ServerInfo;
import server.master.MasterService;
import utility.Task;

/**
 * ClientServer - The client will send the task to master
 * 
 * @author yu
 *
 */
public class ClientServer implements ClientService {
	/** the max number of tasks */
	private final static int MAX_SUBTASKS = 100;

	/** the master info */
	private final static ServerInfo MASTERINFO = new ServerInfo("Master", "127.0.0.1", 19091);

	/**
	 * The main entry to build and submit the task to the master
	 * 
	 * @param args
	 *            the arguments
	 * @throws IOException
	 *             if not found
	 * @throws NotBoundException
	 *             if not found
	 */
	public static void main(String[] args) throws IOException, NotBoundException {
		// create a service
		ClientService clientService = new ClientServer();
		Task taskRequest = clientService.buildTask();

		// submit the task
		System.out.println("[INFO ] Client begin to submit the task to the master.");
		clientService.sendToMaster(taskRequest);

	}

	@Override
	public Task buildTask() {
		// build the task list, here the example is the sume of sin(x)
		Task.Builder builder = new Task.Builder();
		builder.setTaskName("Simulate a simple task");
		for (int i = 0; i < MAX_SUBTASKS; i++) {
			builder.addSubTask("task" + Integer.toString(i));
		}
		return builder.build();
	}

	@Override
	public Task sendToMaster(Task taskRequest) throws NotBoundException, IOException {
		MasterService masterService = (MasterService) LocateRegistry
				.getRegistry(MASTERINFO.getHostName(), MASTERINFO.getPort()).lookup(MASTERINFO.getServiceName());
		Task task = masterService.submitTask(taskRequest);
		System.out.println(
				"[INFO ] All the " + task.getSize() + " sub tasks are finished. The results are listed as following:");
		for (Map.Entry<String, String> e : task.getSubTasks().entrySet()) {
			System.out.println("Request : " + e.getKey() + ", Result: " + e.getValue());
		}
		return task;
	}
}
