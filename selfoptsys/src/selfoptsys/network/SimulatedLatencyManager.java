package selfoptsys.network;

import java.util.*;


public interface SimulatedLatencyManager {

    void loadLatenciesFromFile(
            String latencyFile
            );
    int assignLatencyIndexForUserIndex(
            int userIndex
            );
    void mapLatencyIndexForUserIndex(
            int userIndex,
            int latencyIndex
            );
    HashMap<Integer, Integer> getUserIndexToLatencyIndexMappings();
    
    double getLatency(
            int sourceUserIndex,
            int destUserIndex
            );
    void setLatencies(
            double[][] latencies
            );
    double[][] getLatencies();
    double[] getLatenciesForUser(
            int userIndex
            );
    Map<Integer,Map<Integer,Double>> getLatenciesInUsableFormAsMap();
    HashMap<Integer,HashMap<Integer,Double>> getLatenciesInUsableFormAsHashMap();
    
}
