package selfoptsysapps.demo;

public interface DemoStarter {

	void setSessionRegistryHost(
			String sessionRegistryHost
			);
	String getSessionRegistryHost();
	
	void setSessionRegistryPort(
			int sessionRegistryPort
			);
	int getSessionRegistryPort();
	
	void setConfigFile(
			String configFile
			);
	String getConfigFile();
	
	void startSessionRegistry();
	void startPerfOptimizer();
	
	void startCheckersLoggers(
			int startUserIndex,
			int endUserIndex,
			boolean usersInputCommands
			);
	
	void startCheckersLoggersAsMasters(
			int startUserIndex,
			int endUserIndex,
			boolean usersInputCommands
			);
	
	void startCheckersLoggersAsSlaves(
			int startUserIndex,
			int endUserIndex,
			int masterUserIndex,
			boolean usersInputCommands
			);
	
//	void startPerfDisplayManager();

}
