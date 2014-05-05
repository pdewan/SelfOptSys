package selfoptsys.perf;

import java.net.InetAddress;
import java.util.Map;

public interface NetworkLatencyCollectorServer {

    void quit();
    
    void addUserHost(
            int userIndex,
            NetworkLatencyCollectorClient client,
            InetAddress host
            );

    void removeUserHost(
            int userIndex
            );
    
    Map<Integer,Map<Integer,Double>> getCopyOfLatencies();

}
