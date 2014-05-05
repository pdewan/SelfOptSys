package selfoptsys.overlay;

import java.util.*;

import selfoptsys.network.*;

public interface GlobalOverlayManager {

	void userJoining(
			int joiningUserIndex,
			int masterIndexOfJoiningUser,
			boolean inputsCommands
			);
	
	Overlay getCurrentOverlay(
			int rootUserIndex
			);
	
	void userLeaving(
			int leavingUserIndex
			);
	
	Vector<AnOverlayMapping> getInputSourceMappingsForUser(
			int userIndex
			);
	void setInputSourceMappingsForUser(
			int userIndex,
			Vector<AnOverlayMapping> inputSourceMappings
			);
	Vector<AnOverlayMapping> getInputSourceMappingsForUser(
			int userIndex,
			int rootUserIndex
			);
	
	Vector<AnOverlayMapping> getInputDestMappingsForUser(
			int userIndex
			);
	void setInputDestMappingsForUser(
			int userIndex,
			Vector<AnOverlayMapping> inputDestMappings
			);
	Vector<AnOverlayMapping> getInputDestMappingsForUser(
			int userIndex,
			int rootUserIndex
			);
	
	Vector<AnOverlayMapping> getOutputSourceMappingsForUser(
			int userIndex
			);
	void setOutputSourceMappingsForUser(
			int userIndex,
			Vector<AnOverlayMapping> outputSourceMappings
			);
	Vector<AnOverlayMapping> getOutputSourceMappingsForUser(
			int rootUserIndex,
			int userIndex
			);
	
	Vector<AnOverlayMapping> getOutputDestMappingsForUser(
			int userIndex
			);
	void setOutputDestMappingsForUser(
			int userIndex,
			Vector<AnOverlayMapping> outputDestinputSourceMappings
			);
	Vector<AnOverlayMapping> getOutputDestMappingsForUser(
			int userIndex,
			int rootUserIndex
			);
	
	void updateUserInputsCommands(
			int userIndex,
			boolean inputsCommands
			);
	
	void setupRelayerCommArch(
			int relayerUserIndex
			);
	
	void setupOverlayForRootUserIndices(
			Map<Integer, Overlay> overlaysForRootUserIndices
			);
}
