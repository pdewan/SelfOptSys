package selfoptsys.perf;

import selfoptsys.comm.*;

public class APerformanceOptimizationReportMessage 
    extends AMessage
    implements PerformanceOptimizationReportMessage {

    private static final long serialVersionUID = 8734741720141724237L;

    APerformanceOptimizationReport m_performanceOptimizationReport;
    
    public APerformanceOptimizationReportMessage(
            APerformanceOptimizationReport performanceOptimizationReport
            ) {
        super( MessageType.PERFORMANCE_OPTIMIZATION_MESSAGE );
        
        m_performanceOptimizationReport = performanceOptimizationReport;
    }
    
    public void setPerformanceOptimizationReport(
            APerformanceOptimizationReport performanceOptimizationReport
            ) {
        m_performanceOptimizationReport = performanceOptimizationReport;
    }
    
    public APerformanceOptimizationReport getPerformanceOptimizationReport() {
        return m_performanceOptimizationReport;
    }
    
}
