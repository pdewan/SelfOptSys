package selfoptsys.comm;

import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.*;

import selfoptsys.config.*;
import selfoptsys.perf.*;

import commonutils.scheduling.*;
import commonutils.basic.*;
import commonutils.basic2.*;

public abstract class AMessageDest 
	extends ASelfOptArchThread
	implements MessageDest {

    protected MessageDest m_rmiStub;
    
	protected LocalMessageDest m_localMessageDest;
	protected BlockingQueue<Message> m_messages;
	
    protected PerformanceOptimizationClient m_performanceOptimizationClient = null;

	public AMessageDest(
			LocalMessageDest localMessageDest,
			WindowsThreadPriority windowsPriority
			) {
        int[] coresToUseForThread = AMainConfigParamProcessor.getInstance().getIntArrayParam( Parameters.CORES_TO_USE_FOR_FRAMEWORK_THREADS );
        coresToUseForThread = SchedulingUtils.parseCoresToUseFromSpec( coresToUseForThread );
        setThreadCoreAffinity( SchedulingUtils.pickOneCoreToUseForThreadFromPossibleCores( coresToUseForThread ) );

        setWindowsPriority( windowsPriority );
		m_localMessageDest = localMessageDest;
		
		m_messages = new ArrayBlockingQueue<Message>( 1000 );
		
        try {
            m_rmiStub =
                (MessageDest) UnicastRemoteObject.exportObject( (MessageDest) this, 0 );        }
        catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "AMessageDest: Error while creating rmi stub",
                    e
                    );
        }
	}
	
    public MessageDest getRmiStub() {
        return m_rmiStub;
    }
    
	public void queueMsg(
			Message msg
			) {
		try {
            if ( m_performanceOptimizationClient != null ) {
                CommandMessage cmdMsg = (CommandMessage) msg;

                if ( cmdMsg.isMsgForLatecomerOrNewMaster() == false &&
                        cmdMsg.getReportReceiveTime() ) {
                    m_performanceOptimizationClient.sendPerformanceReport(
        	                cmdMsg.getMessageType() == MessageType.INPUT ? 
        	                        MessageType.PERF_INPUT_RECEIVED_TIME : MessageType.PERF_OUTPUT_RECEIVED_TIME,
        	                cmdMsg.getSysConfigVersion(),
        	                cmdMsg.getSourceUserIndex(),
        	                cmdMsg.getSeqId(),
        	                0,
        	                0,
        	                0,
        	                0,
        	                -1,
        	                0,
        	                -1,
        	                false,
        	                cmdMsg.getReportTransCostForMessage()
        	                );
                }
            }

	        m_messages.put( msg );
		}
		catch ( InterruptedException e ) {}
		catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "AMessageDest: Error while queueing message",
                    e
                    );
		}
	}
	
	protected void deliverMsg( Message msg ) {
		m_localMessageDest.receiveMessage( msg );
	}
	
	public void setPerformanceOptimizationClient(
            PerformanceOptimizationClient performanceOptimizationClient
	        ) {
	    m_performanceOptimizationClient = performanceOptimizationClient;
	}
	
	public void run() {
	    super.run();
	    
		try {
			for (;;) {
				Message msg = m_messages.take();
				deliverMsg( msg );
			}
		}
		catch ( InterruptedException e ) {}
		catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "AMessageDest: Error in run",
                    e
                    );
		}
	}
}
