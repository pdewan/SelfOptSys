package selfoptsys.comm;

import java.net.*;
import selfoptsys.config.*;
import commonutils.scheduling.*;
import commonutils.basic2.*;


public abstract class ATCPListeningThread 
    extends ASelfOptArchThread
    implements TCPListeningThread {
    
    protected MessageDest m_messageDest = null;
    protected ServerSocket m_servSock = null;
    protected boolean m_stopFlag = false;

    public ATCPListeningThread(
            ServerSocket sock,
            MessageDest messageDest
            ) {
        int[] coresToUseForThread = AMainConfigParamProcessor.getInstance().getIntArrayParam( Parameters.CORES_TO_USE_FOR_FRAMEWORK_THREADS );
        coresToUseForThread = SchedulingUtils.parseCoresToUseFromSpec( coresToUseForThread );
        setThreadCoreAffinity( SchedulingUtils.pickOneCoreToUseForThreadFromPossibleCores( coresToUseForThread ) );
        
        m_servSock = sock;
        m_messageDest = messageDest;
    }
    
    public void setStopFlag( boolean stopFlag ) {
        m_stopFlag = stopFlag;
        this.interrupt();
    }
    
}
