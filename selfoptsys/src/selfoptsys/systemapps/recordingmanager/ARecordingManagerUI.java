package selfoptsys.systemapps.recordingmanager;

import util.models.AListenableVector;

public class ARecordingManagerUI 
	implements RecordingManagerUI {

	private AListenableVector<String> m_history;
	
	public ARecordingManagerUI() {
		m_history = new AListenableVector<String>();
	}
	
	public void updateString(
			String newHistoryEntry
			) {
		m_history.addElement( newHistoryEntry );
	}
	
	public AListenableVector<String> getHistory() {
		return m_history;
	}
	
}
