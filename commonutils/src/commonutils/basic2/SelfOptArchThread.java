package commonutils.basic2;

import commonutils.scheduling.*;


public interface SelfOptArchThread {

    void setWindowsPriority(
            int windowsPriority
            );
    
    void setWindowsPriority(
            WindowsThreadPriority windowsPriority
            );
    
    int getThreadCoreAffinity();
    void setThreadCoreAffinity(
            int threadCoreAffinity
            );
}
