/*
 * This class was taken from "Thread Pooling - Chapter 13" 
 * available online 
 */

package commonutils.threadpool;

import java.util.concurrent.*;

import commonutils.scheduling.*;
import commonutils.basic.*;
import commonutils.basic2.*;

public class AThreadPoolWorker 
	implements ThreadPoolWorker {

	private BlockingQueue<ThreadPoolWorker> m_idleWorkers;
	private BlockingQueue<Runnable> m_handoffBox;
	
	private SelfOptArchThread m_internalThread;
	private volatile boolean m_stopRequested;
	
    protected int m_coreToUseForWorkerThread;
    
	public AThreadPoolWorker(
			BlockingQueue<ThreadPoolWorker> idleWorkers,
			int coreToUseForWorkerThread
			) {
		m_idleWorkers = idleWorkers;
        m_handoffBox = new ArrayBlockingQueue<Runnable>(1);

        m_coreToUseForWorkerThread = coreToUseForWorkerThread;
		
		Runnable r = new Runnable() {
			public void run() {
				try {
					runWork();
				}
				catch ( Exception e ) {
		            ErrorHandlingUtils.logSevereExceptionAndContinue(
		                    "AThreadPoolWorker: Error in runnable",
		                    e
		                    );
				}
			}
		};
		
		m_internalThread = new ASelfOptArchThread( r );
		m_internalThread.setWindowsPriority( WindowsThreadPriority.ABOVE_NORMAL );
		if ( m_coreToUseForWorkerThread != -1 ) {
			m_internalThread.setThreadCoreAffinity( m_coreToUseForWorkerThread );
		}
		( (ASelfOptArchThread) m_internalThread ).start();
        m_stopRequested = false;
        
	}
	
	private void runWork() {
		while ( !m_stopRequested ) {
			try {
				m_idleWorkers.put( this );
				Runnable r = m_handoffBox.take();
				runIt(r);
			}
			catch ( Exception e ) {
	            ErrorHandlingUtils.logSevereExceptionAndContinue(
	                    "AThreadPoolWorker: Error while running work",
	                    e
	                    );
			}
		}
	}
	
	private void runIt(
	        Runnable r 
	        ) {
		try {
			r.run();
		}
		catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "AThreadPoolWorker: Uncaught exception fell through from run()",
                    e
                    );
		}
		finally {
			Thread.interrupted();
		}
	}
	
	public void process(
	        Runnable r
	        ) {
		try {
			m_handoffBox.put( r );
		}
		catch( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "AThreadPoolWorker: Error while processing",
                    e
                    );
		}
	}
	
	public void process( 
	        Runnable r,
	        int coreToUse,
	        WindowsThreadPriority windowsPriority
	        ) {
        m_internalThread.setThreadCoreAffinity( coreToUse );
        m_internalThread.setWindowsPriority( windowsPriority );
        process( r );
    }
	
	public void stopWorker() {
		m_stopRequested = true;
		( (ASelfOptArchThread) m_internalThread ).interrupt();
	}
	
	public boolean isAlive() {
		return ( (ASelfOptArchThread) m_internalThread ).isAlive();
	}
	
	public int getWorkerCoreAffinity() {
	    return m_internalThread.getThreadCoreAffinity();
	}
}
