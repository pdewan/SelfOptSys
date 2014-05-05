package selfoptsys;

import java.rmi.server.UnicastRemoteObject;
import java.util.LinkedList;
import java.util.List;

import selfoptsys.comm.*;

import commonutils.basic.*;

public class AReplayClient 
    implements ReplayClient {

    protected int m_userIndex;
    protected ReplayUserInfo m_replayUserInfo;
    protected Logger m_logger;
    
    protected TimeServer m_replayServer;
    protected List<TimeServerReportMessage> m_replayServerReports;
    
    protected ReplayClient m_rmiStub;

    public AReplayClient(
            int userIndex,
            ReplayUserInfo replayUserInfo,
            Logger logger
            ) {
        m_userIndex = userIndex;
        m_replayUserInfo = replayUserInfo;
        m_logger = logger;
        
        m_replayServerReports = new LinkedList<TimeServerReportMessage>();
        
        try {
            m_rmiStub =
                (ReplayClient) UnicastRemoteObject.exportObject( (ReplayClient) this, 0 );
        }
        catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "ATimeServer: Exception while creating clock sync server rmi stub",
                    e
                    );
        }

    }
    
    public void registerWithReplayServer(
            TimeServer replayServer
            ) {
        try {
            m_replayServer = replayServer;
            
            m_replayServer.reportReadyFirstTime(
                    m_userIndex,
                    getRmiStub(),
                    m_replayUserInfo
                    );
        }
        catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "AReplayClient:registerWithReplayServer(): Error while registering with the replay server",
                    e
                    );
        }
    }
        
    public void reportReadySecondTimeToReplayServer() {
        try {
            m_replayServer.reportReadySecondTime( m_userIndex );
        }
        catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "AReplayClient::reportReadySecondTimeToTimeServer(): Error while reporting ready to time server the second time",
                    e
                    );
        }
    }
    
    public void recordReplayServerReport(
            MessageType messageType,
            int userIndex,
            int sourceUserIndex,
            int seqId,
            long time
            ) {
        TimeServerReportMessage msg = new ATimeServerReportMessage(
                messageType,
                userIndex,
                sourceUserIndex,
                seqId,
                time
                );
        m_replayServerReports.add( msg );
    }
    
    public void reportDoneToReplayServer() {
        try {
            m_replayServer.reportDone( m_userIndex );
        }
        catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "AReplayClient::reportDoneToTimeServer(): Error while reporting done to time server",
                    e
                    );
        }
    }
    
    public void beginExperiment() {
        try {
            m_logger.beginExperiment();
        }
        catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "AReplayClient::beginExperiment(): Error while attempting to being experiment",
                    e
                    );
        }
    }
    
    public void joinByCommandFromReplayServer() {
        try {
            m_logger.beginExperiment();
        }
        catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "AReplayClient::joinByCommandFromReplayServer(): Error while attempting to join on command from replay server",
                    e
                    );
        }
    }
    
    public List<TimeServerReportMessage> getReplayServerReports() {
        return m_replayServerReports;
    }
    
    public void syncClockWithReplayServer() {
        try {
            if ( m_replayUserInfo.getMeasurePerformance() ) {
                ClockSyncClient clockSyncClient = new AClockSyncClient(
                        m_userIndex,
                        m_replayServer.getClockSyncServer()
                        );
                clockSyncClient.sync();
            }
        }
        catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "AReplayClient::reportResultsToReplayServer: Failed to synchronize clocks with replay server",
                    e
                    );
        }
    }

    public void prepareToQuit() {
        try {
            m_logger.prepareToQuit();
        }
        catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "AReplayClient::prepareToQuit(): Error while preparing to quit",
                    e
                    );
        }
    }
    
    public void quit() {
        try {
            m_logger.quit();
        }
        catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "AReplayClient::quit(): Error while attempting to being experiment quitting",
                    e
                    );
        }        
    }
    
    public ReplayClient getRmiStub() {
        return m_rmiStub;
    }
}
