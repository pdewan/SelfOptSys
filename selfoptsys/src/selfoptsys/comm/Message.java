package selfoptsys.comm;

import java.io.*;

public interface Message 
	extends Serializable {

	void setMessageType(
	        MessageType msgType
	        );
	MessageType getMessageType();

}
