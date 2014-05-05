package selfoptsys.sim.sched;

import java.io.*;
import java.text.*;
import java.util.*;

import selfoptsys.network.*;
import selfoptsys.network.overlay.*;
import selfoptsys.sim.*;

import commonutils.basic2.*;
import commonutils.config.*;


public class ASingleSimulation {

	ConfigParamProcessor m_mainCpp;
	
	String m_outputFile;
	int m_numRepetitions;
	
	int m_sourceComputer;
	boolean m_useUnicastCommunication;
	boolean m_useMulticastCommunication;
	List<SchedulingPolicy> m_schedPolsToUse;
	int m_numComputers;
	
	Vector<Overlay> m_mcastOverlays = new Vector<Overlay>();
    Vector<Overlay> m_ucastOverlays = new Vector<Overlay>();
	Vector<double[]> m_mcastResponseTimesProcessFirst = new Vector<double[]>();
	Vector<double[]> m_mcastResponseTimesTransFirst = new Vector<double[]>();
	Vector<double[]> m_mcastResponseTimesConc = new Vector<double[]>();
	Vector<double[]> m_mcastResponseTimesLazyProcessFirst = new Vector<double[]>();
    Vector<double[]> m_ucastResponseTimesProcessFirst = new Vector<double[]>();
    Vector<double[]> m_ucastResponseTimesTransFirst = new Vector<double[]>();
    Vector<double[]> m_ucastResponseTimesConc = new Vector<double[]>();
    Vector<double[]> m_ucastResponseTimesLazyProcessFirst = new Vector<double[]>();
	
	public ASingleSimulation(
			String outputFile
			) {
		
		m_mainCpp = AMainConfigParamProcessor.getInstance();
		
		m_outputFile = outputFile;
		m_numRepetitions = m_mainCpp.getIntParam( SchedulingSimulationParameters.REPETITIONS );
		
        m_useUnicastCommunication = m_mainCpp.getBooleanParam( SchedulingSimulationParameters.USE_UNICAST_COMMUNICATION );
        m_useMulticastCommunication = m_mainCpp.getBooleanParam( SchedulingSimulationParameters.USE_MULTICAST_COMMUNICATION );
        
        String[] strSchedulingPoliciesToUse = m_mainCpp.getStringArrayParam( SchedulingSimulationParameters.SCHEDULING_POLICIES_TO_USE );
        m_schedPolsToUse = new LinkedList<SchedulingPolicy>();
        for ( int i = 0; i < strSchedulingPoliciesToUse.length; i++ ) {
            m_schedPolsToUse.add( SchedulingPolicy.valueOf( strSchedulingPoliciesToUse[ i ] ) );
        }
        
        m_numComputers = m_mainCpp.getIntParam( SchedulingSimulationParameters.NUM_COMPUTERS );
        m_sourceComputer = m_mainCpp.getIntParam( SchedulingSimulationParameters.SOURCE_COMPUTER );

	}
	
	
	public void run() {
		
		String networkLatencyMatrixFile = m_mainCpp.getStringParam( SchedulingSimulationParameters.NETWORK_LATENCY_MATRIX_FILE );
		boolean useNetworkLatencyMatrixAsIs = m_mainCpp.getBooleanParam( SchedulingSimulationParameters.USE_NETWORK_LATENCY_MATRIX_AS_IS );
		
		double[][] networkLatencyMatrix = ASimulatedLatencyUtils.readInLatenciesFromFile( networkLatencyMatrixFile );
		
		long startTime = System.nanoTime();
		long totalMcastOverlayGenerationTime = 0;
		
		for ( int i = 0; i < m_numRepetitions; i++ ) {
		    
		    int[] computerTypes = m_mainCpp.getIntArrayParam( SchedulingSimulationParameters.USERS_COMPUTER_TYPES );
		    if ( computerTypes.length != m_numComputers ) {
		        computerTypes = null;
		    }
		    
    		NetworkInfo nwInfo = ANetworkGenerator.generateNetwork(
    				m_numComputers,
    				m_mainCpp.getIntParam( SchedulingSimulationParameters.NUM_COMPUTER_TYPES ),
    				m_mainCpp.getIntArrayParam( SchedulingSimulationParameters.COMPUTER_TYPE_PERCETANGES ),
                    computerTypes,
    				m_sourceComputer,
    				m_mainCpp.getIntParam( SchedulingSimulationParameters.SOURCE_COMPUTER_TYPE ),
    				m_mainCpp.getDoubleParam( SchedulingSimulationParameters.SOURCE_COMPUTER_INPUT_PROC_COST ),
    				m_mainCpp.getDoubleParam( SchedulingSimulationParameters.SOURCE_COMPUTER_OUTPUT_PROC_COST ),
    				m_mainCpp.getDoubleParam( SchedulingSimulationParameters.SOURCE_COMPUTER_INPUT_TRANS_COST ),
                    m_mainCpp.getDoubleParam( SchedulingSimulationParameters.SOURCE_COMPUTER_INPUT_TRANS_COST_TO_FIRST_DEST ),
                    m_mainCpp.getDoubleParam( SchedulingSimulationParameters.SOURCE_COMPUTER_OBSERVED_INPUT_TRANS_COST ),
                    m_mainCpp.getDoubleParam( SchedulingSimulationParameters.SOURCE_COMPUTER_OBSERVED_INPUT_TRANS_COST_TO_FIRST_DEST ),
    				m_mainCpp.getDoubleParam( SchedulingSimulationParameters.SOURCE_COMPUTER_OUTPUT_TRANS_COST ),
                    m_mainCpp.getDoubleParam( SchedulingSimulationParameters.SOURCE_COMPUTER_OUTPUT_TRANS_COST_TO_FIRST_DEST ),
                    m_mainCpp.getDoubleParam( SchedulingSimulationParameters.SOURCE_COMPUTER_OBSERVED_OUTPUT_TRANS_COST ),
                    m_mainCpp.getDoubleParam( SchedulingSimulationParameters.SOURCE_COMPUTER_OBSERVED_OUTPUT_TRANS_COST_TO_FIRST_DEST ),
    				m_mainCpp.getDoubleArrayParam( SchedulingSimulationParameters.INPUT_PROC_COSTS ),
    				m_mainCpp.getDoubleArrayParam( SchedulingSimulationParameters.OUTPUT_PROC_COSTS ),
    				m_mainCpp.getDoubleArrayParam( SchedulingSimulationParameters.INPUT_TRANS_COSTS ),
                    m_mainCpp.getDoubleArrayParam( SchedulingSimulationParameters.INPUT_TRANS_COSTS_TO_FIRST_DEST ),
                    m_mainCpp.getDoubleArrayParam( SchedulingSimulationParameters.OBSERVED_INPUT_TRANS_COSTS ),
                    m_mainCpp.getDoubleArrayParam( SchedulingSimulationParameters.OBSERVED_INPUT_TRANS_COSTS_TO_FIRST_DEST ),
    				m_mainCpp.getDoubleArrayParam( SchedulingSimulationParameters.OUTPUT_TRANS_COSTS ),
                    m_mainCpp.getDoubleArrayParam( SchedulingSimulationParameters.OUTPUT_TRANS_COSTS_TO_FIRST_DEST ),
                    m_mainCpp.getDoubleArrayParam( SchedulingSimulationParameters.OBSERVED_OUTPUT_TRANS_COSTS ),
                    m_mainCpp.getDoubleArrayParam( SchedulingSimulationParameters.OBSERVED_OUTPUT_TRANS_COSTS_TO_FIRST_DEST ),
    				networkLatencyMatrix,
    				useNetworkLatencyMatrixAsIs,
    				m_mainCpp.getBooleanParam( SchedulingSimulationParameters.USE_NETWORK_LATENCY_CLUSTERS ),
                    m_mainCpp.getStringParam( SchedulingSimulationParameters.NETWORK_LATENCY_CLUSTERS ).split( ":" ),
                    m_mainCpp.getIntArrayParam( SchedulingSimulationParameters.NETWORK_LATENCY_CLUSTER_HEADS ),
                    m_mainCpp.getBooleanParam( SchedulingSimulationParameters.USE_NETWORK_LATENCY_MATRIX_WITHIN_CLUSTER ),
                    m_mainCpp.getBooleanParam( SchedulingSimulationParameters.COMMUNICATION_ALLOWED_WITHIN_CLUSTER ),
                    m_mainCpp.getBooleanParam( SchedulingSimulationParameters.USE_NETWORK_LATENCY_MATRIX_ACROSS_CLUSTERS ),
                    m_mainCpp.getDoubleParam( SchedulingSimulationParameters.NETWORK_LATENCY_WITHIN_CLUSTER ),
                    m_mainCpp.getDoubleParam( SchedulingSimulationParameters.NETWORK_LATENCY_ACROSS_CLUSTERS ),
                    m_mainCpp.getBooleanParam( SchedulingSimulationParameters.OVERRIDE_SOME_NETWORK_LATENCIES ),
                    m_mainCpp.getStringParam( SchedulingSimulationParameters.NETWORK_LATENCY_OVERRIDES ).split( ":" ),
    				m_mainCpp.getStringArrayParam( SchedulingSimulationParameters.CORE_USED_BY_EACH_PROCESSING_THREAD ),
                    m_mainCpp.getStringArrayParam( SchedulingSimulationParameters.CORE_USED_BY_EACH_PROCESSING_THREAD_FOR_COMPUTER_TYPE ),
    				m_mainCpp.getStringArrayParam( SchedulingSimulationParameters.CORE_USED_BY_EACH_TRANSMISSION_THREAD ),
                    m_mainCpp.getStringArrayParam( SchedulingSimulationParameters.CORE_USED_BY_EACH_TRANSMISSION_THREAD_FOR_COMPUTER_TYPE )
    				);
    
    		ProcessingArchitectureType architecture = ProcessingArchitectureType.valueOf( 
    		        m_mainCpp.getStringParam( SchedulingSimulationParameters.PROCESSING_ARCHITECTURE ) );
    		if ( architecture == ProcessingArchitectureType.CENTRALIZED ) {
    			nwInfo.setProcessingArchitecture( ProcessingArchitectureType.CENTRALIZED );
    		}
    		else if ( architecture == ProcessingArchitectureType.REPLICATED ) {
    			nwInfo.setProcessingArchitecture( ProcessingArchitectureType.REPLICATED );
    		}
    		
            List<double[]> respTimesForSchedPols = null;        

            if ( m_useMulticastCommunication ) {
                OverlayBuilder mcastOverlayBuilder = new AnHmdmMulticastOverlayBuilder(
                        nwInfo
                        );

                long mcastStartTime = System.nanoTime();
                
                Overlay mcastOverlay = mcastOverlayBuilder.generateOverlay();
        		m_mcastOverlays.add( mcastOverlay );
        		
        		totalMcastOverlayGenerationTime += System.nanoTime() - mcastStartTime;
    
        		respTimesForSchedPols = getResponseTimes(
        		        nwInfo,
        		        mcastOverlay,
        		        m_schedPolsToUse
        		        );
        		for ( int j = 0; j < m_schedPolsToUse.size(); j++ ) {
        		    SchedulingPolicy schedPol = m_schedPolsToUse.get( j );
        		    if ( schedPol == SchedulingPolicy.MULTI_CORE_LAZY_PROCESS_FIRST ) {
        		        m_mcastResponseTimesLazyProcessFirst.add( respTimesForSchedPols.get( j ) );
        		    }
        		    else if ( schedPol == SchedulingPolicy.MULTI_CORE_CONCURRENT ) {
        		        m_mcastResponseTimesConc.add( respTimesForSchedPols.get( j ) );
                    }
        		    else if ( schedPol == SchedulingPolicy.MULTI_CORE_PROCESS_FIRST ) {
        		        m_mcastResponseTimesProcessFirst.add( respTimesForSchedPols.get( j ) );
                    }
        		    else if ( schedPol == SchedulingPolicy.MULTI_CORE_TRANSMIT_FIRST ) {
        		        m_mcastResponseTimesTransFirst.add( respTimesForSchedPols.get( j ) );
                    }
        		}

            }
    		
    		if ( m_useUnicastCommunication ) {
                OverlayBuilder ucastOverlayBuilder = new AUnicastOverlayBuilder(
                        nwInfo
                        );
                
                Overlay ucastOverlay = ucastOverlayBuilder.generateOverlay();
                m_ucastOverlays.add( ucastOverlay );
    
                respTimesForSchedPols = getResponseTimes(
                        nwInfo,
                        ucastOverlay,
                        m_schedPolsToUse
                        );
                for ( int j = 0; j < m_schedPolsToUse.size(); j++ ) {
                    SchedulingPolicy schedPol = m_schedPolsToUse.get( j );
                    if ( schedPol == SchedulingPolicy.MULTI_CORE_LAZY_PROCESS_FIRST ) {
                        m_ucastResponseTimesLazyProcessFirst.add( respTimesForSchedPols.get( j ) );
                    }
                    else if ( schedPol == SchedulingPolicy.MULTI_CORE_CONCURRENT ) {
                        m_ucastResponseTimesConc.add( respTimesForSchedPols.get( j ) );
                    }
                    else if ( schedPol == SchedulingPolicy.MULTI_CORE_PROCESS_FIRST ) {
                        m_ucastResponseTimesProcessFirst.add( respTimesForSchedPols.get( j ) );
                    }
                    else if ( schedPol == SchedulingPolicy.MULTI_CORE_TRANSMIT_FIRST ) {
                        m_ucastResponseTimesTransFirst.add( respTimesForSchedPols.get( j ) );
                    }
                }
                
    		}

		}
		
        long endTime = System.nanoTime();
        
        System.out.println( "Total time needed for " + m_numRepetitions + " repetitions = " + ( ( endTime - startTime ) / 1000000 ) + "ms" );
        System.out.println( "Total time needed for mcast overlay generation = " + ( totalMcastOverlayGenerationTime / 1000000 ) + "ms" );
        		
        if ( m_useMulticastCommunication ) {
            printResults(
                    m_mcastOverlays,
                    m_mcastResponseTimesProcessFirst,
                    m_mcastResponseTimesTransFirst,
                    m_mcastResponseTimesConc,
                    m_mcastResponseTimesLazyProcessFirst,
                    "_mcast"
                    );
        }

        if ( m_useUnicastCommunication ) {
            printResults(
                    m_ucastOverlays,
                    m_ucastResponseTimesProcessFirst,
                    m_ucastResponseTimesTransFirst,
                    m_ucastResponseTimesConc,
                    m_ucastResponseTimesLazyProcessFirst,
                    "_ucast"
                    );
        }
        
        if ( m_useMulticastCommunication ) {
            printRemoteResponseTimeStats(
                    m_mcastOverlays,
                    m_mcastResponseTimesProcessFirst,
                    m_mcastResponseTimesTransFirst,
                    m_mcastResponseTimesConc,
                    m_mcastResponseTimesLazyProcessFirst,
                    "_mcastStats"
                    );
        }

        if ( m_useUnicastCommunication ) {
            printRemoteResponseTimeStats(
                    m_ucastOverlays,
                    m_ucastResponseTimesProcessFirst,
                    m_ucastResponseTimesTransFirst,
                    m_ucastResponseTimesConc,
                    m_ucastResponseTimesLazyProcessFirst,
                    "_ucastStats"
                    );
        }

	}
	
	private List<double[]> getResponseTimes(
	        NetworkInfo nwInfo,
	        Overlay overlay,
	        List<SchedulingPolicy> schedPolsToUse
	        ) {
        AnOverlayAnalyzer analyzer;
        double[] responseTimes;
        List<double[]> responseTimesForSchedPols = new LinkedList<double[]>();
	    
	    for ( int i = 0; i < schedPolsToUse.size(); i++ ) {
	        SchedulingPolicy schedPol = schedPolsToUse.get( i );
	        
	        if ( schedPol == SchedulingPolicy.MULTI_CORE_LAZY_PROCESS_FIRST ) {
    	        analyzer = new ALazyProcessFirstOverlayAnalyzer(
    	                nwInfo,
    	                m_mainCpp.getIntParam( SchedulingSimulationParameters.LAZY_DELAY ),
    	                m_mainCpp.getIntParam( SchedulingSimulationParameters.LAZY_DELAY )
    	                );
    	        responseTimes = analyzer.analyzeOverlay(
    	                overlay
    	                );
    	        responseTimesForSchedPols.add( responseTimes );
	        }
	        else if ( schedPol == SchedulingPolicy.MULTI_CORE_CONCURRENT ) {
    	        analyzer = new AConcOverlayAnalyzer(
    	                nwInfo,
    	                m_mainCpp.getIntParam( SchedulingSimulationParameters.SCHEDULING_QUANTUM )
    	                );
    	        responseTimes = analyzer.analyzeOverlay(
    	                overlay
    	                );
    	        responseTimesForSchedPols.add( responseTimes );
	        }
	        else if ( schedPol == SchedulingPolicy.MULTI_CORE_PROCESS_FIRST ) {
    	        analyzer = new AProcFirstOverlayAnalyzer(
    	                nwInfo
    	                );
    	        responseTimes = analyzer.analyzeOverlay(
    	                overlay
    	                );
    	        responseTimesForSchedPols.add( responseTimes );
	        }
	        else if ( schedPol == SchedulingPolicy.MULTI_CORE_TRANSMIT_FIRST ) {
    	        analyzer = new ATransFirstOverlayAnalyzer(
    	                nwInfo
    	                );
    	        responseTimes = analyzer.analyzeOverlay(
    	                overlay
    	                );
    	        responseTimesForSchedPols.add( responseTimes );
	        }
	    }
        
        return responseTimesForSchedPols;
	}
	
	void printResults(
	        List<Overlay> overlays,
	        Vector<double[]> procFirstRespTimes,
	        Vector<double[]> transFirstRespTimes,
	        Vector<double[]> concRespTimes,
	        Vector<double[]> lazyProcessFirstRespTimes,
	        String fileNameSuffix
	        ) {
		
		try {
			FileWriter fw = null;
			
			fw = new FileWriter( m_outputFile + fileNameSuffix + ".txt" );
			
			DecimalFormat df = new DecimalFormat( ".###" );
			
			String line = "USER\t";
		    if ( m_schedPolsToUse.contains( SchedulingPolicy.MULTI_CORE_PROCESS_FIRST ) ) {
		        line += "PF\t";
		    }
            if ( m_schedPolsToUse.contains( SchedulingPolicy.MULTI_CORE_TRANSMIT_FIRST ) ) {
                line += "TF\t";
            }
            if ( m_schedPolsToUse.contains( SchedulingPolicy.MULTI_CORE_CONCURRENT ) ) {
                line += "CONC\t";
            }
            if ( m_schedPolsToUse.contains( SchedulingPolicy.MULTI_CORE_LAZY_PROCESS_FIRST ) ) {
                line += "LAZY\t";
            }
			
			fw.write( line );
			fw.write( '\n' );
			
			for ( int i = 0; i < m_numComputers; i++ ) {
				line = "";
                for ( int j = 0; j < m_numRepetitions; j++ ) {
    				line += overlays.get( j ).getAddOrder()[ i ] + "\t";
    				if ( m_schedPolsToUse.contains( SchedulingPolicy.MULTI_CORE_PROCESS_FIRST ) ) {
    				    line += df.format( procFirstRespTimes.elementAt( j )[ i ] ) + "\t";
    				}
                    if ( m_schedPolsToUse.contains( SchedulingPolicy.MULTI_CORE_TRANSMIT_FIRST ) ) {
                        line += df.format( transFirstRespTimes.elementAt( j )[ i ] ) + "\t";
                    }
                    if ( m_schedPolsToUse.contains( SchedulingPolicy.MULTI_CORE_CONCURRENT ) ) {
                        line += df.format( concRespTimes.elementAt( j )[ i ] ) + "\t";
                    }
                    if ( m_schedPolsToUse.contains( SchedulingPolicy.MULTI_CORE_LAZY_PROCESS_FIRST ) ) {
                        line += df.format( lazyProcessFirstRespTimes.elementAt( j )[ i ] ) + "\t";
                    }
				}
				fw.write( line );
				fw.write( '\n' );
			}
			
			fw.close();
			
		}
		catch ( Exception e ) {
			e.printStackTrace();
		}
		
	}
	
    void printRemoteResponseTimeStats(
            List<Overlay> overlays,
            Vector<double[]> procFirstRespTimes,
            Vector<double[]> transFirstRespTimes,
            Vector<double[]> concRespTimes,
            Vector<double[]> lazyProcessFirstRespTimes,
            String fileNameSuffix
            ) {
        
        try {
            FileWriter fw = null;
            
            fw = new FileWriter( m_outputFile + fileNameSuffix + ".txt" );
            
            DecimalFormat df = new DecimalFormat( ".###" );
            
            String line = "USER\t";
            if ( m_schedPolsToUse.contains( SchedulingPolicy.MULTI_CORE_PROCESS_FIRST ) ) {
                line += "PF\t";
            }
            if ( m_schedPolsToUse.contains( SchedulingPolicy.MULTI_CORE_TRANSMIT_FIRST ) ) {
                line += "TF\t";
            }
            if ( m_schedPolsToUse.contains( SchedulingPolicy.MULTI_CORE_CONCURRENT ) ) {
                line += "CONC\t";
            }
            if ( m_schedPolsToUse.contains( SchedulingPolicy.MULTI_CORE_LAZY_PROCESS_FIRST ) ) {
                line += "LAZY\t";
            }
            
            fw.write( line );
            fw.write( '\n' );
            
            
            for ( int i = 0; i < m_numRepetitions; i++ ) {
                double maxProcFirstRespTime = Double.MIN_NORMAL;
                double maxTransFirstRespTime = Double.MIN_NORMAL;
                double maxConcRespTime = Double.MIN_NORMAL;
                double maxLazyProcessFirstRespTime = Double.MIN_NORMAL;
                double avgProcFirstRespTime = 0;
                double avgTransFirstRespTime = 0;
                double avgConcRespTime = 0;
                double avgLazyProcessFirstRespTime = 0;

                line = "";
                for ( int j = 0; j < m_numComputers; j++ ) {
                    
                    int curUserIndex = overlays.get( i ).getAddOrder()[ j ];
                    if ( curUserIndex == m_sourceComputer ) {
                        continue;
                    }
                    
                    if ( m_schedPolsToUse.contains( SchedulingPolicy.MULTI_CORE_PROCESS_FIRST ) ) {
                        maxProcFirstRespTime = Math.max( maxProcFirstRespTime, procFirstRespTimes.elementAt( i )[ j ] );
                        avgProcFirstRespTime += procFirstRespTimes.elementAt( i )[ j ];
                    }
                    if ( m_schedPolsToUse.contains( SchedulingPolicy.MULTI_CORE_TRANSMIT_FIRST ) ) {
                        maxTransFirstRespTime = Math.max( maxTransFirstRespTime, transFirstRespTimes.elementAt( i )[ j ] );
                        avgTransFirstRespTime += transFirstRespTimes.elementAt( i )[ j ];
                    }
                    if ( m_schedPolsToUse.contains( SchedulingPolicy.MULTI_CORE_CONCURRENT ) ) {
                        maxConcRespTime = Math.max( maxConcRespTime, concRespTimes.elementAt( i )[ j ] );
                        avgConcRespTime += concRespTimes.elementAt( i )[ j ];
                    }
                    if ( m_schedPolsToUse.contains( SchedulingPolicy.MULTI_CORE_LAZY_PROCESS_FIRST ) ) {
                        maxLazyProcessFirstRespTime = Math.max( maxLazyProcessFirstRespTime, lazyProcessFirstRespTimes.elementAt( i )[ j ] );
                        avgLazyProcessFirstRespTime += lazyProcessFirstRespTimes.elementAt( i )[ j ];
                    }

                }
                
                if ( m_schedPolsToUse.contains( SchedulingPolicy.MULTI_CORE_PROCESS_FIRST ) ) {
                    line += df.format( maxProcFirstRespTime ) + "\t";
                }
                if ( m_schedPolsToUse.contains( SchedulingPolicy.MULTI_CORE_TRANSMIT_FIRST ) ) {
                    line += df.format( maxTransFirstRespTime ) + "\t";
                }
                if ( m_schedPolsToUse.contains( SchedulingPolicy.MULTI_CORE_CONCURRENT ) ) {
                    line += df.format( maxConcRespTime ) + "\t";
                }
                if ( m_schedPolsToUse.contains( SchedulingPolicy.MULTI_CORE_LAZY_PROCESS_FIRST ) ) {
                    line += df.format( maxLazyProcessFirstRespTime ) + "\t";
                }

                if ( m_schedPolsToUse.contains( SchedulingPolicy.MULTI_CORE_PROCESS_FIRST ) ) {
                    line += df.format( avgProcFirstRespTime / ( m_numComputers - 1 ) ) + "\t";
                }
                if ( m_schedPolsToUse.contains( SchedulingPolicy.MULTI_CORE_TRANSMIT_FIRST ) ) {
                    line += df.format( avgTransFirstRespTime / ( m_numComputers - 1 ) ) + "\t";
                }
                if ( m_schedPolsToUse.contains( SchedulingPolicy.MULTI_CORE_CONCURRENT ) ) {
                    line += df.format( avgConcRespTime / ( m_numComputers - 1 ) ) + "\t";
                }
                if ( m_schedPolsToUse.contains( SchedulingPolicy.MULTI_CORE_LAZY_PROCESS_FIRST ) ) {
                    line += df.format( avgLazyProcessFirstRespTime / ( m_numComputers - 1 ) ) + "\t";
                }

                fw.write( line );
                fw.write( '\n' );                
            }
            
            fw.close();
            
        }
        catch ( Exception e ) {
            e.printStackTrace();
        }
        
    }
    
	public static void main( String[] args ) {
		
        ConfigParamProcessor mainCpp = AMainConfigParamProcessor.getInstance(
        		args,
        		true
        		);
        String configFilePath = mainCpp.getStringParam( SchedulingSimulationParameters.CUSTOM_CONFIGURATION_FILE );
        AMainConfigParamProcessor.overrideValuesByThoseSpecifiedInSource(
        		mainCpp,
		        ASettingsFileConfigParamProcessor.getInstance(
		                configFilePath,
		                true
		                )
		        );

        String outputDirectory = mainCpp.getStringParam( SchedulingSimulationParameters.OUTPUT_DIRECTORY );
    	String outputFile = mainCpp.getStringParam( SchedulingSimulationParameters.GENERAL_OUTPUT_FILE );
    	
    	ASingleSimulation singleExperiment = new ASingleSimulation(
    			outputDirectory + "\\" + outputFile
    			); 
    	singleExperiment.run();
    	
	}
	
}
