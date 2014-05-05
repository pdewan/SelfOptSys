package selfoptsys.systemapps.recordingmanager;

import util.models.AListenableVector;

public interface RecordingManagerUI {

	void updateString(
			String newHistoryEntry 
			);
	
	AListenableVector<String> getHistory();
	
}
