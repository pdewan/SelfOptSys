package selfoptsys.network;

import java.io.*;
import java.util.*;

import commonutils.config.*;


public class ANetworkInfo 
	implements NetworkInfo {

	int m_numComputers;
	
	double[] m_inputProcCosts;
	double[] m_outputProcCosts;
	double[] m_inputTransCosts;
    double[] m_inputTransCostsToFirstDest;
	double[] m_outputTransCosts;
    double[] m_outputTransCostsToFirstDest;
	double[] m_observedInputTransCosts;
    double[] m_observedInputTransCostsToFirstDest;
	double[] m_observedOutputTransCosts;
    double[] m_observedOutputTransCostsToFirstDest;
	
	Map<Integer, Integer> m_coreUsedByProcessingThread;
    Map<Integer, Integer> m_coreUsedByTransmissionThread;
    Map<Integer, Boolean> m_procAndTransThreadsShareCores;
	
	double[][] m_networkLatencies;
	
	int m_sourceComputer;
	
	ProcessingArchitectureType m_processingArchitecture;
	
	public void setNumComputers( int numComputers ) {
		m_numComputers = numComputers;
	}
	public int getNumComputers() {
		return m_numComputers;
	}
	
	public void setCoreUsedByProcessingThread(
            Map<Integer, Integer> coreUsedByProcessingThread
            ) {
	    m_coreUsedByProcessingThread = coreUsedByProcessingThread;
	}
	public Map<Integer, Integer> getCoreUsedByProcessingThread() {
	    return m_coreUsedByProcessingThread;
	}
    
	public void setCoreUsedByTransmissionThread(
            Map<Integer, Integer> coreUsedByTransmissionThread
            ) {
	    m_coreUsedByTransmissionThread = coreUsedByTransmissionThread;
	}
	public Map<Integer, Integer> getCoreUsedByTransmissionThread() {
	    return m_coreUsedByTransmissionThread;
	}
    
	public void setProcAndTransThreadShareCores(
            Map<Integer, Boolean> procAndTransThreadsShareCores
            ) {
	    m_procAndTransThreadsShareCores = procAndTransThreadsShareCores;
	}
	public Map<Integer, Boolean> getProcAndTransThreadShareCores() {
	    return m_procAndTransThreadsShareCores;
	}
    
	public void setProcessingArchitecture( 
			ProcessingArchitectureType processingArchitecture 
			) {
		m_processingArchitecture = processingArchitecture;
	}
	public ProcessingArchitectureType getProcessingArchitecture() {
		return m_processingArchitecture;
	}

	public void setInputProcCosts( double[] inputProcCosts ) {
		m_inputProcCosts = inputProcCosts;
	}
	public double[] getInputProcCosts() {
		return m_inputProcCosts;
	}
	
	public void setOutputProcCosts( double[] outputProcCosts ) {
		m_outputProcCosts = outputProcCosts;
	}
	public double[] getOutputProcCosts() {
		return m_outputProcCosts;
	}

	public void setInputTransCosts( double[] inputTransCosts ) {
		m_inputTransCosts = inputTransCosts;
	}
	public double[] getInputTransCosts() {
		return m_inputTransCosts;
	}
	
    public void setInputTransCostsToFirstDest( double[] inputTransCostsToFirstDest ) {
        m_inputTransCostsToFirstDest = inputTransCostsToFirstDest;
    }
    public double[] getInputTransCostsToFirstDest() {
        return m_inputTransCostsToFirstDest;
    }
    
    public void setObservedInputTransCosts(
            double[] observedInputTransCosts
            ) {
        m_observedInputTransCosts = observedInputTransCosts;
    }
    public double[] getObservedInputTransCosts() {
        return m_observedInputTransCosts;
    }
    
    public void setObservedInputTransCostsToFirstDest(
            double[] observedInputTransCostsToFirstDest
            ) {
        m_observedInputTransCostsToFirstDest = observedInputTransCostsToFirstDest;
    }
    public double[] getObservedInputTransCostsToFirstDest() {
        return m_observedInputTransCostsToFirstDest;
    }
    
	public void setOutputTransCosts( double[] outputTransCosts ) {
		m_outputTransCosts = outputTransCosts;
	}
	public double[] getOutputTransCosts() {
		return m_outputTransCosts;
	}

    public void setOutputTransCostsToFirstDest( double[] outputTransCostsToFirstDest ) {
        m_outputTransCostsToFirstDest = outputTransCostsToFirstDest;
    }
    public double[] getOutputTransCostsToFirstDest() {
        return m_outputTransCostsToFirstDest;
    }

    public void setObservedOutputTransCosts(
            double[] observedOutputTransCosts
            ) {
        m_observedOutputTransCosts = observedOutputTransCosts;
    }
    public double[] getObservedOutputTransCosts() {
        return m_observedOutputTransCosts;
    }
    
    public void setObservedOutputTransCostsToFirstDest(
            double[] observedOutputTransCostsToFirstDest
            ) {
        m_observedOutputTransCostsToFirstDest = observedOutputTransCostsToFirstDest;
    }
    public double[] getObservedOutputTransCostsToFirstDest() {
        return m_observedOutputTransCostsToFirstDest;
    }
    
	public void setNetworkLatencies( double[][] networkLatencies ) {
		m_networkLatencies = networkLatencies;
	}
	public double[][] getNetworkLatencies() {
		return m_networkLatencies;
	}
	
	public void setSourceComputer( int sourceComputer ) {
		m_sourceComputer = sourceComputer;
	}
	public int getSourceComputer() {
		return m_sourceComputer;
	}
	
	public double getInputProcCostForComp(
			int computer
			) {
		return m_inputProcCosts[ computer ];
	}
	public double getOutputProcCostForComp(
			int computer
			) {
		return m_outputProcCosts[ computer ];
	}
	
	public double getProcCostForCompAndArch(
			int computer
			) {
		
        double cost = 0;
        
        if ( m_processingArchitecture == ProcessingArchitectureType.REPLICATED ) {
            cost = m_inputProcCosts[ computer ] + m_outputProcCosts[ computer ];
        }
        else if ( m_processingArchitecture == ProcessingArchitectureType.CENTRALIZED ) {
            cost = m_outputProcCosts[ computer ];
        }
        
        return cost;

	}
	
	public double getOldProcCostForCompAndArch(
			int computer
			) {
		
        double cost = 0;
        
        if ( m_processingArchitecture == ProcessingArchitectureType.REPLICATED ) {
            cost = m_inputProcCosts[ computer ] + m_outputProcCosts[ computer ];
        }
        else if ( m_processingArchitecture == ProcessingArchitectureType.CENTRALIZED ) {
            cost = m_outputProcCosts[ computer ];
            
            /*
             * Because the source vertex is the master computer in the centralized architecture,
             * it processes not only output commands but also input commands. Therefore, we
             * have to include the input processing cost the total processing cost.
             */
            if ( computer == m_sourceComputer ) {
                cost += m_inputProcCosts[ computer ];
            }
        }
        
        return cost;

		
	}
	
	public double getTransCostForCompAndArch(
			int computer
			) {
		
        double cost = 0;
        
        if ( m_processingArchitecture == ProcessingArchitectureType.REPLICATED ) {
            cost = m_inputTransCosts[ computer ];
        }
        else if ( m_processingArchitecture == ProcessingArchitectureType.CENTRALIZED ) {
            cost = m_outputTransCosts[ computer ];
        }
        
        return cost;
		
	}
	
    public double getTransCostToFirstDestForCompAndArch(
            int computer
            ) {
        
        double cost = 0;
        
        if ( m_processingArchitecture == ProcessingArchitectureType.REPLICATED ) {
            cost = m_inputTransCostsToFirstDest[ computer ];
        }
        else if ( m_processingArchitecture == ProcessingArchitectureType.CENTRALIZED ) {
            cost = m_outputTransCostsToFirstDest[ computer ];
        }
        
        return cost;
        
    }
    
    public double getObservedTransCostForCompAndArch(
            int computer
            ) {
        
        double cost = 0;
        
        if ( m_processingArchitecture == ProcessingArchitectureType.REPLICATED ) {
            cost = m_observedInputTransCosts[ computer ];
        }
        else if ( m_processingArchitecture == ProcessingArchitectureType.CENTRALIZED ) {
            cost = m_observedOutputTransCosts[ computer ];
        }
        
        return cost;
        
    }
    
    public double getObservedTransCostToFirstDestForCompAndArch(
            int computer
            ) {
        
        double cost = 0;
        
        if ( m_processingArchitecture == ProcessingArchitectureType.REPLICATED ) {
            cost = m_observedInputTransCostsToFirstDest[ computer ];
        }
        else if ( m_processingArchitecture == ProcessingArchitectureType.CENTRALIZED ) {
            cost = m_observedOutputTransCostsToFirstDest[ computer ];
        }
        
        return cost;
        
    }
    
	public static void saveNetworkInfo(
			NetworkInfo nwInfo,
			String outputFilePath
			) {
		
		try {
			FileWriter fw = new FileWriter( outputFilePath );
			
			fw.write( nwInfo.getNumComputers() + "\n\n"  );
			fw.write( nwInfo.getSourceComputer() + "\n\n"  );
			fw.write( nwInfo.getInputProcCosts().toString() + "\n\n"  );
			fw.write( nwInfo.getInputTransCosts().toString() + "\n\n"  );
            fw.write( nwInfo.getObservedInputTransCosts().toString() + "\n\n"  );
			fw.write( nwInfo.getOutputProcCosts().toString() + "\n\n"  );
			fw.write( nwInfo.getOutputTransCosts().toString() + "\n\n"  );
            fw.write( nwInfo.getObservedOutputTransCosts().toString() + "\n\n"  );
			fw.write( nwInfo.getNetworkLatencies().toString() + "\n\n" );

			fw.flush();
			fw.close();
		}
		catch ( Exception e ) {
			e.printStackTrace();
		}
		
	}
	
}
