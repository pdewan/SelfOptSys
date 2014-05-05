package selfoptsysapps.im;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class AnImUI extends JFrame implements ActionListener {
    
	private static final long serialVersionUID = 5440189560419184327L;
	private AnImLogger m_program = null;
    private int m_userIndex;
    
    private JTextField m_textArea = new JTextField();
    private JButton m_enterButton;
    
    private JTextPane m_history = new JTextPane();
    private String m_historyText = "";
    private JScrollPane m_scrollingArea;
    
    private int m_numLineInStatusBox = 0;
    
    public AnImUI(
            int userIndex
            ) {
        super( "IM: " + userIndex );
        
        m_userIndex = userIndex;
        
        JPanel newTextMessageArea = new JPanel( new GridLayout( 2, 1 ) );
        newTextMessageArea.setBorder(BorderFactory.createTitledBorder( "Enter Message" ) );
        m_textArea = new JTextField();
        m_textArea.setText("");
        newTextMessageArea.add( m_textArea );
        m_enterButton = new JButton( "Send" );
        newTextMessageArea.add( m_enterButton );
        getContentPane().add( newTextMessageArea, BorderLayout.NORTH );
        
        JPanel historyArea = new JPanel(new GridLayout(1,1));
        historyArea.setBorder(BorderFactory.createTitledBorder( "Chat History" ) );
        m_history.setText( "" );
        m_history.setFocusable( false );
        m_scrollingArea = new JScrollPane( m_history );
        m_scrollingArea.setPreferredSize( new Dimension( 250, 450 ) );
        m_scrollingArea.setMinimumSize( new Dimension( 250, 450 ) );
        historyArea.add( m_scrollingArea );
        getContentPane().add( historyArea, BorderLayout.SOUTH );
        
        m_enterButton.addActionListener( this );
        
        setSize( 400, 600 );
        setVisible(true);
        //show();
    }
    
    public void setProgram(
            AnImLogger logger
            ) {
        m_program = logger;
    }
    
    public void actionPerformed( ActionEvent e ) {
        if ( e.getSource().equals( m_enterButton ) ) {
            if ( !m_textArea.getText().equals( "" ) ) {
                m_program.sendInputMsg( new AnImMessage( m_userIndex, m_textArea.getText() + "\n" ) );
                m_textArea.setText( "" );
            }
        }
    }
    
    public void appendText(
            AnImMessage msg
            ) {
        m_historyText = msg.getText() + m_historyText;
        m_history.setText( m_historyText ); 
        //m_history.setCaretPosition( m_history.getText().length() - m_numLineInStatusBox );
        //jTextPaneAppend( msg.m_userName, msg.m_text );
        m_numLineInStatusBox++;
    }
    
//    private void jTextPaneAppend( String user, String s ) {
//        StyleContext sc = StyleContext.getDefaultStyleContext();
//        
//        Color color = Color.BLUE;
//        if ( !user.equals( m_userName ) ) {
//            color = Color.RED;
//        }
//        AttributeSet aset = sc.addAttribute(
//                SimpleAttributeSet.EMPTY,
//                StyleConstants.Foreground, 
//                color
//                );
//
//        int len = m_history.getText().length();
//        if ( len > 0 ) {
//            len--;
//        }
//        
//        m_history.setCaretPosition( m_history.getText().length() - m_numLineInStatusBox );
//        m_history.setCharacterAttributes( aset, false );
//        m_history.replaceSelection( s );
//    }
    
}