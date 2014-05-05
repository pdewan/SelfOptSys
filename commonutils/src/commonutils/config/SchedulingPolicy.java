package commonutils.config;

public enum SchedulingPolicy {

	UNDEFINED {
	        public String toString() { return "UNDEFINED"; }
	    },
    MULTI_CORE_PROCESS_FIRST {
            public String toString() { return "MULTI_CORE_PROCESS_FIRST"; }
        },
    MULTI_CORE_TRANSMIT_FIRST {
            public String toString() { return "MULTI_CORE_TRANSMIT_FIRST"; }
        },
    MULTI_CORE_CONCURRENT {
            public String toString() { return "MULTI_CORE_CONCURRENT"; }
        },
    MULTI_CORE_LAZY_PROCESS_FIRST {
            public String toString() { return "MULTI_CORE_LAZY_PROCESS_FIRST"; }
        }
	
}
