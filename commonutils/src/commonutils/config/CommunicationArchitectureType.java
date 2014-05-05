package commonutils.config;

public enum CommunicationArchitectureType {

	UNDEFINED {
            public String toString() { return "UNDEFINED"; }
        },
	UNICAST {
            public String toString() { return "UNICAST"; }
        },
	HMDM {
            public String toString() { return "HMDM"; }
        }
	
}
