package selfoptsys;

import java.util.Hashtable;
import java.util.Map;


public class ALoggableUserInfo {

    protected int m_userIndex;
    
    protected Map<Integer, Map<Integer, Long>> m_inputProcessingTimes;
    protected Map<Integer, Map<Integer, Long>> m_inputTransmissionTimes;
    protected Map<Integer, Map<Integer, Long>> m_outputProcessingTimes;
    protected Map<Integer, Map<Integer, Long>> m_outputTransmissionTimes;
    protected Map<Integer, Long> m_thinkTimes;

    public ALoggableUserInfo(
            int userIndex
            ) {
        m_userIndex = userIndex;
        
        m_inputProcessingTimes = new Hashtable<Integer, Map<Integer,Long>>();
        m_inputTransmissionTimes = new Hashtable<Integer, Map<Integer,Long>>();
        m_outputProcessingTimes = new Hashtable<Integer, Map<Integer,Long>>();
        m_outputTransmissionTimes = new Hashtable<Integer, Map<Integer,Long>>();
        m_thinkTimes = new Hashtable<Integer, Long>();
    }
    
    public int getUserIndex() {
        return m_userIndex;
    }
    
    public void addInputProcessingTime(
            int cmdSourceUserIndex,
            int seqId,
            long duration
            ) {
        Map<Integer, Long> inputProcTimes = m_inputProcessingTimes.get( cmdSourceUserIndex );
        if ( inputProcTimes == null ) {
            inputProcTimes = new Hashtable<Integer, Long>();
            m_inputProcessingTimes.put(
                    cmdSourceUserIndex,
                    inputProcTimes
                    );
        }
        inputProcTimes.put(
                seqId,
                duration
                );
    }
    
    public void addInputTransmissionTime(
            int cmdSourceUserIndex,
            int seqId,
            long duration
            ) {
        Map<Integer, Long> inputTransTimes = m_inputTransmissionTimes.get( cmdSourceUserIndex );
        if ( inputTransTimes == null ) {
            inputTransTimes = new Hashtable<Integer, Long>();
            m_inputTransmissionTimes.put(
                    cmdSourceUserIndex,
                    inputTransTimes
                    );
        }
        inputTransTimes.put(
                seqId,
                duration
                );
    }
    
    public void addOutputProcessingTime(
            int cmdSourceUserIndex,
            int seqId,
            long duration
            ) {
        Map<Integer, Long> outputProcTimes = m_outputProcessingTimes.get( cmdSourceUserIndex );
        if ( outputProcTimes == null ) {
            outputProcTimes = new Hashtable<Integer, Long>();
            m_outputProcessingTimes.put(
                    cmdSourceUserIndex,
                    outputProcTimes
                    );
        }
        outputProcTimes.put(
                seqId,
                duration
                );
    }
    
    public void addOutputTransmissionTime(
            int cmdSourceUserIndex,
            int seqId,
            long duration
            ) {
        Map<Integer, Long> outputTransTimes = m_outputTransmissionTimes.get( cmdSourceUserIndex );
        if ( outputTransTimes == null ) {
            outputTransTimes = new Hashtable<Integer, Long>();
            m_outputTransmissionTimes.put(
                    cmdSourceUserIndex,
                    outputTransTimes
                    );
        }
        outputTransTimes.put(
                seqId,
                duration
                );
    }
    
    public void addThinkTime(
            int seqId,
            long thinkTime
            ) {
        m_thinkTimes.put(
                seqId,
                thinkTime
                );
    }
    
    public long getInputProcessingTime(
            int cmdSourceUserIndex,
            int seqId
            ) {
        Map<Integer, Long> procTimes = m_inputProcessingTimes.get( cmdSourceUserIndex );
        if ( procTimes == null ) {
            return -1;
        }
        
        Long procTime = procTimes.get( seqId );
        if ( procTime == null ) {
            return -1;
        }
        
        return procTime.longValue();
    }
    
    public long getInputTransmissionTime(
            int cmdSourceUserIndex,
            int seqId
            ) {
        Map<Integer, Long> transTimes = m_inputTransmissionTimes.get( cmdSourceUserIndex );
        if ( transTimes == null ) {
            return -1;
        }
        
        Long transTime = transTimes.get( seqId );
        if ( transTime == null ) {
            return -1;
        }
        
        return transTime.longValue();
    }
    
    public long getOutputProcessingTime(
            int cmdSourceUserIndex,
            int seqId
            ) {
        Map<Integer, Long> procTimes = m_outputProcessingTimes.get( cmdSourceUserIndex );
        if ( procTimes == null ) {
            return -1;
        }
        
        Long procTime = procTimes.get( seqId );
        if ( procTime == null ) {
            return -1;
        }
        
        return procTime.longValue();
    }
    
    public long getOutputTransmissionTime(
            int cmdSourceUserIndex,
            int seqId
            ) {
        Map<Integer, Long> transTimes = m_outputTransmissionTimes.get( cmdSourceUserIndex );
        if ( transTimes == null ) {
            return -1;
        }
        
        Long transTime = transTimes.get( seqId );
        if ( transTime == null ) {
            return -1;
        }
        
        return transTime.longValue();
    }
    
    public long getThinkTime(
            int seqId
            ) {
        Long thinkTime = m_thinkTimes.get( seqId );
        if ( thinkTime == null ) {
            return -1;
        }
        
        return thinkTime.longValue();
    }
    
}
