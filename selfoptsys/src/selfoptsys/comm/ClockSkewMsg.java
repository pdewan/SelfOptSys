package selfoptsys.comm;


public interface ClockSkewMsg 
    extends Message {

    int getUserIndex();
    
    void setSendTimeAtSender(
            long sendTimeAtSender
            );
    long getSendTimeAtSender();
    
    void setTimeElapsedSinceLastSendOnSender(
            long timeElapsedSinceLastSendOnSender
            );
    long getTimeElapsedSinceLastSendOnSender();
    
    void setPrevMsgDelayTime(
            long prevMsgSendTime
            );
    long getPrevMsgDelayTime();
    
    void setReceivedTime(
            long receivedTime
            );
    long getReceivedTime();
}
