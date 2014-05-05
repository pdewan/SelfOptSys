package selfoptsys.comm;

import java.rmi.*;

import selfoptsys.perf.*;

public interface MessageDest 
	extends Remote {

    MessageDest getRmiStub() throws RemoteException;
    
    void queueMsg( Message msg ) throws RemoteException;
    void resetMsgDest() throws RemoteException;
    
    void setPerformanceOptimizationClient(
            PerformanceOptimizationClient performanceOptimizationClient
            ) throws RemoteException;
	
}
