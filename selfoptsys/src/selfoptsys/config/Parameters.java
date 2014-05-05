package selfoptsys.config;


public class Parameters {
    
    /*
     * General Settings
     */
	public static String DEFAULT_CONFIGURATION_FILE = "DefaultConfigurationFile";
    public static String CUSTOM_CONFIGURATION_FILE = "CustomConfigurationFile";
    public static String RMI_REGISTRY_HOST = "RmiRegistryHost";
    public static String RMI_REGISTRY_PORT = "RmiRegistryPort";
    public static String USER_INDEX = "UserIndex";
    public static String PPT_PRESENTATION_FILE = "PptPresentationFile";
    public static String PPT_FILES_DIR = "PptFilesDir";
    public static String PPT_SINGLE_SLIDE_FILE_PREFIX = "pptSingleSlideFilePrefix";
    public static String PPT_SINGLE_SLIDE_FILE_START_NUM = "pptSingleSlideFileStartNum";
    public static String PPT_SINGLE_SLIDE_FILE_END_NUM = "pptSingleSlideFileEndNum";
    public static String PPT_REPORT_START_PRESENTATION_COSTS = "PptReportStartPresentationCosts";
    public static String PPT_REPORT_SLIDE_COSTS = "PptReportSlideCosts";
    public static String PPT_REPORT_ANIMATION_COSTS = "PptReportAnimationCosts";
    public static String LOGGING_LEVEL = "LoggingLevel";
    
    /*
     * Replay Mode Settings
     */
    public static String USER_TURNS = "UserTurns";
    public static String THINK_TIMES = "ThinkTimes";
    public static String NUM_USERS = "NumUsers";
    
    /*
     * Communication Settings
     */
    public static String COMMUNICATION_TYPE = "CommunicationType";
    
    /*
     * Performance Measurement Settings
     */
    public static String USERS_FOR_WHO_TO_MEASURE_PERFORMANCE = "UsersForWhoToMeasurePerformance";
    public static String MEASURE_PERFORMANCE_FOR_ALL_USERS = "MeasurePerformanceForAllUsers";
    
    /*
     * Performance Optimizer Settings
     */
    public static String OPTIMIZE_PERFORMANCE = "OptimizePerformance";
    public static String OUTPUT_SYS_CONFIG_CHANGES = "OutputSysConfigChanges";
    public static String SYS_CONFIG_CHANGES_OUTPUT_FILE = "SysConfigChangesOutputFile";
    public static String RESP_TIME_TRADEOFF_POLICY_FACTORY_CLASS_NAME = "RespTimeTradeoffPolicyFactoryClassName";
    public static String USERS_FOR_WHO_TO_COLLECT_PERFORMANCE_DATA = "UsersForWhoToCollectPerformanceData";
    public static String COLLECT_PERFORMANCE_DATA_FOR_ALL_USERS = "CollectPerformanceDataForAllUsers";
    public static String NETWORK_LATENCY_COLLECTING_TIMEOUT = "NetworkLatencyCollectingTimeout";
    public static String PINGER_FILE = "PingerFile";
    public static String SIGNIFICANT_DIFFERENCE_IN_LOCAL_RESPONSE_TIMES = "SignificantDifferenceInLocalResponseTimes";
    public static String SIGNIFICANT_DIFFERENCE_IN_REMOTE_RESPONSE_TIMES = "SignificantDifferenceInRemoteResponseTimes";
    
    /*
     * Output Settings
     */
    public static String OUTPUT_DIRECTORY = "OutputDirectory";
    public static String PRINT_COSTS = "PrintCosts";
    public static String COSTS_OUTPUT_FILE = "CostsOutputFile";
    public static String PRINT_THINK_TIMES = "PrintThinkTimes";
    public static String THINK_TIMES_OUTPUT_FILE = "ThinkTimesOutputFile";
    public static String COST_ESTIMATES_FILE_OUT = "CostEstimatesFileOut";
    public static String THINK_TIME_ESTIMATE_FILE_OUT = "ThinkTimeEstimateFileOut";
    public static String TIME_SERVER_OUTPUT_FILE = "TimeServerOutputFile";
    public static String GENERAL_OUTPUT_FILE = "GeneralOutputFile";
    public static String LOG_FILE_OUT = "LogFileOut";
    
    /*
     * Input Settings
     */
    public static String INPUT_DIRECTORY = "InputDirectory";
    public static String NETWORK_LATENCY_MATRIX_FILE = "NetworkLatencyMatrixFile";
    public static String COST_ESTIMATES_FILE_IN = "CostEstimatesFileIn";
    public static String THINK_TIME_ESTIMATE_FILE_IN = "ThinkTimeEstimateFileIn";
    public static String LOG_FILE_IN = "LogFileIn";
    
    /*
     * UI Settings
     */
    public static String SHOW_ALL_UIS = "ShowAllUIs";
    public static String SHOW_GRAPHICAL_MAPPINGS = "ShowGraphicalMappings";
    
    /*
     * Network Latency Settings
     */
    public static String SIMULATING_NETWORK_LATENCIES = "SimulatingNetworkLatencies";
    public static String USE_NETWORK_LATENCY_MATRIX_FILE_LATENCIES = "UseNetworkLatencyMatrixFileLatencies";
    public static String USE_NETWORK_LATENCY_MATRIX_AS_IS = "UseNetworkLatencyMatrixAsIs";
    
    /*
     * Architecture Settings
     */
    public static String PROCESSING_ARCHITECTURE = "ProcessingArchitecture";
    public static String MASTER_USERS = "MasterUsers";
    
    /*
     * Processing Architecture Settings
     */
    public static String SUPPORT_CHANGING_PROCESSING_ARCHITECTURE = "SupportChangingProcessingArchitecture";
    public static String SUPPORT_REPLICATED_ARCHITECTURE = "SupportReplicatedArchitecture";
    public static String SUPPORT_ALL_CENTRALIZED_ARCHITECTURES = "SupportAllCentralizedArchitectures";
    public static String SUPPORTED_CENTRALIZED_ARCHITECTURE_MASTERS = "SupportedCentralizedArchitectureMasters";
    public static String SUPPORT_MULTICAST_COMMUNICATION = "SupportMulticastCommunication";
    public static String SUPPORT_SCHEDULING_POLICY_CHANGES = "SupportSchedulingPolicyChanges";
    
    /*
     * Scheduling Settings
     */
    public static String SCHEDULING_POLICY = "SchedulingPolicy";
    public static String LAZY_DELAY = "LazyDelay";
    public static String SCHEDULING_QUANTUM = "SchedulingQuantum";
    public static String CORES_TO_USE_FOR_FRAMEWORK_THREADS = "CoresToUseForFrameworkThreads";
    public static String CORE_TO_USE_FOR_PROCESSING_TASK_THREAD = "CoreToUseForProcessingTaskThread";
    public static String CORE_TO_USE_FOR_TRANSMISSION_TASK_THREAD = "CoreToUseForTransmissionTaskThread";
    public static String CORES_TO_USE_FOR_EXTERNAL_LOGGABLE_APPS = "CoresToUseForExternalLoggableApps";
    
    /*
     * Misc Settings
     */
    public static String USERS_WHO_INPUT_COMMANDS = "UsersWhoInputCommands";
    public static String ALL_USERS_INPUT_COMMANDS = "AllUsersInputCommands";
    public static String OPERATION_MODE = "OperationMode";
    public static String TIMES_AT_WHICH_USERS_JOIN = "TimesAtWhichUsersJoin";
    
    /*
     * Standard Loggable (i.e. Checkers, PowerPoint, IM) Parameters
     */
    public static String INPUTS_AND_OUTPUTS_HAVE_A_ONE_TO_ONE_MAPPING = "InputsAndOutputsHaveAOneToOneMapping";
    public static String REPORT_PROC_COST_FOR_EACH_COMMAND = "ReportProcCostForEachCommand";
    public static String REPORT_TRANS_COST_FOR_EACH_COMMAND = "ReportTransCostForEachCommand";
    public static String MIN_NUM_REPORTS_REQUIRED_TO_ESTIMATE_COSTS = "MinNumReportsRequiredToEstimateCosts";
    public static String MAX_NUM_REPORTS_USED_TO_ESTIMATE_COSTS = "MaxNumReportsUsedToEstimateCosts";
    public static String REPORT_EACH_COMMAND_TO_TIME_SERVER = "ReportEachCommandToTimeServer";
    
    /*
     * PowerPoint Loggable Settings
     */
    public static String PPT_PREFETCH_MODE = "PptPrefetchMode";
    
    /*
     * Specialized Loggable (i.e. SecondLife) Settings
     */
    public static String RECORDING_MANAGER_FACTORY_CLASS_NAME = "RecordingManagerFactoryClassName";
    public static String META_SCHEDULER_FACTORY_CLASS_NAME = "MetaSchedulerFactoryClassName";
    public static String SYS_CONFIG_OPTIMIZER_FACTORY_CLASS_NAME = "SysConfigOptimizerFactoryClassName";
    public static String REPLAY_COMMANDS_TO_LATECOMERS = "ReplayCommandsToLatecomers";
    public static String LOGGABLE_WILL_REPORT_READY_TWICE = "LoggableWillReportReadyTwice";
    
    /*
     * Fake Loggable Settings
     */
    public static String START_USER_INDEX = "StartUserIndex";
    public static String END_USER_INDEX = "EndUserIndex";
    
    /*
     * Demo Settings
     */
    public static String WAIT_FOR_USER_TO_RELEASE_TASKS = "WaitForUserToReleaseTasks";
    public static String WAIT_FOR_USER_TO_SCHEDULE_TASKS = "WaitForUserToScheduleTasks";
}
