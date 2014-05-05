package selfoptsys.systemapps.recordingmanager;

import bus.uigen.*;

public class ARecordingManagerStarter 
	implements RecordingManagerStarter {

	protected RecordingManagerUI m_rmUI;
	
	protected String m_registryHost = null;
	protected int m_registryPort = 0;

	// NOTE: When using ObjectEditor, concurrent edit operations
	// fail. Hence, the edit in this class is done in the constructor,
	// which is a synchronous call.
	// Had we done the edit operation in the run() method, then OE
	// fails with an exception because this edit operation would run 
	// concurrent to the edit operation in SessionRegistryUI.
	public ARecordingManagerStarter(
			String registryHost,
			int registryPort
			) {
		m_registryHost = registryHost;
		m_registryPort = registryPort;
		m_rmUI = new ARecordingManagerUI();
	}
	
	public void run() {
		RecordingManager rm = new ARecordingManager(
				RECORDING_LOGGABLE_USER_INDEX,
				m_registryHost,
				m_registryPort
				);
		rm.setUI(m_rmUI);
		ObjectEditor.edit(m_rmUI);
	}
}

