package selfoptsys.perf;

import java.net.*;


public interface NetworkLatencyCollectorUserInfo {
    
    int getUserIndex();
    NetworkLatencyCollectorClient getClient();
    InetAddress getHost();

}
