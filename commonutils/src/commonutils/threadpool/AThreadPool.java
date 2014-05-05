/*
 * This class was taken from "Thread Pooling - Chapter 13" 
 * available online 
 */

package commonutils.threadpool;

import java.util.concurrent.*;

import commonutils.basic.*;
import commonutils.scheduling.*;

public abstract class AThreadPool 
	implements ThreadPool {

	protected BlockingQueue<ThreadPoolWorker> m_idleWorkers;
	protected ThreadPoolWorker[] m_workerList;
	
	public AThreadPool(
			int numberOfThreads,
			int[] coreToUseForEachThread
			) {
		numberOfThreads = Math.max( 1, numberOfThreads );
		
		m_idleWorkers = new LinkedBlockingQueue<ThreadPoolWorker>();
		
		m_workerList = new AThreadPoolWorker[numberOfThreads];
		for ( int i = 0; i < numberOfThreads; i++ ) {
			m_workerList[i] = new AThreadPoolWorker(
			        m_idleWorkers,
			        coreToUseForEachThread[ i ]
			        );
		}
	}
	
    public void execute(
            Runnable r,
            int coreToRunOn,
            WindowsThreadPriority priority
            ) {
        try {
            ThreadPoolWorker worker = m_idleWorkers.take();
            worker.process(
                    r,
                    coreToRunOn,
                    priority
                    );
        }
        catch( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "AThreadPool: Error in execute",
                    e
                    );
        }
    }

    public void execute(
			Runnable r
			) {
        execute(
                r,
                0,
                WindowsThreadPriority.NORMAL
                );
	}
	
	public void stopIdleWorkers() {
		BlockingQueue<ThreadPoolWorker> idleWorkers = new LinkedBlockingQueue<ThreadPoolWorker>();
		m_idleWorkers.drainTo(idleWorkers);
		
		for (ThreadPoolWorker threadPoolWorker : idleWorkers) {
			threadPoolWorker.stopWorker();
		}
	}
	
	public void stopAllWorkers() {
		stopIdleWorkers();
		
		try {
			Thread.sleep( 250 );
		}
		catch ( InterruptedException e ) { 
		}
	
		for ( int i = 0; i < m_workerList.length; i++ ) {
			if ( m_workerList[i].isAlive() ) {
				m_workerList[i].stopWorker();
			}
		}
	}
	
}
