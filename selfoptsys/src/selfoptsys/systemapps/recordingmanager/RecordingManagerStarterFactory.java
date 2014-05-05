package selfoptsys.systemapps.recordingmanager;

public interface RecordingManagerStarterFactory {

	RecordingManagerStarter createScheduler(
			String registryHost,
			int registryPort
			);
	
}
