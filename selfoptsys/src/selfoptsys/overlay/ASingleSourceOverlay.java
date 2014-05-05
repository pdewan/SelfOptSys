package selfoptsys.overlay;

import java.util.*;

public class ASingleSourceOverlay 
	implements SingleSourceOverlay {

	private ASingleSourceOverlayUserInfo m_sourceUserInfo;
	
	private Map<Integer, ASingleSourceOverlayUserInfo> m_userInfos;
	
	public ASingleSourceOverlay(
			int sourceUserIndex,
			int masterIndexOfSource
			) {
		m_userInfos = new Hashtable<Integer, ASingleSourceOverlayUserInfo>();
		m_sourceUserInfo = new ASingleSourceOverlayUserInfo(
				sourceUserIndex,
				masterIndexOfSource
				);
		m_userInfos.put(
				sourceUserIndex,
				m_sourceUserInfo
				);
	}
	
	public int getSourceUserIndex() {
		return m_sourceUserInfo.getUserIndex();
	}
	
	public int getMasterIndexOfSource() {
		return m_sourceUserInfo.getMasterIndex();
	}
	
	public void userJoiningAsMaster(
			int joiningUserIndex
			) {
		
		if ( joiningUserIndex == m_sourceUserInfo.getUserIndex() ) {
			// Handled in constructor
			return;
		}
		
		ASingleSourceOverlayUserInfo joiningUserInfo = m_userInfos.get( joiningUserIndex );
		if ( joiningUserInfo == null ) {
			joiningUserInfo = new ASingleSourceOverlayUserInfo(
					joiningUserIndex,
					joiningUserIndex
					);
			m_userInfos.put( 
					joiningUserIndex, 
					joiningUserInfo 
					);
		}
		
		/*
		 
		 The logic:
		 if the source is a master
		 	then source sends input to joining master
		 		and joining master sends outputs to all of its slaves
		 else
		 	if the joining master is master of source
		 		then source sends inputs to joining master
		 			and joining master sends outputs to all of its slaves
		 	else
		 		the master of source user sends inputs to joining master
		 
		 */
		
		if ( m_sourceUserInfo.isMaster() ) {
			
			m_sourceUserInfo.addInputDestination( joiningUserIndex );
			joiningUserInfo.addInputSource( m_sourceUserInfo.getUserIndex() );
			
			Iterator<Map.Entry<Integer, ASingleSourceOverlayUserInfo>> userInfoIterator =
				m_userInfos.entrySet().iterator();
			while ( userInfoIterator.hasNext() ) {
				ASingleSourceOverlayUserInfo userInfo = userInfoIterator.next().getValue();
				
				if ( userInfo.getUserIndex() == joiningUserIndex ||
						userInfo.getUserIndex() == m_sourceUserInfo.getUserIndex() ) {
					continue;
				}
				
				if ( userInfo.getMasterIndex() == joiningUserIndex ) {
					joiningUserInfo.addOutputDestination( userInfo.getUserIndex() );
					userInfo.addOutputSource( joiningUserInfo.getUserIndex() );
				}
				
			}
			
		}
		else if ( joiningUserIndex == m_sourceUserInfo.getMasterIndex() ) {
			
			m_sourceUserInfo.addInputDestination( joiningUserIndex );
			joiningUserInfo.addInputSource( m_sourceUserInfo.getUserIndex() );
			
			Iterator<Map.Entry<Integer, ASingleSourceOverlayUserInfo>> userInfoIterator =
				m_userInfos.entrySet().iterator();
			while ( userInfoIterator.hasNext() ) {
				ASingleSourceOverlayUserInfo userInfo = userInfoIterator.next().getValue();
				
				if ( userInfo.getUserIndex() == joiningUserIndex ) {
					continue;
				}
				
				if ( userInfo.isMaster() ) {
					joiningUserInfo.addInputDestination( userInfo.getUserIndex() );
					userInfo.addInputSource( joiningUserInfo.getUserIndex() );
				}
				else if ( userInfo.getMasterIndex() == joiningUserIndex ) {
					joiningUserInfo.addOutputDestination( userInfo.getUserIndex() );
					userInfo.addOutputSource( joiningUserInfo.getUserIndex() );
				}
			}			
			
		}
		else { 
			
			ASingleSourceOverlayUserInfo masterOfSourceUserInfo =
				m_userInfos.get( m_sourceUserInfo.getMasterIndex() );
			
			if ( masterOfSourceUserInfo != null ) {
				masterOfSourceUserInfo.addInputDestination( joiningUserIndex );
				joiningUserInfo.addInputSource( masterOfSourceUserInfo.getUserIndex() );
			}
		}
		
	}
	
	public void userJoiningAsSlave(
			int joiningUserIndex,
			int masterIndexOfJoiningUser
			) {
		
		if ( joiningUserIndex == m_sourceUserInfo.getUserIndex() ) {
			// Handled in constructor
			return;
		}
		
		ASingleSourceOverlayUserInfo joiningUserInfo = m_userInfos.get( joiningUserIndex );
		if ( joiningUserInfo == null ) {
			joiningUserInfo = new ASingleSourceOverlayUserInfo(
					joiningUserIndex,
					masterIndexOfJoiningUser
					);
			m_userInfos.put( 
					joiningUserIndex, 
					joiningUserInfo 
					);
		}
		
		ASingleSourceOverlayUserInfo masterUserInfo = m_userInfos.get( masterIndexOfJoiningUser );
		if ( masterUserInfo == null ) {
			// Can happen if slave joins before its master. When master joins
			// later, we take care of the mapping then.
			return;
		}
		
		masterUserInfo.addOutputDestination( joiningUserIndex );
		joiningUserInfo.addOutputSource( masterUserInfo.getUserIndex() );
	}
	
	public void userLeaving(
			int leavingUserIndex
			) {
		
		m_userInfos.remove( leavingUserIndex );
		
		Iterator<Map.Entry<Integer, ASingleSourceOverlayUserInfo>> userInfoIterator =
			m_userInfos.entrySet().iterator();
		while ( userInfoIterator.hasNext() ) {
			ASingleSourceOverlayUserInfo userInfo = userInfoIterator.next().getValue();

			userInfo.removeInputDestination( leavingUserIndex );
			userInfo.removeOutputDestination( leavingUserIndex );
		}
		
	}
	
	public Vector<Integer> getInputDestMappingsForUser(
			int userIndex
			) {
		ASingleSourceOverlayUserInfo userInfo = m_userInfos.get( userIndex );
		return userInfo.getInputDestinations();
	}
	public void setInputDestMappingsForUser(
			int userIndex,
			Vector<Integer> inputDestMappings
			) {
		ASingleSourceOverlayUserInfo userInfo = m_userInfos.get( userIndex );
		userInfo.setInputDestinations( inputDestMappings );
	}
	
	public Vector<Integer> getInputSourceMappingsForUser(
			int userIndex
			) {
		ASingleSourceOverlayUserInfo userInfo = m_userInfos.get( userIndex );
		return userInfo.getInputSources();
	}
	public void setInputSourceMappingsForUser(
			int userIndex,
			Vector<Integer> inputSourceMappings
			) {
		ASingleSourceOverlayUserInfo userInfo = m_userInfos.get( userIndex );
		userInfo.setInputSources( inputSourceMappings );
	}
	
	public Vector<Integer> getOutputDestMappingsForUser(
			int userIndex
			) {
		ASingleSourceOverlayUserInfo userInfo = m_userInfos.get( userIndex );
		return userInfo.getOutputDestinations();
	}
	public void setOutputDestMappingsForUser(
			int userIndex,
			Vector<Integer> outputDestMappings
			) {
		ASingleSourceOverlayUserInfo userInfo = m_userInfos.get( userIndex );
		userInfo.setOutputDestinations( outputDestMappings );
	}
	
	public Vector<Integer> getOutputSourceMappingsForUser(
			int userIndex
			) {
		ASingleSourceOverlayUserInfo userInfo = m_userInfos.get( userIndex );
		return userInfo.getOutputSources();
	}
	public void setOutputSourceMappingsForUser(
			int userIndex,
			Vector<Integer> outputSourceMappings
			) {
		ASingleSourceOverlayUserInfo userInfo = m_userInfos.get( userIndex );
		userInfo.setOutputSources( outputSourceMappings );
	}
	
	public void removeAllMappingsForUser(
			int userIndex
			) {
		
		Iterator<Map.Entry<Integer, ASingleSourceOverlayUserInfo>> userInfoIterator = 
			m_userInfos.entrySet().iterator();
		while ( userInfoIterator.hasNext() ) {
			ASingleSourceOverlayUserInfo userInfo = userInfoIterator.next().getValue();
			
			userInfo.removeInputDestination( userIndex );
			userInfo.removeInputSource( userIndex );
			userInfo.removeOutputDestination( userIndex );
			userInfo.removeOutputSource( userIndex );
		}
		
	}
	
}

class ASingleSourceOverlayUserInfo {
	
	private int m_userIndex;
	private int m_masterIndex;
	
	private Vector<Integer> m_inputDestinations;
	private Vector<Integer> m_inputSources;
	
	private Vector<Integer> m_outputDestinations;
	private Vector<Integer> m_outputSources;

	public ASingleSourceOverlayUserInfo(
			int userIndex,
			int masterUserIndex
			) {
		m_userIndex = userIndex;
		setMasterIndex( masterUserIndex );
		
		m_inputDestinations = new Vector<Integer>();
		m_inputSources = new Vector<Integer>();
		m_outputDestinations = new Vector<Integer>();
		m_outputSources = new Vector<Integer>();
	}
	
	public int getUserIndex() {
		return m_userIndex;
	}
	
	public void setMasterIndex(
			int masterIndex
			) {
		m_masterIndex = masterIndex;
	}
	public int getMasterIndex() {
		return m_masterIndex;
	}
	
	public boolean isMaster() {
		return m_userIndex == m_masterIndex;
	}
	
	public void addInputDestination(
			int userIndex
			) {
		m_inputDestinations.addElement( userIndex );
	}
	public void removeInputDestination(
			int userIndex
			) {
		m_inputDestinations.removeElement( userIndex );
	}
	
	public void addInputSource(
			int userIndex
			) {
		m_inputSources.addElement( userIndex );
	}
	public void removeInputSource(
			int userIndex
			) {
		m_inputSources.removeElement( userIndex );
	}
	
	public void addOutputDestination(
			int userIndex
			) {
		m_outputDestinations.addElement( userIndex );
	}
	public void removeOutputDestination(
			int userIndex
			) {
		m_outputDestinations.removeElement( userIndex );
	}
	
	public void addOutputSource(
			int userIndex
			) {
		m_outputSources.addElement( userIndex );
	}
	public void removeOutputSource(
			int userIndex
			) {
		m_outputSources.removeElement( userIndex );
	}
	
	public Vector<Integer> getInputDestinations() {
		return m_inputDestinations;
	}
	public void setInputDestinations(
			Vector<Integer> inputDestinations
			) {
		m_inputDestinations = inputDestinations;
	}
	
	public Vector<Integer> getInputSources() {
		return m_inputSources;
	}
	public void setInputSources(
			Vector<Integer> inputSources
			) {
		m_inputSources = inputSources;
	}
	
	public Vector<Integer> getOutputDestinations() {
		return m_outputDestinations;
	}
	public void setOutputDestinations(
			Vector<Integer> outputDestinations
			) {
		m_outputDestinations = outputDestinations;
	}
	
	public Vector<Integer> getOutputSources() {
		return m_outputSources;
	}
	public void setOutputSources(
			Vector<Integer> outputSources
			) {
		m_outputSources = outputSources;
	}
	
}