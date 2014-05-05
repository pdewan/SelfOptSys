package selfoptsysapps.demo;

import selfoptsys.*;

public class AUserControlledLoggerFactory 
    implements LoggerFactory {

    public Logger createLogger( 
            Loggable myLoggable,
            String registryHost,
            int registryPort,
            boolean haveAFakeLoggable,
            boolean myUserInputsCommands,
            boolean runningUIAsMaster,
            boolean waitForUserToScheduleTasks
            ) {
        return new AUserControlledLogger(
                myLoggable,
                registryHost,
                registryPort,
                haveAFakeLoggable,
                myUserInputsCommands,
                runningUIAsMaster,
                waitForUserToScheduleTasks
                );
    }
    
}
