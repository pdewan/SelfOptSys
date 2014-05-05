package selfoptsys.comm;

import java.util.*;
import commonutils.config.*;

public class ACommandMessage 
	extends AMessage 
	implements CommandMessage {

	private static final long serialVersionUID = 2047326364924497044L;

    protected int m_seqId = 0;
    protected int m_sourceUserIndex = 0;
    protected int m_senderUserIndex = 0;
	protected int m_sysConfigVersion = 0;
	protected boolean m_isMsgForLatecomerOrNewMaster = false;
	protected Vector<Integer> m_destUserIndices;
	protected Object m_data;
	protected SchedulingPolicy m_suggestedSchedulingPolicy = SchedulingPolicy.UNDEFINED;
    protected boolean m_reportTransCostForMessage = false;
    protected boolean m_reportProcCostForMessage = false;
    protected boolean m_reportReceiveTime = false;
    transient protected double m_delaySoFar = 0;
	
    protected transient int m_networkLatencyDelay = 0;

    public ACommandMessage(
    		MessageType msgType,
            int sourceUserIndex,
            int seqId,
            int senderUserIndex,
            int sysConfigVersion,
            boolean isMsgForLatecomerOrNewMaster,
            Vector<Integer> destUserIndices,
            Object data,
            SchedulingPolicy suggestedSchedulingPolicy,
            boolean reportProcCostForMessage,
            boolean reportTransCostForMessage
            ) {
    	super( msgType );
    	
        m_sourceUserIndex = sourceUserIndex;
    	m_seqId = seqId;
    	m_senderUserIndex = senderUserIndex;
    	m_sysConfigVersion = sysConfigVersion;
    	m_isMsgForLatecomerOrNewMaster = isMsgForLatecomerOrNewMaster;
        m_destUserIndices = destUserIndices;
    	m_data = data;
        m_suggestedSchedulingPolicy = SchedulingPolicy.UNDEFINED;
        m_reportProcCostForMessage = reportProcCostForMessage;
        m_reportTransCostForMessage = reportTransCostForMessage;
    }
    
    public void setSeqId(
            int seqId
            ) {
        m_seqId = seqId;
    }
    public int getSeqId() {
        return m_seqId;
    }
    
    public void setSourceUserIndex(
            int sourceUserIndex
            ) {
        m_sourceUserIndex = sourceUserIndex;
    }
    public int getSourceUserIndex() {
        return m_sourceUserIndex;
    }

    public void setSenderUserIndex(
            int senderUserIndex
            ) {
        m_senderUserIndex = senderUserIndex;
    }
    public int getSenderUserIndex() {
        return m_senderUserIndex;
    }

    public void setSysConfigVersion(
            int sysConfigVersion
            ) {
        m_sysConfigVersion = sysConfigVersion;
    }
    public int getSysConfigVersion() {
        return m_sysConfigVersion;
    }
	
	public void isMsgForLatecomerOrNewMaster(
			boolean isMsgForLatecomerOrNewMaster
			) {
		m_isMsgForLatecomerOrNewMaster = isMsgForLatecomerOrNewMaster;
	}
	public boolean isMsgForLatecomerOrNewMaster() {
		return m_isMsgForLatecomerOrNewMaster;
	}
	
	public Vector<Integer> getDestUserIndices() {
		return m_destUserIndices;
	}
	
	public Object getData() {
		return m_data;
	}
	public void setData( Object newData ) {
		m_data = newData;
	}
	
	public void setSuggestedSchedulingPolicy(
	        SchedulingPolicy newSuggestedSchedulingPolicy
	        ) {
	    m_suggestedSchedulingPolicy = newSuggestedSchedulingPolicy;
	}
	public SchedulingPolicy getSuggestedSchedulingPolicyForMsg() {
	    return m_suggestedSchedulingPolicy;
	}
	
    public void setReportTransCostForMessage(
            boolean reportTransCostForMessage
            ) {
        m_reportTransCostForMessage = reportTransCostForMessage;
    }
    public boolean getReportTransCostForMessage() {
        return m_reportTransCostForMessage;
    }

    public void setReportProcCostForMessage(
            boolean reportProcCostForMessage
            ) {
        m_reportProcCostForMessage = reportProcCostForMessage;
    }
    public boolean getReportProcCostForMessage() {
        return m_reportProcCostForMessage;
    }

    public int getNetworkLatencyDelay() {
        return m_networkLatencyDelay;
    }
    public void setNetworkLatencyDelay(
            int networkLatencyDelay
            ) {
        m_networkLatencyDelay = networkLatencyDelay;
    }
    
    public void setDelaySoFar(
            double delaySoFar
            ) {
        m_delaySoFar = delaySoFar;
    }
    public double getDelaySoFar() {
        return m_delaySoFar;
    }
    
    public void setReportReceiveTime(
            boolean reportReceiveTime
            ) {
        m_reportReceiveTime = reportReceiveTime;
    }
    public boolean getReportReceiveTime() {
        return m_reportReceiveTime;
    }

    public static CommandMessage copy( CommandMessage msg ) {
    	CommandMessage msgCopy = new ACommandMessage(
    			msg.getMessageType(),
                msg.getSourceUserIndex(),
                msg.getSeqId(),
                msg.getSenderUserIndex(),
                msg.getSysConfigVersion(),
                msg.isMsgForLatecomerOrNewMaster(),
    			msg.getDestUserIndices(),
                msg.getData(),
    			msg.getSuggestedSchedulingPolicyForMsg(),
    			msg.getReportProcCostForMessage(),
    			msg.getReportTransCostForMessage()
    			);
    	msgCopy.setDelaySoFar( msg.getDelaySoFar() );
    	msgCopy.setReportReceiveTime( msg.getReportReceiveTime() );
    	return msgCopy;
    }
}
