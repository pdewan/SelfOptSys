package selfoptsys.ui;

import commonutils.ui.shapes.*;

public interface AppComponent {
	
	public final int LABEL_X = 5;
	public final int LABEL_Y = 15;
	public final int LABEL_WIDTH = 40;
	public final int LABEL_HEIGHT = 20;
	
	public final int OVAL_X = 0;
	public final int OVAL_Y = 0;
	public final int OVAL_WIDTH = 50;
	public final int OVAL_HEIGHT = 50;
	
	public final int TOTAL_WIDTH = 30;
	public final int TOTAL_HEIGHT = 30;
	
	void setOval( Oval newOval );
	Oval getOval();
	
	void setLabel( Label newLabel );
	Label getLabel();
	
	AppComponentType getAppComponentType();
	
}
