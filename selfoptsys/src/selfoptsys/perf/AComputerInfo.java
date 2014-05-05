package selfoptsys.perf;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

public class AComputerInfo 
    implements ComputerInfo, Serializable {

    private static final long serialVersionUID = 901280708231040778L;
    
    protected String m_uniqueComputerTypeId;
    protected String m_processorName;
    protected String m_processorIdentifier;
    protected long m_processorSpeed;
    
    protected final static String[] m_knownProcessorNames = {
    		"Intel(R) Core(TM)2 Duo CPU     E4400  @ 2.00GHz",
    		"              Intel(R) Pentium(R) 4 CPU 1700MHz",
    		"Intel(R) Atom(TM) CPU N270   @ 1.60GHz"
    		};
    protected final static String[] m_knownProcessorIdentifiers = {
    		"x86 Family 6 Model 15 Stepping 13",
    		"x86 Family 15 Model 0 Stepping 10",
    		"x86 Family 6 Model 28 Stepping 2"
    		};
    protected final static long[] m_knownProcessorSpeeds = {
    		2000,
    		1700,
    		1600
    		};
    
    protected static List<ComputerInfo> m_knownComputerInfos = null;
    
    public AComputerInfo(
            String processorIdentifier,
            String processorName,
            long processorSpeed
            ) {
        this(
                processorIdentifier,
                processorName,
                processorSpeed,
                true
                );
    }
    
    private AComputerInfo(
            String processorIdentifier,
            String processorName,
            long processorSpeed,
            boolean translateToKnowComputerInfo
            ) {
        m_processorIdentifier = processorIdentifier;
        m_processorName = processorName;
        m_processorSpeed = processorSpeed;
        
        if ( translateToKnowComputerInfo ) {
        ComputerInfo nearestKnownComputerInfo = AComputerInfo.translateToNearestKnownComputerInfo( this );
            m_processorIdentifier = nearestKnownComputerInfo.getProcessorIdentifier();
            m_processorName = nearestKnownComputerInfo.getProcessorName();
            m_processorSpeed = nearestKnownComputerInfo.getProcessorSpeed();
        }
                
        m_uniqueComputerTypeId = 
            m_processorIdentifier + " -- " + 
            m_processorName + " -- " + 
            m_processorSpeed;
    }
            
    
    public String getProcessorIdentifier() {
        return m_processorIdentifier;
    }
    public String getProcessorName() {
        return m_processorName;
    }
    public long getProcessorSpeed() {
        return m_processorSpeed;
    }
    
    public String getUniqueComputerTypeId() {
        return m_uniqueComputerTypeId;
    }
    
    public boolean equals(
            Object other
            ) {
        return ( (ComputerInfo) other ).getUniqueComputerTypeId().equals( m_uniqueComputerTypeId );
    }
    
    public int hashCode() {
        return m_uniqueComputerTypeId.hashCode();
    }
    
    /*
     * TODO: Check whether processor identifier matches across different speeds
     * of the same processor
     */
    private static synchronized ComputerInfo translateToNearestKnownComputerInfo(
    		ComputerInfo computerInfoToTranslate
    		) {
    	
    	if ( m_knownComputerInfos == null ) {
    		buildKnownComputerInfos();
    	}
    	
    	ComputerInfo translatedComputerInfo = null; 
    	long translatedProcessorSpeed = getApproximateProcessorSpeed( 
				computerInfoToTranslate.getProcessorSpeed()
				);
    	
    	for ( ComputerInfo knownComputerInfo: m_knownComputerInfos ) {
    		if ( computerInfoToTranslate.getProcessorIdentifier().equals( knownComputerInfo.getProcessorIdentifier() ) &&
    				translatedProcessorSpeed == knownComputerInfo.getProcessorSpeed() ) {
    			translatedComputerInfo = knownComputerInfo;
    			break;
    		}
    	}
    	
    	if ( translatedComputerInfo == null ) {
    		translatedComputerInfo = new AComputerInfo(
    				computerInfoToTranslate.getProcessorIdentifier(),
    				computerInfoToTranslate.getProcessorName(),
    				translatedProcessorSpeed,
    				false
    				);
    		m_knownComputerInfos.add( translatedComputerInfo );
    	}
    	
    	return translatedComputerInfo;
    }
    
    private static void buildKnownComputerInfos() {
    	m_knownComputerInfos = new LinkedList<ComputerInfo>();
    	
    	for ( int i = 0; i < m_knownProcessorIdentifiers.length; i++ ) {
    		ComputerInfo knownComputerInfo = new AComputerInfo(
    				m_knownProcessorIdentifiers[ i ],
    				m_knownProcessorNames[ i ],
    				m_knownProcessorSpeeds[ i ],
    				false
    				);
    		m_knownComputerInfos.add( knownComputerInfo );
    	}
    }
    
    private static long getApproximateProcessorSpeed(
    		long processorSpeed
    		) {
    	long remainder = processorSpeed % 100;
    	
    	long adjustor = 0;
    	if ( remainder <= 17 ) {
    		adjustor = 0;
    	}
    	else if ( remainder <= 50 ) {
    		adjustor = 33;
    	}
    	else if ( remainder <= 83 ) {
    		adjustor = 66;
    	}
    	else {
    		adjustor = 100;
    	}
    	
    	return ( processorSpeed - remainder ) + adjustor;
    }
}
