package commonutils.threadpool;

public class ASharedThreadPool 
    extends AThreadPool {

    private static final int NUM_THREADS_IN_POOL = 5;
    private static final int[] CORE_TO_USE_FOR_EACH_THREAD = { 0, 0, 0, 0, 0 };
    
    private static class ASharedThreadPoolHolder {
        private static final ThreadPool INSTANCE = new ASharedThreadPool(
                NUM_THREADS_IN_POOL,
                CORE_TO_USE_FOR_EACH_THREAD
                );
    }
    
    public static ThreadPool getInstance() {
        return ASharedThreadPoolHolder.INSTANCE;
    }
    
    private ASharedThreadPool(
            int numThreads,
            int[] coreToUseForEachThread
            ) {
        super(
                numThreads,
                coreToUseForEachThread
                );
    }
}
