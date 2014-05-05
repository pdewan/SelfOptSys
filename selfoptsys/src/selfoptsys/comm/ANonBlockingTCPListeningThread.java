package selfoptsys.comm;

import java.io.*;
import java.net.*;
import commonutils.basic.*;

public class ANonBlockingTCPListeningThread 
    extends ATCPListeningThread {
    
    protected Socket m_clientSocket;
    
    public ANonBlockingTCPListeningThread(
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
            m_clientSocket = m_servSock.accept();
            m_servSock.close();

            byte[] msgLenBuf = new byte[4];
            byte[] dataBuf;
            byte[] restOfDataBuf = new byte[8];
            ObjectInputStream in;
            Message msg;
        
            for (;;) {
                try {
                    try {
                        m_clientSocket.getInputStream().read( msgLenBuf, 0, 4 );
                        
                        int msgLen = MiscUtils.bigEndianByteArrayToInt( msgLenBuf, 0 );
                        if ( msgLen == -1 || msgLen == 0 ) {
                            break;
                        }
                        
                        int numBytesRead = 0;
                        dataBuf = new byte[ msgLen ];
                        while ( numBytesRead < msgLen ) {
                            numBytesRead += m_clientSocket.getInputStream().read( dataBuf, numBytesRead, msgLen - numBytesRead );
                            if ( numBytesRead < 0 ) {
                                break;
                            }
                        }
                        if ( numBytesRead < 0 ) {
                            break;
                        }
                        
                        in = new ObjectInputStream( new ByteArrayInputStream( dataBuf ) );
                        msg = (Message) in.readObject(); 

                        if ( msg.getMessageType() == MessageType.CLOCK_SKEW_MSG ) {
                            ( (AClockSkewMsg) msg ).setReceivedTime( System.nanoTime() );
                        }
                        /*
                         * Why is this here? The reason is that we could not efficiently send (slightly) different data to each
                         * destination from the sender by modifying the buffer being sent. This would have required that we
                         * rebuild the buffer for each dest which takes too long. Instead, we build the buffer once and then
                         * encode the differences into the send stream manually. The differences include values of the 
                         * reportReceiveTime and delaySoFar properties of the CommandMessage.
                         * 
                         * See ANonBlockingTCPLoggalbeMessageForwarder to see how the manual values are encoded.
                         */
                        else if ( msg.getMessageType() == MessageType.INPUT ||
                                msg.getMessageType() == MessageType.OUTPUT ) {
                            if ( ( (CommandMessage) msg ).isMsgForLatecomerOrNewMaster() == false ) { 
                                numBytesRead = 0;
                                while ( numBytesRead < 8 ) {
                                    numBytesRead += m_clientSocket.getInputStream().read( restOfDataBuf, numBytesRead, 8 - numBytesRead );
                                    if ( numBytesRead < 0 ) {
                                        break;
                                    }
                                }
                                if ( numBytesRead < 0 ) {
                                    break;
                                }
    
                                int i = MiscUtils.bigEndianByteArrayToInt( restOfDataBuf, 0 );
                                double delaySoFar = ( MiscUtils.bigEndianByteArrayToInt( restOfDataBuf, 4 ) ) / (double) 1000;
                                CommandMessage cmdMsg = (CommandMessage) msg;
                                cmdMsg.setReportReceiveTime( i == 1 ? true : false );
                                cmdMsg.setDelaySoFar( delaySoFar );
                            }
                        }
                    }
                    catch ( IOException e ) {
                        if ( m_stopFlag == false ) { 
                            ErrorHandlingUtils.logSevereExceptionAndContinue( 
                                    "ANonBlockingTCPListeningThread: Error in run", 
                                    e
                                    );
                        }
                        break;
                    }

                    m_messageDest.queueMsg( msg );
                }
                catch ( IOException e ) {
                    if ( m_stopFlag == false ) { 
                        ErrorHandlingUtils.logSevereExceptionAndContinue( 
                                "ANonBlockingTCPListeningThread: Error in run", 
                                e
                                );
                    }
                    break;
                }
            }
            
            m_clientSocket.close();
        }
        catch ( Exception e ) {
            if ( m_stopFlag == false ) { 
                ErrorHandlingUtils.logSevereExceptionAndContinue(
                        "ANonBlockingTCPListeningThread: Error in run",
                        e
                        );
            }
        }
    }
}
