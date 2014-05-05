package selfoptsys.comm;

import java.util.*;

import selfoptsys.LocalLogger;
import selfoptsys.perf.*;
import commonutils.config.*;


public class ALoggableMessageForwarderDataHolder
        implements LoggableMessageForwarderDataHolder {
    
    protected int m_userIndex;
    protected Map<Integer, Vector<Integer>> m_singleSourceOverlayDests;

    protected PerformanceOptimizationClient m_performanceOptimizationClient;

    protected CommunicationArchitectureType m_commArchType;
    
    protected Map<Integer, Map<Integer, LoggableMessageForwarderLazyPolicyPerMessageDataHolder>> m_lazyPolicyPerMessageDataHolders;
    
    public ALoggableMessageForwarderDataHolder(
            int userIndex
            ) {
        m_userIndex = userIndex;

        m_singleSourceOverlayDests = new Hashtable<Integer, Vector<Integer>>();
        
        m_lazyPolicyPerMessageDataHolders = new Hashtable<Integer, Map<Integer,LoggableMessageForwarderLazyPolicyPerMessageDataHolder>>();
    }
    
    public void addDest(
            int rootUserIndex,
            int userIndex,
            MessageDest msgDest
            ) {
        Vector<Integer> singleSourceOverlayDests = m_singleSourceOverlayDests.get( rootUserIndex );
        if ( singleSourceOverlayDests == null ) {
            singleSourceOverlayDests = new Vector<Integer>();
            m_singleSourceOverlayDests.put(
                    rootUserIndex,
                    singleSourceOverlayDests
                    );
        }
        singleSourceOverlayDests.addElement( userIndex );
    }
    
    public void removeDest(
            int rootUserIndex,
            int userIndex
            ) {
        Vector<Integer> dests = m_singleSourceOverlayDests.get( rootUserIndex );
        dests.removeElement( userIndex );
    }
    
    public boolean isUserNewRegisteredWithOverlayForRootUserIndex(
            int rootUserIndex,
            int userIndex
            ) {
        if ( m_singleSourceOverlayDests.get( rootUserIndex ) != null ) {
            if ( m_singleSourceOverlayDests.get( rootUserIndex ).contains( userIndex ) ) {
                return true;
            }
        }
        
        return false;
    }
    
    public Vector<Integer> getDestsForSourceUserIndex(
            int userIndex
            ) {
        return m_singleSourceOverlayDests.get( userIndex );
    }
    
    public boolean isUserADestination(
            int userIndex
            ) {
        boolean isDestination = false;
        
        Iterator<Map.Entry<Integer, Vector<Integer>>> singleSourceOverlayDestIterator =
            m_singleSourceOverlayDests.entrySet().iterator();
        while ( singleSourceOverlayDestIterator.hasNext() ) {
            Vector<Integer> dests = singleSourceOverlayDestIterator.next().getValue();
            if ( dests.contains( userIndex ) ) {
                isDestination = true;
                break;
            }
        }
        
        return isDestination;
    }
    
    public void reportTransInfoToPerfCollector(
            MessageType messageType,
            int sysConfigVersion,
            int cmdSourceUserIndex,
            int seqId,
            long transmissionTime,
            long tranmsissionTimeToFirstDest,
            int numDestsTransmittedTo,
            int lastDestTransmittedTo,
            int coreNum,
            boolean reportTransTime
            ) {
        if ( m_performanceOptimizationClient != null ) {
            m_performanceOptimizationClient.sendPerformanceReport(
                    messageType,
                    sysConfigVersion,
                    cmdSourceUserIndex,
                    seqId,
                    0,
                    transmissionTime,
                    tranmsissionTimeToFirstDest,
                    numDestsTransmittedTo,
                    lastDestTransmittedTo,
                    0,
                    coreNum,
                    false,
                    reportTransTime
                    );
        }
    }

    public void setCommunicationArchitectureType(
            CommunicationArchitectureType commArchType
            ) {
        m_commArchType = commArchType;
    }
    public CommunicationArchitectureType getCommunicationArchitectureType() {
        return m_commArchType;
    }
        
    public void setPerformanceOptimizationClient(
            PerformanceOptimizationClient performanceOptimizationClient 
            ) {
        m_performanceOptimizationClient = performanceOptimizationClient;
    }
    
    public LoggableMessageForwarderLazyPolicyPerMessageDataHolder getLazyPolicyPerMessageDataHolder(
    		LocalLogger logger,
    		CommandMessage cmdMsg
    		) {
        Map<Integer, LoggableMessageForwarderLazyPolicyPerMessageDataHolder> sourceUserLazyPolicyPerMessageDataHolders = 
        	m_lazyPolicyPerMessageDataHolders.get( cmdMsg.getSourceUserIndex() );
        if ( sourceUserLazyPolicyPerMessageDataHolders == null ) {
        	sourceUserLazyPolicyPerMessageDataHolders = new Hashtable<Integer, LoggableMessageForwarderLazyPolicyPerMessageDataHolder>();
        	m_lazyPolicyPerMessageDataHolders.put(
        			cmdMsg.getSourceUserIndex(),
        			sourceUserLazyPolicyPerMessageDataHolders
        			);
        }
        LoggableMessageForwarderLazyPolicyPerMessageDataHolder curMessageLazyPolicyPerMessageDataHolder = 
        	sourceUserLazyPolicyPerMessageDataHolders.get( cmdMsg.getSeqId() );
        if ( curMessageLazyPolicyPerMessageDataHolder == null ) {
        	curMessageLazyPolicyPerMessageDataHolder = new ALoggableMessageForwarderLazyPolicyPerMessageDataHolder(
        			logger,
        			cmdMsg
        			);
        	sourceUserLazyPolicyPerMessageDataHolders.put(
        			cmdMsg.getSeqId(),
        			curMessageLazyPolicyPerMessageDataHolder
        			);
        	curMessageLazyPolicyPerMessageDataHolder.init();
        }

        return curMessageLazyPolicyPerMessageDataHolder;
    }
    
    public void removeLazyPolicyPerMessageDataHolder(
    		CommandMessage cmdMsg
    		) {
        Map<Integer, LoggableMessageForwarderLazyPolicyPerMessageDataHolder> sourceUserLazyPolicyPerMessageDataHolders = 
        	m_lazyPolicyPerMessageDataHolders.get( cmdMsg.getSourceUserIndex() );
        if ( sourceUserLazyPolicyPerMessageDataHolders != null ) {
            sourceUserLazyPolicyPerMessageDataHolders.remove( cmdMsg.getSeqId() );
        }
    }
    
}
