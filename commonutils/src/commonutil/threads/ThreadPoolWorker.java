package commonutil.threads;

import java.util.concurrent.*;

public interface ThreadPoolWorker {

	void process(
			Runnable target
			);
	
	void stopWorker();
	
	boolean isAlive();
	
    void setWaitForQueue(
            BlockingQueue<Integer> waitForQ
            );

    void setFinishedSignalQueue(
			BlockingQueue<Integer> finishedSignalQ
			);
    
    int getWorkerCoreAffinity();
}
