package selfoptsys;


public interface LoggerFactory {

    Logger createLogger( 
            Loggable myLoggable,
            String registryHost,
            int registryPort,
            boolean haveAFakeLoggable,
            boolean myUserInputsCommands,
            boolean runningUIAsMaster,
            boolean waitForUserToScheduleTasks
            );
}
