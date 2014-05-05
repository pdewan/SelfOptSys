package selfoptsysapps.demo;

import java.util.concurrent.*;
import util.models.AListenableVector;

import commonutils.basic.*;
import selfoptsys.*;
import selfoptsys.comm.*;

public class AUserControlledLogger 
    extends ALogger
    implements UserControlledLogger {

    protected AListenableVector<CommandMessageInfo> m_processTaskInfosQueue;
    protected AListenableVector<CommandMessageInfo> m_transmitTaskInfosQueue;
    
    protected BlockingQueue<Integer> m_procInputBB = new ArrayBlockingQueue<Integer>( 1 );
    protected BlockingQueue<Integer> m_procOutputBB = new ArrayBlockingQueue<Integer>( 1 );
    protected BlockingQueue<Integer> m_transInputBB = new ArrayBlockingQueue<Integer>( 1 );
    protected BlockingQueue<Integer> m_transOutputBB = new ArrayBlockingQueue<Integer>( 1 );
    
    public AUserControlledLogger( 
            Loggable myLoggable,
            String registryHost,
            int registryPort,
            boolean haveAFakeLoggable,
            boolean myUserInputsCommands,
            boolean runningUIAsMaster,
            boolean waitForUserToScheduleTasks
            ){
        super( 
            myLoggable,
            registryHost,
            registryPort,
            haveAFakeLoggable,
            myUserInputsCommands,
            runningUIAsMaster,
            waitForUserToScheduleTasks
            );
        
        m_processTaskInfosQueue = new AListenableVector<CommandMessageInfo>();
        m_transmitTaskInfosQueue = new AListenableVector<CommandMessageInfo>();
        
//        if ( m_showAllUIs ) {
//            UserControlledLoggerUI userControlledLoggerUI = new AUserControlledLoggerUI( this );
//            m_loggerUI.setUserControlledLoggerUI( userControlledLoggerUI );
//        }
    }
    
    public void processInputMsg(
            CommandMessage msg
            ) {
        try {
            m_processTaskInfosQueue.add( new ACommandMessageInfo( msg ) );
            m_procInputBB.take();
        }
        catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "AUserControlledLogger: Error while processing input message",
                    e
                    );
        }
    }
    
    public void processOutputMsg(
            CommandMessage msg
            ) {
        try {
            m_processTaskInfosQueue.add( new ACommandMessageInfo( msg ) );
            m_procOutputBB.take();
        }
        catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "AUserControlledLogger: Error while processing output message",
                    e
                    );
        }
    }
    
    public void forwardInputMsg(
            CommandMessage msg
            ) {
        try {
            m_transmitTaskInfosQueue.add( new ACommandMessageInfo( msg ) );
            m_transInputBB.take();
        }
        catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "AUserControlledLogger: Error while forwarding input message",
                    e
                    );
        }
    }
    
    public void forwardOutputMsg(
            CommandMessage msg
            ) {
        try {
            m_transmitTaskInfosQueue.add( new ACommandMessageInfo( msg ) );
            m_transOutputBB.take();
        }
        catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "AUserControlledLogger: Error while forwarding output message",
                    e
                    );
        }
    }
    
    public AListenableVector<CommandMessageInfo> getProcessTaskInfosQueue() {
        return m_processTaskInfosQueue;
    }
    
    public AListenableVector<CommandMessageInfo> getTransmitTaskInfosQueue() {
        return m_transmitTaskInfosQueue;
    }
    
    public void performNextProcessTask() {
        
        try {
            if ( m_processTaskInfosQueue.size() == 0 ) {
                return;
            }
            
            CommandMessageInfo info = m_processTaskInfosQueue.remove( 0 );
            CommandMessage msg = info.getCommandMessage();
            if ( info.getMessageType() == MessageType.INPUT ) {
                super.processInputMsg( msg );
                m_procInputBB.put( 0 );
            } 
            else if ( info.getMessageType() == MessageType.OUTPUT ) {
                super.processOutputMsg( msg );
                m_procOutputBB.put( 0 );
            }
        }
        catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "AUserControlledLogger: Error while performing next processing task",
                    e
                    );
        }
        
    }
    
    public void performNextTransmitTask() {
        
        try {
            if ( m_transmitTaskInfosQueue.size() == 0 ) {
                return;
            }
            
            CommandMessageInfo info = m_transmitTaskInfosQueue.remove( 0 );
            CommandMessage msg = info.getCommandMessage();
            if ( info.getMessageType() == MessageType.INPUT ) {
                super.forwardInputMsg( msg );
                m_transInputBB.put( 0 );
            } 
            else if ( info.getMessageType() == MessageType.OUTPUT ) {
                super.forwardOutputMsg( msg );
                m_transOutputBB.put( 0 );
            }
        }
        catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "AUserControlledLogger: Error while performing next transmission task",
                    e
                    );
        }

    }
    
}
