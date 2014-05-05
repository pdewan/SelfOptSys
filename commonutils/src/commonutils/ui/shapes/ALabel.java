package commonutils.ui.shapes;

import java.beans.*;

public class ALabel
	extends AShape
	implements Label {
	
    String m_text;
    String m_imageFileName;
    
    public ALabel (
            int initX, 
            int initY, 
            int initWidth, 
            int initHeight, 
            String initText, 
            String theImageFile
            ) {		
    	super( initX,
    			initY,
    			initWidth,
    			initHeight
    			);
    	
        m_text = initText;
        m_imageFileName = theImageFile;
     }
    
     public String getText() {
    	 return m_text;
     }
     
     public void setText( 
    		 String newText 
    		 ) {
    	 String oldText = m_text;
    	 m_text = newText;
    	 notifyAllListeners( new PropertyChangeEvent(
				this, "text", oldText, newText ) );
     }
     
     public String getImageFileName() {
    	 return m_imageFileName;
     }
     
     public void setImageFileName( 
    		 String newImageFileName 
    		 ) {
    	 String oldImageFileName = m_imageFileName;
    	 m_imageFileName = newImageFileName;
    	 notifyAllListeners( new PropertyChangeEvent(
 				this, "imageFileName", oldImageFileName, newImageFileName ) );
     }
     
}
