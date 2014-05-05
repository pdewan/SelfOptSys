package selfoptsys;

import java.util.*;

import selfoptsys.comm.*;

public interface SchedulingLogger {

    void processInputMsg( CommandMessage msg );
    void processOutputMsg( CommandMessage msg );
    
    void forwardInputMsg( CommandMessage msg );
    void forwardOutputMsg( CommandMessage msg );
    
    ALoggerSysConfigInfo getLoggerSysConfig(
            int sysConfigVersion
            );
    
    int getUserIndex();
    
    Vector<Integer> getTransDests(
    		int sourceUserIndex,
			MessageType msgType,
			int sysConfigVersion
			);
    
}
