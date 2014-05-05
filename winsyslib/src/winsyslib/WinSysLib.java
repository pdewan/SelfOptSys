package winsyslib;

class WinSysLib {

	// winsyslib.dll must be in the java path, so add its directory
	// to your path environment variable or add it through 
	// java.library.path JVM parameter
	static 
	{
		System.loadLibrary( "winsyslib" );
	}
	
	public native int getNumProcessors();
	
	public native void setProcessProcessorAffinity( int processorNumber );
	
	public native void setThreadProcessorAffinity( int processorNumber );
	
	public native void setNumProcessorAffinityForProcess( int numProcs );
	
	public native void setProcessorsToUseForProcess( int[] processors, int length );
	
	public native void setThreadPriority( int priority );
	
    public native long getWindowHwnd( String windowTitle );

    public native void setProcessorsToUseForProcess( String processName, int[] processors, int length );
    
    public native boolean isWindowAlive( long hwnd );
    
    public native long getProcessorSpeed();
    
    public native String getProcessorName();
    
    public native String getProcessorIdentifier();
    
}
