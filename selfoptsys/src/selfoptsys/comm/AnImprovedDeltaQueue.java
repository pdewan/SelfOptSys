package selfoptsys.comm;

import java.util.*;
import java.util.concurrent.*;
import commonutils.basic.*;


public class AnImprovedDeltaQueue {

    protected Thread m_thread;
    
    protected Vector<Long> m_releaseTimes;
    protected Vector<CommandMessage> m_msgQueue;
    protected BlockingQueue<CommandMessage> m_releasedMessages;
    
    public AnImprovedDeltaQueue() {
        
        Runnable r = new Runnable() {
            public void run() {
                startDeltaQueue();
            }
        };
        m_thread = new Thread( r );
        
        m_releaseTimes = new Vector<Long>();
        m_msgQueue = new Vector<CommandMessage>();
        m_releasedMessages = new ArrayBlockingQueue<CommandMessage>( 1000 );
        
    }

    public void begin() {
        m_thread.start();
    }
    
    public synchronized void put(
            CommandMessage msg
            ) {
        
        long releaseTime = System.nanoTime() + ( (long) msg.getNetworkLatencyDelay() * 1000000L );
        
        if ( m_msgQueue.size() == 0 ) {
            m_msgQueue.add( msg );
            m_releaseTimes.add( releaseTime );
            notifyAll();
        }
        else {
            int insertionPoint = m_releaseTimes.size();
            for ( int i = m_releaseTimes.size() - 1; i >= 0; i-- ) {
                if ( releaseTime <= m_releaseTimes.get( i ) ) {
                    insertionPoint--;
                }
                else {
                    break;
                }
            }            
            m_msgQueue.insertElementAt( msg, insertionPoint );
            m_releaseTimes.insertElementAt( releaseTime, insertionPoint );
            m_thread.interrupt();
        }
        
    }
    
    public synchronized void putDirect(
            CommandMessage msg
            ) {
        try {
            m_releasedMessages.put( msg );
        }
        catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "AnImprovedDeltaQueue: An error in putDirect",
                    e
                    );
        }
    }
    
    public CommandMessage take() {
        CommandMessage m = null;
        try {
            m = m_releasedMessages.take();
        }
        catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "AnImprovedDeltaQueue: Error in take",
                    e
                    );
        }
        
        return m;
    }
    
    private synchronized void startDeltaQueue() {
        for (;;) {
            try {
                while ( m_releaseTimes.size() == 0 ) {
                    this.wait();
                }
                
                long timeLeftBeforeRelease = m_releaseTimes.get( 0 ).longValue() - System.nanoTime();
                if ( timeLeftBeforeRelease > 0 ) {
                    try {
                        Thread.sleep( timeLeftBeforeRelease / 1000000 );
                    }
                    catch ( InterruptedException e ) {
                        
                    }
                }
                else {
                    while ( timeLeftBeforeRelease <= 0 ) {
                        m_releaseTimes.remove( 0 );
                        CommandMessage msg = m_msgQueue.remove( 0 );
                        m_releasedMessages.put( msg );
                        
                        if ( m_releaseTimes.size() > 0 ) {
                            timeLeftBeforeRelease = m_releaseTimes.get( 0 ).longValue() - System.nanoTime();
                        }
                        else {
                            timeLeftBeforeRelease = Long.MAX_VALUE;
                        }
                    }
                }
            }
            catch ( Exception e ) {
                ErrorHandlingUtils.logSevereExceptionAndContinue(
                        "AnImprovedDeltaQueue: Error while running",
                        e
                        );
                break;
            }
        }
    }
    
}
