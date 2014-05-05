package selfoptsys;

import java.util.*;
import commonutils.config.*;
import selfoptsys.comm.*;
import selfoptsys.perf.*;


public class ALoggerSysConfigInfo {

    public int SysConfigVersion;
    
    public boolean IsMaster;
    public int MasterUserIndex;
    
    public Vector<CommandMessage> InputCommandHistory;
    public Vector<CommandMessage> OutputCommandHistory;
    
    public LoggableMessageForwarder InputForwarder;
    public LoggableMessageForwarder OutputForwarder;
    public MessageDest InputDest;
    public MessageDest OutputDest;
    public MessageDest InputDestRmiStub;
    public MessageDest OutputDestRmiStub;
    
    public HashMap<PerformanceParameterType, Double> ObservedTransCosts;
    
    public SchedulingPolicy SchedulingPolicy;
    public int CoreUsedByTransmissionThread;
    public int CoreUsedByProcessingThread;
    
    public static ALoggerSysConfigInfo copy(
            ALoggerSysConfigInfo other
            ) {
        ALoggerSysConfigInfo copy = new ALoggerSysConfigInfo();
        
        if ( other != null ) {
            copy.SysConfigVersion = other.SysConfigVersion;
            copy.IsMaster = other.IsMaster;
            copy.MasterUserIndex = other.MasterUserIndex;
            copy.InputCommandHistory = other.InputCommandHistory;
            copy.OutputCommandHistory = other.OutputCommandHistory;
            copy.InputForwarder = other.InputForwarder;
            copy.OutputForwarder = other.OutputForwarder;
            copy.InputDest = other.InputDest;
            copy.OutputDest = other.OutputDest;
            copy.InputDestRmiStub = other.InputDestRmiStub;
            copy.OutputDestRmiStub = other.OutputDestRmiStub;
            copy.SchedulingPolicy = other.SchedulingPolicy;
            copy.ObservedTransCosts = new HashMap<PerformanceParameterType, Double>();
            if ( other.ObservedTransCosts != null ) {
                Iterator<Map.Entry<PerformanceParameterType, Double>> observedTransTimeItr = other.ObservedTransCosts.entrySet().iterator();
                while ( observedTransTimeItr.hasNext() ) {
                    Map.Entry<PerformanceParameterType, Double> entry = observedTransTimeItr.next();
                    copy.ObservedTransCosts.put(
                            entry.getKey(),
                            entry.getValue()
                            );
                }
            }
            copy.CoreUsedByProcessingThread = other.CoreUsedByProcessingThread;
            copy.CoreUsedByTransmissionThread = other.CoreUsedByTransmissionThread;
        }
        
        return copy;
    }
    
}
