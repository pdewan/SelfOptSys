package selfoptsys.network;

import java.io.*;
import java.util.*;
import commonutils.basic.*;


public class ASimulatedLatencyManager 
    implements SimulatedLatencyManager {

    protected double[][] m_latencies;
    
    protected Map<Integer, Integer> m_userIndexToLatencyIndexMap;
    protected Random m_rand;
    
    public ASimulatedLatencyManager() {
        m_userIndexToLatencyIndexMap = new Hashtable<Integer, Integer>();
        m_rand = new Random( 0 );
    }
    
    public void loadLatenciesFromFile(
            String latencyFile
            ) {
        try {
            File rttMatrixFile = new File( latencyFile );
            if ( rttMatrixFile.exists() == false ) {
                ErrorHandlingUtils.logSevereMessageAndContinue( "RTT matrix file '" + latencyFile + "' does not exist." );
            }
        }
        catch ( Exception e ) {
            ErrorHandlingUtils.logSevereMessageAndContinue( "Error reading RTT matrix file '" + latencyFile + ".'" );
        }
        
        m_latencies = ASimulatedLatencyUtils.readInLatenciesFromFile( latencyFile );
    }
    
    public void setLatencies(
            double[][] latencies
            ) {
        m_latencies = latencies;
    }
    public double[][] getLatencies() {
        return m_latencies;
    }
    
    public Map<Integer,Map<Integer,Double>> getLatenciesInUsableFormAsMap() {
        
        Map<Integer,Map<Integer,Double>> latencies = new Hashtable<Integer, Map<Integer,Double>>();
        
        Iterator<Map.Entry<Integer, Integer>> itr = m_userIndexToLatencyIndexMap.entrySet().iterator();
        while ( itr.hasNext() ) {
            Map.Entry<Integer, Integer> entry = itr.next();
            int sourceUserIndex = entry.getKey();
            int sourceLatencyIndex = entry.getValue();

            Map<Integer, Double> latenciesForCurUser = latencies.get( sourceUserIndex );
            if ( latenciesForCurUser == null ) {
                latenciesForCurUser = new Hashtable<Integer, Double>();
                latencies.put(
                        sourceUserIndex,
                        latenciesForCurUser
                        );
            }
            
            Iterator<Map.Entry<Integer, Integer>> itr2 = m_userIndexToLatencyIndexMap.entrySet().iterator();
            while ( itr2.hasNext() ) {
                Map.Entry<Integer, Integer> entry2 = itr2.next();
                int destUserIndex = entry2.getKey();
                int destLatencyIndex = entry2.getValue();
                
                latenciesForCurUser.put(
                        destUserIndex, 
                        m_latencies[ sourceLatencyIndex ][ destLatencyIndex ]
                        );
            }
        }
        
        return latencies;
        
    }
    
    public HashMap<Integer,HashMap<Integer,Double>> getLatenciesInUsableFormAsHashMap() {
        
        HashMap<Integer,HashMap<Integer,Double>> latencies = new HashMap<Integer, HashMap<Integer,Double>>();
        
        Iterator<Map.Entry<Integer, Integer>> itr = m_userIndexToLatencyIndexMap.entrySet().iterator();
        while ( itr.hasNext() ) {
            Map.Entry<Integer, Integer> entry = itr.next();
            int sourceUserIndex = entry.getKey();
            int sourceLatencyIndex = entry.getValue();

            HashMap<Integer, Double> latenciesForCurUser = latencies.get( sourceUserIndex );
            if ( latenciesForCurUser == null ) {
                latenciesForCurUser = new HashMap<Integer, Double>();
                latencies.put(
                        sourceUserIndex,
                        latenciesForCurUser
                        );
            }
            
            Iterator<Map.Entry<Integer, Integer>> itr2 = m_userIndexToLatencyIndexMap.entrySet().iterator();
            while ( itr2.hasNext() ) {
                Map.Entry<Integer, Integer> entry2 = itr2.next();
                int destUserIndex = entry2.getKey();
                int destLatencyIndex = entry2.getValue();
                
                latenciesForCurUser.put(
                        destUserIndex, 
                        m_latencies[ sourceLatencyIndex ][ destLatencyIndex ]
                        );
            }
        }
        
        return latencies;
        
    }
    
    public double[] getLatenciesForUser(
            int userIndex
            ) {
        int latencyIndex = m_userIndexToLatencyIndexMap.get( userIndex );
        return m_latencies[ latencyIndex ];
    }
    
    public int assignLatencyIndexForUserIndex(
            int userIndex
            ) {
        int latencyIndex = -1;
        
        latencyIndex = m_rand.nextInt( m_latencies.length );
        m_userIndexToLatencyIndexMap.put(
                userIndex,
                latencyIndex
                );
        
        return latencyIndex;
    }
    
    public void mapLatencyIndexForUserIndex(
            int userIndex,
            int latencyIndex
            ) {
        m_userIndexToLatencyIndexMap.put( userIndex, latencyIndex );
    }
    
    public HashMap<Integer, Integer> getUserIndexToLatencyIndexMappings() {
        HashMap<Integer, Integer> userIndexToLatencyIndexMap = new HashMap<Integer, Integer>();
        
        Iterator<Map.Entry<Integer, Integer>> itr = m_userIndexToLatencyIndexMap.entrySet().iterator();
        while ( itr.hasNext() ) {
            Map.Entry<Integer, Integer> entry = itr.next();
            userIndexToLatencyIndexMap.put(
                    entry.getKey(),
                    entry.getValue()
                    );
        }
        
        return userIndexToLatencyIndexMap;
    }
    
    public double getLatency(
            int sourceUserIndex,
            int destUserIndex
            ) {
        int sourceLatencyIndex = m_userIndexToLatencyIndexMap.get( sourceUserIndex );
        int destLatencyIndex = m_userIndexToLatencyIndexMap.get( destUserIndex );
        return m_latencies[ sourceLatencyIndex ][ destLatencyIndex ];
    }
    
}
