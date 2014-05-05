package selfoptsys.sched;

import java.util.*;
import java.util.concurrent.*;

import commonutils.config.*;

public interface MessageTaskSchedule {

	SchedulingPolicy getSchedulingPolicy();
	
	List<BlockingQueue<Integer>> getWaitQ( 
			TaskType taskType,
			boolean masterUser,
			ProcessingArchitectureType architecture
			);
	
	List<BlockingQueue<Integer>> getSignalDoneQ( 
			TaskType taskType,
			boolean masterUser,
			ProcessingArchitectureType architecture
			);
	
}
