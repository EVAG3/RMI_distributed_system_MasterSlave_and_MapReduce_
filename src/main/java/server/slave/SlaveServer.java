package server.slave;

import utility.Task;

import java.io.*;
import java.rmi.AlreadyBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Slave - Execute the work assigned to this sever from the
 * {@link MasterServer}}
 */
public class SlaveServer implements SlaveService {
	/**
	 * The number of threads to work. Restrict to a small number as my I use a
	 * virtual machine with only 1 GB memory
	 */
	private static final int NUM_THREAD = 50;

	/** the length of the required arguments */
	private static final int ARGS_LENGTH = 3;

	/** service name */
	private final String serviceName;

	/** host name */
	private final String hostName;

	/** port */
	private final int port;

	/** root directory */
	private final File rootDirectory;

	/** the executor */
	private ExecutorService executor = Executors.newFixedThreadPool(NUM_THREAD);

	/** construct a slave server */
	public SlaveServer(String serviceName, String hostName, int port, String rootPath) throws IOException {
		this.serviceName = serviceName;
		this.hostName = hostName;
		this.port = port;
		this.rootDirectory = new File(rootPath);

		// if the directory is not there, create one
		if (!rootDirectory.exists()) {
			rootDirectory.mkdir();
		}
	}

	@Override
	public Task execute(Task task) throws IOException {

		if (task == null || task.getSize() == 0) {
			throw new IllegalArgumentException("[ERROR] Receive empty sub task list");
		} else {
			System.out.println(
					"[INFO ] Slave server(" + this.serviceName + ") recognizes " + task.getSize() + " subTasks");
		}

		Map<String, String> subTasks = task.getSubTasks();
		List<Callable<String>> subTasksCallables = new ArrayList<Callable<String>>();

		for (String request : subTasks.keySet()) {
			subTasksCallables.add(new Callable<String>() {
				@Override
				public String call() throws Exception {
					return concreteFunction(request);
				}
			});
		}

		try {
			List<Future<String>> subTasksResults = executor.invokeAll(subTasksCallables);
			int i = 0;
			for (Map.Entry<String, String> e : subTasks.entrySet()) {
				Future<String> future = subTasksResults.get(i);
				subTasks.put(e.getKey(), future.get());
				i++;
			}
			return task;
		} catch (InterruptedException | ExecutionException e) {
			throw new RuntimeException(e);
		}

	}
	
	/**
	 * The function to execute. Replace based on the situation
	 * 
	 * @param request the input
	 * @return the result
	 */
	private String concreteFunction(String request) {
		System.out.println("[INFO] " + this.serviceName + " is processing the request: " + request);
		return "Result(Assume we have calculated the result)";
	}

	/**
	 * The main entry to run the slave server.
	 * 
	 * Command Line arguments: <Service name> <host name> <port number>
	 * 
	 * @param args
	 *            the input arguments
	 * @throws IOException
	 * @throws AlreadyBoundException
	 *             if the created worker server is already bounded
	 */
	public static void main(String[] args) throws IOException, AlreadyBoundException {
		if (args.length != ARGS_LENGTH) {
			throw new IllegalArgumentException("[ERROR] Constructor for SlaveServer requires 4 input arguments.");
		}

		// parse the args
		String serviceName = args[0];
		String hostName = args[1];
		int port = Integer.parseInt(args[2]);
		String rootPath = serviceName + "_WorkingDirectory";

		// set property
		System.setProperty("java.rmi.server.hostname", hostName);

		// create worker server
		SlaveServer slaveServer = new SlaveServer(serviceName, hostName, port, rootPath);

		// registry
		Registry registry = LocateRegistry.createRegistry(port);
		registry.bind(serviceName, UnicastRemoteObject.exportObject(slaveServer, port));
		StringBuilder sb = new StringBuilder();
		sb.append("[INFO ] Slave server, ").append(slaveServer.serviceName).append(", ").append(slaveServer.hostName)
				.append(", ").append(slaveServer.port).append(", start running.");
		System.out.println(sb.toString());
	}

}
