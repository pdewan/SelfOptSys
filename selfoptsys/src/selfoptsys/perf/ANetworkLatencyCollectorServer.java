package selfoptsys.perf;

import java.net.InetAddress;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import commonutils.basic.*;
import commonutils.basic2.*;
import commonutils.threadpool.*;

import selfoptsys.config.*;

public class ANetworkLatencyCollectorServer 
    extends ASelfOptArchThread
    implements NetworkLatencyCollectorServer {
    
    protected Map<Integer, NetworkLatencyCollectorUserInfo> m_userInfos;
    protected Map<Integer, Map<Integer, Double>> m_latencies;
    protected ReentrantLock m_lock;
    
    protected int m_networkLatencyCollectingTimeout;
    protected boolean m_timeoutReset;
    
    protected boolean m_quitFlag = false;
    
    public ANetworkLatencyCollectorServer() {
        boolean simulatingLatencies = AMainConfigParamProcessor.getInstance().getBooleanParam( Parameters.SIMULATING_NETWORK_LATENCIES );
        assert( simulatingLatencies == false );
        
        m_networkLatencyCollectingTimeout = AMainConfigParamProcessor.getInstance().getIntParam( Parameters.NETWORK_LATENCY_COLLECTING_TIMEOUT );

        m_lock = new ReentrantLock();
        
        m_userInfos = new Hashtable<Integer, NetworkLatencyCollectorUserInfo>();
        m_latencies = new Hashtable<Integer, Map<Integer,Double>>();
    }
    
    public void quit() {
        m_quitFlag = true;
        this.interrupt();
    }
    
    public void addUserHost(
            int userIndex,
            NetworkLatencyCollectorClient client,
            InetAddress host
            ) {
        
        m_lock.lock();
        {
            NetworkLatencyCollectorUserInfo userInfo = new ANetworkLatencyCollectorUserInfo(
                    userIndex,
                    client,
                    host
                    );
            m_userInfos.put( 
                    userIndex,
                    userInfo
                    );
            m_latencies.put(
                    userIndex,
                    new Hashtable<Integer,Double>()
                    );
        }
        m_lock.unlock();

    }
    
    public void removeUserHost(
            int userIndex
            ) {
        m_lock.lock();
        {
            m_userInfos.remove( userIndex );
            m_latencies.remove( userIndex );
            Iterator<Map.Entry<Integer, Map<Integer,Double>>> itr = 
                m_latencies.entrySet().iterator();
            while ( itr.hasNext() ) {
                itr.next().getValue().remove( userIndex );
            }
        }
        m_lock.unlock();
    }
    
    public void run() {
        while( true ) {
            try {
                List<Integer> allUserIndices = new LinkedList<Integer>();
                List<Integer> userIndicesToWhichToMeasureLatencies = new LinkedList<Integer>();
                List<InetAddress> userHostsToWhichToMeasureLatencies = new LinkedList<InetAddress>();
    
                m_lock.lock();
                {
                    Iterator<Map.Entry<Integer, NetworkLatencyCollectorUserInfo>> itr = 
                        m_userInfos.entrySet().iterator();
                    while ( itr.hasNext() ) {
                        Map.Entry<Integer, NetworkLatencyCollectorUserInfo> entry = itr.next();
                        boolean alreadyAdded = false;
                        for ( int i = 0; i < userHostsToWhichToMeasureLatencies.size(); i++ ) {
                            if ( userHostsToWhichToMeasureLatencies.get( i ).equals( entry.getValue().getHost() ) ) {
                                alreadyAdded = true;
                                break;
                            }
                        }
                        
                        allUserIndices.add( entry.getValue().getUserIndex() );
                        if ( alreadyAdded == false ) {
                            userIndicesToWhichToMeasureLatencies.add( entry.getValue().getUserIndex() );
                            userHostsToWhichToMeasureLatencies.add( entry.getValue().getHost() );
                        }
                    }
                }
                m_lock.unlock();
    
                for ( int i = 0; i < allUserIndices.size(); i++ ) {
                    askClientToCollectLatencies(
                            allUserIndices.get( i ),
                            userIndicesToWhichToMeasureLatencies,
                            userHostsToWhichToMeasureLatencies
                            );
                }
                
                Thread.sleep( m_networkLatencyCollectingTimeout );
            }
            catch ( InterruptedException e ) {
                if ( m_quitFlag ) {
                    break;
                }
                else {
                    ErrorHandlingUtils.logSevereExceptionAndContinue(
                            "ANetworkLatencyCollectorServer::run(): Unexpected interruption",
                            e
                            );
                }
            }
        }
    }
    
    private void askClientToCollectLatencies(
            final int userIndex,
            final List<Integer> userIndices,
            final List<InetAddress> userHosts
            ) {
        try {
            Runnable r = new Runnable() {
                public void run() {
                    try {
                        NetworkLatencyCollectorClient client = m_userInfos.get( userIndex ).getClient();
                        List<Integer> latencies = client.measureNetworkLatenciesToDestinations( userHosts );
                        processLatencies(
                                userIndex,
                                userIndices,
                                userHosts,
                                latencies
                                );
                    }
                    catch ( Exception e ) {
                        ErrorHandlingUtils.logSevereExceptionAndContinue(
                                "ANetworkLatencyCollectorServer::askClientToCollectLatencies: Error while asking client to measure latencies",
                                e
                                );
                    }
                }
            };
            ASharedThreadPool.getInstance().execute(
                    r
                    );

        }
        catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "ANetworkLatencyCollector: Error while asking agents to measure latencies",
                    e
                    );
        }
    }
    
    private synchronized void processLatencies(
            int userIndex,
            List<Integer> userIndices,
            List<InetAddress> userHosts,
            List<Integer> latencies
            ) {
        m_lock.lock();
        {
            Map<Integer,Double> latenciesForUser = m_latencies.get( userIndex );
            
            for ( int i = 0; i < userHosts.size(); i++ ) {
                int curLatency = latencies.get( i );
                
                Iterator<NetworkLatencyCollectorUserInfo> userInfoItr = m_userInfos.values().iterator();
                while ( userInfoItr.hasNext() ) {
                    NetworkLatencyCollectorUserInfo curDestUserInfo = userInfoItr.next();
                    int curDestUserIndex = curDestUserInfo.getUserIndex();
                    if ( userIndex == curDestUserIndex ) {
                        continue;
                    }
                    
                    if ( curDestUserInfo.getHost().equals( userHosts.get( i ) ) ) {
                        if ( m_latencies.get( curDestUserIndex ) != null ) {
                            latenciesForUser.put(
                                    curDestUserIndex,
                                    (double) curLatency
                                    );
                            Map<Integer,Double> latenciesForCurDestUser = m_latencies.get( curDestUserIndex );
                            latenciesForCurDestUser.put(
                                    userIndex,
                                    (double) curLatency
                                    );
                        }
                    }
                }
            }
        }
        m_lock.unlock();
    }
    
    public Map<Integer,Map<Integer,Double>> getCopyOfLatencies() {
        Map<Integer,Map<Integer,Double>> latencies = new Hashtable<Integer, Map<Integer,Double>>();
        
        m_lock.lock();
        {
            Iterator<Map.Entry<Integer, Map<Integer,Double>>> itr = m_latencies.entrySet().iterator();
            while ( itr.hasNext() ) {
                Map.Entry<Integer, Map<Integer,Double>> entry = itr.next();
                
                Map<Integer,Double> newLatenciesForUser = new Hashtable<Integer, Double>();
                latencies.put(
                        entry.getKey(),
                        newLatenciesForUser
                        );
                
                Iterator<Map.Entry<Integer, Double>> itr2 = entry.getValue().entrySet().iterator();
                while ( itr2.hasNext() ) {
                    Map.Entry<Integer, Double> entry2 = itr2.next();
                    newLatenciesForUser.put(
                            entry2.getKey(),
                            entry2.getValue()
                            );
                }
            }
        }
        m_lock.unlock();
        
        return latencies;
    }
}
