package selfoptsys.ui;

import util.models.ChangeableVector;

public interface GraphicalSessionRegistryUI 
	extends SessionRegistryUI {
	
	public final int HORZ_DIST_BETWEEN_COMPONENTS = 30;
	public final int VERT_DIST_BETWEEN_COMPONENTS = 75;
	public final int TOP_MARGIN = 75;

	ChangeableVector<GraphicalUIMapping> getUIMappings();
    
	void setShowAllCommunicationOverlays(
			boolean showAllCommunicationOverlays
			);
	boolean getShowAllCommunicationOverlays();
	
	void setShowOverlayForRootUserIndex(
			int userIndex
			);
	int getShowOverlayForRootUserIndex();

}
