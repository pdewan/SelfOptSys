package selfoptsys.perf;

import selfoptsys.comm.*;

public interface PerformanceOptimizationReportMessage 
    extends Message {
    
    void setPerformanceOptimizationReport(
            APerformanceOptimizationReport performanceOptimizationReport
            );
    APerformanceOptimizationReport getPerformanceOptimizationReport();

}
