package selfoptsys.overlay;

import java.util.Vector;

public interface SingleSourceOverlay {

	int getSourceUserIndex();
	int getMasterIndexOfSource();
	
	void userJoiningAsMaster(
			int joiningUserIndex
			);
	
	void userJoiningAsSlave(
			int joiningUserIndex,
			int masterIndexOfJoiningUser
			);
	
	void userLeaving(
			int leavingUserIndex
			);
	
	Vector<Integer> getInputDestMappingsForUser(
			int userIndex
			);
	void setInputDestMappingsForUser(
			int userIndex,
			Vector<Integer> inputDestMappings
			);

	Vector<Integer> getInputSourceMappingsForUser(
			int userIndex
			);
	void setInputSourceMappingsForUser(
			int userIndex,
			Vector<Integer> inputSourceMappings
			);

	Vector<Integer> getOutputDestMappingsForUser(
			int userIndex
			);
	void setOutputDestMappingsForUser(
			int userIndex,
			Vector<Integer> outputDestMappings
			);

	Vector<Integer> getOutputSourceMappingsForUser(
			int userIndex
			);
	void setOutputSourceMappingsForUser(
			int userIndex,
			Vector<Integer> outputSourceMappings
			);
	
	void removeAllMappingsForUser(
			int userIndex
			);
	
}
