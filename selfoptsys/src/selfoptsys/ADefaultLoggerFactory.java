package selfoptsys;

public class ADefaultLoggerFactory 
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
        return new ALogger(
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
