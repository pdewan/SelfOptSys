package commonutil.threads;

import java.util.*;
import java.util.concurrent.*;

public interface ThreadPool {

	void execute( 
			Runnable r,
            List<BlockingQueue<Integer>> waitForQList,
            List<BlockingQueue<Integer>> signalDoneQList
			);
	
	void execute(
			Runnable r
			);
	
	void stopIdleWorkers();
	
	void stopAllWorkers();
	
}
