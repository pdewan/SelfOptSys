package selfoptsys.comm;

import commonutils.basic2.*;


public interface TCPListeningThread 
    extends SelfOptArchThread {
    
    void setStopFlag(
            boolean stopFlag
            );
    
}
