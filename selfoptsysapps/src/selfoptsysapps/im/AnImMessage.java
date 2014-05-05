package selfoptsysapps.im;

import java.io.*;

public class AnImMessage implements Serializable {

	private static final long serialVersionUID = 2382667187339047349L;
	private int m_userIndex;
    private String m_text = "";
    
    public AnImMessage(
            int userIndex,
            String text
            ) {
    	m_userIndex = userIndex;
        m_text = text;
    }
    
    public int getUserIndex() {
    	return m_userIndex;
    }
    
    public String getText() {
    	return m_text;
    }
}
