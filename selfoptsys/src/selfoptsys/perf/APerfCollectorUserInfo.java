package selfoptsys.perf;


public class APerfCollectorUserInfo 
    implements PerfCollectorUserInfo {

    protected int m_userIndex;
    protected boolean m_collectStatsForUser;
    protected ComputerInfo m_computerInfo;
    
    protected int m_coreUsedByProcessingThread;
    protected int m_coreUsedByTransmissionThread;
    protected boolean m_procAndTransCoresShareThreads;
    protected int m_quantumSize;
    
    public APerfCollectorUserInfo(
            int userIndex
            ) {
        m_userIndex = userIndex;
    }
    
    public int getUserIndex() {
        return m_userIndex;
    }
    
    public void setComputerInfo(
            ComputerInfo computerInfo
            ) {
        m_computerInfo = computerInfo;
    }
    public ComputerInfo getComputerInfo() {
        return m_computerInfo;
    }
    
    public void setCollectStatsForUser(
            boolean collectStatsForUser
            ) {
        m_collectStatsForUser = collectStatsForUser;
    }
    public boolean getCollectStatsForUser() {
        return m_collectStatsForUser;
    }
    
    public void setCoreUsedByProcessingThread(
            int coreUsedByProcessingThread
            ) {
        m_coreUsedByProcessingThread = coreUsedByProcessingThread;
    }
    public int getCoreUsedByProcessingThread() {
        return m_coreUsedByProcessingThread;
    }
    
    public void setCoreUsedByTransmissionThread(
            int coreUsedByTransmissionThread
            ) {
        m_coreUsedByTransmissionThread = coreUsedByTransmissionThread;
    }
    public int getCoreUsedByTransmissionThread() {
        return m_coreUsedByTransmissionThread;
    }
    
    public void setProcAndTransThreadsShareCores(
            boolean procAndTransThreadsShareCores
            ) {
        m_procAndTransCoresShareThreads = procAndTransThreadsShareCores;
    }
    public boolean getProcAndTransThreadsShareCores() {
        return m_procAndTransCoresShareThreads;
    }
    
    public void setQuantumSize(
            int quantumSize
            ) {
        m_quantumSize = quantumSize;
    }
    public int getQuantumSize() {
        return m_quantumSize;
    }
    
}
