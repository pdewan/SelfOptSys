package selfoptsys.systemapps.recordingmanager;

public class ARecordingManagerStarterFactory 
	implements RecordingManagerStarterFactory {

	public RecordingManagerStarter createScheduler(
			String registryHost,
			int registryPort
			) {
		return new ARecordingManagerStarter(
				registryHost,
				registryPort
				);
	}
	
}
