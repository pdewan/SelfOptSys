package selfoptsys.perf;


public interface ComputerInfo {

    String getProcessorName();
    String getProcessorIdentifier();
    long getProcessorSpeed();
    String getUniqueComputerTypeId();
    
    public boolean equals(
            Object obj
            );
    public int hashCode();
}
