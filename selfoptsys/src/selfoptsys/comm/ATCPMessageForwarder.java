package selfoptsys.comm;

import java.net.*;
import java.nio.channels.*;
import java.util.*;

import commonutils.misc.*;
import commonutils.basic.*;


public abstract class ATCPMessageForwarder 
    extends AMessageForwarder {

    protected Map<Integer, Socket> m_sockets;
    protected Map<Integer, ConnSocketInfo> m_connSockInfos;
    
    public ATCPMessageForwarder(
            int userIndex
            ) {
        super(
                userIndex
                );
        
        m_sockets = new Hashtable<Integer, Socket>();
        m_connSockInfos = new Hashtable<Integer, ConnSocketInfo>();
    }
    
    public void addDest(
            int userIndex,
            MessageDest msgDest
            ) {
        try {
            SocketMessageDest socketMsgDest = (SocketMessageDest) msgDest;
            
            ConnSocketInfo info = socketMsgDest.getConnSocket();
            SocketChannel sChannel = SocketChannel.open();
            sChannel.connect( new InetSocketAddress( info.m_addr, info.m_port ) );
            Socket sock = sChannel.socket();
            sock.setTcpNoDelay( true );                
            m_sockets.put( userIndex, sock );
            m_connSockInfos.put( userIndex, info );
            
            super.addDest(
                    userIndex, 
                    msgDest 
                    );
        }
        catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "ATCPMessageForwarder: Error while adding a dest",
                    e
                    );
        }
    }
    
    public void removeDest(
            int userIndex
            ) {
        try { 
            ( (SocketMessageDest) m_msgDests.get( userIndex ) ).closeSocketConn(
                    m_connSockInfos.get( userIndex )
                    );
            
            super.removeDest( 
                    userIndex 
                    );
            
            Socket sock = m_sockets.get( userIndex );
            sock.close();
            m_sockets.remove( userIndex );
        }
        catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "ATCPMessageForwarder::removeDest(): Error while removing a dest from forwarder",
                    e
                    );
        }
        
    }
    
    public void resetMsgSender() {
        try {
            super.resetMsgSender();
            
            for ( int i = 0; i < m_sockets.size(); i++ ) {
                m_sockets.get( i ).close();
            }
            
            m_sockets.clear();
        }
        catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "ATCPMessageForwarder::resetMsgSender(): Error while resetting forwarder",
                    e
                    );
        }
    }
    
}
