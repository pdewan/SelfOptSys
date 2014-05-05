package selfoptsys.sched;

import java.util.*;
import java.util.concurrent.*;

import selfoptsys.*;
import selfoptsys.comm.*;
import commonutil.threads.*;


public class ASpecializedProcTaskScheduler 
	extends AScheduler {
	
    protected int m_coreToUseForThread;
    
	protected ThreadPool m_threadPool = null;
	
	public ASpecializedProcTaskScheduler(
			int coreToUseForThread,
			SchedulingLogger logger
			) {
		super( logger );
		
		m_coreToUseForThread = coreToUseForThread;
		int[] coresToUseForThreadPoolThreads = new int[ 1 ];
		coresToUseForThreadPoolThreads[ 0 ] = m_coreToUseForThread;

        m_threadPool = new AThreadPool(
                1,
                coresToUseForThreadPoolThreads
                );
	}
	
	protected void scheduleMsg( 
			CommandMessage msg,
			List<BlockingQueue<Integer>> waitForQList,
			List<BlockingQueue<Integer>> signalForQList
			) {
		Runnable r = getRunnable( msg );
		m_threadPool.execute( r, waitForQList, signalForQList );
	}

	private Runnable getRunnable(
            final CommandMessage msg
            ) {
        Runnable r = new Runnable() {
            public void run() {
                if ( msg.getMessageType() == MessageType.INPUT ) {
                    m_logger.processInputMsg( msg );
                }
                else {
                    m_logger.processOutputMsg( msg );
                }
            }
        };
        return r;
	}
	
}
