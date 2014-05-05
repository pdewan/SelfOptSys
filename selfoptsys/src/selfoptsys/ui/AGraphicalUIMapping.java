package selfoptsys.ui;

import java.awt.BasicStroke;
import java.awt.Stroke;
import java.beans.*;

import commonutils.ui.shapes.*;

public class AGraphicalUIMapping 
	extends AUIMapping 
	implements GraphicalUIMapping {

	AppComponent m_sourceAppComponent;
	AppComponent m_destAppComponent;
	Connector m_connector;
	Stroke m_lastStroke;
	
	final float[] dashes0_50 = { 7.0f, 1.0f, 7.0f, 1.0f };
	final float[] dashes50_100 = { 6.0f, 2.0f };
	final float[] dashes100_150 = { 5.0f, 3.0f };
	final float[] dashes150_200 = { 4.0f, 4.0f };
	final float[] dashes200_250 = { 3.0f, 5.0f };
	final float[] dashes250_300 = { 2.0f, 6.0f };
	final float[] dashes300_350 = { 1.0f, 7.0f };
	
	public AGraphicalUIMapping(
			int sourceUserIndex,
			int destUserIndex,
			int rootUserIndex,
			MappedCommand mappedCommand,
			MappedDirection mappedDirection,
			int delay
			) {
		super( sourceUserIndex,
				destUserIndex,
				rootUserIndex,
				mappedCommand,
				mappedDirection,
				delay
				);
		
		m_connector = new AConnector(
				mappedCommand,
				mappedDirection
				);
	}
	
	public AppComponent getSourceAppComponent() {
		return m_sourceAppComponent;
	}
	public void setSourceAppComponent( AppComponent newSourceAppComponent ) {		
		if ( m_sourceAppComponent != null ) {
			m_sourceAppComponent.getOval().removePropertyChangeListener( this );
		}
		
		m_sourceAppComponent = newSourceAppComponent;
		
		if ( m_sourceAppComponent != null ) {
			m_sourceAppComponent.getOval().addPropertyChangeListener( this );
		}
		
		m_sourceAppComponent = newSourceAppComponent;
		updateConnector();
	}
	
	public AppComponent getDestAppComponent() {
		return m_destAppComponent;
	}
	public void setDestAppComponent( AppComponent newDestAppComponent ) {
		if ( m_destAppComponent != null ) {
			m_destAppComponent.getOval().removePropertyChangeListener( this );
		}
		
		m_destAppComponent = newDestAppComponent;
		
		if ( m_destAppComponent != null ) {
			m_destAppComponent.getOval().addPropertyChangeListener( this );
		}

		m_destAppComponent = newDestAppComponent;
		updateConnector();
	}
	
	public synchronized void updateConnector() {

		if ( m_sourceAppComponent == null || m_destAppComponent == null ) {
			m_connector.setStartLocation( null );
			m_connector.setEndLocation( null );
			return;
		}
		
		int x1 = m_sourceAppComponent.getOval().getLocation().getX() + m_sourceAppComponent.getOval().getWidth() / 2;
		int y1 = m_sourceAppComponent.getOval().getLocation().getY() + m_sourceAppComponent.getOval().getHeight() / 2;
		int x2 = m_destAppComponent.getOval().getLocation().getX() + m_destAppComponent.getOval().getWidth() / 2;
		int y2 = m_destAppComponent.getOval().getLocation().getY() + m_destAppComponent.getOval().getHeight() / 2;
		
		m_connector.setStartLocation( new APoint( x1, y1 ) );
		m_connector.setEndLocation( new APoint( x2, y2 ) );
		
		Stroke stroke = null;
		if ( m_delay > 0 && m_delay <= 50 ) {
			stroke = new BasicStroke( 1.0F, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0F, dashes0_50, 0.F);
		}
		else if ( m_delay > 50 && m_delay <= 100 ) {
			stroke = new BasicStroke( 1.0F, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0F, dashes50_100, 0.F);
		}
		else if ( m_delay > 100 && m_delay <= 150 ) {
			stroke = new BasicStroke( 1.0F, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0F, dashes100_150, 0.F);
		}
		else if ( m_delay > 150 && m_delay <= 200 ) {
			stroke = new BasicStroke( 1.0F, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0F, dashes150_200, 0.F);
		}
		else if ( m_delay > 200 && m_delay <= 250 ) {
			stroke = new BasicStroke( 1.0F, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0F, dashes200_250, 0.F);
		}
		else if ( m_delay > 250 && m_delay <= 300 ) {
			stroke = new BasicStroke( 1.0F, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0F, dashes250_300, 0.F);
		}
		else if ( m_delay > 300 && m_delay <= 350 ) {
			stroke = new BasicStroke( 1.0F, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0F, dashes300_350, 0.F);
		}
		m_connector.setStroke( stroke );

	}
	
	public Connector getConnector() {
		return m_connector;
	}
	
	public void propertyChange(
			PropertyChangeEvent evt
			) {
		updateConnector();
	}
	
	public void setDelay(
			int delay
			) {
		super.setDelay( delay );
		updateConnector();
	}
	
}
