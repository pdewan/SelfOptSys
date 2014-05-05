package selfoptsys.comm;

import selfoptsys.network.*;
import commonutils.scheduling.*;


public class ATCPLoggableMessageDest 
    extends ATCPMessageDest {
    
    protected AnImprovedDeltaQueue m_messagesDeltaQueue;
    protected boolean m_simulatingNetworkLatencies = false;
    
    protected int m_userIndex;
    protected SimulatedLatencyManager m_simulatedLatencyManager;
    
    protected boolean m_haveAFakeLoggable = false;
    
    public ATCPLoggableMessageDest(
            int userIndex,
            LocalMessageDest localMessageDest,
            boolean simulatingNetworkLatencies,
            WindowsThreadPriority windowsPriority,
            SimulatedLatencyManager simulatedLatencyManager,
            TCPListeningThreadFactory tcpListeningThreadFactory,
            boolean haveAFakeLoggable
            ) {
        this( 
                userIndex,
                localMessageDest,
                windowsPriority,
                WindowsThreadPriority.HIGHEST,
                simulatingNetworkLatencies,
                simulatedLatencyManager,
                tcpListeningThreadFactory,
                haveAFakeLoggable
                );
    }
    
    public ATCPLoggableMessageDest(
            int userIndex,
            LocalMessageDest localMessageDest,
            WindowsThreadPriority windowsPriority,
            WindowsThreadPriority receiveThreadWindowsPriority,
            boolean simulatingNetworkLatencies,
            SimulatedLatencyManager simulatedLatencyManager,
            TCPListeningThreadFactory tcpListeningThreadFactory,
            boolean haveAFakeLoggable
            ) {
        super( 
                localMessageDest,
                windowsPriority,
                receiveThreadWindowsPriority,
                tcpListeningThreadFactory
                );
        m_userIndex = userIndex;
        m_simulatingNetworkLatencies = simulatingNetworkLatencies;
        m_haveAFakeLoggable = haveAFakeLoggable;
        if ( m_simulatingNetworkLatencies ) {
            m_simulatedLatencyManager = simulatedLatencyManager;
            m_messagesDeltaQueue = new AnImprovedDeltaQueue();
            m_messagesDeltaQueue.begin();
        }
    }
    
    public void queueMsg(
            Message msg
            ) {
        
        if ( m_haveAFakeLoggable ) {
            return;
        }
        
        if ( m_simulatingNetworkLatencies == false ) {
            super.queueMsg( msg );
            return;
        }
        
        CommandMessage cmdMsg = (CommandMessage) msg;

        if ( m_performanceOptimizationClient != null ) {

            if ( cmdMsg.isMsgForLatecomerOrNewMaster() == false  &&
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
        
        int senderUserIndex = cmdMsg.getSenderUserIndex();
        int simulatedLatency = (int) m_simulatedLatencyManager.getLatency(
                senderUserIndex,
                m_userIndex
                );
        
        /* 
         * TODO: Fix hack
         * This is a temp hack until we figure out why our delta queue sometimes causes
         * our program to hang because it doesn't release previous messages.
         */
        try {
            if ( cmdMsg.isMsgForLatecomerOrNewMaster() == false ) {
                if ( simulatedLatency != 0 && simulatedLatency != 100000 && simulatedLatency != 50000 ) {
                    Thread.sleep( simulatedLatency );
                }
            }
        }
        catch ( InterruptedException e ) {}
        
        m_messagesDeltaQueue.putDirect( cmdMsg );
        
//        cmdMsg.setNetworkLatencyDelay( simulatedLatency );
////        System.out.println( "Simulating latency: sender = " + senderUserIndex + " dest = " + m_userIndex + " latency = " + simulatedLatency );
//        
//        if ( simulatedLatency == 0 || simulatedLatency == 100000 || simulatedLatency == 50000 ) {
//            m_messagesDeltaQueue.putDirect( cmdMsg );
//            return;
//        }
//        
//        m_messagesDeltaQueue.put( cmdMsg );
    }
    
    public void run() {
        
        if ( m_simulatingNetworkLatencies == false ) {
            super.run();
            return;
        }
        
        for (;;) {
            CommandMessage cmdMsg = m_messagesDeltaQueue.take();
            deliverMsg( cmdMsg );
        }
    }

}
