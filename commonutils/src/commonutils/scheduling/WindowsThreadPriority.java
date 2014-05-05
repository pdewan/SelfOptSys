package commonutils.scheduling;

public enum WindowsThreadPriority {
        HIGHEST { 
                public String toString() { return "HIGHEST"; }
            },
        ABOVE_NORMAL { 
                public String toString() { return "ABOVE_NORMAL"; } 
            },
        NORMAL { 
                public String toString() { return "NORMAL"; } 
            },
        BELOW_NORMAL { 
                public String toString() { return "BELOW_NORMAL"; }
            },
        LOWEST { 
                public String toString() { return "LOWEST"; } 
            }
    }