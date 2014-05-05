package commonutils.scheduling;



public class WindowsThreadPriorityUtils {

    public static final int WINDOWS_PRIORITY_HIGHEST = 2;
    public static final int WINDOWS_PRIORITY_ABOVE_NORMAL = 1;
    public static final int WINDOWS_PRIORITY_NORMAL = 0;
    public static final int WINDOWS_PRIORITY_BELOW_NORMAL = -1;
    public static final int WINDOWS_PRIORITY_LOWEST = -2;

    public static int getIntPriorityFromWindowsThreadPriority(
            WindowsThreadPriority windowsThreadPriority
            ) {
        int priority = WINDOWS_PRIORITY_NORMAL;
        
        if ( windowsThreadPriority == WindowsThreadPriority.HIGHEST ) {
            priority = WINDOWS_PRIORITY_HIGHEST;
        }
        else if ( windowsThreadPriority == WindowsThreadPriority.ABOVE_NORMAL ) {
            priority = WINDOWS_PRIORITY_ABOVE_NORMAL;
        }
        else if ( windowsThreadPriority == WindowsThreadPriority.NORMAL ) {
            priority = WINDOWS_PRIORITY_NORMAL;
        }
        else if ( windowsThreadPriority == WindowsThreadPriority.BELOW_NORMAL ) {
            priority = WINDOWS_PRIORITY_BELOW_NORMAL;
        }
        else if ( windowsThreadPriority == WindowsThreadPriority.LOWEST ) {
            priority = WINDOWS_PRIORITY_LOWEST;
        }
        
        return priority;
    }
    
    public static WindowsThreadPriority getWindowsThreadPriorityFromInt(
            int priority
            ) {
        WindowsThreadPriority windowsThreadPriority = WindowsThreadPriority.NORMAL;
        
        if ( priority == WINDOWS_PRIORITY_HIGHEST ) {
            windowsThreadPriority = WindowsThreadPriority.HIGHEST;
        }
        else if ( priority == WINDOWS_PRIORITY_ABOVE_NORMAL ) {
            windowsThreadPriority = WindowsThreadPriority.ABOVE_NORMAL;
        }
        else if ( priority == WINDOWS_PRIORITY_NORMAL ) {
            windowsThreadPriority = WindowsThreadPriority.NORMAL;
        }
        else if ( priority == WINDOWS_PRIORITY_BELOW_NORMAL ) {
            windowsThreadPriority = WindowsThreadPriority.BELOW_NORMAL;
        }
        else if ( priority == WINDOWS_PRIORITY_LOWEST ) {
            windowsThreadPriority = WindowsThreadPriority.LOWEST;
        }
        
        return windowsThreadPriority;
    }

}