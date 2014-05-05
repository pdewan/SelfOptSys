package selfoptsys.ui;

import java.beans.PropertyChangeListener;
import java.util.*;

public interface UIMapping {

	int getSourceUserIndex();
	
	int getDestUserIndex();
	
	Vector<Integer> getOverlaysInWhichIAmIn();
	
	MappedCommand getMappedCommand();
	
	MappedDirection getMappedDirection();
	
	void addOverlayInWhichIAmIn(
			int overlayInWhichIAmIn
			);
	void removeOverlayInWhichIAmIn(
			int overlayInWhichIAmIn
			);
	int getCount();
	
	void setShowTextualInfoInOE(
			boolean showTextualInfoInOE
			);
	
	void setDelay(
			int delay
			);
	int getDelay();
	
	void addPropertyChangeListener(
			PropertyChangeListener l
			);
}
