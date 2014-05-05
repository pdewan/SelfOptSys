package selfoptsys;

import java.rmi.server.UnicastRemoteObject;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import commonutils.basic.*;
import commonutils.scheduling.*;

import selfoptsys.comm.*;

public class AClockSyncServer 
    implements ClockSyncServer, LocalMessageDest {
    
    protected MessageDest m_messageDest;
    
    protected int m_numClockSyncMessagesRequired;
    protected int m_pauseTimeBetweenClockSyncMessageSends;
    
    protected Map<Integer, Long> m_clockSkews;
    protected List<ClockSkewMsg> m_clockSkewMsgs;
    
    protected ClockSyncServer m_rmiStub;
    
    public AClockSyncServer() {
        
        /*
         * TODO: Need to have a configuration argument for the number of clock sync
         * messages required and the amount of time the client should pause after 
         * sending each message. For now, just use the default values of 30 and 10ms, 
         * respectively, as they seem to work well enough in experiments.
         */
        m_numClockSyncMessagesRequired = DEFAULT_NUM_CLOCK_SYNC_MESSAGES_REQUIRED;
        m_pauseTimeBetweenClockSyncMessageSends = PAUSE_TIME_BETWEEN_CLOCK_SYNC_MESSAGE_SENDS;
        
        m_clockSkews = new Hashtable<Integer, Long>();
        m_clockSkewMsgs = new LinkedList<ClockSkewMsg>();

        m_messageDest = new ATCPMessageDest(
                this,
                WindowsThreadPriority.NORMAL,
                WindowsThreadPriority.HIGHEST,
                new ANonBlockingTCPListeningThreadFactory()
                );
        ( (AMessageDest) m_messageDest ).start();
        
        try {
            m_rmiStub =
                (ClockSyncServer) UnicastRemoteObject.exportObject( (ClockSyncServer) this, 0 );
        }
        catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "AClockSyncServer: Exception occurred while generating rmi stub",
                    e
                    );
        }
    }
    
    public ClockSyncServer getRmiStub() {
        return m_rmiStub;
    }
    
    public int getNumberOfClockSyncMessagesRequired() {
        return m_numClockSyncMessagesRequired;
    }
    
    public long getPauseTimeBetweenClockSyncMessageSends() {
        return m_pauseTimeBetweenClockSyncMessageSends;
    }
    
    public void start() {
        ( (AMessageDest) m_messageDest ).start();
    }
    
    public void quit() {
        try {
            m_messageDest.resetMsgDest();
        }
        catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "AClockSyncServer::stop(): Exception occured while resetting message receiver",
                    e
                    );
        }
    }
    
    public MessageDest getMessageDest() {
        MessageDest rmiStub = null;
        
        try {
            rmiStub = m_messageDest.getRmiStub();
        }
        catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "AClockSyncSever::getMessageDest(): An exception occurred while trying to get the rmi stub from message dest",
                    e
                    );
        }
        
        return rmiStub;
    }
    
    public synchronized void receiveMessage(
            Message msg
            ) {
        if ( msg.getMessageType() != MessageType.CLOCK_SKEW_MSG ) {
            return;
        }
        handleClockSkewMsg( (ClockSkewMsg) msg );
    }
    
    private void handleClockSkewMsg(
            ClockSkewMsg msg
            ) {
        m_clockSkewMsgs.add( msg );
        if ( m_clockSkewMsgs.size() < m_numClockSyncMessagesRequired ) {
            return;
        }
        
        List<Long> skews = new LinkedList<Long>();
        for ( int i = 10; i < m_clockSkewMsgs.size() - 10; i++ ) {
            ClockSkewMsg curMsg = m_clockSkewMsgs.get( i );
            ClockSkewMsg nextMsg = m_clockSkewMsgs.get( i + 1 );
            long skew = 
                curMsg.getReceivedTime() -
                curMsg.getSendTimeAtSender() -
                nextMsg.getPrevMsgDelayTime();
            skews.add( skew );
        }
        
        m_clockSkewMsgs.clear();
        long avgSkew = 0;
        for ( int i = 0; i < skews.size(); i++ ) {
            avgSkew += skews.get( i );
        }
        avgSkew /= skews.size();
        m_clockSkews.put(
                msg.getUserIndex(),
                avgSkew
                );
    }

    public Map<Integer, Long> getClockSkews() {
        return m_clockSkews;
    }
}
