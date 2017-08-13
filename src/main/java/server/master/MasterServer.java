package server.master;

import java.io.File;
import java.io.IOException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;

import server.ServerInfo;
import server.slave.SlaveService;
import utility.Task;


/**
 * MasterServer - The master server will assign the task to the
 * {@link SlaveServer}}
 * 
 * @author yu
 *
 */
public class MasterServer implements MasterService {
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

	/** the slaves */
	private final List<SlaveService> slaveServices;

	/**
	 * Construct a master server
	 * 
	 * @param serviceName
	 *            the name
	 * @param hostName
	 *            the host name, ip
	 * @param port
	 *            the port number
	 * @param rootPath
	 *            the root path
	 * @throws RemoteException
	 *             if not found
	 * @throws NotBoundException
	 *             if not found
	 */
	public MasterServer(String serviceName, String hostName, int port, String rootPath)
			throws RemoteException, NotBoundException {
		this.serviceName = serviceName;
		this.hostName = hostName;
		this.port = port;
		this.rootDirectory = new File(rootPath);

		// if the directory is not there, create one
		if (!rootDirectory.exists()) {
			rootDirectory.mkdir();
		}

		// slaves
		this.slaveServices = new ArrayList<SlaveService>();
		// hard coded here
		loadSlaveServers(new ServerInfo("Slave1", "127.0.0.1", 19092));
		loadSlaveServers(new ServerInfo("Slave2", "127.0.0.1", 19093));
	}

	/**
	 * Helper function to load the slave servers
	 * 
	 * @param slaveServerInfo
	 * @throws RemoteException
	 * @throws NotBoundException
	 */
	private void loadSlaveServers(ServerInfo slaveServerInfo) throws RemoteException, NotBoundException {
		Registry registry = LocateRegistry.getRegistry(slaveServerInfo.getHostName(), slaveServerInfo.getPort());
		SlaveService slaveService = (SlaveService) registry.lookup(slaveServerInfo.getServiceName());
		slaveServices.add(slaveService);
	}

	@Override
	public Task submitTask(Task task) throws IOException {
		if (task == null || task.getSize() == 0) {
			throw new IllegalArgumentException("[ERROR] Empty task is send to master server.");
		}
		StringBuilder sb = new StringBuilder();
		sb.append("[INFO ] Receive a remote task. \n").append("Task Name: ").append(task.getTaskName())
				.append(" Sub Tasks Size: ").append(task.getSize()).append(" .Receive Time: ")
				.append(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date())).append("\n");
		System.out.println(sb.toString());

		// map and reduce
		List<Task> results = map(task);
		return reduce(results);

	}

	/**
	 * Helper function to split a main task into many smaller tasks
	 * 
	 * @param mainTask
	 *            the main task
	 * @return
	 */
	private List<Task> splitTask(Task mainTask) {
		List<Task> splitTasks = new ArrayList<Task>();
		int total = mainTask.getSize();
		int slaveNum = slaveServices.size();
		int length = total / slaveNum + 1;
		int count = 0;
		int index = 0;

		// build the task list
		Task.Builder builder = new Task.Builder();
		builder.setTaskName(mainTask.getTaskName() + Integer.toString(index));
		for (Map.Entry<String, String> e : mainTask.getSubTasks().entrySet()) {
			builder.addSubTask(e.getKey());
			count++;
			if (count == length) {
				splitTasks.add(builder.build());
				// reset
				count = 0;
				index++;
				builder.setTaskName(mainTask.getTaskName() + Integer.toString(index));
			}
		}
		splitTasks.add(builder.build());
		System.out.println("[INFO] Split the task into " + splitTasks.size() + " smaller tasks.");
		return splitTasks;
	}

	/**
	 * Map function to map the task to different slaves
	 * 
	 * @param mainTask
	 * @return
	 */
	private List<Task> map(Task mainTask) {
		List<Task> splitTasks = splitTask(mainTask);

		List<Callable<Task>> splitTasksCallables = new ArrayList<Callable<Task>>();

		int index = 0;
		for (Task splitTask : splitTasks) {
			SlaveService slaveService = slaveServices.get(index);
			splitTasksCallables.add(new Callable<Task>() {
				@Override
				public Task call() throws Exception {
					return slaveService.execute(splitTask);
				}
			});
			index++;
		}

		try {
			// use future to get the result
			List<Future<Task>> splitTasksResults = executor.invokeAll(splitTasksCallables);
			List<Task> mapResult = new ArrayList<Task>();
			for (Future<Task> splitTaskResult : splitTasksResults) {
				mapResult.add(splitTaskResult.get());
			}
			return mapResult;
		} catch (InterruptedException | ExecutionException e) {
			throw new RuntimeException(e);
		}

	}

	/**
	 * Reduce function to collect results from the slaves
	 * 
	 * @param tasks
	 * @return
	 */
	private Task reduce(List<Task> splitTasks) {
		Task.Builder builder = new Task.Builder();
		builder.setTaskName("[Merged]" + splitTasks.get(0).getTaskName());
		for (Task splitTask : splitTasks) {
			for (Map.Entry<String, String> e : splitTask.getSubTasks().entrySet()) {
				builder.addSubTaskResult(e.getKey(), e.getValue());
			}
		}
		return builder.build();
	}

	/**
	 * The main entry to run the master server.
	 * 
	 * Command Line arguments: <Service name> <host name> <port number>
	 * 
	 * @param args
	 *            the input arguments
	 * @throws IOException
	 * @throws AlreadyBoundException
	 *             if the created worker server is already bounded
	 */
	public static void main(String[] args) throws IOException, NotBoundException, AlreadyBoundException {
		if (args.length != ARGS_LENGTH) {
			throw new IllegalArgumentException("[ERROR] Not enough input argument to set up a master server");
		}
		String serviceName = args[0];
		String hostName = args[1];
		int port = Integer.parseInt(args[2]);
		String rootPath = serviceName + "_WorkingDirectory";

		// set property
		System.setProperty("java.rmi.server.hostname", hostName);

		// create master
		MasterServer masterServer = new MasterServer(serviceName, hostName, port, rootPath);

		Registry registry = LocateRegistry.createRegistry(port);
		registry.bind(serviceName, UnicastRemoteObject.exportObject(masterServer, port));
		if (masterServer.slaveServices.size() == 0) {
			throw new IllegalArgumentException("[ERROR] No valid worker service found. Please check the setting.");
		}
		StringBuilder sb = new StringBuilder();
		sb.append("[INFO ] Master server, ").append(masterServer.serviceName).append(", ").append(masterServer.hostName)
				.append(", ").append(masterServer.port).append(", with ").append(masterServer.slaveServices.size())
				.append(" slave servers, start running.");
		System.out.println(sb.toString());
	}

}
