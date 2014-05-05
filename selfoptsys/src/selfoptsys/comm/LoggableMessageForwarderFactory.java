package selfoptsys.comm;

import selfoptsys.*;


public interface LoggableMessageForwarderFactory {

    LoggableMessageForwarder createMessageForwarder(
            int userIndex,
            boolean temporaryForwarderForNewMasterOrLatecomer,
            boolean autoReportTransCosts,
            LocalLogger logger
            );
    
}
