package selfoptsys.sim.sched;

import selfoptsys.config.*;


public class SchedulingSimulationParameters 
    extends Parameters {

    public static String REPETITIONS = "Repetitions";

    public static String COMMUNICATION_ARCHITECTURE_TYPE = "CommunicationArchitectureType";

    public static String NUM_COMPUTERS = "NumComputers";
    
    public static String NUM_COMPUTER_TYPES = "NumComputerTypes";
    public static String COMPUTER_TYPE_PERCETANGES = "ComputerTypePercentages";
    public static String USERS_COMPUTER_TYPES = "UsersComputerTypes";
    public static String INPUT_PROC_COSTS = "InputProcCosts";
    public static String OUTPUT_PROC_COSTS = "OutputProcCosts";
    public static String INPUT_TRANS_COSTS = "InputTransCosts";
    public static String INPUT_TRANS_COSTS_TO_FIRST_DEST = "InputTransCostsToFirstDest";
    public static String OBSERVED_INPUT_TRANS_COSTS = "ObservedInputTransCosts";
    public static String OBSERVED_INPUT_TRANS_COSTS_TO_FIRST_DEST = "ObservedInputTransCostsToFirstDest";
    public static String OUTPUT_TRANS_COSTS = "OutputTransCosts";
    public static String OUTPUT_TRANS_COSTS_TO_FIRST_DEST = "OutputTransCostsToFirstDest";
    public static String OBSERVED_OUTPUT_TRANS_COSTS = "ObservedOutputTransCosts";
    public static String OBSERVED_OUTPUT_TRANS_COSTS_TO_FIRST_DEST = "ObservedOutputTransCostsToFirstDest";
    
    public static String SOURCE_COMPUTER = "SourceComputer";
    public static String SOURCE_COMPUTER_TYPE = "SourceComputerType";
    public static String SOURCE_COMPUTER_INPUT_PROC_COST = "SourceComputerInputProcCost";
    public static String SOURCE_COMPUTER_OUTPUT_PROC_COST = "SourceComputerOutputProcCost";
    public static String SOURCE_COMPUTER_INPUT_TRANS_COST = "SourceComputerInputTransCost";
    public static String SOURCE_COMPUTER_INPUT_TRANS_COST_TO_FIRST_DEST = "SourceComputerInputTransCostToFirstDest";
    public static String SOURCE_COMPUTER_OBSERVED_INPUT_TRANS_COST = "SourceComputerObservedInputTransCost";
    public static String SOURCE_COMPUTER_OBSERVED_INPUT_TRANS_COST_TO_FIRST_DEST = "SourceComputerObservedInputTransCostToFirstDest";
    public static String SOURCE_COMPUTER_OUTPUT_TRANS_COST = "SourceComputerOutputTransCost";
    public static String SOURCE_COMPUTER_OUTPUT_TRANS_COST_TO_FIRST_DEST = "SourceComputerOutputTransCostToFirstDest";
    public static String SOURCE_COMPUTER_OBSERVED_OUTPUT_TRANS_COST = "SourceComputerObservedOutputTransCost";
    public static String SOURCE_COMPUTER_OBSERVED_OUTPUT_TRANS_COST_TO_FIRST_DEST = "SourceComputerObservedOutputTransCostToFirstDest";

    public static String USE_NETWORK_LATENCY_CLUSTERS = "UseNetworkLatencyClusters";
    public static String NETWORK_LATENCY_CLUSTERS = "NetworkLatencyClusters";
    public static String NETWORK_LATENCY_CLUSTER_HEADS = "NetworkLatencyClusterHeads";
    public static String USE_NETWORK_LATENCY_MATRIX_WITHIN_CLUSTER = "UseNetworkLatencyMatrixWithinCluster";
    public static String COMMUNICATION_ALLOWED_WITHIN_CLUSTER = "CommunicationAllowedWithinCluster";
    public static String USE_NETWORK_LATENCY_MATRIX_ACROSS_CLUSTERS = "UseNetworkLatencyMatrixAcrossClusters";
    public static String NETWORK_LATENCY_WITHIN_CLUSTER = "NetworkLatencyWithinCluster";
    public static String NETWORK_LATENCY_ACROSS_CLUSTERS = "NetworkLatencyAcrossClusters";
    public static String OVERRIDE_SOME_NETWORK_LATENCIES = "OverrideSomeNetworkLatencies";
    public static String NETWORK_LATENCY_OVERRIDES = "NetworkLatencyOverrides";
    
    public static String CORE_USED_BY_EACH_PROCESSING_THREAD = "CoreUsedByEachProcessingThread";
    public static String CORE_USED_BY_EACH_PROCESSING_THREAD_FOR_COMPUTER_TYPE = "CoreUsedByEachProcessingThreadForComputerType";
    public static String CORE_USED_BY_EACH_TRANSMISSION_THREAD = "CoreUsedByEachTransmissionThread";
    public static String CORE_USED_BY_EACH_TRANSMISSION_THREAD_FOR_COMPUTER_TYPE = "CoreUsedByEachTransmissionThreadForComputerType";
    
    public static String USE_UNICAST_COMMUNICATION = "UseUnicastCommunication";
    public static String USE_MULTICAST_COMMUNICATION = "UseMulticastCommunication";
    public static String SCHEDULING_POLICIES_TO_USE = "SchedulingPoliciesToUse";
    
}
