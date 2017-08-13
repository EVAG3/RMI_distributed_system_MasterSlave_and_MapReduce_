package utility;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Task - Server as the data structure to store the task information
 * 
 * @author yu
 *
 */
public class Task implements Serializable {
	/** version UID by default */
	private static final long serialVersionUID = 1L;

	/** the name of the task to identify */
	private final String taskName;

	/**
	 * the request(input) of the task, use the string as the serializable input
	 */
	private final Map<String, String> subTasks;

	/**
	 * Construct a task with the given task name and sub tasks.
	 * 
	 * @param taskName
	 *            the task name
	 * @param subTasks
	 *            the sub tasks
	 */
	public Task(String taskName, Map<String, String> subTasks) {
		if (taskName == null || taskName.length() == 0) {
			throw new IllegalArgumentException("[ERROR] Construct Task failed as the task name is null or empty.");
		}
		this.taskName = taskName;
		this.subTasks = new LinkedHashMap<String, String>(subTasks);
	}

	/**
	 * Get task name
	 * 
	 * @return taskName
	 */
	public String getTaskName() {
		return this.taskName;
	}

	/**
	 * Get sub tasks
	 * 
	 * @return
	 */
	public Map<String, String> getSubTasks() {
		return this.subTasks;
	}

	/**
	 * Get the size of the subTasks(result)
	 * 
	 * @return
	 */
	public int getSize() {
		return this.subTasks.size();
	}

	/**
	 * Builder - Build the task
	 * 
	 * @author yu
	 *
	 */
	public static class Builder {
		/** the name of the task to identify */
		private String builderTaskName;

		/**
		 * the request(input) of the task, use the string as the serializable
		 * input
		 */
		private Map<String, String> builderSubTasks = new LinkedHashMap<String, String>();

		/**
		 * Set the name.
		 * 
		 * @param taskName
		 *            the task name
		 * @return this
		 */
		public Builder setTaskName(String taskName) {
			if (taskName == null || taskName.length() == 0) {
				throw new IllegalArgumentException(
						"[ERROR] Task name is null or empty. Please give the task name properly.");
			}
			this.builderTaskName = taskName;
			return this;
		}

		/**
		 * Add a sub task to the builder
		 * 
		 * @param request
		 *            the sub task request
		 * @return this
		 */
		public Builder addSubTask(String request) {
			addSubTaskResult(request, null);
			return this;
		}

		/**
		 * Add a sub task and its result to the builder
		 * 
		 * @param request
		 *            the sub task request
		 * @param result
		 *            the sub task result
		 * @return this
		 */
		public Builder addSubTaskResult(String request, String result) {
			if (this.builderTaskName == null || this.builderTaskName.length() == 0) {
				throw new IllegalArgumentException(
						"[ERROR] Task name is null or empty. Please firstly set the task name properly.");
			}
			if (this.builderSubTasks == null) {
				this.builderSubTasks = new LinkedHashMap<String, String>();
			}
			if (this.builderSubTasks.containsKey(request)) {
				throw new IllegalArgumentException("[ERROR] Sub task request is the same.");
			}
			this.builderSubTasks.put(request, result);
			return this;
		}

		/**
		 * Build the task
		 * 
		 * @return task
		 */
		public Task build() {
			Task task = new Task(this.builderTaskName, this.builderSubTasks);
			this.builderTaskName = null;
			this.builderSubTasks = new LinkedHashMap<String, String>();
			return task;
		}

	}

}
