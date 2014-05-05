package commonutils.ui.shapes;

public interface Label 
	extends Shape {
	
    public String getText();
    public void setText( String newString );
    
    public String getImageFileName();
    public void setImageFileName( String newString );
    
}
