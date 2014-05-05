package selfoptsys.perf;

import java.net.InetAddress;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface NetworkLatencyCollectorClient 
    extends Remote {

    NetworkLatencyCollectorClient getRmiStub() throws RemoteException;
    
    List<Integer> measureNetworkLatenciesToDestinations(
            List<InetAddress> destinations
            ) throws RemoteException;
    
}
