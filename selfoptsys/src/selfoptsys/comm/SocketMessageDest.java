package selfoptsys.comm;

import java.rmi.*;

import commonutils.misc.*;

public interface SocketMessageDest 
	extends MessageDest {

	public ConnSocketInfo getConnSocket() throws RemoteException;
	public void closeSocketConn(
	        ConnSocketInfo info
	        ) throws RemoteException;
	
}
