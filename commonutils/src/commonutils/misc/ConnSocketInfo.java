package commonutils.misc;

import java.io.*;
import java.net.*;

public class ConnSocketInfo 
	implements Serializable {
	
	private static final long serialVersionUID = 4343965938427521937L;
	public InetAddress m_addr = null;
    public int m_port = -1;
}
