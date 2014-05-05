package selfoptsys;

import java.io.Serializable;

public class AReplayUserInfo 
    implements ReplayUserInfo, Serializable {

    private static final long serialVersionUID = 2910570319852785394L;
    
    protected int m_userIndex;
    protected boolean m_measurePerformance;
    
    public AReplayUserInfo(
            int userIndex,
            boolean measurePerformance
            ) {
        m_userIndex = userIndex;
        m_measurePerformance = measurePerformance;
    }
    
    public int getUserIndex() {
        return m_userIndex;
    }
    
    public boolean getMeasurePerformance() {
        return m_measurePerformance;
    }
    
}
