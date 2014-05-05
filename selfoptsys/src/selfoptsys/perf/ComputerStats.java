package selfoptsys.perf;

import java.util.*;


public interface ComputerStats {
    
    ComputerInfo getComputerInfo();

    double getCostEstimate(
            PerformanceParameterType costType,
            boolean estimateCostsBasedOnOtherCosts,
            boolean useOppositeCosts,
            Map<ComputerInfo, ComputerStats> otherComputerStats
            );
    void setCostEstimate(
            PerformanceParameterType costType,
            double value
            );

    double getOldCostEstimate(
            PerformanceParameterType costType
            );
    
    List<Double> getCostsForCurrentSession(
            PerformanceParameterType costType
            );
    void appendCostToCurrentSession(
            PerformanceParameterType costType,
            double value
            );
        
    Map<PerformanceParameterType, Double> getAllEstimates();
    Map<PerformanceParameterType, List<Double>> getAllReportsForCurrentSession();
    
    void calculateEstimatesBasedOnValuesFromCurrentSession(
            int maxNumReportsToUse
            );
    void discardLatestEstimates();
    
    Map<PerformanceParameterType, Integer> getNumCostsCounters();
    void resetNumCostsCounters();
    
}
