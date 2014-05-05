package selfoptsysapps.ppt;

public class APptCommand 
	implements java.io.Serializable {
	
	private static final long serialVersionUID = -7548101445422892485L;
	private int m_cmdType;
    private int m_slidesInsertPos;
    private byte[] m_data;
    
    public APptCommand( int cmd ) {
        this( cmd, -1, null );
    }
    
    public APptCommand( 
    		int cmdType, 
    		int slidesInsertPos, 
    		byte[] data 
    		) {
    	m_cmdType = cmdType;
    	m_slidesInsertPos = slidesInsertPos;
    	m_data = data;
    }
    
    public int getCmdType() {
        return m_cmdType;
    }
    
    public int getSlidesInsertPos() {
        return m_slidesInsertPos;
    }
    
    public byte[] getData() {
        return m_data;
    }
}
