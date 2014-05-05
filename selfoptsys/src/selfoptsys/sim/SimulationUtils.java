package selfoptsys.sim;


public class SimulationUtils {

    public static long getActualDurationOfTaskWhenRunningConcurrentlyWithAnotherTask(
            long startTimeOfTask,
            long startTimeOfConcurrentTask,
            long durationOfTask,
            long durationOfConcurrentTask,
            long quantumSize
            ) {
        
        long value = 0;
        
        /*
         * If tasks don't overlap or durationOfTask is less than quantum size, then
         * just return durationOfTask
         */
        if ( durationOfTask <= quantumSize ) {
            return durationOfTask;
        }
        else if ( startTimeOfTask + durationOfTask <= startTimeOfConcurrentTask ) {
            return durationOfTask;
        }
        else if ( startTimeOfConcurrentTask + durationOfConcurrentTask <= startTimeOfTask ) {
            return durationOfTask;
        }
        
        long actualDurationOfTask = 0;

        /*
         * Bring us to the point where both the task and the concurrent task are actually
         * executing concurrently. Moreover, advance the time so that the next task that
         * is executing is the concurrent task;
         */
        long concurrentDurationOfTask = durationOfTask;
        long concurrentDurationOfConcurrentTask = durationOfConcurrentTask;
        if ( startTimeOfTask > startTimeOfConcurrentTask ) {
            concurrentDurationOfConcurrentTask -= ( startTimeOfTask - startTimeOfConcurrentTask );
            
            actualDurationOfTask += quantumSize;
            concurrentDurationOfConcurrentTask -= quantumSize;
            concurrentDurationOfTask -= quantumSize;
        }
        else {
            concurrentDurationOfTask -= ( startTimeOfConcurrentTask - startTimeOfTask );
            actualDurationOfTask += ( startTimeOfConcurrentTask - startTimeOfTask ); 
        }

        /* 
         * Based on the remaining time they run concurrently, deduce the actual
         * time the task took.
         */
        int numFullQuantumsOfTask = (int) ( concurrentDurationOfTask / quantumSize );
        int numQuantumsOfTask = numFullQuantumsOfTask;
        if ( numFullQuantumsOfTask * quantumSize < concurrentDurationOfTask ) {
            numQuantumsOfTask++;
        }
        
        int numFullQuantumsOfConcurrentTask = (int) ( concurrentDurationOfConcurrentTask / quantumSize );
        int numQuantumsOfConcurrentTask = numFullQuantumsOfConcurrentTask;
        if ( numFullQuantumsOfConcurrentTask * quantumSize < concurrentDurationOfConcurrentTask ) {
            numQuantumsOfConcurrentTask++;
        }
        
        if ( numFullQuantumsOfConcurrentTask > numFullQuantumsOfTask ) {
            if ( numFullQuantumsOfTask != numQuantumsOfTask ) {
                actualDurationOfTask += ( concurrentDurationOfTask - numQuantumsOfTask * quantumSize );
            }
            else {
                actualDurationOfTask += ( concurrentDurationOfTask - numFullQuantumsOfTask * quantumSize );
            }
        }
        else if ( numFullQuantumsOfConcurrentTask < numFullQuantumsOfTask ) {
            actualDurationOfTask += ( concurrentDurationOfTask - concurrentDurationOfConcurrentTask );
        }
        else {
            if ( numFullQuantumsOfConcurrentTask != numQuantumsOfConcurrentTask &&
                    numFullQuantumsOfTask != numQuantumsOfTask ) {
                actualDurationOfTask += ( concurrentDurationOfTask - concurrentDurationOfTask );
            }
            else if ( numFullQuantumsOfConcurrentTask == numQuantumsOfConcurrentTask &&
                    numFullQuantumsOfTask == numQuantumsOfTask ) {
                actualDurationOfTask += ( concurrentDurationOfTask - concurrentDurationOfTask );
            }
            else if ( numFullQuantumsOfConcurrentTask != numQuantumsOfConcurrentTask ) {
                actualDurationOfTask += ( concurrentDurationOfTask - numFullQuantumsOfTask * quantumSize );
            }
            else {
                actualDurationOfTask += ( concurrentDurationOfTask - concurrentDurationOfTask );
            }
        }
        
        return value;
        
    }

    public static long getDurationOfTaskWhenRunningConcurrentlyWithAnotherTask(
            int numFullQuantumsOfTask,
            int numFullQuantumsOfConcurrentTask,
            int numQuantumsOfTask,
            int numQuantumsOfConcurrentTask,
            long durationOfTaskInLastQuantum,
            long durationOfConcurrentTaskInLastQuantum,
            long quantumSize
            ) {
        
        long value = 0;
        
        /*
         * ASSUMPTION:
         * The concurrent task executes first
         */
        
        if ( numFullQuantumsOfConcurrentTask > numFullQuantumsOfTask ) {
            if ( numFullQuantumsOfTask != numQuantumsOfTask ) {
                value = ( numFullQuantumsOfTask * 2 ) * quantumSize + 
                    ( quantumSize + durationOfTaskInLastQuantum );
            }
            else {
                value = numFullQuantumsOfTask * 2 * quantumSize;
            }
        }
        else if ( numFullQuantumsOfConcurrentTask < numFullQuantumsOfTask ) {
            value = numFullQuantumsOfConcurrentTask * quantumSize + durationOfConcurrentTaskInLastQuantum +
                numFullQuantumsOfTask * quantumSize + durationOfTaskInLastQuantum;
        }
        else {
            if ( numFullQuantumsOfConcurrentTask != numQuantumsOfConcurrentTask &&
                    numFullQuantumsOfTask != numQuantumsOfTask ) {
                value = numFullQuantumsOfConcurrentTask * quantumSize + durationOfConcurrentTaskInLastQuantum +
                numFullQuantumsOfTask * quantumSize + durationOfTaskInLastQuantum;
            }
            else if ( numFullQuantumsOfConcurrentTask == numQuantumsOfConcurrentTask &&
                    numFullQuantumsOfTask == numQuantumsOfTask ) {
                value = numFullQuantumsOfTask * 2 * quantumSize;
            }
            else if ( numFullQuantumsOfConcurrentTask != numQuantumsOfConcurrentTask ) {
                value = numFullQuantumsOfConcurrentTask * quantumSize + durationOfConcurrentTaskInLastQuantum +
                numFullQuantumsOfTask * quantumSize;
            }
            else {
                value = numFullQuantumsOfConcurrentTask * quantumSize +
                numFullQuantumsOfTask * quantumSize + durationOfTaskInLastQuantum;
            }
        }
        
        return value;
        
    }

}
