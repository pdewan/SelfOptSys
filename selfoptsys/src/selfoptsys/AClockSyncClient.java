package selfoptsys;

import selfoptsys.comm.*;
import selfoptsys.config.*;
import commonutils.basic.*;

public class AClockSyncClient 
    implements ClockSyncClient {

    protected int m_userIndex;
    protected ClockSyncServer m_clockSyncServer;
    
    public AClockSyncClient(
            int userIndex,
            ClockSyncServer clockSyncServer
            ) {
        m_userIndex = userIndex;
        m_clockSyncServer = clockSyncServer;
    }
    
    public void sync() {
        
        try {
            int numClockSyncMessagesRequired = m_clockSyncServer.getNumberOfClockSyncMessagesRequired();
            long pauseTimeBetweenClockSyncMessageSends = m_clockSyncServer.getPauseTimeBetweenClockSyncMessageSends();
            
            MessageForwarder forwarder = ( new ANonBlockingTCPMessageForwarderFactory() ).createMessageForwarder(
                    m_userIndex
                    );
            forwarder.addDest(
                    Constants.CLOCK_SYNC_SERVER_USER_INDEX,
                    m_clockSyncServer.getMessageDest()
                    );
            
            long prevMsgSendTime = 0;
            long prevMsgDelayTime = 0;
            for ( int i = 0; i < numClockSyncMessagesRequired; i++ ) {
                ClockSkewMsg msg = new AClockSkewMsg(
                        m_userIndex
                        );
                msg.setSendTimeAtSender( System.nanoTime() );
                
                if ( i > 0 ) {
                    msg.setTimeElapsedSinceLastSendOnSender( System.nanoTime() - prevMsgSendTime );
                    msg.setPrevMsgDelayTime( prevMsgDelayTime );
                }
                
                long startSendTime = System.nanoTime();
                forwarder.sendMsg( msg );
                prevMsgDelayTime = System.nanoTime() - startSendTime;
                
                prevMsgSendTime = System.nanoTime();
                
                Thread.sleep( pauseTimeBetweenClockSyncMessageSends );
            }
            
            /*
             * Sleep to make sure all of the messages get across before shutting
             * down channel.
             */
            // TODO: message not being removed on removeDest. fix.
            forwarder.removeDest( Constants.CLOCK_SYNC_SERVER_USER_INDEX );
            forwarder.resetMsgSender();
        }
        catch (Exception e) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "AClockSyncClient::sync(): Exception occurred during sync",
                    e
                    );
        }
        
    }
    
}
