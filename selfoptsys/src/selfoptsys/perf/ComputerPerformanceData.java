package selfoptsys.perf;

import java.util.List;

public interface ComputerPerformanceData {

    ComputerInfo getComputerInfo();
    
    double getParameterEstimate(
            PerformanceParameterType parameterType
            );
    double getParameterEstimateBasedOnRealData(
            PerformanceParameterType parameterType
            );
    double getParameterEstimateBasedOnOtherParameters(
            PerformanceParameterType parameterType
            );

    void addPerformanceParameterValue(
            PerformanceParameterType parameterType,
            double value
            );
    void setInitialPerformanceParameterEstimate(
            PerformanceParameterType parameterType,
            double estimate
            );
    void estimateValuesOfAllPerformanceParameters(
            List<ComputerPerformanceData> allKnownComputerPerformanceData
            );
    
}
