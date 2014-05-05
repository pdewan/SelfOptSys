package selfoptsys.comm;

import java.net.*;
import java.util.*;
import commonutils.misc.*;
import commonutils.scheduling.*;
import commonutils.basic.*;


public class ATCPMessageDest 
    extends AMessageDest
    implements SocketMessageDest {
    
    protected WindowsThreadPriority m_receiveThreadWindowsPriority = WindowsThreadPriority.HIGHEST;

    protected Vector<TCPListeningThread> m_socketThreads = new Vector<TCPListeningThread>();
    protected Vector<ServerSocket> m_mySockets = new Vector<ServerSocket>();
    
    protected TCPListeningThreadFactory m_tcpListeningThreadFactory;

    public ATCPMessageDest(
            LocalMessageDest localMessageDest,
            WindowsThreadPriority windowsPriority,
            WindowsThreadPriority receiveThreadWindowsPriority,
            TCPListeningThreadFactory tcpListeningThreadFactory
            ) {
        super(
               localMessageDest,
               windowsPriority
               );
        
        m_receiveThreadWindowsPriority = receiveThreadWindowsPriority;
        m_tcpListeningThreadFactory = tcpListeningThreadFactory;
    }
    
    public ConnSocketInfo getConnSocket() {
        ConnSocketInfo info = new ConnSocketInfo();
        
        try {
            ServerSocket sock = new ServerSocket();
            sock.bind( null );
            m_mySockets.add( sock );
            TCPListeningThread slt = m_tcpListeningThreadFactory.createThread( 
                    sock, 
                    this
                    );
            m_socketThreads.add( slt );
            
            slt.setWindowsPriority( m_receiveThreadWindowsPriority );
            ( (ATCPListeningThread) slt ).start();
            info.m_addr = InetAddress.getLocalHost();
            info.m_port = sock.getLocalPort();
        }
        catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "ATCPMessageDest: Error while creating a conn socket info",
                    e
                    );
        }
        
        return info;
    }

    public void closeSocketConn(
            ConnSocketInfo info
            ) {
        try {
            for ( int i = 0; i < m_mySockets.size(); i++ ) {
                if ( m_mySockets.get( i ).getLocalPort() == info.m_port ) {
                    m_socketThreads.get( i ).setStopFlag( true );
                    m_mySockets.get( i ).close();
                    m_socketThreads.remove( i );
                    m_mySockets.remove( i );
                    break;
                }
            }
        }
        catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "ATCPMessageDest::closeSocketConn(): Error while closing socket and stopping socket listening thread",
                    e
                    );
        }
    }
    
    public void resetMsgDest() {
        try {
            for (int i = 0; i < m_mySockets.size(); i++) {
                m_socketThreads.get( i ).setStopFlag( true );
            }
            m_socketThreads.clear();
            m_mySockets.clear();
        }
        catch (Exception e) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "ATCPMessageDest::resetMsgDest(): Error while resetting",
                    e
                    );
        }
    }

}
