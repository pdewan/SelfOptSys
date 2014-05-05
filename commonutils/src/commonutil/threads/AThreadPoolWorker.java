/*
 * This class was taken from "Thread Pooling - Chapter 13" 
 * available online 
 */

package commonutil.threads;

import java.util.concurrent.*;
import commonutils.scheduling.*;
import commonutils.basic.ErrorHandlingUtils;
import commonutils.basic2.*;

public class AThreadPoolWorker 
	implements ThreadPoolWorker {

	private BlockingQueue<ThreadPoolWorker> m_idleWorkers;
	protected int m_coreToUseForWorkerThread;
	
	private BlockingQueue<Runnable> m_handoffBox;
	
	private SelfOptArchThread m_internalThread;
	private volatile boolean m_stopRequested;
	
	private BlockingQueue<Integer> m_finishedSignalQ = null;
    private BlockingQueue<Integer> m_waitForQ = null;
	
	public AThreadPoolWorker(
			BlockingQueue<ThreadPoolWorker> idleWorkers,
			int coreToUseForWorkerThread
			) {
		m_idleWorkers = idleWorkers;
		m_coreToUseForWorkerThread = coreToUseForWorkerThread;
		
		m_handoffBox = new ArrayBlockingQueue<Runnable>(1);
		
		m_stopRequested = false;
		
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
	}
	
	private void runWork() {
		while ( !m_stopRequested ) {
			try {
				m_idleWorkers.put( this );

				Runnable r = m_handoffBox.take();
				if ( m_waitForQ != null ) {
				    waitToStartJob();
				    m_waitForQ = null;
				}
				runIt(r);
				if ( m_finishedSignalQ != null ) {
					signalJobFinished();
					m_finishedSignalQ = null;
				}
			}
			catch ( Exception e ) {
	            ErrorHandlingUtils.logSevereExceptionAndContinue(
	                    "AThreadPoolWorker: Error while running work",
	                    e
	                    );
			}
		}
	}
	
	private void runIt( Runnable r ) {
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
	
    private void waitToStartJob() {
        try {
            m_waitForQ.take();
        }
        catch ( Exception e  ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "AThreadPoolWorker: Error while waiting to start job",
                    e
                    );
        }
    }
    
	private void signalJobFinished() {
		try {
			m_finishedSignalQ.put( 0 );
		}
		catch ( Exception e  ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "AThreadPoolWorker: Error while signalling job completed",
                    e
                    );
		}
	}
	
	public void process( Runnable r ) {
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
	
	public void stopWorker() {
		m_stopRequested = true;
		( (ASelfOptArchThread) m_internalThread ).interrupt();
	}
	
	public boolean isAlive() {
		return ( (ASelfOptArchThread) m_internalThread ).isAlive();
	}
	
    public void setWaitForQueue(
            BlockingQueue<Integer> waitForQ
            ) {
        m_waitForQ = waitForQ;
    }
    
	public void setFinishedSignalQueue(
			BlockingQueue<Integer> finishedSignalQ
			) {
		m_finishedSignalQ = finishedSignalQ;
	}
	
	public int getWorkerCoreAffinity() {
	    return m_internalThread.getThreadCoreAffinity();
	}
}
