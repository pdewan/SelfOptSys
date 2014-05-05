package selfoptsys.perf;


public interface PerfCollectorUserInfo {

    int getUserIndex();
    
    void setComputerInfo(
            ComputerInfo computerInfo
            );
    ComputerInfo getComputerInfo();
    
    void setCollectStatsForUser(
            boolean collectStatsForUser
            );
    boolean getCollectStatsForUser();
    
    void setCoreUsedByProcessingThread(
            int coreUsedByProcessingThread
            );
    int getCoreUsedByProcessingThread();
    
    void setCoreUsedByTransmissionThread(
            int coreUsedByTransmissionThread
            );
    int getCoreUsedByTransmissionThread();
    
    void setProcAndTransThreadsShareCores(
            boolean procAndTransThreadsShareCores
            );
    boolean getProcAndTransThreadsShareCores();
    
    void setQuantumSize(
            int quantumSize
            );
    int getQuantumSize();
    
}
