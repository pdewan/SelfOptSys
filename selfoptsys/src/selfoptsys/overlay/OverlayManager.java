package selfoptsys.overlay;

import java.util.*;

import selfoptsys.*;
import selfoptsys.network.*;

import commonutils.config.*;

public interface OverlayManager {

	void userJoiningAsMaster(
			int userIndex,
			Logger userLogger,
			boolean inputsCommands
			);
	
	void userJoiningAsSlave(
			int userIndex,
			Logger userLogger,
			int masterUserIndex,
			boolean inputsCommands
			);
	
	Overlay getCurrentOverlay(
			int rootUserIndex
			);
	
	Vector<AMapping> getInputSourceMappingsForUser(
			int userIndex
			);
	void setInputSourceMappingsForUser(
			int userIndex,
			Vector<AMapping> inputSourceMappings
			);
	
	Vector<AMapping> getInputDestMappingsForUser(
			int userIndex
			);
	void setInputDestMappingsForUser(
			int userIndex,
			Vector<AMapping> inputDestMappings
			);
	
	Vector<AMapping> getOutputSourceMappingsForUser(
			int userIndex
			);
	void setOutputSourceMappingsForUser(
			int userIndex,
			Vector<AMapping> outputSourceMappings
			);
	
	Vector<AMapping> getOutputDestMappingsForUser(
			int userIndex
			);
	void setOutputDestMappingsForUser(
			int userIndex,
			Vector<AMapping> outputDestinputSourceMappings
			);
	
	void removeAllMappingsForUser(
			int userIndex
			);
	
	AnOverlayManagerUserInfo getSpecialMasterUserMappingUpdatesUserInfo();
	Vector<AMapping> getSpecialMasterUserMappingUpdates();
	
	ProcessingArchitectureType getProcessingArchitecture(
			int rootUserIndex
			);
	
	void userLeaving(
			int userIndex
			);

	void updateUserInputsCommands(
			int userIndex,
			boolean inputsCommands
			);
	
}
