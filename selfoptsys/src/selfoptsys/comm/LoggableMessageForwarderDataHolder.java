package selfoptsys.comm;

import java.util.*;

import selfoptsys.LocalLogger;
import selfoptsys.perf.*;
import commonutils.config.*;


public interface LoggableMessageForwarderDataHolder {

    void addDest(
            int rootUserIndex,
            int userIndex,
            MessageDest msgDest
            );
    
    void removeDest(
            int rootUserIndex,
            int userIndex
            );
    
    Vector<Integer> getDestsForSourceUserIndex(
            int userIndex
            );
    
    boolean isUserNewRegisteredWithOverlayForRootUserIndex(
            int rootUserIndex,
            int userIndex
            );
    
    boolean isUserADestination(
            int userIndex
            );
    
    void setCommunicationArchitectureType( 
            CommunicationArchitectureType commArchType 
            );
    CommunicationArchitectureType getCommunicationArchitectureType();
    
    void setPerformanceOptimizationClient(
            PerformanceOptimizationClient performanceOptimizationClient 
            );
    
    void reportTransInfoToPerfCollector(
            MessageType messageType,
            int sysConfigVersion,
            int cmdSourceUserIndex,
            int seqId,
            long transmissionTime,
            long transmissionTimeToFirstDest,
            int numDestsTransmittedTo,
            int lastDestTransmittedTo,
            int coreNum,
            boolean reportTransTime
            );
    
    LoggableMessageForwarderLazyPolicyPerMessageDataHolder getLazyPolicyPerMessageDataHolder(
    		LocalLogger logger,
    		CommandMessage cmdMsg
    		);
    
    void removeLazyPolicyPerMessageDataHolder(
    		CommandMessage cmdMsg
    		);
}
