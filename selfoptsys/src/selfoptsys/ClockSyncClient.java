package selfoptsys;

import java.rmi.Remote;

public interface ClockSyncClient 
    extends Remote {

    void sync();
    
}
