package selfoptsys.overlay;

import java.util.*;

import selfoptsys.network.*;

import commonutils.config.*;

public class AGlobalOverlayManager 
	implements GlobalOverlayManager {

	private final int INPUT_DEST_MAPPINGS = 0;
	private final int INPUT_SOURCE_MAPPINGS = 1;
	private final int OUTPUT_DEST_MAPPINGS = 2;
	private final int OUTPUT_SOURCE_MAPPINGS = 3;
	
	Map<Integer, AGlobalOverlayManagerUserInfo> m_userInfos;
	
	public AGlobalOverlayManager() {
		m_userInfos = new Hashtable<Integer, AGlobalOverlayManagerUserInfo>();
	}
	
	public void userJoining(
			int joiningUserIndex,
			int masterIndexOfJoiningUser,
			boolean inputsCommands
			) {

		AGlobalOverlayManagerUserInfo joiningUserInfo = new AGlobalOverlayManagerUserInfo(
				joiningUserIndex,
				masterIndexOfJoiningUser,
				inputsCommands
				);
		m_userInfos.put( 
				joiningUserIndex,
				joiningUserInfo
				);
		
		if ( inputsCommands ) {
			buildSingleSourceOverlayForUser( joiningUserInfo );
		}
			
		updateAllSingleSourceOverlaysWithJoiningUser( joiningUserInfo );
	}
	
	public void userLeaving(
			int leavingUserIndex
			) {

		AGlobalOverlayManagerUserInfo leavingUserInfo = m_userInfos.get( leavingUserIndex );
		m_userInfos.remove( leavingUserIndex );
		
		updateAllSingleSourceOverlaysWithLeavingUser( leavingUserInfo );
	}

	public Overlay getCurrentOverlay(
			int rootUserIndex
			) {

		AGlobalOverlayManagerUserInfo rootUserInfo = m_userInfos.get( rootUserIndex );
		if ( rootUserInfo == null ) {
			return null;
		}
		
		ProcessingArchitectureType procArchType = getProcessingArchitectureType();
		if ( procArchType != ProcessingArchitectureType.REPLICATED && 
				procArchType != ProcessingArchitectureType.CENTRALIZED ) {
			return null;
		}
		
		SingleSourceOverlay singleSourceOverlay = rootUserInfo.getSingleSourceOverlay();
		
		int numUsers = m_userInfos.size();
		int[] parents = new int[numUsers];
		int[] numChildren = new int[numUsers];
		int[] addOrder = new int[numUsers];
        Map<Integer, List<Integer>> children = new Hashtable<Integer, List<Integer>>();
		
		int addOrderIndex = 0;
		int firstAddedIndex = rootUserIndex;
		if ( procArchType == ProcessingArchitectureType.CENTRALIZED ) {
		    while ( singleSourceOverlay.getOutputSourceMappingsForUser( firstAddedIndex ).size() > 0 ) {
		        firstAddedIndex = singleSourceOverlay.getOutputSourceMappingsForUser( firstAddedIndex ).get( 0 );
		    }
		}

		addOrder[addOrderIndex] = firstAddedIndex;
		addOrderIndex++;
		parents[firstAddedIndex] = -1;
		
		Vector<Integer> nextIndexToProcess = new Vector<Integer>();
		nextIndexToProcess.add( firstAddedIndex );
		while ( nextIndexToProcess.size() > 0 ) {
			int curIndexToProcess = nextIndexToProcess.elementAt( 0 );
			nextIndexToProcess.remove( 0 );
			
			Vector<Integer> curIndexMappings;
			if ( procArchType == ProcessingArchitectureType.REPLICATED ) {
				curIndexMappings = singleSourceOverlay.getInputDestMappingsForUser( curIndexToProcess );
			}
			else {
				curIndexMappings = singleSourceOverlay.getOutputDestMappingsForUser( curIndexToProcess );
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
				int childIndex = curIndexMappings.elementAt( i );
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
	
	public Vector<AnOverlayMapping> getInputDestMappingsForUser(
			int userIndex
			) {
		Vector<AnOverlayMapping> inputDestMappings = getMappingsForUser(
				userIndex,
				INPUT_DEST_MAPPINGS
				);
		return inputDestMappings;
	}
	
	public void setInputDestMappingsForUser(
			int userIndex,
			Vector<AnOverlayMapping> inputDestMappings
			) {
		setMappingsForUser(
				userIndex,
				inputDestMappings,
				INPUT_DEST_MAPPINGS
				);
	}
	
	public Vector<AnOverlayMapping> getInputDestMappingsForUser(
			int userIndex,
			int rootUserIndex
			) {
		Vector<AnOverlayMapping> inputDestMappings = getMappingsForUser(
				userIndex,
				rootUserIndex,
				INPUT_DEST_MAPPINGS
				);
		return inputDestMappings;
	}
	
	public Vector<AnOverlayMapping> getInputSourceMappingsForUser(
			int userIndex
			) {
		Vector<AnOverlayMapping> inputSourceMappings = getMappingsForUser(
				userIndex,
				INPUT_SOURCE_MAPPINGS
				);
		return inputSourceMappings;
	}
	
	public void setInputSourceMappingsForUser(
			int userIndex,
			Vector<AnOverlayMapping> inputSourceMappings
			) {
		setMappingsForUser(
				userIndex,
				inputSourceMappings,
				INPUT_SOURCE_MAPPINGS
				);
	}
	
	public Vector<AnOverlayMapping> getInputSourceMappingsForUser(
			int userIndex,
			int rootUserIndex
			) {
		Vector<AnOverlayMapping> inputSourceMappings = getMappingsForUser(
				userIndex,
				rootUserIndex,
				INPUT_SOURCE_MAPPINGS
				);
		return inputSourceMappings;
	}

	public Vector<AnOverlayMapping> getOutputDestMappingsForUser(
			int userIndex
			) {
		Vector<AnOverlayMapping> outputDestMappings = getMappingsForUser(
				userIndex,
				OUTPUT_DEST_MAPPINGS
				);
		return outputDestMappings;
	}
	
	public void setOutputDestMappingsForUser(
			int userIndex,
			Vector<AnOverlayMapping> outputDestMappings
			) {
		setMappingsForUser(
				userIndex,
				outputDestMappings,
				OUTPUT_DEST_MAPPINGS
				);
	}
	
	public Vector<AnOverlayMapping> getOutputDestMappingsForUser(
			int userIndex,
			int rootUserIndex
			) {
		Vector<AnOverlayMapping> outputDestMappings = getMappingsForUser(
				userIndex,
				rootUserIndex,
				OUTPUT_DEST_MAPPINGS
				);
		return outputDestMappings;
	}
	
	public Vector<AnOverlayMapping> getOutputSourceMappingsForUser(
			int userIndex
			) {
		Vector<AnOverlayMapping> outputSourceMappings = getMappingsForUser(
				userIndex,
				OUTPUT_SOURCE_MAPPINGS
				);
		return outputSourceMappings;
	}
	
	public void setOutputSourceMappingsForUser(
			int userIndex,
			Vector<AnOverlayMapping> outputSourceMappings
			) {
		setMappingsForUser(
				userIndex,
				outputSourceMappings,
				OUTPUT_SOURCE_MAPPINGS
				);
	}
	
	public Vector<AnOverlayMapping> getOutputSourceMappingsForUser(
			int userIndex,
			int rootUserIndex
			) {
		Vector<AnOverlayMapping> outputSourceMappings = getMappingsForUser(
				userIndex,
				rootUserIndex,
				OUTPUT_SOURCE_MAPPINGS
				);
		return outputSourceMappings;
	}
	
	private Vector<AnOverlayMapping> getMappingsForUser(
			int userIndex,
			int mappingType
			) {
		Vector<AnOverlayMapping> allMappings = new Vector<AnOverlayMapping>();
		
		Iterator<Map.Entry<Integer, AGlobalOverlayManagerUserInfo>> userInfoIterator =
			m_userInfos.entrySet().iterator();
		while ( userInfoIterator.hasNext() ) {
			AGlobalOverlayManagerUserInfo userInfo = userInfoIterator.next().getValue();
			
			Vector<AnOverlayMapping> mappings = getMappingsForUser(
					userIndex,
					userInfo.getUserIndex(),
					mappingType
					);
			
			for ( int i = 0; i < mappings.size(); i++ ) {
				allMappings.add( mappings.elementAt( i ) );
			}
		}
		
		return allMappings;
	}
	
	private Vector<AnOverlayMapping> getMappingsForUser(
			int userIndex,
			int rootUserIndex,
			int mappingType
			) {
		Vector<AnOverlayMapping> allMappings = new Vector<AnOverlayMapping>();
		
		AGlobalOverlayManagerUserInfo userInfo = m_userInfos.get( rootUserIndex );
		
		if ( !userInfo.isInputtingCommands() ) {
			return allMappings;
		}
		Vector<Integer> mappings = null;
		if ( mappingType == INPUT_DEST_MAPPINGS ) {
			mappings = userInfo.getSingleSourceOverlay().getInputDestMappingsForUser( userIndex );
		}
		else if ( mappingType == INPUT_SOURCE_MAPPINGS ) {
			mappings = userInfo.getSingleSourceOverlay().getInputSourceMappingsForUser( userIndex );
		}
		else if ( mappingType == OUTPUT_DEST_MAPPINGS ) {
			mappings = userInfo.getSingleSourceOverlay().getOutputDestMappingsForUser( userIndex );
		}
		else if ( mappingType == OUTPUT_SOURCE_MAPPINGS ) {
			mappings = userInfo.getSingleSourceOverlay().getOutputSourceMappingsForUser( userIndex );
		}
		
		for ( int i = 0; i < mappings.size(); i++ ) {
			allMappings.add( 
					new AnOverlayMapping ( 
							userInfo.getUserIndex(), 
							mappings.elementAt( i ) ) );
		}
		
		return allMappings;
	}

	private void setMappingsForUser(
			int userIndex,
			Vector<AnOverlayMapping> mappings,
			int mappingType
			) {
		Map<Integer, Vector<Integer>> mappingsPerRoot = 
			new Hashtable<Integer, Vector<Integer>>();
		
		for ( int i = 0; i < mappings.size(); i++ ) {
			int rootUserIndex = mappings.elementAt( i ).getRootUserIndex();
			Vector<Integer> rootUserMappings = mappingsPerRoot.get( rootUserIndex );
			if ( rootUserMappings == null ) {
				rootUserMappings = new Vector<Integer>();
				mappingsPerRoot.put( 
						rootUserIndex,
						rootUserMappings
						);
			}
			rootUserMappings.add( mappings.elementAt( i ).getUserIndex() );
		}
		
		Iterator<Map.Entry<Integer, Vector<Integer>>> mappingIterator = 
			mappingsPerRoot.entrySet().iterator();
		while ( mappingIterator.hasNext() ) {
			Map.Entry<Integer, Vector<Integer>> rootUserEntry = mappingIterator.next();
			SingleSourceOverlay rootUserOverlay = 
				m_userInfos.get( rootUserEntry.getKey() ).getSingleSourceOverlay();
			
			if ( mappingType == INPUT_DEST_MAPPINGS ) {
				rootUserOverlay.setInputDestMappingsForUser(
						userIndex, 
						rootUserEntry.getValue()
						);
			}
			else if ( mappingType == INPUT_SOURCE_MAPPINGS ) {
				rootUserOverlay.setInputSourceMappingsForUser(
						userIndex, 
						rootUserEntry.getValue()
						);
			}
			else if ( mappingType == OUTPUT_DEST_MAPPINGS ) {
				rootUserOverlay.setOutputDestMappingsForUser(
						userIndex, 
						rootUserEntry.getValue()
						);
			}
			else if ( mappingType == OUTPUT_SOURCE_MAPPINGS ) {
				rootUserOverlay.setOutputSourceMappingsForUser(
						userIndex, 
						rootUserEntry.getValue()
						);
			}
		}
		
	}
	
	public void updateUserInputsCommands(
			int userIndex,
			boolean inputsCommands
			) {
		
		AGlobalOverlayManagerUserInfo userInfo = m_userInfos.get( userIndex );
		
		userInfo.setInputsCommands( inputsCommands );
		
		if ( inputsCommands ) {
			buildSingleSourceOverlayForUser( userInfo );
		}
		else {
			userInfo.setSingleSourceOverlay( null );
		}
		
	}
	
	private void buildSingleSourceOverlayForUser(
			AGlobalOverlayManagerUserInfo userInfo
			) {
		
		SingleSourceOverlay userSingleSourceOverlay = userInfo.getSingleSourceOverlay();
		if ( userSingleSourceOverlay == null ) {
			userSingleSourceOverlay = new ASingleSourceOverlay(
					userInfo.getUserIndex(),
					userInfo.getMasterIndex()
					);
		}
		
		Iterator<Map.Entry<Integer, AGlobalOverlayManagerUserInfo>> userInfoIterator =
			m_userInfos.entrySet().iterator();
		while ( userInfoIterator.hasNext() ) {
			AGlobalOverlayManagerUserInfo existingUserInfo = userInfoIterator.next().getValue();
			
			if ( existingUserInfo.isMaster() ) {
				userSingleSourceOverlay.userJoiningAsMaster(
						existingUserInfo.getUserIndex()
						);
			}
			else {
				userSingleSourceOverlay.userJoiningAsSlave(
						existingUserInfo.getUserIndex(),
						existingUserInfo.getMasterIndex()
						);
			}
		}
		
		if ( userInfo.getSingleSourceOverlay() == null ) {
			userInfo.setSingleSourceOverlay( userSingleSourceOverlay );
		}
	}
	
	private void updateAllSingleSourceOverlaysWithJoiningUser(
			AGlobalOverlayManagerUserInfo joiningUserInfo
			) {
		
		Iterator<Map.Entry<Integer, AGlobalOverlayManagerUserInfo>> userInfoIterator =
			m_userInfos.entrySet().iterator();
		while ( userInfoIterator.hasNext() ) {
			AGlobalOverlayManagerUserInfo existingUserInfo = userInfoIterator.next().getValue();
			
			if ( !existingUserInfo.isInputtingCommands() || 
					existingUserInfo.getUserIndex() == joiningUserInfo.getUserIndex() ) {
				continue;
			}
			
			if ( joiningUserInfo.isMaster() ) {
				existingUserInfo.getSingleSourceOverlay().userJoiningAsMaster(
						joiningUserInfo.getUserIndex()
						);
			}
			else {
				existingUserInfo.getSingleSourceOverlay().userJoiningAsSlave(
						joiningUserInfo.getUserIndex(),
						joiningUserInfo.getMasterIndex()
						);
			}
		}
		
	}
	
	private void updateAllSingleSourceOverlaysWithLeavingUser(
			AGlobalOverlayManagerUserInfo leavingUserInfo
			) {
		
		Iterator<Map.Entry<Integer, AGlobalOverlayManagerUserInfo>> userInfoIterator =
			m_userInfos.entrySet().iterator();
		while ( userInfoIterator.hasNext() ) {
			AGlobalOverlayManagerUserInfo existingUserInfo = userInfoIterator.next().getValue();
			
			if ( !existingUserInfo.isInputtingCommands() ) {
				continue;
			}
			
			existingUserInfo.getSingleSourceOverlay().userLeaving(
					leavingUserInfo.getUserIndex()
					);
		}

	}
	
	private ProcessingArchitectureType getProcessingArchitectureType() {

		int numMasters = 0;
		
		Iterator<Map.Entry<Integer, AGlobalOverlayManagerUserInfo>> userInfoIterator = 
			m_userInfos.entrySet().iterator();
		while ( userInfoIterator.hasNext() ) {
			AGlobalOverlayManagerUserInfo userInfo = userInfoIterator.next().getValue();
			
			if ( userInfo.isMaster() ) {
				numMasters++;
			}
		}
		
		if ( numMasters == m_userInfos.size() ) {
			return ProcessingArchitectureType.REPLICATED;
		}
		else if ( numMasters == 1 ) {
			return ProcessingArchitectureType.CENTRALIZED;
		}
		else {
			return ProcessingArchitectureType.HYBRID;
		}
	}
	
	public void setupRelayerCommArch(
			int relayerUserIndex
			) {
		AGlobalOverlayManagerUserInfo userInfo;
		
		Vector<Integer> userIndices = new Vector<Integer>();
		Iterator<Integer> itr = 
			m_userInfos.keySet().iterator();
		while ( itr.hasNext() ) {
			userIndices.add( itr.next() );
		}

		ProcessingArchitectureType processingArchitecture = getProcessingArchitectureType();
		if ( processingArchitecture == ProcessingArchitectureType.CENTRALIZED ) {

			int masterUserIndex = getMasterUserIndex();
			
			for ( int userIndex = 0; userIndex < userIndices.size(); userIndex++ ) {

				userInfo = m_userInfos.get( userIndex );
				if ( userInfo.isInputtingCommands() == false ) {
					continue;
				}
				
				Vector<AnOverlayMapping> mappings = new Vector<AnOverlayMapping>();
				
				if ( userIndex == masterUserIndex ) { continue; }
				mappings.add( new AnOverlayMapping( userIndex, masterUserIndex ) );
				
				setInputDestMappingsForUser(
						userIndex,
						mappings
						);
			}
			
			for ( int userIndex = 0; userIndex < userIndices.size(); userIndex++ ) {
				Vector<AnOverlayMapping> mappings = new Vector<AnOverlayMapping>();
				
				if ( userIndex != masterUserIndex ) { continue; }
				for ( int rootUserIndex = 0; rootUserIndex < userIndices.size(); rootUserIndex++ ) {

					userInfo = m_userInfos.get( rootUserIndex );
					if ( userInfo.isInputtingCommands() == false ) {
						continue;
					}
					
					if ( rootUserIndex == masterUserIndex ) { continue; }
					mappings.add( new AnOverlayMapping( rootUserIndex, rootUserIndex ) );
				}
				
				setInputSourceMappingsForUser(
						userIndex,
						mappings
						);
			}
			
			for ( int userIndex = 0; userIndex < userIndices.size(); userIndex++ ) {
				Vector<AnOverlayMapping> mappings = new Vector<AnOverlayMapping>();
				
				if ( userIndex != masterUserIndex && userIndex != relayerUserIndex ) { continue; }
				if ( userIndex == masterUserIndex ) {
					for ( int rootUserIndex = 0; rootUserIndex < userIndices.size(); rootUserIndex++ ) {

						userInfo = m_userInfos.get( rootUserIndex );
						if ( userInfo.isInputtingCommands() == false ) {
							continue;
						}
						
						mappings.add( new AnOverlayMapping( rootUserIndex, relayerUserIndex ) );
					}
				}
				else if ( userIndex == relayerUserIndex ) {
					for ( int rootUserIndex = 0; rootUserIndex < userIndices.size(); rootUserIndex++ ) {

						userInfo = m_userInfos.get( rootUserIndex );
						if ( userInfo.isInputtingCommands() == false ) {
							continue;
						}
						
						for ( int destUserIndex = 0; destUserIndex < userIndices.size(); destUserIndex++ ) {
							if ( destUserIndex == masterUserIndex || destUserIndex == relayerUserIndex ) { continue; }
							mappings.add( new AnOverlayMapping( rootUserIndex, destUserIndex ) );
						}
					}
				}

				setOutputDestMappingsForUser(
						userIndex,
						mappings
						);
			}
			
			for ( int userIndex = 0; userIndex < userIndices.size(); userIndex++ ) {
				Vector<AnOverlayMapping> mappings = new Vector<AnOverlayMapping>();
				
				if ( userIndex == masterUserIndex ) { continue; }
				if ( userIndex == relayerUserIndex ) {
					for ( int rootUserIndex = 0; rootUserIndex < userIndices.size(); rootUserIndex++ ) {

						userInfo = m_userInfos.get( rootUserIndex );
						if ( userInfo.isInputtingCommands() == false ) {
							continue;
						}
						
						mappings.add( new AnOverlayMapping( rootUserIndex, masterUserIndex ) );
					}
				}
				else {
					for ( int rootUserIndex = 0; rootUserIndex < userIndices.size(); rootUserIndex++ ) {

						userInfo = m_userInfos.get( rootUserIndex );
						if ( userInfo.isInputtingCommands() == false ) {
							continue;
						}
						
						mappings.add( new AnOverlayMapping( rootUserIndex, relayerUserIndex ) );
					}
				}
				
				setOutputSourceMappingsForUser(
						userIndex,
						mappings
						);
			}
			
		}
		else if ( processingArchitecture == ProcessingArchitectureType.REPLICATED ) {
			
			for ( int userIndex = 0; userIndex < userIndices.size(); userIndex++ ) {

				userInfo = m_userInfos.get( userIndex );
				if ( userInfo.isInputtingCommands() == false ) {
					continue;
				}
				
				Vector<AnOverlayMapping> mappings = new Vector<AnOverlayMapping>();
				
				if ( userIndex != relayerUserIndex ) {
					mappings.add( new AnOverlayMapping( userIndex, relayerUserIndex ) );
				}
				else {
					for ( int rootUserIndex = 0; rootUserIndex < userIndices.size(); rootUserIndex++ ) {

						userInfo = m_userInfos.get( rootUserIndex );
						if ( userInfo.isInputtingCommands() == false ) {
							continue;
						}
						
						for ( int destUserIndex = 0; destUserIndex < userIndices.size(); destUserIndex++ ) {
							if ( rootUserIndex == destUserIndex || destUserIndex == relayerUserIndex ) { continue; }
							mappings.add( new AnOverlayMapping( rootUserIndex, destUserIndex ) );
						}
					}
				}

				setInputDestMappingsForUser(
						userIndex,
						mappings
						);
			}
			
			for ( int userIndex = 0; userIndex < userIndices.size(); userIndex++ ) {
				Vector<AnOverlayMapping> mappings = new Vector<AnOverlayMapping>();
				
				if ( userIndex != relayerUserIndex ) {
					for ( int j = 0; j < userIndices.size(); j++ ) {

						userInfo = m_userInfos.get( j );
						if ( userInfo.isInputtingCommands() == false ) {
							continue;
						}
						
						if ( userIndex == j ) { continue; }
						mappings.add( new AnOverlayMapping( j, relayerUserIndex ) );
					}
				}
				else {
					for ( int rootUserIndex = 0; rootUserIndex < userIndices.size(); rootUserIndex++ ) {

						userInfo = m_userInfos.get( rootUserIndex );
						if ( userInfo.isInputtingCommands() == false ) {
							continue;
						}
						
						if ( rootUserIndex == relayerUserIndex ) { continue; }
						mappings.add( new AnOverlayMapping( rootUserIndex, rootUserIndex ) );
					}
				}
				
				setInputSourceMappingsForUser(
						userIndex,
						mappings
						);
			}
			
		}

	}
	
	public void setupOverlayForRootUserIndices(
			Map<Integer, Overlay> overlaysForRootUserIndices
			) {
		Map<Integer, Vector<AnOverlayMapping>> inputDestMappings = new HashMap<Integer, Vector<AnOverlayMapping>>();
		Map<Integer, Vector<AnOverlayMapping>> inputSourceMappings = new HashMap<Integer, Vector<AnOverlayMapping>>();
		Map<Integer, Vector<AnOverlayMapping>> outputDestMappings = new HashMap<Integer, Vector<AnOverlayMapping>>();
		Map<Integer, Vector<AnOverlayMapping>> outputSourceMappings = new HashMap<Integer, Vector<AnOverlayMapping>>();
		
		ProcessingArchitectureType processingArchitecture = getProcessingArchitectureType();
		int masterUserIndex = getMasterUserIndex();
				
		Iterator<Map.Entry<Integer, Overlay>> itr = overlaysForRootUserIndices.entrySet().iterator();
		while ( itr.hasNext() ) {
			
			Map.Entry<Integer, Overlay> entry = itr.next();
			int rootUserIndex = entry.getKey();
			
			AGlobalOverlayManagerUserInfo userInfo = m_userInfos.get( rootUserIndex );
			if ( userInfo.isInputtingCommands() == false ) {
				continue;
			}
			
			Overlay overlay = entry.getValue();
			
			Vector<AnOverlayMapping> mappings = null;
			
			for ( int i = 0; i < overlay.getAddOrder().length; i++ ) {
				int parentIndex = overlay.getAddOrder()[i];
				for ( int j = 0; j < overlay.getParents().length; j++ ) {
					if ( overlay.getParents()[j] == parentIndex ) {
						if ( processingArchitecture == ProcessingArchitectureType.REPLICATED ) {
							mappings = inputDestMappings.get( parentIndex );
							if ( mappings == null ) {
								mappings = new Vector<AnOverlayMapping>();
								inputDestMappings.put( parentIndex, mappings );
							}
							mappings.add( new AnOverlayMapping( rootUserIndex, j ) );

							mappings = inputSourceMappings.get( j );
							if ( mappings == null ) {
								mappings = new Vector<AnOverlayMapping>();
								inputSourceMappings.put( j, mappings );
							}
							mappings.add( new AnOverlayMapping( rootUserIndex, parentIndex ) );
						}
						else if ( processingArchitecture == ProcessingArchitectureType.CENTRALIZED ) {
							mappings = outputDestMappings.get( parentIndex );
							if ( mappings == null ) {
								mappings = new Vector<AnOverlayMapping>();
								outputDestMappings.put( parentIndex, mappings );
							}
							mappings.add( new AnOverlayMapping( rootUserIndex, j ) );
							
							mappings = outputSourceMappings.get( j );
							if ( mappings == null ) {
								mappings = new Vector<AnOverlayMapping>();
								outputSourceMappings.put( j, mappings );
							}
							mappings.add( new AnOverlayMapping( rootUserIndex, parentIndex ) );
						}
					}
				}
			}
			
			if ( processingArchitecture == ProcessingArchitectureType.CENTRALIZED ) {
				if ( rootUserIndex == masterUserIndex ) {
					continue;
				}
				
				mappings = inputDestMappings.get( rootUserIndex );
				if ( mappings == null ) {
					mappings = new Vector<AnOverlayMapping>();
					inputDestMappings.put( rootUserIndex, mappings );
				}
				mappings.add( new AnOverlayMapping( rootUserIndex, masterUserIndex ) );
				
				mappings = inputSourceMappings.get( masterUserIndex );
				if ( mappings == null ) {
					mappings = new Vector<AnOverlayMapping>();
					inputSourceMappings.put( masterUserIndex, mappings );
				}
				mappings.add( new AnOverlayMapping( rootUserIndex, rootUserIndex ) );
			}
			
		}
		
		Iterator<Map.Entry<Integer, Vector<AnOverlayMapping>>> mappingItr = null;
		
		mappingItr = inputDestMappings.entrySet().iterator();
		while ( mappingItr.hasNext() ) {
			Map.Entry<Integer, Vector<AnOverlayMapping>> entry = mappingItr.next();
			setInputDestMappingsForUser(
					entry.getKey(),
					entry.getValue()
					);
		}

		mappingItr = inputSourceMappings.entrySet().iterator();
		while ( mappingItr.hasNext() ) {
			Map.Entry<Integer, Vector<AnOverlayMapping>> entry = mappingItr.next();
			setInputSourceMappingsForUser(
					entry.getKey(),
					entry.getValue()
					);
		}

		mappingItr = outputDestMappings.entrySet().iterator();
		while ( mappingItr.hasNext() ) {
			Map.Entry<Integer, Vector<AnOverlayMapping>> entry = mappingItr.next();
			setOutputDestMappingsForUser(
					entry.getKey(),
					entry.getValue()
					);
		}

		mappingItr = outputSourceMappings.entrySet().iterator();
		while ( mappingItr.hasNext() ) {
			Map.Entry<Integer, Vector<AnOverlayMapping>> entry = mappingItr.next();
			setOutputSourceMappingsForUser(
					entry.getKey(),
					entry.getValue()
					);
		}

	}
	
	private int getMasterUserIndex() {
		int masterUserIndex = -1;
		
		Iterator<Map.Entry<Integer, AGlobalOverlayManagerUserInfo>> userInfoIterator = 
			m_userInfos.entrySet().iterator();
		while ( userInfoIterator.hasNext() ) {
			AGlobalOverlayManagerUserInfo userInfo = userInfoIterator.next().getValue();
			
			if ( userInfo.isMaster() ) {
				masterUserIndex = userInfo.getUserIndex();
				break;
			}
		}

		return masterUserIndex;
	}

}

class AGlobalOverlayManagerUserInfo {
	
	private int m_userIndex;
	private int m_masterIndex;
	private boolean m_isInputtingCommands;
	private SingleSourceOverlay m_singleSourceOverlay;
	
	public AGlobalOverlayManagerUserInfo(
			int userIndex,
			int masterIndex,
			boolean inputsCommands
			) {
		m_userIndex = userIndex;
		m_masterIndex = masterIndex;
		m_isInputtingCommands = inputsCommands;
		
		m_singleSourceOverlay = new ASingleSourceOverlay(
				userIndex,
				masterIndex
				);
	}
	
	public int getUserIndex() {
		return m_userIndex;
	}
	
	public int getMasterIndex() {
		return m_masterIndex;
	}
	public void setMasterIndex(
			int masterIndex
			) {
		m_masterIndex = masterIndex;
	}
	
	public boolean isInputtingCommands() {
		return m_isInputtingCommands;
	}
	public void setInputsCommands(
			boolean inputsCommands
			) {
		m_isInputtingCommands = inputsCommands;
	}
	
	public boolean isMaster() {
		return m_userIndex == m_masterIndex;
	}
	
	public SingleSourceOverlay getSingleSourceOverlay() {
		return m_singleSourceOverlay;
	}
	public void setSingleSourceOverlay(
			SingleSourceOverlay singleSourceOverlay
			) {
		m_singleSourceOverlay = singleSourceOverlay;
	}
	
}
