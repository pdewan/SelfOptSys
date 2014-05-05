package selfoptsys.overlay;

import java.util.*;

import selfoptsys.*;
import selfoptsys.network.*;

import commonutils.config.*;

public class AnOverlayManager 
	implements OverlayManager {

	private Map<Integer, AnOverlayManagerUserInfo> m_userInfos;
	
	/*
	 * Needed for hybrid architectures. The big problem is when we have a master
	 * user who does not input, but some of the slaves of the master do input.
	 * When a slave user inputs, the master user must send inputs to the other
	 * masters - maintaining this takes some bookkeeping code!
	 */
	private AnOverlayManagerUserInfo m_specialMasterUserMappingUpdatesUserInfo;
	private Vector<AMapping> m_specialMasterUserMappingUpdates;
	
	public AnOverlayManager() {
		m_userInfos = new Hashtable<Integer, AnOverlayManagerUserInfo>();
		
		m_specialMasterUserMappingUpdates = new Vector<AMapping>();
	}
	
	/*
	 * This method makes several assumptions:
	 * 1) the architecture is either centralized or replicated
	 * 2) it assumes that PerfEstimator.ROOT_USER_INDEX is an inputting user
	 * 3) in a centralized architecture, it assumes PerfEstimator.ROOT_USER_INDEX is the master
	 */
	public ProcessingArchitectureType getProcessingArchitecture(
			int rootUserIndex
			) {
		ProcessingArchitectureType processingArchitecture = ProcessingArchitectureType.CENTRALIZED;
		AnOverlayManagerUserInfo rootUser = m_userInfos.get( rootUserIndex );
		if ( rootUser.getInputDestMappings().size() > 0 ) {
			processingArchitecture = ProcessingArchitectureType.REPLICATED;
		}
		else {
			processingArchitecture = ProcessingArchitectureType.CENTRALIZED;
		}
		return processingArchitecture;
	}

	/*
	 * This method needs a lot of work. Right now, it assumes
	 * 1) that user PerfEstimator.ROOT_USER_INDEX is the only inputting user
	 * 2) that user PerfEstimator.ROOT_USER_INDEX is the root of the overlay
	 * 3) in the centralized architecture, user PerfEstimator.ROOT_USER_INDEX is the master
	 */
	public Overlay getCurrentOverlay(
			int rootUserIndex
			) {

		int numUsers = m_userInfos.size();
		int[] parents = new int[numUsers];
		int[] numChildren = new int[numUsers];
		int[] addOrder = new int[numUsers];
		Map<Integer, List<Integer>> children = new Hashtable<Integer, List<Integer>>();
		
		int addOrderIndex = 0;
		addOrder[addOrderIndex] = rootUserIndex;
		addOrderIndex++;
		parents[rootUserIndex] = -1;
		
		Vector<Integer> nextIndexToProcess = new Vector<Integer>();
		nextIndexToProcess.add( rootUserIndex );
		while (nextIndexToProcess.size() > 0 ) {
			int curIndexToProcess = nextIndexToProcess.elementAt( 0 );
			AnOverlayManagerUserInfo curUserInfo = m_userInfos.get( curIndexToProcess );
			nextIndexToProcess.remove( 0 );
			
			Vector<AMapping> curIndexMappings;
			if ( getProcessingArchitecture( rootUserIndex ) == ProcessingArchitectureType.REPLICATED ) {
				curIndexMappings = curUserInfo.getInputDestMappings();
			}
			else {
				curIndexMappings = curUserInfo.getOutputDestMappings();
			}
			if ( curIndexMappings == null) {
				continue;
			}
			
			numChildren[curIndexToProcess] = curIndexMappings.size();
            List<Integer> childrenList = new LinkedList<Integer>();
            children.put(
                    curIndexToProcess,
                    childrenList
                    );

            for ( int i = 0; i < curIndexMappings.size(); i++ ) {
				int childIndex = curIndexMappings.get( i ).getUserIndex();
				addOrder[addOrderIndex] = childIndex;
				addOrderIndex++;
				nextIndexToProcess.add( childIndex );
				parents[childIndex] = curIndexToProcess;
				childrenList.add( childIndex );
			}
		}
		
		Overlay overlay = new AnOverlay(
				parents,
				addOrder,
				numChildren,
				children
				);
		return overlay;
		
	}

	public void userJoiningAsMaster(
			int newUserIndex, 
			Logger newUserLogger,
			boolean inputsCommands
			) {
		
		boolean includeSpecialMappings = false;
		m_specialMasterUserMappingUpdatesUserInfo = null;
		m_specialMasterUserMappingUpdates.clear();
		
		AnOverlayManagerUserInfo newUserInfo = new AnOverlayManagerUserInfo(
				newUserIndex,
				newUserLogger,
				newUserIndex,
				newUserLogger
				);
		newUserInfo.setUserInputsCommands( inputsCommands );
		
		Iterator<Map.Entry<Integer, AnOverlayManagerUserInfo>> userInfoIterator;
		
		userInfoIterator = m_userInfos.entrySet().iterator();
		while ( userInfoIterator.hasNext() ) {

			AnOverlayManagerUserInfo existingUserInfo = userInfoIterator.next().getValue();
			
			if ( isUserMaster( existingUserInfo ) ) {
				if ( newUserInfo.getUserInputsCommands()  
						|| newUserInfo.getSlaveOfUserInputsCommands() ) {
					newUserInfo.addInputDestMapping( new AMapping( existingUserInfo.getUserIndex(), existingUserInfo.getUserLogger() ) );
					existingUserInfo.addInputSourceMapping( new AMapping( newUserIndex, newUserLogger ) );
				}
				
				if ( existingUserInfo.getUserInputsCommands()  
						|| existingUserInfo.getSlaveOfUserInputsCommands() ) {
					existingUserInfo.addInputDestMapping( new AMapping( newUserIndex, newUserLogger ) );
					newUserInfo.addInputSourceMapping( new AMapping( existingUserInfo.getUserIndex(), existingUserInfo.getUserLogger() ) );
				}
			}
			else {
				if ( existingUserInfo.getMasterUserIndex() == newUserIndex ) {
					
					/*
					 * If slave happened to join before master
					 */
					if ( existingUserInfo.getMasterUserLogger() == null ) {
						existingUserInfo.setMasterUserLogger( newUserLogger );
					}
					
					newUserInfo.addOutputDestMapping( new AMapping( existingUserInfo.getUserIndex(), existingUserInfo.getUserLogger() ) );
					existingUserInfo.addOutputSourceMapping( new AMapping( newUserIndex, newUserLogger ) );
					
					if ( existingUserInfo.getUserInputsCommands() ) {
						newUserInfo.addInputSourceMapping( new AMapping( existingUserInfo.getUserIndex(), existingUserInfo.getUserLogger() ) );
						existingUserInfo.addInputDestMapping( new AMapping( newUserIndex, newUserLogger ) );
						
						if ( !newUserInfo.getSlaveOfUserInputsCommands() ) {
							newUserInfo.setSlaveOfUserInputsCommands( true );
							includeSpecialMappings = true;
						}
					}
				}
			}
			
		}
		
		if ( includeSpecialMappings ) {
			userInfoIterator = m_userInfos.entrySet().iterator();
			while ( userInfoIterator.hasNext() ) {
				AnOverlayManagerUserInfo userInfo = userInfoIterator.next().getValue();
				
				if ( !isUserMaster( userInfo ) ) {
					continue;
				}
				
				newUserInfo.addInputDestMapping(
						new AMapping( userInfo.getUserIndex(), userInfo.getUserLogger() ) );
				userInfo.addInputSourceMapping( 
						new AMapping( newUserIndex, newUserLogger ) );
			}
		}
		
		m_userInfos.put( newUserIndex, newUserInfo );
	}

	public void userJoiningAsSlave(
			int newUserIndex, 
			Logger newUserLogger,
			int masterUserIndex,
			boolean inputsCommands
			) {

		m_specialMasterUserMappingUpdatesUserInfo = null;
		m_specialMasterUserMappingUpdates.clear();
		
		Logger masterUserLogger = null;
		AnOverlayManagerUserInfo masterUserInfo = m_userInfos.get( masterUserIndex );
		if ( masterUserInfo != null ) {
			masterUserLogger = masterUserInfo.getUserLogger();
		}
		
		AnOverlayManagerUserInfo newUserInfo = new AnOverlayManagerUserInfo(
				newUserIndex,
				newUserLogger,
				masterUserIndex,
				masterUserLogger
				);
		newUserInfo.setUserInputsCommands( inputsCommands );
		
		/*
		 * If master has not joined yet, the master will setup the mappings
		 */
		if ( masterUserLogger == null ) {
			m_userInfos.put( newUserIndex, newUserInfo );
			return;
		}
		
		newUserInfo.addOutputSourceMapping( new AMapping( masterUserInfo.getUserIndex(), masterUserInfo.getUserLogger() ) );
		masterUserInfo.addOutputDestMapping( new AMapping( newUserIndex, newUserLogger ) );
		if ( newUserInfo.getUserInputsCommands() ) {
			newUserInfo.addInputDestMapping( new AMapping( masterUserInfo.getUserIndex(), masterUserInfo.getUserLogger() ) );
			masterUserInfo.addInputSourceMapping( new AMapping( newUserIndex, newUserLogger ) );

			if ( !masterUserInfo.getUserInputsCommands()
					&& !masterUserInfo.getSlaveOfUserInputsCommands() ) {
				masterUserInfo.setSlaveOfUserInputsCommands( true );
				addInputDestMappingsForMaster( masterUserInfo, true );
			}
			
		}
		
		m_userInfos.put( newUserIndex, newUserInfo );
	}
	
	public void removeAllMappingsForUser(
			int userIndex
			) {
		boolean includeSpecialMappings = false;
		m_specialMasterUserMappingUpdatesUserInfo = null;
		m_specialMasterUserMappingUpdates.clear();

		Iterator<Map.Entry<Integer, AnOverlayManagerUserInfo>> userInfoIterator;
		
		userInfoIterator = m_userInfos.entrySet().iterator();
		while ( userInfoIterator.hasNext() ) {
			AnOverlayManagerUserInfo userInfo = userInfoIterator.next().getValue();
			if ( userInfo.getUserIndex() != userIndex ) {
				userInfo.removeAllMappingsForUser( userIndex );
			}
		}
		
		AnOverlayManagerUserInfo userInfo = m_userInfos.get( userIndex );
		AnOverlayManagerUserInfo masterUserInfo = m_userInfos.get( userInfo.getMasterUserIndex() );
		if ( userInfo.getUserInputsCommands() 
				&& !isUserMaster( userInfo )
				&& !masterUserInfo.getUserInputsCommands() ) {
			
			includeSpecialMappings = isUserLastInputtingSlaveOfNonInputtingMaster(
					masterUserInfo,
					userInfo
					);
		}
		
		if ( includeSpecialMappings ) {
			for ( int i = 0; i < masterUserInfo.getInputDestMappings().size(); i++ ) {
				m_specialMasterUserMappingUpdates.add( masterUserInfo.getInputDestMappings().elementAt( i ) );
				AnOverlayManagerUserInfo tempInfo = 
					m_userInfos.get( masterUserInfo.getInputDestMappings().elementAt( i ).getUserIndex() );
				tempInfo.removeInputSourceMapping( masterUserInfo.getUserIndex() );
			}
			m_specialMasterUserMappingUpdatesUserInfo = masterUserInfo;
			masterUserInfo.getInputDestMappings().clear();
			masterUserInfo.setSlaveOfUserInputsCommands( false );
		}
	}
	
	public void userLeaving(
			int userIndex
			) {
		m_userInfos.remove( userIndex );
	}

	public Vector<AMapping> getInputDestMappingsForUser(
			int userIndex
			) {
		AnOverlayManagerUserInfo userInfo = m_userInfos.get( userIndex );
		return userInfo.getInputDestMappings();
	}
	
	public void setInputDestMappingsForUser(
			int userIndex,
			Vector<AMapping> inputDestMappings
			) {
		AnOverlayManagerUserInfo userInfo = m_userInfos.get( userIndex );
		for ( int i = 0; i < inputDestMappings.size(); i++ ) {
			userInfo.addInputDestMapping( inputDestMappings.elementAt( i ) );
		}
	}

	public Vector<AMapping> getInputSourceMappingsForUser(
			int userIndex
			) {
		AnOverlayManagerUserInfo userInfo = m_userInfos.get( userIndex );
		return userInfo.getInputSourceMappings();
	}
	
	public void setInputSourceMappingsForUser(
			int userIndex,
			Vector<AMapping> inputSourceMappings
			) {
		AnOverlayManagerUserInfo userInfo = m_userInfos.get( userIndex );
		for ( int i = 0; i < inputSourceMappings.size(); i++ ) {
			userInfo.addInputSourceMapping( inputSourceMappings.elementAt( i ) );
		}
	}

	public Vector<AMapping> getOutputDestMappingsForUser(
			int userIndex
			) {
		AnOverlayManagerUserInfo userInfo = m_userInfos.get( userIndex );
		return userInfo.getOutputDestMappings();
	}

	public void setOutputDestMappingsForUser(
			int userIndex,
			Vector<AMapping> outputDestMappings
			) {
		AnOverlayManagerUserInfo userInfo = m_userInfos.get( userIndex );
		for ( int i = 0; i < outputDestMappings.size(); i++ ) {
			userInfo.addOutputDestMapping( outputDestMappings.elementAt( i ) );
		}
	}

	public Vector<AMapping> getOutputSourceMappingsForUser(
			int userIndex
			) {
		AnOverlayManagerUserInfo userInfo = m_userInfos.get( userIndex );
		return userInfo.getOutputSourceMappings();
	}
	
	public void setOutputSourceMappingsForUser(
			int userIndex,
			Vector<AMapping> outputSourceMappings
			) {
		AnOverlayManagerUserInfo userInfo = m_userInfos.get( userIndex );
		for ( int i = 0; i < outputSourceMappings.size(); i++ ) {
			userInfo.addOutputSourceMapping( outputSourceMappings.elementAt( i ) );
		}
	}

	private boolean isUserMaster(
			AnOverlayManagerUserInfo userInfo
			) {
		return ( userInfo.getUserIndex() == userInfo.getMasterUserIndex() );
	}
	
	public AnOverlayManagerUserInfo getSpecialMasterUserMappingUpdatesUserInfo() {
		return m_specialMasterUserMappingUpdatesUserInfo;
	}
	
	public Vector<AMapping> getSpecialMasterUserMappingUpdates() {
		return m_specialMasterUserMappingUpdates;
	}
	
	public void updateUserInputsCommands(
			int userIndex,
			boolean inputsCommands
			) {
		
		m_specialMasterUserMappingUpdatesUserInfo = null;
		m_specialMasterUserMappingUpdates.clear();
		
		AnOverlayManagerUserInfo userInfo = m_userInfos.get( userIndex );
		userInfo.setUserInputsCommands( inputsCommands );

		if ( inputsCommands ) {
			if ( !isUserMaster( userInfo ) ) {
				AnOverlayManagerUserInfo masterUserInfo = m_userInfos.get( userInfo.getMasterUserIndex() );
				masterUserInfo.addInputSourceMapping(
						new AMapping( userInfo.getUserIndex(), userInfo.getUserLogger() ) );
				userInfo.addInputDestMapping(
						new AMapping( masterUserInfo.getUserIndex(), masterUserInfo.getUserLogger() ) );
				
				if ( !masterUserInfo.getUserInputsCommands() &&
						!masterUserInfo.getSlaveOfUserInputsCommands() ) {
					m_specialMasterUserMappingUpdatesUserInfo = masterUserInfo;
					addInputDestMappingsForMaster( masterUserInfo, true );
					masterUserInfo.setSlaveOfUserInputsCommands( true );
				}
			}
			else {
				addInputDestMappingsForMaster( userInfo, false );
				m_specialMasterUserMappingUpdates.clear();
				m_specialMasterUserMappingUpdatesUserInfo = null;
			}
		}
		else {
			if ( !isUserMaster( userInfo ) ) {
				AnOverlayManagerUserInfo masterUserInfo = m_userInfos.get( userInfo.getMasterUserIndex() );
				masterUserInfo.removeInputSourceMapping( userInfo.getUserIndex() );
				userInfo.removeInputDestMapping( masterUserInfo.getUserIndex() );
				
				boolean includeSpecialMappings = false;
				if ( !masterUserInfo.getUserInputsCommands() ) {
					includeSpecialMappings = isUserLastInputtingSlaveOfNonInputtingMaster(
							masterUserInfo,
							userInfo
							);
				}
				
				if ( includeSpecialMappings ) {
					for ( int i = 0; i < masterUserInfo.getInputDestMappings().size(); i++ ) {
						m_specialMasterUserMappingUpdates.add( masterUserInfo.getInputDestMappings().elementAt( i ) );
						AnOverlayManagerUserInfo tempInfo = 
							m_userInfos.get( masterUserInfo.getInputDestMappings().elementAt( i ).getUserIndex() );
						tempInfo.removeInputSourceMapping( masterUserInfo.getUserIndex() );
					}
					m_specialMasterUserMappingUpdatesUserInfo = masterUserInfo;
					masterUserInfo.getInputDestMappings().clear();
					masterUserInfo.setSlaveOfUserInputsCommands( false );
				}
			}
			else {
				for ( int i = 0; i < userInfo.getInputDestMappings().size(); i++ ) {
					AnOverlayManagerUserInfo tempInfo = 
						m_userInfos.get( userInfo.getInputDestMappings().elementAt( i ).getUserIndex() );
					tempInfo.removeInputSourceMapping( userInfo.getUserIndex() );
				}
				userInfo.getInputDestMappings().clear();
			}
		}
		
	}
	
	private void addInputDestMappingsForMaster(
			AnOverlayManagerUserInfo masterUserInfo,
			boolean specialMappings
			) {
		if ( specialMappings ) {
			m_specialMasterUserMappingUpdatesUserInfo = masterUserInfo;
			m_specialMasterUserMappingUpdates.clear();
		}
		
		Iterator<Map.Entry<Integer, AnOverlayManagerUserInfo>> userInfoIterator = m_userInfos.entrySet().iterator();
		while ( userInfoIterator.hasNext() ) {
			AnOverlayManagerUserInfo currentUserInfo = userInfoIterator.next().getValue();
			if ( currentUserInfo.getUserIndex() == currentUserInfo.getMasterUserIndex() &&
					currentUserInfo.getUserIndex() != masterUserInfo.getUserIndex() ) {
				currentUserInfo.addInputSourceMapping(
					new AMapping( masterUserInfo.getUserIndex(), masterUserInfo.getUserLogger() ) );
				AMapping mapping = new AMapping(
						currentUserInfo.getUserIndex(), currentUserInfo.getUserLogger() );
				masterUserInfo.addInputDestMapping( mapping );
				
				if ( specialMappings ) {
					m_specialMasterUserMappingUpdates.add( mapping );
				}
			}
		}
	}
	
	private boolean isUserLastInputtingSlaveOfNonInputtingMaster(
			AnOverlayManagerUserInfo masterUserInfo,
			AnOverlayManagerUserInfo slaveUserInfo
			) {
		boolean lastInputtingUser = true;
		
		Iterator<Map.Entry<Integer, AnOverlayManagerUserInfo>>userInfoIterator = m_userInfos.entrySet().iterator();
		while ( userInfoIterator.hasNext() ) {
			AnOverlayManagerUserInfo currentUserInfo = userInfoIterator.next().getValue();
			if ( currentUserInfo.getUserIndex() == masterUserInfo.getUserIndex()
					&& currentUserInfo.getUserIndex() != slaveUserInfo.getUserIndex()
					&& currentUserInfo.getUserInputsCommands() ) {
				lastInputtingUser = false;
				break;
			}
		}
		
		return lastInputtingUser;
	}

}
