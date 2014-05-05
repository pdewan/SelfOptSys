package commonutils.misc;

import java.util.concurrent.*;

public class AQuitThread extends Thread {
    
    BlockingQueue<Object> m_quitBB = null;
    String m_quitMsg = "";
    int m_numSignals;
    int m_timeToWaitBeforeQuitting = 1000;
    
    public AQuitThread(
            BlockingQueue<Object> quitBB,
            String quitMsg,
            int numSignals
            ) {
        m_quitBB = quitBB;
        m_quitMsg = quitMsg;
        m_numSignals = numSignals;
    }
    
    public AQuitThread(
            BlockingQueue<Object> quitBB,
            String quitMsg,
            int numSignals,
            int timeToWaitBeforeQuitting
            ) {
        this(
                quitBB,
                quitMsg,
                numSignals
                );
        m_timeToWaitBeforeQuitting = timeToWaitBeforeQuitting;
    }
    
    public void run() {
        try {
        	for ( int i = 0; i < m_numSignals; i++ ) {
        		m_quitBB.take();
        	}
        	Thread.sleep( m_timeToWaitBeforeQuitting );
        	if ( m_quitMsg != null && m_quitMsg.length() > 0 ) {
        	    System.err.println( m_quitMsg );
        	}
            System.exit(1);
        }
        catch ( Exception e ) {
        	e.printStackTrace();
        }
    }
}