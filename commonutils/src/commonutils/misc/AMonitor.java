package commonutils.misc;

import java.util.concurrent.*;

public abstract class AMonitor {

	BlockingQueue<Object> m_monitorSemaphore;
	
	public AMonitor() {
		m_monitorSemaphore = new ArrayBlockingQueue<Object>( 1 );
		signalMonitorAccess();
	}
	
	public void signalMonitorAccess() {
		
		try {
			m_monitorSemaphore.put( new Integer( 0 ) );
		}
		catch ( InterruptedException e ) {
			e.printStackTrace();
		}
		
	}
	
	public void waitForMonitorAccess() {
		
		try {
			m_monitorSemaphore.take();
		}
		catch ( InterruptedException e ) {
			e.printStackTrace();
		}

	}
	
}
