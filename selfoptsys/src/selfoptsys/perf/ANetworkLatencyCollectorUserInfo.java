package selfoptsys.perf;

import java.net.InetAddress;


public class ANetworkLatencyCollectorUserInfo 
    implements NetworkLatencyCollectorUserInfo {

    protected int m_userIndex;
    protected NetworkLatencyCollectorClient m_client;
    protected InetAddress m_host;
    
    public ANetworkLatencyCollectorUserInfo(
            int userIndex,
            NetworkLatencyCollectorClient client,
            InetAddress host
            ) {
        m_userIndex = userIndex;
        m_client = client;
        m_host = host;
    }
    
    public int getUserIndex() {
        return m_userIndex;
    }
    
    public NetworkLatencyCollectorClient getClient() {
        return m_client;
    }
    
    public InetAddress getHost() {
        return m_host;
    }
    
}
