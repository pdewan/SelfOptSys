package selfoptsys.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Stroke;
import java.util.*;

import commonutils.ui.shapes.*;

public class AConnector 
	implements Connector {

	MappedCommand m_mappedCommand;
	MappedDirection m_mappedDirection;
	
	Point m_startLocation;
	Point m_endLocation;
	
	Line m_startLine;
	Line m_middleLine;
	Line m_endLine;
	
	Line m_arrowSide1;
	Line m_arrowSide2;
	
	Color m_color;
	
	final int ARROW_SIDE_L = 10;
	final int ARROW_SIDE_W = 5; 
	final double ARROW_SIDE_H = Math.sqrt( Math.pow( ARROW_SIDE_L, 2) + Math.pow( ARROW_SIDE_W, 2) );
	final double ARROW_THETA = Math.atan( (double) ARROW_SIDE_W / (double) ARROW_SIDE_L );
	
	Stroke m_stroke = new BasicStroke();
	
	public AConnector(
			MappedCommand mappedCommand,
			MappedDirection mappedDirection
			) {
		
		m_mappedCommand = mappedCommand;
		m_mappedDirection = mappedDirection;
		
		if ( m_mappedCommand == MappedCommand.INPUT ) {
			m_color = INPUT_CONNECTOR_COLOR;
		}
		else if ( m_mappedCommand == MappedCommand.OUTPUT ) {
			m_color = OUTPUT_CONNECTOR_COLOR;
		}
		
	}
	
	public boolean preGetStartLocation() {
		return false;
	}
	public Point getStartLocation() {
		return m_startLocation;
	}
	
	public void setStartLocation( Point newStartLocation ) {
		m_startLocation = newStartLocation;
		updateLines();
		//updateLabel();
	}
	
	public boolean preGetEndLocation() {
		return false;
	}
	public Point getEndLocation() {
		return m_endLocation;
	}
	
	public void setEndLocation( Point newEndLocation ) {
		m_endLocation = newEndLocation;
		updateLines();
		//updateLabel();
	}
	
	public boolean preGetStartLine() {
		return m_startLine != null;
	}
	public Line getStartLine() {
		return m_startLine;
	}
	
	public boolean preGetMiddleLine() {
		return m_middleLine != null;
	}
	public Line getMiddleLine() {
		return m_middleLine;
	}
	
	public boolean preGetEndLine() {
		return m_endLine != null;
	}
	public Line getEndLine() {
		return m_endLine;
	}
	
	public boolean preGetArrowSide1() {
		return m_arrowSide1 != null;
	}
	public Line getArrowSide1() {
		return m_arrowSide1;
	}
	
	public boolean preGetArrowSide2() {
		return m_arrowSide2 != null;
	}
	public Line getArrowSide2() {
		return m_arrowSide2;
	}
	
	public void setStroke(
			Stroke stroke
			) {
		m_stroke = stroke;
		if ( m_startLine != null ) {
			m_startLine.setStroke( m_stroke );
		}
		if ( m_middleLine != null ) {
			m_middleLine.setStroke( m_stroke );
		}
		if ( m_endLine != null ) {
			m_endLine.setStroke( m_stroke );
		}
	}
	
	void updateHorzMapping(
			Point leftPoint,
			Point rightPoint
			) {
		
		int startPositionX = 0;
		int startPositionY = 0;
		int endPositionX = 0;
		int endPositionY = 0;
		
		startPositionX = leftPoint.getX() +
			AppComponent.OVAL_WIDTH / 4;
		endPositionX = rightPoint.getX() -
			AppComponent.OVAL_WIDTH / 4;
		
		if ( m_mappedDirection == MappedDirection.PC_TO_REMOTE_PC ) {
			startPositionY = leftPoint.getY() +
				AppComponent.OVAL_HEIGHT / 2;
			endPositionY = rightPoint.getY() +
				AppComponent.OVAL_HEIGHT / 2;
		}
		else if ( m_mappedDirection == MappedDirection.UI_TO_REMOTE_UI ) {
			startPositionY = leftPoint.getY() -
				AppComponent.OVAL_HEIGHT / 2;
			endPositionY = rightPoint.getY() -
				AppComponent.OVAL_HEIGHT / 2;
		}
		
		Point updatedLeftPoint = new APoint(
				startPositionX,
				startPositionY
				);
		Point updatedRightPoint = new APoint(
				endPositionX,
				endPositionY
				);
		
		int middleLineY = 0;
		if ( m_mappedDirection == MappedDirection.PC_TO_REMOTE_PC ) {
			middleLineY = 
				Math.max( updatedLeftPoint.getY(), updatedRightPoint.getY() ) + 
					MIN_VERTICAL_LINE_HEIGHT;
		}
		else if ( m_mappedDirection == MappedDirection.UI_TO_REMOTE_UI ) {
			middleLineY = 
				Math.min( updatedLeftPoint.getY(), updatedRightPoint.getY() ) - 
					MIN_VERTICAL_LINE_HEIGHT;
		}
		
		m_middleLine = updateLine(
				m_middleLine,
				new APoint( updatedLeftPoint.getX(), middleLineY ),
				new APoint( updatedRightPoint.getX(), middleLineY )
				);
		
		manageMiddleLines(
				m_middleLine,
				false,
				m_mappedDirection
				);
		
		m_startLine = updateLine(
				m_startLine,
				new APoint( updatedLeftPoint.getX(), m_middleLine.getLocation().getY() ),
				updatedLeftPoint
				);

		m_endLine = updateLine(
				m_endLine,
				new APoint( updatedRightPoint.getX(), m_middleLine.getLocation().getY() ),
				updatedRightPoint
				);
		
		if ( m_startLocation.getX() <= m_endLocation.getX() ) {
			updateArrowHead( m_endLine );
		}
		else {
			updateArrowHead( m_startLine );
		}

	}
	
	void updateVertMapping(
			Point leftPoint,
			Point rightPoint
			) {

		int startPositionX = 0;
		int startPositionY = 0;
		int endPositionX = 0;
		int endPositionY = 0;
		
		if ( m_mappedCommand == MappedCommand.INPUT ) {
			
			startPositionX = leftPoint.getX() - 
				AppComponent.OVAL_WIDTH / 4;
			endPositionX = rightPoint.getX() - 
				AppComponent.OVAL_WIDTH / 4;
			
		}
		else {
			
			startPositionX = leftPoint.getX() + 
				AppComponent.OVAL_WIDTH / 4;
			endPositionX = rightPoint.getX() + 
				AppComponent.OVAL_WIDTH / 4;
			
		}
		
		if ( leftPoint.getY() <= rightPoint.getY() ) {
			
			startPositionY = leftPoint.getY() +
				AppComponent.OVAL_HEIGHT / 2;
			endPositionY = rightPoint.getY() -
				AppComponent.OVAL_HEIGHT / 2;
			
		}
		else {
			
			startPositionY = leftPoint.getY() -
				AppComponent.OVAL_HEIGHT / 2;
			endPositionY = rightPoint.getY() +
				AppComponent.OVAL_HEIGHT / 2;

		}
		
		Point startPoint = new APoint(
				startPositionX,
				startPositionY
				);
		Point endPoint = new APoint(
				endPositionX,
				endPositionY
				);
		
		m_startLine = updateLine(
				m_startLine,
				startPoint,
				endPoint
				);

		updateArrowHead( m_startLine );

	}
	
	void updateLines() {
		
		if ( m_startLocation == null || 
				m_endLocation == null ) {
			
			if ( m_middleLine != null ) {
				manageMiddleLines( m_middleLine, true, m_mappedDirection );
			}
			
			m_startLine = null;
			m_middleLine = null;
			m_endLine = null;
			
			m_arrowSide1 = null;
			m_arrowSide2 = null;
			
			return;
		}
		
		Point leftPoint = null;
		Point rightPoint = null;
		
		if ( m_startLocation.getX() <= 
				m_endLocation.getX() ) {
			leftPoint = m_startLocation;
			rightPoint = m_endLocation;
		}
		else {
			leftPoint = m_endLocation;
			rightPoint = m_startLocation;
		}
		
		if ( m_mappedDirection == MappedDirection.PC_TO_REMOTE_PC 
				|| m_mappedDirection == MappedDirection.UI_TO_REMOTE_UI ) {
			updateHorzMapping(
					leftPoint,
					rightPoint
					);
		}
		else {
			updateVertMapping(
					leftPoint,
					rightPoint
					);
		}
		
	}	
	
	Line updateLine(
			Line lineIn,
			Point startLocation,
			Point endLocation
			) {
		
		Line lineOut;
		
		if ( lineIn == null ) {
			lineOut = new ALine(
					startLocation.getX(),
					startLocation.getY(),
					endLocation.getX() - startLocation.getX(),
					endLocation.getY() - startLocation.getY()
					);
		}
		else {
			lineOut = lineIn;
			lineOut.getLocation().setX( startLocation.getX() );
			lineOut.getLocation().setY( startLocation.getY() );
			lineOut.setWidth( endLocation.getX() - startLocation.getX() );
			lineOut.setHeight( endLocation.getY() - startLocation.getY() );
		}
		
		lineOut.setColor( m_color );
		
		return lineOut;

	}

	private void updateArrowHead(
			Line line
			) {
		
		Point arrowSide1StartPoint = null;
		Point arrowSide1EndPoint = null;
		Point arrowSide2StartPoint = null;
		Point arrowSide2EndPoint = null;
		
		if ( line.getWidth() == 0 ) {
			if ( line.getHeight() < 0 ) {
				arrowSide1StartPoint = new APoint(
						line.getLocation().getX() - ARROW_SIDE_W,
						line.getLocation().getY() + line.getHeight() + ARROW_SIDE_L
						);
				arrowSide1EndPoint = new APoint(
						line.getLocation().getX() - ARROW_SIDE_W + ARROW_SIDE_W,
						line.getLocation().getY() + line.getHeight() + ARROW_SIDE_L - ARROW_SIDE_L
						);
				
				arrowSide2StartPoint = new APoint(
						line.getLocation().getX(),
						line.getLocation().getY() + line.getHeight()
						);
				arrowSide2EndPoint = new APoint(
						line.getLocation().getX() + ARROW_SIDE_W,
						line.getLocation().getY() + line.getHeight() + ARROW_SIDE_L
						);
			}
			else {
				arrowSide1StartPoint = new APoint(
						line.getLocation().getX() - ARROW_SIDE_W,
						line.getLocation().getY() + line.getHeight() - ARROW_SIDE_L
						);
				arrowSide1EndPoint = new APoint(
						line.getLocation().getX() - ARROW_SIDE_W + ARROW_SIDE_W,
						line.getLocation().getY() + line.getHeight() - ARROW_SIDE_L + ARROW_SIDE_L
						);
				
				arrowSide2StartPoint = new APoint(
						line.getLocation().getX(),
						line.getLocation().getY() + line.getHeight()
						);
				arrowSide2EndPoint = new APoint(
						line.getLocation().getX() + ARROW_SIDE_W,
						line.getLocation().getY() + line.getHeight() - ARROW_SIDE_L
						);
			}
		}
		else {

			double x1Prime = 0;
			double y1Prime = 0;
			double x2Prime = 0;
			double y2Prime = 0;
			double theta1 = 0;
			double theta2 = 0;
			
			if ( line.getHeight() > 0 && line.getWidth() > 0 ) {
				double lineTheta = Math.atan( (double) line.getHeight() / (double) line.getWidth() );
				
				if ( m_mappedCommand == MappedCommand.INPUT ) {
					theta1 = Math.PI/2 - lineTheta - ARROW_THETA;
					x1Prime = 
						line.getLocation().getX() + line.getWidth() - 
						ARROW_SIDE_H * Math.sin( theta1 );
					y1Prime = 
						line.getLocation().getY() + line.getHeight() - 
						ARROW_SIDE_H * Math.cos( theta1 );
	
					theta2 = Math.PI/2 - lineTheta + ARROW_THETA;
					x2Prime = 
						line.getLocation().getX() + line.getWidth() - 
						ARROW_SIDE_H * Math.sin( theta2 );
					y2Prime = 
						line.getLocation().getY() + line.getHeight() - 
						ARROW_SIDE_H * Math.cos( theta2 );
					
					arrowSide1StartPoint = new APoint(
							(int) x1Prime,
							(int) y1Prime
							);
					arrowSide1EndPoint = new APoint(
							line.getLocation().getX() + line.getWidth(),
							line.getLocation().getY() + line.getHeight()
							);
					
					arrowSide2StartPoint = new APoint(
							(int) x2Prime,
							(int) y2Prime
							);
					arrowSide2EndPoint = new APoint(
							line.getLocation().getX() + line.getWidth(),
							line.getLocation().getY() + line.getHeight()
							);
				}
				else if ( m_mappedCommand == MappedCommand.OUTPUT ) {
					theta1 = Math.PI/2 - lineTheta - ARROW_THETA;
					x1Prime = 
						line.getLocation().getX() + 
						ARROW_SIDE_H * Math.sin( theta1 );
					y1Prime = 
						line.getLocation().getY() + 
						ARROW_SIDE_H * Math.cos( theta1 );
	
					theta2 = Math.PI/2 - lineTheta + ARROW_THETA;
					x2Prime = 
						line.getLocation().getX() + 
						ARROW_SIDE_H * Math.sin( theta2 );
					y2Prime = 
						line.getLocation().getY() +
						ARROW_SIDE_H * Math.cos( theta2 );
					
					arrowSide1StartPoint = new APoint(
							line.getLocation().getX(),
							line.getLocation().getY()
							);
					arrowSide1EndPoint = new APoint(
							line.getLocation().getX() + (int) x1Prime - line.getLocation().getX(),
							line.getLocation().getY() + (int) y1Prime - line.getLocation().getY()
							);
					
					arrowSide2StartPoint = new APoint(
							line.getLocation().getX(),
							line.getLocation().getY()
							);
					arrowSide2EndPoint = new APoint(
							line.getLocation().getX() + (int) x2Prime - line.getLocation().getX(),
							line.getLocation().getY() + (int) y2Prime - line.getLocation().getY()
							);					
				}
				
			}
			else if ( line.getHeight() < 0 && line.getWidth() > 0 ) {
				double lineTheta = Math.atan( (double) line.getHeight() / (double) line.getWidth() );
				
				if ( m_mappedCommand == MappedCommand.INPUT ) {
					theta1 = Math.PI/2 - lineTheta - ARROW_THETA;
					x1Prime = 
						line.getLocation().getX() + 
						ARROW_SIDE_H * Math.sin( theta1 );
					y1Prime = 
						line.getLocation().getY() - 
						ARROW_SIDE_H * Math.cos( theta1 );
	
					theta2 = Math.PI/2 - lineTheta + ARROW_THETA;
					x2Prime = 
						line.getLocation().getX() + 
						ARROW_SIDE_H * Math.sin( theta2 );
					y2Prime = 
						line.getLocation().getY() - 
						ARROW_SIDE_H * Math.cos( theta2 );
					
					arrowSide1StartPoint = new APoint(
							line.getLocation().getX(),
							line.getLocation().getY()
							);
					arrowSide1EndPoint = new APoint(
							line.getLocation().getX() + (int) x1Prime - line.getLocation().getX(),
							line.getLocation().getY() + line.getLocation().getY() - (int) y1Prime
							);
					
					arrowSide2StartPoint = new APoint(
							line.getLocation().getX(),
							line.getLocation().getY()
							);
					arrowSide2EndPoint = new APoint(
							line.getLocation().getX() + (int) x2Prime - line.getLocation().getX(),
							line.getLocation().getY() + line.getLocation().getY() - (int) y2Prime
							);
				}
				else if ( m_mappedCommand == MappedCommand.OUTPUT ) {
					theta1 = - Math.PI/2 - lineTheta - ARROW_THETA;
					x1Prime = 
						line.getLocation().getX() + line.getWidth() + 
						ARROW_SIDE_H * Math.sin( theta1 );
					y1Prime = 
						line.getLocation().getY() + line.getHeight() + 
						ARROW_SIDE_H * Math.cos( theta1 );
	
					theta2 = - Math.PI/2 - lineTheta + ARROW_THETA;
					x2Prime = 
						line.getLocation().getX() + line.getWidth() + 
						ARROW_SIDE_H * Math.sin( theta2 );
					y2Prime = 
						line.getLocation().getY() + line.getHeight() +
						ARROW_SIDE_H * Math.cos( theta2 );
					
					arrowSide1StartPoint = new APoint(
							line.getLocation().getX() + line.getWidth(),
							line.getLocation().getY() + line.getHeight()
							);
					arrowSide1EndPoint = new APoint(
							line.getLocation().getX() + line.getWidth() + (int) x1Prime - line.getLocation().getX() - line.getWidth(),
							line.getLocation().getY() + line.getHeight() + (int) y1Prime - line.getLocation().getY() - line.getHeight()
							);
					
					arrowSide2StartPoint = new APoint(
							line.getLocation().getX() + line.getWidth(),
							line.getLocation().getY() + line.getHeight()
							);
					arrowSide2EndPoint = new APoint(
							line.getLocation().getX() + line.getWidth() + (int) x2Prime - line.getLocation().getX() - line.getWidth(),
							line.getLocation().getY() + line.getHeight() + (int) y2Prime - line.getLocation().getY() - line.getHeight()
							);
				}
			}

		}
		
		if ( arrowSide1EndPoint != null ) {
			m_arrowSide1 = updateLine(
					m_arrowSide1,
					arrowSide1StartPoint,
					arrowSide1EndPoint
					);
			m_arrowSide2 = updateLine(
					m_arrowSide2,
					arrowSide2StartPoint,
					arrowSide2EndPoint
					);
		}
	}
	
	static Vector<Line> s_middlePCtoPCLines = new Vector<Line>();
	static Vector<Line> s_middleUItoUILines = new Vector<Line>();
	
	static synchronized void manageMiddleLines(
			Line middleLine,
			boolean removing,
			MappedDirection mappedDirection
			) {
		
		Vector<Line> middleLines = null;
		if ( mappedDirection == MappedDirection.PC_TO_REMOTE_PC ) {
			middleLines = s_middlePCtoPCLines;
		}
		else if ( mappedDirection == MappedDirection.UI_TO_REMOTE_UI ) {
			middleLines = s_middleUItoUILines;
		}
		
		if ( removing ) {
			middleLines.remove( middleLine );
			return;
		}
		
		int newY = middleLine.getLocation().getY();
		
		while ( true ) {
			
			boolean yValOccupied = false;
			for ( int i = 0; i < middleLines.size(); i++ ) {
				Line curLine = middleLines.elementAt( i );
				if ( newY != curLine.getLocation().getY() ) {
					continue;
				}
				
				if ( ( middleLine.getLocation().getX() >= curLine.getLocation().getX() && 
				middleLine.getLocation().getX() <= curLine.getLocation().getX() + curLine.getWidth() ) || 
				( middleLine.getLocation().getX() + middleLine.getWidth() >= curLine.getLocation().getX() && 
						middleLine.getLocation().getX() + middleLine.getWidth() <= curLine.getLocation().getX() + curLine.getWidth() ) || 
				( curLine.getLocation().getX() >= middleLine.getLocation().getX() && 
						curLine.getLocation().getX() <= middleLine.getLocation().getX() + middleLine.getWidth() ) || 
				( curLine.getLocation().getX() + curLine.getWidth() >= middleLine.getLocation().getX() && 
						curLine.getLocation().getX() + curLine.getWidth() <= middleLine.getLocation().getX() + middleLine.getWidth() )) {
					yValOccupied = true;
					break;
				}
			}
			
			if ( !yValOccupied ) {
				break;
			}

			if ( mappedDirection == MappedDirection.PC_TO_REMOTE_PC ) {
				newY += MIN_SEPARATION_BETWEEN_HORIZONTAL_LINES;
			} else if ( mappedDirection == MappedDirection.UI_TO_REMOTE_UI ) {
				newY -= MIN_SEPARATION_BETWEEN_HORIZONTAL_LINES;
			}
		}
		
		middleLine.getLocation().setY( newY );
		
		if ( !middleLines.contains( middleLine ) ) {
			middleLines.add( middleLine );
		}
	}
	
}
