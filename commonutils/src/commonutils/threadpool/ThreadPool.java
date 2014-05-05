package commonutils.threadpool;

import commonutils.scheduling.*;

public interface ThreadPool {

	void execute(
			Runnable r
			);
	
	void execute(
            Runnable r,
            int coreToRunOn,
            WindowsThreadPriority priority
            );
	
	void stopIdleWorkers();
	
	void stopAllWorkers();
	
}
