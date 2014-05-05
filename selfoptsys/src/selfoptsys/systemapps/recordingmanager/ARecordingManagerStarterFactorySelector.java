package selfoptsys.systemapps.recordingmanager;

public class ARecordingManagerStarterFactorySelector {

	private static RecordingManagerStarterFactory m_factory = new ARecordingManagerStarterFactory();
	
	public static void setRecordingManagerStarterFactory(
			RecordingManagerStarterFactory factory
			) {
		m_factory = factory;
	}
	
	public static RecordingManagerStarterFactory getSchedulerFactory() {
		return m_factory;
	}
	
}
