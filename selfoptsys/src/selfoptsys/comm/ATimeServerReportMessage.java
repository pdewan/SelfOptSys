package selfoptsys.comm;

public class ATimeServerReportMessage 
	extends AMessage
	implements TimeServerReportMessage {

	private static final long serialVersionUID = 6172430250195246371L;

	protected int m_seqId = 0;
	protected int m_userIndex = 0;
	protected int m_cmdSourceUserIndex = 0;
    protected long m_eventTime = 0;

    public ATimeServerReportMessage(
            MessageType msgType,
            int userIndex,
            int cmdSourceUserIndex,
            int seqId,
            long eventTime
			) {
		super( msgType );
		
		m_userIndex = userIndex;
		m_cmdSourceUserIndex = cmdSourceUserIndex;
		m_seqId = seqId;
		m_eventTime = eventTime;
	}
    
    public void setUserIndex(
            int userIndex
            ) {
        m_userIndex = userIndex;
    }
    public int getUserIndex() {
        return m_userIndex;
    }

    public void setCmdSourceUserIndex(
            int cmdSourceUserIndex
            ) {
        m_cmdSourceUserIndex = cmdSourceUserIndex;
    }
    public int getCmdSourceUserIndex() {
        return m_cmdSourceUserIndex;
    }
    
    public void setSeqId(
            int seqId
            ) {
        m_seqId = seqId;
    }
    public int getSeqId() {
        return m_seqId;
    }
    
    public void setEventTime(
            long eventTime
            ) {
        m_eventTime = eventTime;
    }
    public long getEventTime() {
        return m_eventTime;
    }
}
