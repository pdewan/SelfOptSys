package commonutils.basic2;

import winsyslib.*;
import commonutils.scheduling.*;
import commonutils.basic.*;


public class ASelfOptArchThread 
    extends Thread
    implements SelfOptArchThread {

    protected WindowsThreadPriority m_windowsPriority = WindowsThreadPriority.NORMAL;
    protected int m_intWindowsPriority = WindowsThreadPriorityUtils.WINDOWS_PRIORITY_NORMAL;
    protected int m_threadCoreAffinity;
    
    protected int m_oldIntWindowsPriority = Integer.MAX_VALUE;
    protected int m_oldThreadCoreAffinity = Integer.MAX_VALUE;
    
    public ASelfOptArchThread() {}
    
    public ASelfOptArchThread(
            Runnable r
            ) {
        super( r );
    }
    
    public void setWindowsPriority(
            WindowsThreadPriority windowsPriority
            ) {
        m_windowsPriority = windowsPriority;
        m_intWindowsPriority = WindowsThreadPriorityUtils.getIntPriorityFromWindowsThreadPriority( m_windowsPriority );
        WinSysLibUtilities.setThreadPriority( m_intWindowsPriority );
    }
    
    public void setWindowsPriority(
            int windowsPriority
            ) {
        m_intWindowsPriority = windowsPriority;
        m_windowsPriority = WindowsThreadPriorityUtils.getWindowsThreadPriorityFromInt( windowsPriority );
        WinSysLibUtilities.setThreadPriority( m_intWindowsPriority );
    }
    
    public int getThreadCoreAffinity() {
        return m_threadCoreAffinity;
    }
    public void setThreadCoreAffinity(
            int threadCoreAffinity
            ) {
        m_threadCoreAffinity = threadCoreAffinity;
        WinSysLibUtilities.setThreadProcessorAffinity_0Based( m_threadCoreAffinity );
    }
    
    public void run() {
        try {
//            if ( m_intWindowsPriority != m_oldIntWindowsPriority ) {
//                WinSysLibUtilities.setThreadPriority( m_intWindowsPriority );
//                m_oldIntWindowsPriority = m_intWindowsPriority;
//            }
//            
//            if ( m_threadCoreAffinity != m_oldThreadCoreAffinity ) {
//                WinSysLibUtilities.setThreadProcessorAffinity_0Based( m_threadCoreAffinity );
//                m_oldThreadCoreAffinity = m_threadCoreAffinity;
//            }
            
            super.run();
        }
        catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    null,
                    e
                    );
        }
    }
}
