package selfoptsys.network;

import java.io.*;
import java.util.*;

import commonutils.basic.*;

public class ANetworkGenerator {

	public static NetworkInfo generateNetwork(
			int numComputers,
			int numComputerTypes,
			int[] computerTypePercentages,
			int[] usersComputerTypes,
			int sourceComputer,
			int sourceComputerType,
			double sourceComputerInputProcCost,
			double sourceComputerOutputProcCost,
			double sourceComputerInputTransCost,
            double sourceComputerInputTransCostToFirstDest,
            double sourceComputerObservedInputTransCost,
            double sourceComputerObservedInputTransCostToFirstDest,
            double sourceComputerOutputTransCost,
			double sourceComputerOutputTransCostToFirstDest,
            double sourceComputerObservedOutputTransCost,
            double sourceComputerObservedOutputTransCostToFirstDest,
			double[] inputProcCostValues,
			double[] outputProcCostValues,
			double[] inputTransCostValues,
            double[] inputTransCostValuesToFirstDest,
            double[] observedInputTransCostValues,
            double[] observedInputTransCostValuesToFirstDest,
			double[] outputTransCostValues,
            double[] outputTransCostValuesToFirstDest,
            double[] observedOutputTransCostValues,
            double[] observedOutputTransCostValuesToFirstDest,
			double[][] networkLatencyMatrix,
            boolean useNetworkLatencyMatrixAsIs,
            boolean useNetworkLatencyClusters,
            String[] networkLatencyClusters,
            int[] networkLatencyClusterHeads,
            boolean useNetworkLatencyMatrixWithinCluster,
            boolean communicationAllowedWithinCluster,
            boolean useNetworkLatencyMatrixAcrossClusters,
            double networkLatencyWithinCluster,
            double networkLatencyAcrossClusters,
            boolean overrideSomeNetworkLatencies,
            String[] networkLatencyOverrides,
            String[] strCoreUsedByProcessingThread,
            String[] strCoreUsedByProcessingThreadForEachComputerType,
            String[] strCoreUsedByTransmissionThread,
            String[] strCoreUsedByTransmissionThreadForEachComputerType
			) {
	    
		NetworkInfo nwInfo = new ANetworkInfo();
		
		nwInfo.setNumComputers( numComputers );
		nwInfo.setSourceComputer( sourceComputer );
		
		int[] compTypes = usersComputerTypes;
		if ( usersComputerTypes == null ) {
		    compTypes = generateCompTypes(
		            numComputers,
		            numComputerTypes,
		            computerTypePercentages
		            );
		    compTypes[ sourceComputer ] = sourceComputerType;
		}
				
		double[] inputProcCosts = generateRealisticCosts(
				numComputers,
				compTypes,
				inputProcCostValues
				);
		inputProcCosts[ sourceComputer ] = sourceComputerInputProcCost;
		nwInfo.setInputProcCosts( inputProcCosts );
		
		double[] outputProcCosts = generateRealisticCosts(
				numComputers,
				compTypes,
				outputProcCostValues
				);
		outputProcCosts[ sourceComputer ] = sourceComputerOutputProcCost;
		nwInfo.setOutputProcCosts( outputProcCosts );
		
		double[] inputTransCosts = generateRealisticCosts(
				numComputers,
				compTypes,
				inputTransCostValues
				);
		inputTransCosts[ sourceComputer ] = sourceComputerInputTransCost;
		nwInfo.setInputTransCosts( inputTransCosts );
		
        double[] inputTransCostsToFirstDest = generateRealisticCosts(
                numComputers,
                compTypes,
                inputTransCostValuesToFirstDest
                );
        inputTransCostsToFirstDest[ sourceComputer ] = sourceComputerInputTransCostToFirstDest;
        nwInfo.setInputTransCostsToFirstDest( inputTransCostsToFirstDest );
		
        double[] observedInputTransCosts = generateRealisticCosts(
                numComputers,
                compTypes,
                observedInputTransCostValues
                );
        observedInputTransCosts[ sourceComputer ] = sourceComputerObservedInputTransCost;
        nwInfo.setObservedInputTransCosts( observedInputTransCosts );

        double[] observedInputTransCostsToFirstDest = generateRealisticCosts(
                numComputers,
                compTypes,
                observedInputTransCostValuesToFirstDest
                );
        observedInputTransCostsToFirstDest[ sourceComputer ] = sourceComputerObservedInputTransCostToFirstDest;
        nwInfo.setObservedInputTransCostsToFirstDest( observedInputTransCostsToFirstDest );

        double[] outputTransCosts = generateRealisticCosts(
				numComputers,
				compTypes,
				outputTransCostValues
				);
		outputTransCosts[ sourceComputer ] = sourceComputerOutputTransCost;
		nwInfo.setOutputTransCosts( outputTransCosts );

        double[] outputTransCostsToFirstDest = generateRealisticCosts(
                numComputers,
                compTypes,
                outputTransCostValuesToFirstDest
                );
        outputTransCostsToFirstDest[ sourceComputer ] = sourceComputerOutputTransCostToFirstDest;
		nwInfo.setOutputTransCostsToFirstDest( outputTransCostsToFirstDest );
		
        double[] observedOutputTransCosts = generateRealisticCosts(
                numComputers,
                compTypes,
                observedOutputTransCostValues
                );
        observedOutputTransCosts[ sourceComputer ] = sourceComputerObservedOutputTransCost;
        nwInfo.setObservedOutputTransCosts( observedOutputTransCosts );

        double[] observedOutputTransCostsToFirstDest = generateRealisticCosts(
                numComputers,
                compTypes,
                observedOutputTransCostValuesToFirstDest
                );
        observedOutputTransCostsToFirstDest[ sourceComputer ] = sourceComputerObservedOutputTransCostToFirstDest;
        nwInfo.setObservedOutputTransCostsToFirstDest( observedOutputTransCostsToFirstDest );

        
        
        
		double[][] networkLatencies = null;
	    if ( useNetworkLatencyMatrixAsIs ) {
	        networkLatencies = networkLatencyMatrix;
	    }
	    else {
	        networkLatencies = generateNetworkLatenciesFromMatrix(
				numComputers,
				networkLatencyMatrix
				);
	    }
	    if ( useNetworkLatencyClusters ) {
	        int[] computerClusters = getComputerClusters(
	                numComputers,
	                networkLatencyClusters
	                );
	        List<Integer> clusterHeads = new LinkedList<Integer>();
	        for ( int i = 0; i < networkLatencyClusterHeads.length; i++ ) {
	            clusterHeads.add( networkLatencyClusterHeads[ i ] );
	        }
	        
	        if ( useNetworkLatencyMatrixWithinCluster == false ) {
	            for ( int i = 0; i < numComputers; i++ ) {
	                for ( int j = i; j < numComputers; j++ ) {
	                    if ( computerClusters[i] == computerClusters[j] ) {
	                        networkLatencies[i][j] = networkLatencies[j][i] = networkLatencyWithinCluster;
	                    }
	                }
	            }
	        }
	        if ( communicationAllowedWithinCluster == false ) {
                for ( int i = 0; i < numComputers; i++ ) {
                    if ( clusterHeads.contains( i  ) ) {
                        continue;
                    }
                    for ( int j = 0; j < numComputers; j++ ) {
                        if ( computerClusters[i] == computerClusters[j] ) {
                            networkLatencies[i][j] = 100000;
                        }
                    }
                }
	        }
	        
            if ( useNetworkLatencyMatrixAcrossClusters == false ) {
                for ( int i = 0; i < numComputers; i++ ) {
                    for ( int j = i; j < numComputers; j++ ) {
                        if ( computerClusters[i] != computerClusters[j] ) {
                            networkLatencies[i][j] = networkLatencies[j][i] = networkLatencyAcrossClusters;
                        }
                    }
                }
            }
	    }
	    if ( overrideSomeNetworkLatencies ) {
	        Vector<NetworkLatencySpec> overrides = getNetworkLatencyOverrides(
	                networkLatencyOverrides
	                );
	        for ( int i = 0; i < overrides.size(); i++ ) {
	            NetworkLatencySpec spec = overrides.get( i );
	            networkLatencies[ spec.SourceUser ][ spec.DestUser ] =
	                networkLatencies[ spec.DestUser ][ spec.SourceUser ] = 
	                    spec.Latency;
	                    
	        }
	    }
		nwInfo.setNetworkLatencies( networkLatencies );
		
		Map<Integer, Integer> coreUsedByProcessingThread = new Hashtable<Integer, Integer>();
		for ( int i = 0; i < numComputers; i++ ) {
            int val = 0;
            if ( strCoreUsedByProcessingThread.length < numComputers ) {
                val = Integer.parseInt( strCoreUsedByProcessingThreadForEachComputerType[ compTypes[ i ] ] );
            }
            else {
                val = Integer.parseInt( strCoreUsedByProcessingThread[ i ] );
            }
		    
            coreUsedByProcessingThread.put(
                    i,
                    val
                    );
		}
		nwInfo.setCoreUsedByProcessingThread( coreUsedByProcessingThread );
		
        Map<Integer, Integer> coreUsedByTransmissionThread = new Hashtable<Integer, Integer>();
        for ( int i = 0; i < numComputers; i++ ) {
        	int val = 0;
            if ( strCoreUsedByTransmissionThread.length < numComputers ) {
                val = Integer.parseInt( strCoreUsedByTransmissionThreadForEachComputerType[ compTypes[ i ] ] );
            }
            else {
                val = Integer.parseInt( strCoreUsedByTransmissionThread[ i ] );
            }
            
            coreUsedByTransmissionThread.put(
                    i,
                    val
                    );
        }
        nwInfo.setCoreUsedByTransmissionThread( coreUsedByTransmissionThread );
        
        Map<Integer, Boolean> procAndTransThreadShareCores = new Hashtable<Integer, Boolean>();
        Iterator<Map.Entry<Integer, Integer>> itr = coreUsedByProcessingThread.entrySet().iterator();
        while ( itr.hasNext() ) {
            Map.Entry<Integer, Integer> entry = itr.next();
            int userIndex = entry.getKey();
            int tmpCoreUsedByProcessingThread = entry.getValue();
            int tmpCoreUsedByTransmissionThread = coreUsedByTransmissionThread.get( userIndex );
            procAndTransThreadShareCores.put(
                    userIndex,
                    tmpCoreUsedByProcessingThread == tmpCoreUsedByTransmissionThread
                    );
        }
        nwInfo.setProcAndTransThreadShareCores( procAndTransThreadShareCores );

        return nwInfo;
		
	}
	
    static int[] generateCompTypes(
    		int numComputers,
    		int numComputerTypes,
    		int[] computerTypePercentages
    		) {
        
    	Random rand = new Random( System.nanoTime() );
    	
        int[] compTypes = new int[ numComputers ];
        for ( int i = 0; i < numComputers; i++ ) {
        	int randInt = rand.nextInt( 100 );
        	compTypes[i] = getIntBucket(
        			randInt,
        			computerTypePercentages
        			);
        }
        
        return compTypes;
        
    }
    
    static int[] generateNumCores(
    		int numComputers,
    		int[] numCoresValues,
    		int[] numCoresPercentages
    		) {
        
    	Random rand = new Random( System.nanoTime() );
    	
        int[] numCores = new int[ numComputers ];
        for ( int i = 0; i < numComputers; i++ ) {
        	int randInt = rand.nextInt( 100 );
        	int numCoresBucket = getIntBucket(
        			randInt,
        			numCoresPercentages
        			);
        	numCores[i] = numCoresValues[ numCoresBucket ];
        }
        
        return numCores;
        
    }
    
    static double[] generateRealisticCosts( 
    		int numComputers,
    		int[] computerTypes,
    		double[] costValues
    		) {
    	
        double[] costs = new double[ numComputers ];
        
        for ( int i = 0; i < numComputers; i++ ) {
            costs[i] = costValues[ computerTypes[ i ] ];
        }
        
        return costs;
        
    }

    public static double[][] readInNetworkRttMatrixFromFile(
    		String networkRttMatrixFile
    		) {
    	
    	if ( networkRttMatrixFile == null ) {
    		return null;
    	}
    	
    	Vector<String> networkLatencyMatrixRows = new Vector<String>();
    	double[][] networkLatencyMatrix;
    	
    	try {
    		File f = new File( networkRttMatrixFile );
    		if ( f.exists() == false ) {
	    		return null;
    		}
    		
    		FileReader fr = new FileReader( networkRttMatrixFile );
            BufferedReader br = new BufferedReader( fr );

            String line = null;
            while ( ( line = br.readLine() ) != null ) {
            	networkLatencyMatrixRows.add( line );
            }

            br.close();
            fr.close();
    	}
    	catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "ANetworkGenerator: Error while reading in rtt matrix from file",
                    e
                    );
    		return null;
    	}
    	
    	int numNodesInMatrix = networkLatencyMatrixRows.size();
    	networkLatencyMatrix = new double[ numNodesInMatrix ][ numNodesInMatrix ];
    	
    	for ( int i = 0; i < numNodesInMatrix; i++ ) {
			String[] networkLatencyMatrixRowValues = networkLatencyMatrixRows.elementAt( i ).split( " " ); 
    		for ( int j = 0; j < numNodesInMatrix; j++ ) {
    			networkLatencyMatrix[ i ][ j ] = Double.parseDouble( networkLatencyMatrixRowValues[ j ] );
    			if ( networkLatencyMatrix[ i ][ j ] > 0 ) {
    				networkLatencyMatrix[ i ][ j ] /= 2;
	    			networkLatencyMatrix[ i ][ j ] /= 1000;
	    			networkLatencyMatrix[ i ][ j ] = MathUtils.round( networkLatencyMatrix[ i ][ j ], 3 );
    			}
    		}
    	}
    	
    	networkLatencyMatrixRows = null;
    	
    	
    	return networkLatencyMatrix;
    	
    }
    
    public static double[][] generateNetworkLatenciesFromMatrix(
    		int numComputers,
    		double[][] networkLatencyMatrix
    		) {
    	
        double[][] networkLatencies = new double[ numComputers ][ numComputers ];
    	
        if ( numComputers > networkLatencyMatrix.length ) {
        	return null;
        }
        
        Random rand = new Random( System.nanoTime() );
        Vector<Integer> indicesUsed = new Vector<Integer>();
        
        while ( indicesUsed.size() < numComputers ) {
        	int nextIndex = rand.nextInt( networkLatencyMatrix.length );
        	
        	if ( indicesUsed.contains( nextIndex ) ) {
        		continue;
        	}
        	
        	indicesUsed.add( nextIndex );
        	for ( int j = 0; j < indicesUsed.size(); j++ ) {
        		networkLatencies[j][indicesUsed.size() - 1] =
        			networkLatencies[indicesUsed.size() - 1][j] =
        				networkLatencyMatrix[indicesUsed.elementAt(j)][nextIndex];
        	}
        }
        
        return networkLatencies;
        
    }
    
    private static int getIntBucket(
    		int value,
    		int[] buckets
    		) {
    	
    	int curLowBucketBoundary = 0;
    	int curHighBucketBoundary = -1;
    	
    	for ( int i = 0; i < buckets.length; i++ ) {
    		curLowBucketBoundary = curHighBucketBoundary + 1;
    		curHighBucketBoundary += buckets[i];
    		if ( curLowBucketBoundary <= value && value <= curHighBucketBoundary) {
    			return i;
    		}
    		
    	}
    	
    	return -1;
    	
    }
    
    private static int[] getComputerClusters(
            int numComputers,
            String[] networkLatencyClustersSpec
            ) {
        int[] computerClusters = new int[ numComputers ];
        
        for ( int i = 0; i < networkLatencyClustersSpec.length; i++ ) {
            Vector<Integer> userIndices = new Vector<Integer>();
            int clusterIndex = Integer.parseInt( networkLatencyClustersSpec[i].split( " " )[0] );
            String[] compIndexSpec = networkLatencyClustersSpec[i].split( " " )[1].split( "," );
            
            for ( int j = 0; j < compIndexSpec.length; j++ ) {
                if ( compIndexSpec[j].contains( "-" ) ) {
                    int startIndex = Integer.parseInt( compIndexSpec[j].split( "-" )[0] );
                    int endIndex = Integer.parseInt( compIndexSpec[j].split( "-" )[1] );
                    for ( int k = startIndex; k <= endIndex; k++ ) {
                        userIndices.add( k );
                    }
                }
                else {
                    userIndices.add( Integer.parseInt( compIndexSpec[j] ) );
                }
            }
            
            for ( int j = 0; j < userIndices.size(); j++ ) {
                computerClusters[ userIndices.get(j) ] = clusterIndex;
            }
        }
        
        return computerClusters;
    }
	
    private static Vector<NetworkLatencySpec> getNetworkLatencyOverrides(
            String[] networkLatencyOverridesSpec
            ) {
        Vector<NetworkLatencySpec> latencyOverrides = new Vector<NetworkLatencySpec>();
        
        for ( int i = 0; i < networkLatencyOverridesSpec.length; i++ ) {
            Vector<Integer> compIndices = new Vector<Integer>();
            double latency = Integer.parseInt( networkLatencyOverridesSpec[i].split( " " )[0] );
            String[] compIndexSpec = networkLatencyOverridesSpec[i].split( " " )[1].split( "," );
            
            for ( int j = 0; j < compIndexSpec.length; j++ ) {
                if ( compIndexSpec[j].contains( "-" ) ) {
                    int startIndex = Integer.parseInt( compIndexSpec[j].split( "-" )[0] );
                    int endIndex = Integer.parseInt( compIndexSpec[j].split( "-" )[1] );
                    for ( int k = startIndex; k <= endIndex; k++ ) {
                        compIndices.add( k );
                    }
                }
                else {
                    compIndices.add( Integer.parseInt( compIndexSpec[j] ) );
                }
            }
            
            for ( int j = 0; j < compIndices.size(); j++ ) {
                for ( int k = 0; k < compIndices.size(); k++ ) {
                    NetworkLatencySpec spec = new NetworkLatencySpec();
                    spec.SourceUser = compIndices.get( j );
                    spec.DestUser = compIndices.get( k );
                    spec.Latency = latency;
                    latencyOverrides.add( spec );
                }
            }
        }
        
        return latencyOverrides;
    }
    
}

class NetworkLatencySpec {
    public int SourceUser;
    public int DestUser;
    public double Latency;
}
