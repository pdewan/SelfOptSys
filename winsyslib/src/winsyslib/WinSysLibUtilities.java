package winsyslib;

public class WinSysLibUtilities {

    protected static WinSysLib m_sysInfo = new WinSysLib();
    
    public static void setThreadPriority(
            int priority
            ) {
        m_sysInfo.setThreadPriority( priority );
    }
    
    public static int getNumProcessors() {
        return m_sysInfo.getNumProcessors();
    }
    
    public static void setProcessorsToUseForProcess(
            int[] processors
            ) {
        m_sysInfo.setProcessorsToUseForProcess(
                processors,
                processors.length
                );
    }
    
    public static void setProcessorsToUseForProcess_0Based(
            int[] processors
            ) {
        int[] modProcessors = new int[ processors.length ];
        for ( int i = 0; i < processors.length; i++ ) {
            modProcessors[i] = processors[i] + 1;
        }
        setProcessorsToUseForProcess( modProcessors );
    }
    
//    public static long setThreadProcessorAffinity(
//            int processorNumber
//            ) {
//        return m_sysInfo.setThreadProcessorAffinity( processorNumber );
//    }
//    
//    public static long setThreadProcessorAffinity_0Based(
//            int processorNumber
//            ) {
//        return ( m_sysInfo.setThreadProcessorAffinity( processorNumber + 1 ) - 1 );
//    }
    
    public static void setThreadProcessorAffinity(
            int processorNumber
            ) {
        m_sysInfo.setThreadProcessorAffinity( processorNumber );
    }
    
    public static void setThreadProcessorAffinity_0Based(
            int processorNumber
            ) {
        m_sysInfo.setThreadProcessorAffinity( processorNumber + 1 );
    }

    public static void setProcessProcessorAffinity(
            int processorNumber
            ) {
        m_sysInfo.setProcessProcessorAffinity( processorNumber );
    }
    
    public static void setProcessProcessorAffinity_0Based(
            int processorNumber
            ) {
        m_sysInfo.setProcessProcessorAffinity( processorNumber + 1 );
    }
    
    public static void setProcessorsToUseForProcess(
            String processTitle,
            int[] processors
            ) {
        m_sysInfo.setProcessorsToUseForProcess(
                processTitle,
                processors,
                processors.length
                );
    }
    
    public static void setProcessorsToUseForProcess_0Based(
            String processTitle,
            int[] processors
            ) {
        int[] modProcessors = new int[ processors.length ];
        for ( int i = 0; i < processors.length; i++ ) {
            modProcessors[i] = processors[i] + 1;
        }
        setProcessorsToUseForProcess(
                processTitle,
                modProcessors
                );
    }
    
    public static long getWindowHwnd(
            String windowTitle
            ) {
        return m_sysInfo.getWindowHwnd( windowTitle );
    }

    public static boolean isWindowAlive(
            long hwnd
            ) {
        return m_sysInfo.isWindowAlive( hwnd );
    }
    
    public static long getProcessorSpeed() {
        return m_sysInfo.getProcessorSpeed();
    }
    
    public static String getProcessorName() {
        return m_sysInfo.getProcessorName();
    }
    
    public static String getProcessorIdentifier() {
        return m_sysInfo.getProcessorIdentifier();
    }
    
}
