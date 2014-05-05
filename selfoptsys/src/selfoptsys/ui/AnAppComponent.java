package selfoptsys.ui;

import java.awt.Color;

import commonutils.ui.shapes.*;



public class AnAppComponent 
	implements AppComponent {

	AppComponentType m_appComponentType;
	
	Label m_label;
	Oval m_oval;
	
	int m_offsetX;
	int m_offsetY;
	
	public AnAppComponent(
			AppComponentType appComponentType,
			String labelText,
			int offsetX,
			int offsetY
			) {
		
		m_appComponentType = appComponentType;
		
		m_label = new ALabel(
				LABEL_X + offsetX, 
				LABEL_Y + offsetY, 
				LABEL_WIDTH, 
				LABEL_HEIGHT, 
				labelText, 
				null 
				);
		
		m_oval = new AnOval(
				OVAL_X + offsetX, 
				OVAL_Y + offsetY, 
				OVAL_WIDTH, 
				OVAL_HEIGHT 
				);
		m_oval.isFilled( false );
		m_oval.setColor( Color.LIGHT_GRAY );

	}
	
	public void setOval( Oval newOval ) {
		m_oval = newOval;
		updateLabel();
	}
	public Oval getOval() {
		return m_oval;
	}
	
	public void setLabel( Label newLabel ) {
		m_label = newLabel;
	}
	public Label getLabel() { 
		return m_label;
	}
	
	public boolean preGetAppComponentType() {
		return false;
	}
	public AppComponentType getAppComponentType() {
		return m_appComponentType;
	}
	
	void updateLabel() {
		
		m_label.getLocation().setX( m_oval.getLocation().getX() + LABEL_X );
		m_label.getLocation().setY( m_oval.getLocation().getY() + LABEL_Y );
		
	}
	
}
