package commonutils.threadpool;

import commonutils.scheduling.*;

public interface ThreadPoolWorker {

	void process(
			Runnable target
			);
	
	void process( 
            Runnable r,
            int coreToUse,
            WindowsThreadPriority windowsPriority
            );
	
	void stopWorker();
	
	boolean isAlive();
	
}
