package selfoptsys.network;

import java.util.*;
import commonutils.config.*;

public interface NetworkInfo {

	void setNumComputers( int numComputers );
	int getNumComputers();
	
	void setProcessingArchitecture( 
			ProcessingArchitectureType architecture 
			);
	ProcessingArchitectureType getProcessingArchitecture();

    void setCoreUsedByProcessingThread(
            Map<Integer, Integer> coreUsedByEachProcessingThread
            );
    Map<Integer, Integer> getCoreUsedByProcessingThread();
    
    void setCoreUsedByTransmissionThread(
            Map<Integer, Integer> coreUsedByTransmissionThread
            );
    Map<Integer, Integer> getCoreUsedByTransmissionThread();
    
    void setProcAndTransThreadShareCores(
            Map<Integer, Boolean> procAndTransThreadsShareCores
            );
    Map<Integer, Boolean> getProcAndTransThreadShareCores();
    
	void setInputProcCosts( double[] inputProcCosts );
	double[] getInputProcCosts();
	
	void setOutputProcCosts( double[] outputProcCosts );
	double[] getOutputProcCosts();

	void setInputTransCosts( double[] inputTransCosts );
	double[] getInputTransCosts();
	
    void setInputTransCostsToFirstDest( double[] inputTransCostsToFirstDest );
    double[] getInputTransCostsToFirstDest();
    
    void setObservedInputTransCosts( double[] observedInputTransCosts );
    double[] getObservedInputTransCosts();
    
    void setObservedInputTransCostsToFirstDest( double[] observedInputTransCostsToFirstDest );
    double[] getObservedInputTransCostsToFirstDest();
    
	void setOutputTransCosts( double[] outputTransCosts );
	double[] getOutputTransCosts();

    void setOutputTransCostsToFirstDest( double[] outputTransCostsToFirstDest );
    double[] getOutputTransCostsToFirstDest();

    void setObservedOutputTransCosts( double[] observedOutputTransCosts );
    double[] getObservedOutputTransCosts();

    void setObservedOutputTransCostsToFirstDest( double[] observedOutputTransCostsToFirstDest );
    double[] getObservedOutputTransCostsToFirstDest();

	void setNetworkLatencies( double[][] networkLatencies );
	double[][] getNetworkLatencies(); 
	
	void setSourceComputer( int sourceComputer );
	int getSourceComputer();
	
	double getInputProcCostForComp(
			int computer
			);
	double getOutputProcCostForComp(
			int computer
			);
	double getProcCostForCompAndArch( int computer );
	double getOldProcCostForCompAndArch( int computer );
	double getTransCostForCompAndArch( int computer );
    double getTransCostToFirstDestForCompAndArch( int computer );
    double getObservedTransCostForCompAndArch( int computer );
    double getObservedTransCostToFirstDestForCompAndArch( int computer );
	
}
