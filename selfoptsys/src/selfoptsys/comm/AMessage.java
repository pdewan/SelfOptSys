package selfoptsys.comm;

public abstract class AMessage 
	implements Message {

	private static final long serialVersionUID = 5574146219128730034L;

	protected MessageType m_msgType;
	protected transient long m_receivedTime;
	
	public AMessage(
			MessageType cmdType
			) {
		m_msgType = cmdType;
	}
	
	public void setMessageType( MessageType cmdType ) {
	    m_msgType = cmdType;
	}
	
	public MessageType getMessageType() {
		return m_msgType;
	}
	
}
