package selfoptsys.perf;

import java.util.List;

import commonutils.basic.MathUtils;

public class APerformanceParameterEstimatingFunction 
    implements PerformanceParameterEstimatingFunction {

    public double getPerformanceParameterEstimate(
            List<Double> reportedValues,
            double previousEstimate,
            int numValuesReportedSincePreviousEstimate,
            double initialEstimate,
            double previousDataWeight,
            double newDataWeight
            ) {
        
        if ( numValuesReportedSincePreviousEstimate == 0 ) {
            if ( previousEstimate != -1 ) {
                return previousEstimate;
            }
            else {
                return initialEstimate;
            }
        }
        
        double total = 0;
        for ( int i = reportedValues.size() - numValuesReportedSincePreviousEstimate;
                i < reportedValues.size(); 
                i++ ) {
            total += reportedValues.get( i );
        }
        
        if ( previousEstimate == -1 ) {
            if ( initialEstimate == -1 ) {
                return MathUtils.round( 
                        total / numValuesReportedSincePreviousEstimate, 
                        3 
                        );
            }
            else {
                return MathUtils.round(
                        initialEstimate * previousDataWeight +
                            total / numValuesReportedSincePreviousEstimate * newDataWeight, 
                        3 
                        );
            }
        } 
        else {
            return MathUtils.round(
                    previousEstimate * previousDataWeight +
                        total / numValuesReportedSincePreviousEstimate * newDataWeight, 
                    3 
                    );
        }
        
    }
}
