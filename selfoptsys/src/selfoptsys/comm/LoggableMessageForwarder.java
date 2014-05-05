package selfoptsys.comm;

import java.util.*;

import selfoptsys.perf.*;
import commonutils.config.*;

public interface LoggableMessageForwarder 
	extends MessageForwarder {

	void addDest(
    		int rootUserIndex,
    		int userIndex,
    		MessageDest msgDest
    		);
	
	void removeDest(
    		int rootUserIndex,
    		int userIndex
    		);
	
    void setCommunicationArchitectureType( 
    		CommunicationArchitectureType commArchType 
    		);
    
    void setPerformanceOptimizationClient(
    		PerformanceOptimizationClient performanceOptimizationClient
    		);
    
    Vector<Integer> getDestsForSourceUserIndex(
    		int sourceUserIndex
    		);

}
