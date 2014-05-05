package selfoptsys.comm;

public interface TimeServerReportMessage 
	extends Message {
    
    void setSeqId(
            int seqId
            );
    int getSeqId();
    
    void setUserIndex(
            int userIndex
            );
    int getUserIndex();

    void setCmdSourceUserIndex(
            int cmdSourceUserIndex
            );
    int getCmdSourceUserIndex();

    void setEventTime(
            long eventTime
            );
    long getEventTime();
}
