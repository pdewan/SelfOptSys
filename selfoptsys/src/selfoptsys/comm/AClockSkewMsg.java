package selfoptsys.comm;


public class AClockSkewMsg 
    extends AMessage 
    implements ClockSkewMsg {

    private static final long serialVersionUID = 8374456121545697597L;

    protected int m_userIndex;
    
    protected long m_sendTimeAtSender;
    protected long m_timeElapsedSinceLastSendOnSender;
    protected long m_prevMsgSendTime;
    protected transient long m_receivedTime;
    
    public AClockSkewMsg(
            int userIndex
            ) {
        super( MessageType.CLOCK_SKEW_MSG );
        
        m_userIndex = userIndex;
    }
    
    public int getUserIndex() {
        return m_userIndex;
    }
    
    public void setSendTimeAtSender(
            long sendTimeAtSender
            ) {
        m_sendTimeAtSender = sendTimeAtSender;
    }
    public long getSendTimeAtSender() {
        return m_sendTimeAtSender;
    }
    
    public void setTimeElapsedSinceLastSendOnSender(
            long timeElapsedSinceLastSendOnSender
            ) {
        m_timeElapsedSinceLastSendOnSender = timeElapsedSinceLastSendOnSender;
    }
    public long getTimeElapsedSinceLastSendOnSender() {
        return m_timeElapsedSinceLastSendOnSender;
    }
    
    public void setPrevMsgDelayTime(
            long prevMsgSendTime
            ) {
        m_prevMsgSendTime = prevMsgSendTime;
    }
    public long getPrevMsgDelayTime() {
        return m_prevMsgSendTime;
    }
    
    public void setReceivedTime(
            long receivedTime
            ) {
        m_receivedTime = receivedTime;
    }
    public long getReceivedTime() {
        return m_receivedTime;
    }

}
