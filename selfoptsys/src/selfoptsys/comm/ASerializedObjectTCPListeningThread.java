package selfoptsys.comm;

import java.io.*;
import java.net.*;
import commonutils.basic.*;

public class ASerializedObjectTCPListeningThread 
    extends ATCPListeningThread {
    
    private ObjectInputStream m_ois = null;
    
    public ASerializedObjectTCPListeningThread(
            ServerSocket sock,
            MessageDest messageDest
            ) {
        super(
                sock,
                messageDest
                );
    }
    
    public void run() {
        super.run();
        
        try {
            Socket clientSock = m_servSock.accept();
            m_ois = new ObjectInputStream( clientSock.getInputStream() );
            m_servSock.close();
        
            for (;;) {
                try {
                    Message msg = (Message) m_ois.readObject();
                    if ( msg.getMessageType() == MessageType.CLOCK_SKEW_MSG ) {
                        ( (ClockSkewMsg) msg ).setReceivedTime( System.nanoTime() );
                    }
                    m_messageDest.queueMsg( msg );
                }
                catch ( IOException e ) {
                    break;
                }
            }
            
            m_ois.close();
        }
        catch ( Exception e ) {
            if ( m_stopFlag == false ) { 
                ErrorHandlingUtils.logSevereExceptionAndContinue(
                        "ASerializedObjectTCPListeningThread: Error in run",
                        e
                        );
            }
        }
    }
}
