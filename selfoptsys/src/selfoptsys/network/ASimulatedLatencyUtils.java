package selfoptsys.network;

import java.io.*;
import java.util.*;
import commonutils.basic.*;


public class ASimulatedLatencyUtils {

    public static double[][] readInLatenciesFromFile(
            String networkRttMatrixFile
            ) {
        
        if ( networkRttMatrixFile == null ) {
            return null;
        }
        
        Vector<String> networkLatencyMatrixRows = new Vector<String>();
        double[][] networkLatencyMatrix;
        
        try {
            File f = new File( networkRttMatrixFile );
            if ( f.exists() == false ) {
                return null;
            }
            
            FileReader fr = new FileReader( networkRttMatrixFile );
            BufferedReader br = new BufferedReader( fr );

            String line = null;
            while ( ( line = br.readLine() ) != null ) {
                networkLatencyMatrixRows.add( line );
            }

            br.close();
            fr.close();
        }
        catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "ASimulatedLatencyUtils: Error while reading in latencies from file",
                    e
                    );
            return null;
        }
        
        int numNodesInMatrix = networkLatencyMatrixRows.size();
        networkLatencyMatrix = new double[ numNodesInMatrix ][ numNodesInMatrix ];
        
        for ( int i = 0; i < numNodesInMatrix; i++ ) {
            String[] networkLatencyMatrixRowValues = networkLatencyMatrixRows.elementAt( i ).split( " " ); 
            for ( int j = 0; j < numNodesInMatrix; j++ ) {
                networkLatencyMatrix[ i ][ j ] = Double.parseDouble( networkLatencyMatrixRowValues[ j ] );
            }
        }
        
        networkLatencyMatrixRows = null;
        
        
        return networkLatencyMatrix;
        
    }    
    public static void pruneP2PSimRttMatrixFileToLatencyFile(
            String rttMatrixFileIn,
            String rttMatrixFileOut,
            int numIndicesToUse
            ) {
        
        double[][] rawLatencies = ANetworkGenerator.readInNetworkRttMatrixFromFile(
                rttMatrixFileIn
                );
        rawLatencies = ANetworkGenerator.generateNetworkLatenciesFromMatrix(
                numIndicesToUse,
                rawLatencies
                );
        
        try {
            File out = new File( rttMatrixFileOut );
            PrintWriter pw = new PrintWriter( out );
            
            for ( int i = 0; i < rawLatencies.length; i++ ) {
                String line = "";
                for ( int j = 0; j < rawLatencies[ i ].length; j++ ) {
                    line += rawLatencies[ i ][ j ] + " "; 
                }
                line = line.substring( 0, line.length() - 1 );
                pw.println( line );
            }
            
            pw.close();
        }
        catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "ASimulatedLatencyUtils: Error while pruning rtt matrix to latency file",
                    e
                    );
        }
                
    }
    
    public static void getLatencyMatrixFileFromSimP2RttMatrixFile(
            String rttMatrixFileIn,
            String rttMatrixFileOut
            ) {
        
        double[][] rawLatencies = ANetworkGenerator.readInNetworkRttMatrixFromFile(
                rttMatrixFileIn
                );
        
        double[] avgLatencies = new double[ rawLatencies.length ];
        for ( int i = 0; i < rawLatencies.length; i++ ) {
            double total = 0;
            int count = 0;
            for ( int j = 0; j < rawLatencies[ i ].length; j++ ) {
                if ( rawLatencies[i][j] != -1 ) {
                    total += rawLatencies[i][j];
                    count++;
                }
            }
            avgLatencies[i] = (int) ( total / count );
        }
        
        for ( int i = 0; i < rawLatencies.length; i++ ) {
            for ( int j = 0; j < rawLatencies[ i ].length; j++ ) {
                if ( rawLatencies[i][j] == -1 ) {
                    rawLatencies[i][j] = avgLatencies[ i ];
                }
            }
        }

        try {
            File out = new File( rttMatrixFileOut );
            PrintWriter pw = new PrintWriter( out );
            
            for ( int i = 0; i < rawLatencies.length; i++ ) {
                String line = "";
                for ( int j = 0; j < rawLatencies[ i ].length; j++ ) {
                    line += rawLatencies[ i ][ j ] + " "; 
                }
                line = line.substring( 0, line.length() - 1 );
                pw.println( line );
            }
            
            pw.close();
        }
        catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "ASimulatedLatencyUtils: Error while pruning rtt matrix to latency file",
                    e
                    );
        }
                
    }

    
    public static void main( String[] args ) {
        
        String rttFileIn = args[0];
        String rttFileOut = args[1];
//        int numIndicesToUse = Integer.parseInt( args[2] );
//        pruneP2PSimRttMatrixFileToLatencyFile( rttFileIn, rttFileOut, numIndicesToUse );
        
        getLatencyMatrixFileFromSimP2RttMatrixFile(
                rttFileIn,
                rttFileOut
                );
    }
    
}
