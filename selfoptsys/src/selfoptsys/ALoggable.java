package selfoptsys;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.io.*;

import selfoptsys.comm.*;
import selfoptsys.config.*;

import commonutils.config.*;
import commonutils.misc.*;
import commonutils.basic.*;
import commonutils.basic2.*;

public abstract class ALoggable implements Loggable {
	
	protected final int MILLION = 1000000;
	
    protected LocalLogger m_myLogger = null;
    
    protected int m_userIndex = -1;
    protected int m_masterUserIndex = -1;
    
    protected Vector<Object> m_replayLog = new Vector<Object>();
    
    protected int[] m_userTurns = null;
    protected int[] m_thinkTimes = null;
    
    protected OperationMode m_operationMode;
      
    protected boolean m_amAFakeLoggable = false;
    
    protected AtomicInteger m_curInputMsg = new AtomicInteger( Loggable.INIT_SEQ_NUMBER );
    protected AtomicInteger m_curOutputMsg = new AtomicInteger( Loggable.INIT_SEQ_NUMBER );
    
    protected ConfigParamProcessor m_mainCpp;
    
    protected String m_registryHost = null;
    protected int m_registryPort = 0;
    
    protected boolean m_overrideConfigFileUsersWhoInputSettings = false;
    protected boolean m_userInputsCommands = false;
    
    protected boolean m_printCosts = false;
    
    protected String m_loggableCostsOutputFile = "";
    protected PrintStream m_loggableCostsOutputStream = null;
    
    protected boolean m_waitForUserToReleaseTasks = false;
    protected boolean m_waitForUserToScheduleTasks = false;
    
    protected ALoggableUserInfo m_loggableUserInfo;
    protected long m_inputProcStartTime;
    protected long m_inputProcEndTime;
    protected long m_outputProcStartTime;
    protected long m_outputProcEndTime;
    
    protected boolean m_runningUIAsMaster = true;
    
    protected boolean m_experimentHasBegun = false;
    
    protected int m_myInputCommandSeqId = 0;
    protected List<ACommandInfo> m_receivedInputCommandInfos;
    protected ACommandInfo m_lastReceivedCmdInfo;
    
    protected String m_loggingLevel = "DEBUG";
    
    public ALoggable( 
    		int userIndex,
    		String registryHost,
    		int registryPort,
            boolean amAFakeLoggable
            ) {
    	m_mainCpp = AMainConfigParamProcessor.getInstance();
    	
    	m_registryHost = registryHost;
    	m_registryPort = registryPort;
    	
        m_userIndex = userIndex;

        m_amAFakeLoggable = amAFakeLoggable;
        
        m_operationMode = OperationMode.valueOf( m_mainCpp.getStringParam( Parameters.OPERATION_MODE ) );
        
        if ( m_operationMode == OperationMode.REPLAY ) {
            m_userTurns = m_mainCpp.getIntArrayParam( Parameters.USER_TURNS );
            m_thinkTimes = m_mainCpp.getIntArrayParam( Parameters.THINK_TIMES );
        }
        
        m_receivedInputCommandInfos = new LinkedList<ACommandInfo>();
        
        m_loggableUserInfo = new ALoggableUserInfo( m_userIndex );
        
        m_loggingLevel = m_mainCpp.getStringParam( Parameters.LOGGING_LEVEL );
        LoggingUtils.setLoggingLevel( m_loggingLevel );
    }
    
    public void startLogger() {
    	
//        if ( m_waitForUserToReleaseTasks ) {
//            ALoggerFactorySelector.setFactory( new AUserControlledLoggerFactory() );
//        }
        
    	boolean inputsCommands = false;
		if ( m_overrideConfigFileUsersWhoInputSettings ) {
			inputsCommands = m_userInputsCommands;
		}
		else {
	    	int[] usersWhoInput = MiscUtils.getSpecifiedUserIndices( m_mainCpp.getStringArrayParam( Parameters.USERS_WHO_INPUT_COMMANDS ) );
	    	boolean allUsersInput = m_mainCpp.getBooleanParam( Parameters.ALL_USERS_INPUT_COMMANDS );
	    	if ( allUsersInput ) {
	    	    inputsCommands = true;
	    	}
	    	else {
                for ( int i = 0; i < usersWhoInput.length; i++ ) {
                    if ( usersWhoInput[i] == m_userIndex ) {
                        inputsCommands = true;
                        break;
                    }
                }
	    	}
		}
    	
    	if ( m_operationMode == OperationMode.REPLAY ) {
    		if ( inputsCommands ) {
                readInReplayLog();
    		}
    	}
    	
    	m_userInputsCommands = inputsCommands;
    	
        m_myLogger = (LocalLogger) ALoggerFactorySelector.getFactory().createLogger(
        		this,
        		m_registryHost,
        		m_registryPort,
                m_amAFakeLoggable,
                inputsCommands,
                m_runningUIAsMaster,
                m_waitForUserToScheduleTasks
                );
    }
    
    public void deliverInputMsg(
            CommandMessage msg
            ) {
    	if ( m_printCosts ) {
    		m_inputProcStartTime = System.nanoTime();
    	}
    	
    	if ( m_myLogger.getOutputCorrespondsToInput() ) {
        	ACommandInfo cmdInfo = new ACommandInfo();
        	cmdInfo.CmdSourceUserIndex = msg.getSourceUserIndex();
        	cmdInfo.SeqId = msg.getSeqId();
        	cmdInfo.SysConfigVersion = msg.getSysConfigVersion();
        	cmdInfo.SenderUserIndex = msg.getSenderUserIndex();
        	m_receivedInputCommandInfos.add( cmdInfo );
    	}
    	
        replayToModel( msg.getData() );
        
        if ( msg.getSourceUserIndex() != m_userIndex 
        		|| msg.isMsgForLatecomerOrNewMaster() ) {
        	m_curInputMsg.incrementAndGet();
        }
    }
    
    public void deliverOutputMsg(
    		CommandMessage msg
            ) {
    	
        m_outputProcStartTime = System.nanoTime();        
        replayToView( msg.getData() );
        m_outputProcEndTime = System.nanoTime();
        
        if ( m_printCosts ) {
	        long outputProcTime = m_outputProcEndTime - m_outputProcStartTime;
	        m_loggableUserInfo.addOutputProcessingTime(
	                msg.getSourceUserIndex(),
	                msg.getSeqId(),
	                outputProcTime
	                );
        }
        
        if ( msg.getSourceUserIndex() != m_userIndex 
        		 || msg.isMsgForLatecomerOrNewMaster() ) {
        	m_curOutputMsg.incrementAndGet();
        }
        
        if ( m_userIndex != m_masterUserIndex && 
                m_userIndex != msg.getSourceUserIndex() ) {
        	m_curInputMsg.incrementAndGet();
        }
    }
    
    public synchronized void sendInputMsg(
            Object msg
            ) {
    	sendInputMsg(
    			msg,
    			true,
    			true,
    			null
    			);
    }
    
    public synchronized void sendInputMsg(
            Object msg,
            Vector<Integer> destUserIndices
            ) {
        sendInputMsg(
                msg,
                true,
                true,
                destUserIndices
                );
    }
    
    public void sendInputMsg(
            Object msg,
            boolean reportProcCost,
            boolean reportTransCost
            ) {
        sendInputMsg(
                msg,
                reportProcCost,
                reportTransCost,
                null
                );
    }
    public void sendInputMsg(
            Object msg,
            boolean reportProcCost,
            boolean reportTransCost,
            Vector<Integer> destUserIndices
            ) {
        if ( !m_userInputsCommands ) {
            return;
        }

        long inputEnteredTime = System.nanoTime();
        long thinkTime = 0;
        if ( m_outputProcEndTime > 0 ) {
            thinkTime = inputEnteredTime - m_outputProcEndTime;
        }
        m_loggableUserInfo.addThinkTime(
                m_myInputCommandSeqId,
                thinkTime
                );
        
        CommandMessage m = new ACommandMessage(
                MessageType.INPUT,
                m_userIndex,
                m_myInputCommandSeqId,
                m_userIndex,
                -1,
                false,
                destUserIndices,
                msg,
                SchedulingPolicy.UNDEFINED,
                reportProcCost,
                reportTransCost
                );
        m_myInputCommandSeqId++;
        m_curInputMsg.incrementAndGet();
        m_myLogger.sendInputMsg( m );
    }

    
    public synchronized void sendOutputMsg(
            Object msg
            ) {
    	sendOutputMsg(
    			msg,
    			true,
    			true,
    			null
    			);
    }
    
    public synchronized void sendOutputMsg(
    		Object msg,
    		Vector<Integer> destUserIndices
    		) {
        sendOutputMsg(
                msg,
                true,
                true,
                destUserIndices
                );
    }
    
    public void sendOutputMsg(
            Object msg,
            boolean reportProcCost,
            boolean reportTransCost
            ) {
        sendOutputMsg(
                msg,
                reportProcCost,
                reportTransCost,
                null
                );
    }
    public void sendOutputMsg(
            Object msg,
            boolean reportProcCost,
            boolean reportTransCost,
            Vector<Integer> destUserIndices
            ) {

        int cmdSourceUserIndex = -1;
        int seqId = -1;
        int sysConfigVersion = -1;
        if ( m_myLogger.getOutputCorrespondsToInput() ) {
            m_lastReceivedCmdInfo = m_receivedInputCommandInfos.remove( 0 );
            cmdSourceUserIndex = m_lastReceivedCmdInfo.CmdSourceUserIndex;
            seqId = m_lastReceivedCmdInfo.SeqId;
            sysConfigVersion = m_lastReceivedCmdInfo.SysConfigVersion;
        }
        else {
            cmdSourceUserIndex = m_userIndex;
            seqId = -1;
        }

        CommandMessage m = new ACommandMessage( 
                MessageType.OUTPUT, 
                cmdSourceUserIndex,
                seqId,
                -1,
                sysConfigVersion,
                false,
                destUserIndices,
                msg, 
                SchedulingPolicy.UNDEFINED,
                reportProcCost,
                reportTransCost
                );
        m_curOutputMsg.incrementAndGet();
        
        if ( m_printCosts && m_myLogger.getOutputCorrespondsToInput() ) {
            if ( m_inputProcStartTime > 0 ) {
                m_inputProcEndTime = System.nanoTime();
                long inputProcTime = m_inputProcEndTime - m_inputProcStartTime;
                m_loggableUserInfo.addInputProcessingTime(
                        cmdSourceUserIndex,
                        seqId,
                        inputProcTime
                        );
                m_inputProcStartTime = 0;
            }
        }
        
        m_myLogger.sendOutputMsg( m );
    }
    
    public int getUserIndex() {
        return m_userIndex;
    }
    
    public int getMasterUserIndex() {
        return m_masterUserIndex;
    }
    
    public void setMasterUserIndex(
    		int masterUserIndex
    		) {
    	m_masterUserIndex = masterUserIndex;
    }
    
    private void readInReplayLog() {
             
        ObjectInputStream ois = null;
        
        try {
        	String logFile = m_mainCpp.getStringParam( Parameters.INPUT_DIRECTORY ) + "//" + 
        		m_mainCpp.getStringParam( Parameters.LOG_FILE_IN );
            ois = new ObjectInputStream( new FileInputStream( logFile ) );
            while ( true ) {
                Object o = ois.readObject();
                m_replayLog.addElement( o );
            }
        }
        catch ( EOFException e ) {
        }
        catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "ALoggable: Error while reading in replay log",
                    e
                    );
        }
    }
    
    public void beginExperiment() {
    	m_experimentHasBegun = true;
        replayMessageFromReplayLogIfMyTurn();
    }
    
    public void loggerAboutToQuit() {
    	
    }
    
    public void quit() {
    	
    	printCostsAndThinkTimes();

    	BlockingQueue<Object> quitBB = new ArrayBlockingQueue<Object>( 1 );
        ( new AQuitThread(
        		quitBB,
        		"Loggable quitting!",
        		1
        		) ).start();
        
        try {
        	quitBB.put( new Integer( 1 ) );
        }
        catch ( Exception e ) {
        	e.printStackTrace();
        }
    }
      
    public void replayMessageFromReplayLogIfMyTurn() {
    	
        final int curInputMsg = m_curInputMsg.get();
        
    	if ( m_experimentHasBegun == false ) {
    		return;
    	}
    	
    	if ( curInputMsg >= m_userTurns.length ) {
    		m_myLogger.reportDoneToReplayServer();
    		return;
    	}
        
        if ( m_userTurns[ curInputMsg ] == m_userIndex ) {
            
        	Runnable r = new Runnable() {
        		public void run() {
        			replayMessageFromReplayLog( curInputMsg );
        		}
        	};
        	Thread nextInputThread = new Thread( r );
        	nextInputThread.setPriority( Thread.currentThread().getPriority() );
        	nextInputThread.start();

        }
    }
    
    protected void replayMessageFromReplayLog(
            int messageIndex
            ) {
        try {
            if ( messageIndex > 0 ) {
                    int thinkTime = m_thinkTimes[ messageIndex ];
                    Thread.sleep( thinkTime );
            }
            
            long inputEnteredTime = System.nanoTime();
            long thinkTime = 0;
            if ( m_outputProcEndTime > 0 ) {
                thinkTime = inputEnteredTime - m_outputProcEndTime;
            }
            m_loggableUserInfo.addThinkTime(
                    m_myInputCommandSeqId,
                    thinkTime
                    );
            
            CommandMessage m = (CommandMessage) m_replayLog.elementAt( messageIndex );
            m.setReportProcCostForMessage(
                    shouldProcCostForMsgBeReported( m.getData() ) 
                    );
            m.setReportTransCostForMessage(
                    shouldTransCostForMsgBeReported( m.getData() ) 
                    );
            m.setMessageType( MessageType.INPUT );
            m.setSourceUserIndex( m_userIndex );
            m.setSeqId( m_myInputCommandSeqId );
            m_myInputCommandSeqId++;
            m_curInputMsg.incrementAndGet();

            m_myLogger.sendInputMsg( m );
        }
        catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "ALoggable: Error while replaying message from replay log",
                    e
                    );
        }
    }

    public void resetLoggableForArchChange(
    		int masterUserIndex
    		) {
    	
    	setMasterUserIndex( masterUserIndex );
    	
    	if ( m_userIndex == m_masterUserIndex ) {
	    	m_curInputMsg.set( Loggable.INIT_SEQ_NUMBER );
	    	m_curOutputMsg.set( Loggable.INIT_SEQ_NUMBER );
    	}
    	
    }
    
    public LocalLogger getLogger() {
    	return m_myLogger;
    }
    
    public void setPrintCosts(
    		boolean printCosts
    		) {
    	m_printCosts = printCosts;
		setupCostsOutputStream();
    }
    public void setCostsOutputFile(
    		String costsOutputFileName
    		) {
    	m_loggableCostsOutputFile = costsOutputFileName;
    	if ( m_printCosts ) {
    		setupCostsOutputStream();
    	}
    }
    
    public void setWaitForUserToScheduleTasks(
            boolean waitForUsersToScheduleTasks
            ) {
        m_waitForUserToScheduleTasks = waitForUsersToScheduleTasks;
    }
    
    public void setWaitForUserToReleaseTasks(
            boolean waitForUsersToReleaseTasks
            ) {
        m_waitForUserToReleaseTasks = waitForUsersToReleaseTasks;
    }
    
    private void setupCostsOutputStream() {
    	try {
	    	if ( m_loggableCostsOutputFile != null && !m_loggableCostsOutputFile.equals( "" ) ) {
	    		File outputFile = new File( m_loggableCostsOutputFile );
	    		if ( outputFile.exists() ) {
	    			outputFile.delete();
	    		}
    			outputFile.createNewFile();
    			m_loggableCostsOutputStream = new PrintStream( outputFile );
	    	}
    	} catch ( Exception e ) {
			System.out.println( "Loggable costs output file not valid." );
			System.out.println( "	Costs will be printed to the screen." );
    	} finally {
    		if ( m_loggableCostsOutputStream == null ) {
    			m_loggableCostsOutputStream = System.out;
    		}
    	}
    }

    private void printCostsAndThinkTimes() {
    	if ( m_printCosts == false ) {
    		return;
    	}
    	
    	ALoggableCostReporter costReporter = new ALoggableCostReporter();
    	costReporter.printCosts(
    	        m_loggableUserInfo,
    	        m_userTurns,
    	        m_loggableCostsOutputStream
    	        );
    }
    
    public void setOverrideConfigFileUsersWhoInputSettings(
    		boolean overrideConfigFileUsersWhoInputSetting
    		) {
    	m_overrideConfigFileUsersWhoInputSettings = overrideConfigFileUsersWhoInputSetting;
    }

    public void setUserInputsCommands(
    		boolean userInputsCommands
    		) {
    	m_userInputsCommands = userInputsCommands;
    }
    
    public void setRunningUIAsMaster(
    		boolean runningUIAsMaster
    		) {
    	m_runningUIAsMaster = runningUIAsMaster;
    }
    
    public void reportReadySecondTimeToTimeServer() {
    	m_myLogger.reportReadySecondTimeToReplayServer();
    }
    
    public void sendPerfReportCollectorReportMessage(
            Message perfReportCollectorReportMessage
            ) {
        m_myLogger.sendPerfReportCollectorReportMessage( perfReportCollectorReportMessage );
    }
    
    public void setAutoReplayCommandsFromReplayLog(
    		boolean autoReplayCommandsFromReplayLog
    		) {
    	m_myLogger.setAutoReplayCommandsFromReplayLog( autoReplayCommandsFromReplayLog );
    }
    
    public boolean isMaster() {
        return m_userIndex == m_masterUserIndex;
    }
    
    public ALoggableUserInfo getLoggableUserInfo() {
        return m_loggableUserInfo;
    }
    
    public boolean shouldProcCostForMsgBeReported(
            Object msg
            ) {
        return true;
    }
    public boolean shouldTransCostForMsgBeReported(
            Object msg
            ) {
        return true;
    }
    
    public void recordTimeServerMessage(
    		MessageType messageType,
            int userIndex,
            int sourceUserIndex,
            int seqId,
            long time
    		) {
    	m_myLogger.recordReplayServerReport(
    			messageType,
    			userIndex,
    			sourceUserIndex,
    			seqId,
    			time
    			);
    }

    
}

