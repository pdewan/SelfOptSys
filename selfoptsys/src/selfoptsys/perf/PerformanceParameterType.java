package selfoptsys.perf;


public enum PerformanceParameterType {

    REPLICATED_INPUTTING_MASTER_INPUT_PROC { public String toString() { return "ReplicatedInputtingMasterInputProcCost"; } },
    REPLICATED_INPUTTING_MASTER_INPUT_TRANS { public String toString() { return "ReplicatedInputtingMasterInputTransCost"; } },
    REPLICATED_INPUTTING_MASTER_INPUT_TRANS_TO_FIRST_DEST { public String toString() { return "ReplicatedInputtingMasterInputTransCostToFirstDest"; } },
    REPLICATED_INPUTTING_MASTER_INPUT_TRANS_BASED_ON_OBSERVED_TIME { public String toString() { return "ReplicatedInputtingMasterInputTransCostBasedOnObservedTime"; } },
    REPLICATED_INPUTTING_MASTER_INPUT_TRANS_TO_FIRST_DEST_BASED_ON_OBSERVED_TIME { public String toString() { return "ReplicatedInputtingMasterInputTransCostToFirstDestBasedOnObservedTime"; } },
    REPLICATED_INPUTTING_MASTER_OUTPUT_PROC { public String toString() { return "ReplicatedInputtingMasterOutputProcCost"; } },
    
    REPLICATED_OBSERVING_MASTER_INPUT_PROC { public String toString() { return "ReplicatedObservingMasterInputProcCost"; } },
    REPLICATED_OBSERVING_MASTER_INPUT_TRANS { public String toString() { return "ReplicatedObservingMasterInputTransCost"; } },
    REPLICATED_OBSERVING_MASTER_INPUT_TRANS_TO_FIRST_DEST { public String toString() { return "ReplicatedObservingMasterInputTransCostToFirstDest"; } },
    REPLICATED_OBSERVING_MASTER_INPUT_TRANS_BASED_ON_OBSERVED_TIME { public String toString() { return "ReplicatedObservingMasterInputTransCostBasedOnObservedTime"; } },
    REPLICATED_OBSERVING_MASTER_INPUT_TRANS_TO_FIRST_DEST_BASED_ON_OBSERVED_TIME { public String toString() { return "ReplicatedObservingMasterInputTransCostToFirstDestBasedOnObservedTime"; } },
    REPLICATED_OBSERVING_MASTER_OUTPUT_PROC { public String toString() { return "ReplicatedObservingMasterOutputProcCost"; } },
    
    CENTRALIZED_MASTER_INPUT_PROC { public String toString() { return "CentralizedMasterInputProcCost"; } },
    CENTRALIZED_MASTER_OUTPUT_PROC { public String toString() { return "CentralizedMasterOutputProcCost"; } },
    CENTRALIZED_MASTER_OUTPUT_TRANS { public String toString() { return "CentralizedMasterOutputTransCost"; } },
    CENTRALIZED_MASTER_OUTPUT_TRANS_TO_FIRST_DEST { public String toString() { return "CentralizedMasterOutputTransCostToFirstDest"; } },
    CENTRALIZED_MASTER_OUTPUT_TRANS_BASED_ON_OBSERVED_TIME { public String toString() { return "CentralizedMasterOutputTransCostBasedOnObservedTime"; } },
    CENTRALIZED_MASTER_OUTPUT_TRANS_TO_FIRST_DEST_BASED_ON_OBSERVED_TIME { public String toString() { return "CentralizedMasterOutputTransCostToFirstDestBasedOnObservedTime"; } },
    
    CENTRALIZED_SLAVE_INPUT_TRANS { public String toString() { return "CentralizedSlaveInputTransCost"; } },
    CENTRALIZED_SLAVE_INPUT_TRANS_TO_FIRST_DEST { public String toString() { return "CentralizedSlaveInputTransCostToFirstDest"; } },
    CENTRALIZED_SLAVE_INPUT_TRANS_BASED_ON_OBSERVED_TIME { public String toString() { return "CentralizedSlaveInputTransCostBasedOnObservedTime"; } },
    CENTRALIZED_SLAVE_INPUT_TRANS_TO_FIRST_DEST_BASED_ON_OBSERVED_TIME { public String toString() { return "CentralizedSlaveInputTransCostToFirstDestBasedOnObservedTime"; } },
    CENTRALIZED_SLAVE_OUTPUT_PROC { public String toString() { return "CentralizedSlaveOutputProcCost"; } },
    CENTRALIZED_SLAVE_OUTPUT_TRANS { public String toString() { return "CentralizedSlaveOutputTransCost"; } },
    CENTRALIZED_SLAVE_OUTPUT_TRANS_TO_FIRST_DEST { public String toString() { return "CentralizedSlaveOutputTransCostToFirstDest"; } },
    CENTRALIZED_SLAVE_OUTPUT_TRANS_BASED_ON_OBSERVED_TIME { public String toString() { return "CentralizedSlaveOutputTransCostBasedOnObservedTime"; } },
    CENTRALIZED_SLAVE_OUTPUT_TRANS_TO_FIRST_DEST_BASED_ON_OBSERVED_TIME { public String toString() { return "CentralizedSlaveOutputTransCostToFirstDestBasedOnObservedTime"; } }
    
}
