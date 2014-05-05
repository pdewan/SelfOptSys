package commonutils.config;

public enum ProcessingArchitectureType {

	UNDEFINED {
            public String toString() { return "UNDEFINED"; }
        },
	REPLICATED {
            public String toString() { return "REPLICATED"; }
        },
	CENTRALIZED {
            public String toString() { return "CENTRALIZED"; }
        },
	HYBRID {
            public String toString() { return "HYBRID"; }
        }
	
}
