package selfoptsys.perf;

import java.util.List;

public interface PerformanceParameterEstimatingFunction {

    double getPerformanceParameterEstimate(
            List<Double> reportedValues,
            double previousEstimate,
            int numValuesReportedSincePreviousEstimate,
            double initialEstimate,
            double previousDataWeight,
            double newDataWeight
            );
    
}
