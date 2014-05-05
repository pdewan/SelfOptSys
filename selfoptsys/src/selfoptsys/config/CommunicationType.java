package selfoptsys.config;


public enum CommunicationType {

    TCP_SOCKETS_SERIALIZED_OBJECT() { public String toString() { return "TCP_SOCKETS_SERIALIZED_OBJECT"; } },
    TCP_SOCKETS_BLOCKING() { public String toString() { return "TCP_SOCKETS_BLOCKING"; } },
    TCP_SOCKETS_NON_BLOCKING() { public String toString() { return "TCP_SOCKETS_NON_BLOCKING"; } },
    UDP_SOCKETS_BLOCKING() { public String toString() { return "UDP_SOCKETS_BLOCKING"; } },
    RMI() { public String toString() { return "RMI"; } }
    
}
