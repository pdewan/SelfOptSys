package selfoptsys.ui;

import java.beans.*;

public interface GraphicalUIMapping 
	extends UIMapping, PropertyChangeListener {

	AppComponent getSourceAppComponent();
	void setSourceAppComponent( AppComponent newUserInterface );
	
	AppComponent getDestAppComponent();
	void setDestAppComponent( AppComponent newProgramComponent );
	
	Connector getConnector();
	
}
