package selfoptsys.comm;


public enum MessageType {
    
    INPUT() { public String toString() { return "INPUT"; } },
    OUTPUT() { public String toString() { return "OUTPUT"; } },
    TS_INPUT_ENTERED() { public String toString() { return "TS_INPUT_ENTERED"; } },
    TS_OUTPUT_PROCESSED() { public String toString() { return "TS_OUTPUT_PROCESSED"; } },
    TS_CONFIGURATION_CHANGE_START() { public String toString() { return "TS_CONFIGURATION_CHANGE_START"; } },
    TS_CONFIGURATION_CHANGE_END() { public String toString() { return "TS_CONFIGURATION_CHANGE_END"; } },
    PERF_INPUT_ENTERED() { public String toString() { return "PERF_INPUT_ENTERED"; } },
    PERF_INPUT_PROC_TIME() { public String toString() { return "PERF_INPUT_PROC_TIME"; } },
    PERF_INPUT_TRANS_TIME() { public String toString() { return "PERF_INPUT_TRANS_TIME"; } },
    PERF_INPUT_RECEIVED_TIME() { public String toString() { return "PERF_INPUT_RECEIVED_TIME"; } },
    PERF_OUTPUT_PROC_TIME() { public String toString() { return "PERF_OUTPUT_PROC_TIME"; } },
    PERF_OUTPUT_TRANS_TIME() { public String toString() { return "PERF_OUTPUT_TRANS_TIME"; } },
    PERF_OUTPUT_RECEIVED_TIME() { public String toString() { return "PERF_OUTPUT_RECEIVED_TIME"; } },
    CLOCK_SKEW_MSG() { public String toString() { return "CLOCK_SKEW_MSG"; } },
    PERFORMANCE_OPTIMIZATION_MESSAGE() { public String toString() { return "PERFORMANCE_OPTIMIZATION_MESSAGE"; } }

}
