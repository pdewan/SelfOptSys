package selfoptsys.comm;

import selfoptsys.*;


public class ASerializedObjectTCPLoggableMessageForwarderFactory 
    implements LoggableMessageForwarderFactory {

    public LoggableMessageForwarder createMessageForwarder(
            int userIndex,
            boolean temporaryForwarderForNewMasterOrLatecomer,
            boolean autoReportTransCosts,
            LocalLogger logger
            ) {
        return new ASerializedObjectTCPLoggableMessageForwarder(
                userIndex,
                temporaryForwarderForNewMasterOrLatecomer,
                autoReportTransCosts,
                logger
                );
    }
    
}
