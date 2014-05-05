package selfoptsys.perf;

import java.io.Serializable;

public class APerformanceOptimizationReport
    implements Serializable {
    
    private static final long serialVersionUID = 5434857263380142900L;
    
    public int SysConfigVersion = 0;
    public int UserIndex = 0;
    public int CmdSourceUserIndex = 0;
    public int SeqId = 0;
    
    public long InputProcStartTime = 0;
    public long InputProcTime = 0;
    public double ThinkTime = 0;

    public long OutputProcTime = 0;
    public long OutputProcStartTime = 0;
    
    public long InputTransTime = 0;
    public long InputTransTimeToFirstDest = 0;
    public long InputTransStartTime = 0;
    public long InputOriginalReceiveTime = 0;
    
    public long OutputTransTime = 0;
    public long OutputTransTimeToFirstDest = 0;
    public long OutputTransStartTime = 0;
    public long OutputOriginalReceiveTime = 0;
    
    public boolean ReceivedOutputProcCost = false;
    public boolean ReceivedOutputTransCost = false;
    
}

