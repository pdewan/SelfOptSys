package selfoptsys.comm;

import selfoptsys.*;


public class ANonBlockingTCPLoggableMessageForwarderFactory 
    implements LoggableMessageForwarderFactory {

    public LoggableMessageForwarder createMessageForwarder(
            int userIndex,
            boolean temporaryForwarderForNewMasterOrLatecomer,
            boolean autoReportTransCosts,
            LocalLogger logger
            ) {
        return new ANonBlockingTCPLoggableMessageForwarder(
                userIndex,
                temporaryForwarderForNewMasterOrLatecomer,
                autoReportTransCosts,
                logger
                );
    }
    
}
