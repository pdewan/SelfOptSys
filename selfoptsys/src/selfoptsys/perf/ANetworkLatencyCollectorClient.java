package selfoptsys.perf;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.rmi.server.UnicastRemoteObject;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import selfoptsys.config.*;

import commonutils.basic.*;
import commonutils.basic2.*;
import commonutils.scheduling.WindowsThreadPriority;
import commonutils.threadpool.ASharedThreadPool;

public class ANetworkLatencyCollectorClient 
    implements NetworkLatencyCollectorClient {

    protected String m_inputDirectory;
    protected String m_outputDirectory;
    protected String m_pingerFilePath;
    
    protected NetworkLatencyCollectorClient m_rmiStub;
    
    public ANetworkLatencyCollectorClient() {
        m_inputDirectory = AMainConfigParamProcessor.getInstance().getStringParam( Parameters.INPUT_DIRECTORY );
        m_outputDirectory = AMainConfigParamProcessor.getInstance().getStringParam( Parameters.OUTPUT_DIRECTORY );
        m_pingerFilePath = AMainConfigParamProcessor.getInstance().getStringParam( Parameters.PINGER_FILE );
        
        try {
            m_rmiStub = 
                (NetworkLatencyCollectorClient) UnicastRemoteObject.exportObject( (NetworkLatencyCollectorClient) this, 0 );
        }
        catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "ANetworkLatencyCollectorClient: Exception occurred while generating rmi stub",
                    e
                    );
        }
    }
    
    public NetworkLatencyCollectorClient getRmiStub() {
        return m_rmiStub;
    }
    
    public List<Integer> measureNetworkLatenciesToDestinations(
            List<InetAddress> destinations
            ) {
        
        List<Integer> latencies = new LinkedList<Integer>();
        
        try {
            final BlockingQueue<String> pingBB = new ArrayBlockingQueue<String>( destinations.size() );
            int[] arrayLatencies = new int[ destinations.size() ];
            
            for ( int i = 0; i < destinations.size(); i++ ) {
                final InetAddress hostAddressToPing = destinations.get( i );
                final int id = i;
                
                Runnable r = new Runnable() {
                    
                    public void run() {
                        try {
                            String result = pingHost(
                                    id, 
                                    hostAddressToPing
                                    );
                            pingBB.put( result );
                        }
                        catch ( Exception e ) {
                            ErrorHandlingUtils.logSevereExceptionAndContinue(
                                    "ANetworkLatencyCollectorClient::measureNetworkLatenciesToDestinations: Exception occurred while collecting latencies",
                                    e
                                    );
                        }
                    }
                };
                ASharedThreadPool.getInstance().execute(
                        r,
                        0,
                        WindowsThreadPriority.BELOW_NORMAL
                        );
            }
            
            for ( int i = 0; i < destinations.size(); i++ ) {
                String pingAnswer = pingBB.take();
                int id = Integer.parseInt( pingAnswer.split( " " )[0] );
                int latency = Integer.parseInt( pingAnswer.split( " " )[1] );
                arrayLatencies[ id ] = latency;
            }
            
            for ( int i = 0; i < destinations.size(); i++ ) {
                latencies.add( arrayLatencies[ i ] );
            }
        }
        catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "ANetworkLatencyCollectorClient::measureNetworkLatenciesToDestinations: Exception occurred while collecting latencies",
                    e
                    );
        }
        
        return latencies;
        
    }
    
    private String pingHost(
            int id,
            InetAddress hostAddressToPing
            ) {
        int latency = 0;
        
        try {
            String ipAddress = hostAddressToPing.getHostAddress();
            
            String fileName = m_outputDirectory + "\\" + UUID.randomUUID().toString();
            File outFile = new File( fileName );
            outFile.createNewFile();
            outFile.deleteOnExit();
            
            Process p = Runtime.getRuntime().exec( 
                    "cmd.exe /C " + m_inputDirectory + "\\" + m_pingerFilePath + " " + ipAddress + " " + fileName
                    );
            p.waitFor();

            InputStream is = new FileInputStream( outFile );
            BufferedReader bin = new BufferedReader( new InputStreamReader( is ) );
            while ( true ) {
                String line = bin.readLine();
                if ( line.contains( "Destination host unreachable" ) ) {
                    latency = Integer.MAX_VALUE;
                    break;
                }
                else if ( line.contains( "Minimum" ) &&
                       line.contains( "Maximum" ) &&
                       line.contains( "Average" ) ) {
                    String[] vals = line.split( " " );
                    String latencyStr = vals[ vals.length - 1 ];
                    latencyStr = latencyStr.substring( 0, latencyStr.length() - 2 );
                    latency = Integer.parseInt( latencyStr );
                    break;
                }
            }
            is.close();
        }
        catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "ANetworkLatencyCollectorClient::pingHost(): Exception occurred while pinging host",
                    e
                    );
        }
        
        return id + " " + latency;
    }

}
