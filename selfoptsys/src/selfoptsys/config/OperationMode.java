package selfoptsys.config;


public enum OperationMode {

    NORMAL() { public String toString() { return "NORMAL"; } },
    RECORD() { public String toString() { return "RECORD"; } },
    REPLAY() { public String toString() { return "REPLAY"; } }
    
}
