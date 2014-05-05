package commonutils.basic;

import java.util.*;
import commonutils.config.*;


public class MiscUtils {

	public static int getMasterIndexWhenUserIsJoining(
	        ProcessingArchitectureType architecture,
			int[] masterUsers,
			int userIndex
			) {
		int masterIndex = -1;
		
		if ( architecture == ProcessingArchitectureType.REPLICATED ) {
			masterIndex = userIndex;
		}
		else if ( architecture == ProcessingArchitectureType.CENTRALIZED ) {
			masterIndex = masterUsers[0];
		}
		else if ( architecture == ProcessingArchitectureType.HYBRID ) {
			if ( masterUsers != null ) {
				masterIndex = masterUsers[ userIndex ];
			}
			else {
			    ErrorHandlingUtils.logFineMessageAndContinue( 
			            "MiscUtils::getMasterUserIndexWhenUserIsJoining\n" +
			            "    Master for user " + userIndex + " not specified in config file." + 
			            "    Defaulting to joining user " + userIndex + " as master."
			            );
				masterIndex = userIndex;
			}
		}
		
		return masterIndex;
	}
	
	public static int[] getUserSlaveAssignments(
	        int numUsers,
			int[] userAssignmentSpec
			) {
		int[] usersAssignments = new int[ numUsers ];
		for ( int i = 0; i < userAssignmentSpec.length; i += 3 ) {
			int startIndex = userAssignmentSpec[i];
			int endIndex = userAssignmentSpec[i+1];
			int slaveIndex = userAssignmentSpec[i+2];
			
			for ( int j = startIndex; j <= endIndex; j++ ) {
				usersAssignments[j] = slaveIndex;
			}
		}
		
		return usersAssignments;
	}
	
	public static int getClusterForUserWhenJoining(
	        int[] userClusterSpec,
			int userIndex
		) {
		int usersCluster = 0;
		boolean clusterSpecifiedForUser = false;
		
		if ( userClusterSpec != null ) {
			for ( int i = 0; i < userClusterSpec.length; i += 3 ) {
				int startIndex = userClusterSpec[i];
				int endIndex = userClusterSpec[i+1];
				int clusterIndex = userClusterSpec[i+2];
				
				if ( startIndex <= userIndex && userIndex <= endIndex ) {
					clusterSpecifiedForUser = true;
					usersCluster = clusterIndex;
					break;
				}
			}
		}
		
		if ( !clusterSpecifiedForUser ) {
            ErrorHandlingUtils.logFineMessageAndContinue( 
                    "MiscUtils::getClusterForUserWhenJoining\n" +
                    "    Cluster for user " + userIndex + " not specified in config file." + 
                    "    Defaulting to joining user " + userIndex + " into cluster 0."
                    );
		}
		
		return usersCluster;
	}
	
    public static double[] makeArrayCopy( double[] source ) {
        double[] dest = new double[ source.length ];
        
        for ( int i = 0; i < source.length; i++ ) {
            dest[i] = source[i];
        }
        
        return dest;
    }

    public static double[][] makeArrayCopy( double[][] source ) {
        double[][] dest = new double[ source.length ][ source.length ];
        
        for ( int i = 0; i < source.length; i++ ) {
            for ( int j = 0; j < source.length; j++ ) {
                dest[i][j] = source[i][j];
            }
        }
        
        return dest;
    }
    
    public static int getNumDecimalPoints( double d ) {
        int numDecPoints = 0;
        double dTemp = d;
        double dTemp2 = 0;
        
        while (true) {
            dTemp2 = ((int) dTemp * 10)/10;
            if ( dTemp == dTemp2 ) {
                break;
            }
            dTemp = dTemp * 10;
            numDecPoints++;
        }
        
        return numDecPoints;
    }
    
    public static int[] getSpecifiedUserIndices(
            String[] specifiedUserIndices 
        ) {
        int[] userIndices = null;
        Vector<Integer> listOfUserIndices = new Vector<Integer>();
        
        if ( specifiedUserIndices == null ) {
            return null;
        }
        
        for ( int i = 0; i < specifiedUserIndices.length; i++ ) {
            String curVal = specifiedUserIndices[i];
            if ( curVal.contains( "-" ) ) {
                int startIndex = Integer.parseInt( curVal.split( "-" )[0] );
                int endIndex = Integer.parseInt( curVal.split( "-" )[1] );
                for ( int j = startIndex; j <= endIndex; j++ ) {
                    if ( !listOfUserIndices.contains( j ) ) {
                        listOfUserIndices.add( j );
                    }
                }
            }
            else {
                int index = Integer.parseInt( curVal );
                if ( !listOfUserIndices.contains( index ) ) {
                    listOfUserIndices.add( index );
                }
            }
        }
        
        userIndices = new int[ listOfUserIndices.size() ];
        for ( int i = 0; i < listOfUserIndices.size(); i++ ) {
            userIndices[i] = listOfUserIndices.elementAt( i );
        }
        
        return userIndices;
    }
    
    public static int bigEndianByteArrayToInt(byte[] b, int offset) {
        int value = 0;
        for (int i = 0; i < 4; i++) {
            int shift = (4 - 1 - i) * 8;
            value += (b[i + offset] & 0x000000FF) << shift;
        }
        return value;
    }
    
    public static byte[] intToBigEndianByteArray(int value) {
    	int byteNum = ( 40 - Integer.numberOfLeadingZeros ( value < 0 ? ~value : value ) ) / 8;
		byte[] byteArray = new byte[4];
		for (int n = 0; n < byteNum; n++) {
			byteArray[3 - n] = (byte) ( value >>> ( n * 8 ) );
		}
		return byteArray;
    }
    
    public static short bigEndianByteArrayToShort(byte[] b, int offset) {
        short value = 0;
        for (int i = 0; i < 2; i++) {
            int shift = (2 - 1 - i) * 8;
            value += (b[i + offset] & 0x00FF) << shift;
        }
        return value;
    }

    public static final long toLong(
            byte[] byteArray, 
            int offset, 
            int len
            ) {
        long val = 0;
        len = Math.min(len, 8);
        for ( int i = (len - 1); i >= 0; i-- ) {
            val <<= 8;
            val |= (byteArray [offset + i] & 0x00FF);
        }
        return val;}
    
    public static short swapEndienness( short value )
	{
	    int b1 = value & 0xff;
	    int b2 = (value >> 8) & 0xff;

	    return (short) (b1 << 8 | b2 << 0);
	}
	
	public static int swapEndienness( int value )
	{
	    int b1 = (value >>  0) & 0xff;
	    int b2 = (value >>  8) & 0xff;
	    int b3 = (value >> 16) & 0xff;
	    int b4 = (value >> 24) & 0xff;

	    return b1 << 24 | b2 << 16 | b3 << 8 | b4 << 0;
	}
	
	/*
	 * Algorithm based on the ZeroDecode function in
	 * http://openmetaverse.org/svn/omf/libopenmetaverse/branches/0.6-devel/OpenMetaverse/Helpers.cs
	 */
	public static byte[] zeroDecode(
			byte[] zeroEncodedData,
			int zeroEncodedDataLength,
			int fullLength
			) {
	
		byte[] zeroDecodedData = null;
		int zerolen = 6;
		int extraSpaceForZeroes = 0;
		 
		for ( int i = zerolen; i < zeroEncodedDataLength; i++ ) {
			if ( zeroEncodedData[i] == 0x00 ) {
				extraSpaceForZeroes += zeroEncodedData[i+1] - 2;
				i++;
			}
			if ( zeroEncodedData[i] == 0x00 && i < zeroEncodedDataLength - 1 && zeroEncodedData[i+1] == 0x00 ) {
				System.out.print( "" );
			}
		}
		
		zeroDecodedData = new byte[ fullLength + extraSpaceForZeroes ];
		for ( int i = 0; i < zerolen; i++ ) {
			zeroDecodedData[i] = zeroEncodedData[i];
		}
		int zeroDecodedDataIndex = 6;
		for ( int i = zerolen; i < zeroEncodedDataLength; i++ ) {
			if ( zeroEncodedData[i] == 0x00 ) {
				for ( int j = 0; j < zeroEncodedData[i+1]; j++ ) {
					zeroDecodedData[zeroDecodedDataIndex] = 0x00;
					zeroDecodedDataIndex++;
				}
				i++;
			}
			else {
				zeroDecodedData[zeroDecodedDataIndex] = zeroEncodedData[i];
				zeroDecodedDataIndex++;
			}
		}
		
		for ( int i = 0; i < fullLength - zeroEncodedDataLength; i++ ) {
			zeroDecodedData[zeroDecodedDataIndex] = zeroEncodedData[zeroEncodedDataLength + i];
			zeroDecodedDataIndex++;
		}
		
		return zeroDecodedData;
		
	}
	
	/*
	 * Algorithm based on the ZeroDecode function in
	 * http://openmetaverse.org/svn/omf/libopenmetaverse/branches/0.6-devel/OpenMetaverse/Helpers.cs
	 */
    public static byte[] zeroEncode(
    		byte[] zeroDecodedData,
    		int zeroDecodedDataLength,
    		int fullLength
    		)
    {
    	Vector<Byte> zeroEncodedDataVector = new Vector<Byte>();
        int zerolen = 6;
        for ( int i = 0; i < zerolen; i++ ) {
        	zeroEncodedDataVector.add( zeroDecodedData[i] );
        }
		 
		for ( int i = zerolen; i < zeroDecodedDataLength; i++ ) {
			if ( zeroDecodedData[i] == 0x00 ) {
				int zeroStartIndex = i;
				int zeroEndIndex = zeroStartIndex;
				for ( int j = i + 1; j < zeroDecodedDataLength; j++ ) {
					if ( zeroDecodedData[j] != 0x00 ) {
						break;
					}
					zeroEndIndex++;
					i++;
				}

				int numZeroes = zeroEndIndex - zeroStartIndex + 1;
				zeroEncodedDataVector.add( new Byte( (byte) 0 ) );
				zeroEncodedDataVector.add( new Byte( (byte) numZeroes ) );
			}
			else {
				zeroEncodedDataVector.add( zeroDecodedData[i] );
			}
		}
		
		
		for ( int i = 0; i < fullLength - zeroDecodedDataLength; i++ ) {
			zeroEncodedDataVector.add( zeroDecodedData[zeroDecodedDataLength + i] );
		}
		
    	byte[] zeroEncodedData = new byte[ zeroEncodedDataVector.size() ];
    	for ( int i = 0; i < zeroEncodedDataVector.size(); i++ ) {
    		zeroEncodedData[i] = zeroEncodedDataVector.elementAt( i );
    	}

    	return zeroEncodedData;
    }
	
	public static int unsignedByteToInt( byte b ) {
		return (int) b & 0xFF;
	}

	public static byte[] hexStringToByteArray( String s ) {
		int len = s.length();
		byte[] data = new byte[ len / 2 ];
		for (int i = 0; i < len; i += 2) {
			data[ i / 2 ] = (byte) ( ( Character.digit( s.charAt( i ), 16 ) << 4 )
					+ Character.digit( s.charAt( i+1 ), 16 ) );
		}
		return data;
	}

	public static int getTimeAtWhichUserIsJoining(
	        int[] timesAtWhichUsersJoinSpec,
	        int userIndex
	        ) {
	    
	    int timeWhichUserIsJoining = 0;

        boolean foundUserIndex = false;
	    for ( int i = 0; i < timesAtWhichUsersJoinSpec.length; i++ ) {
	        if ( timesAtWhichUsersJoinSpec[ i ] < 0 ) {
	            timeWhichUserIsJoining = Math.abs( timesAtWhichUsersJoinSpec[ i ] );
	        }
	        else if ( timesAtWhichUsersJoinSpec[ i ] == userIndex ) {
	            foundUserIndex = true;
	            break;
	        }
	        
	    }
	    
	    if ( foundUserIndex == false ) {
	        return 0;
	    }

	    return timeWhichUserIsJoining;
	    
	}

    public static List<Integer> getSortedTimesAtWhichUsersJoin(
            int[] timesAtWhichUsersJoinSpec
            ) {
        
        List<Integer> timesAtWhichUsersJoin = new LinkedList<Integer>();

        for ( int i = 0; i < timesAtWhichUsersJoinSpec.length; i++ ) {
            if ( timesAtWhichUsersJoinSpec[ i ] < 0 ) {
                timesAtWhichUsersJoin.add( Math.abs( timesAtWhichUsersJoinSpec[ i ] ) );
            }
        }
        
        return timesAtWhichUsersJoin;
        
    }

    public static List<List<Integer>> getUserJoinsByTimeSortedByTime(
            int[] timesAtWhichUsersJoinSpec
            ) {
        
        List<List<Integer>> userJoinsByTime = new LinkedList<List<Integer>>();

        List<Integer> userJoinsForPrevTime = null;
        for ( int i = 0; i < timesAtWhichUsersJoinSpec.length; i++ ) {
            if ( timesAtWhichUsersJoinSpec[ i ] < 0 ) {
                if ( userJoinsForPrevTime != null ) {
                    userJoinsByTime.add( userJoinsForPrevTime );
                }
                userJoinsForPrevTime = new LinkedList<Integer>();
            }
            else {
                if ( userJoinsForPrevTime != null ) {
                    userJoinsForPrevTime.add( timesAtWhichUsersJoinSpec[ i ] );
                }
            }
        }
        if ( userJoinsForPrevTime != null ) {
            userJoinsByTime.add( userJoinsForPrevTime );
        }
        
        return userJoinsByTime;
        
    }

}
