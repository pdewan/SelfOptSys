package commonutils.basic2;

import java.io.*;
import java.util.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import commonutils.basic.*;

/*
 * We manually do the check to make sure there are no cast exceptions
 */
@SuppressWarnings("unchecked")
public class AMainConfigParamProcessor 
	extends AConfigParamProcessor {

	public final String DEFAULT_CONFIGURATION_FILE = "DefaultConfigurationFile";
	public final String DEFAULT_CONFIGURATION_FILE_NAME = "config.default.xml";

	protected String[] m_args;
	protected boolean m_initialized = false;

	private static class ACmdLineConfigParamProcessorHolder {
		private final static AMainConfigParamProcessor INSTANCE =
			new AMainConfigParamProcessor();
	}
	
	public static ConfigParamProcessor getInstance() {
		return ACmdLineConfigParamProcessorHolder.INSTANCE;
	}
	
	public static ConfigParamProcessor getInstance(
			String[] args,
			boolean reinit
			) {
		
		ACmdLineConfigParamProcessorHolder.INSTANCE.init( args, reinit );
		return ACmdLineConfigParamProcessorHolder.INSTANCE;
	}
	
	public static ConfigParamProcessor getInstance(
			String configFile,
			boolean reinit
			) {
		
		ACmdLineConfigParamProcessorHolder.INSTANCE.init( configFile, reinit );
		return ACmdLineConfigParamProcessorHolder.INSTANCE;
	}
	
	private AMainConfigParamProcessor() {}

	protected void init(
			String[] args,
			boolean reinit
			) {

		if ( m_initialized && !reinit ) {
			return;
		}
		
        m_paramSpecs = new Hashtable<String, ParamSpec>();
        m_paramInfos = new Hashtable<String, AParamInfo>();
        
		m_args = args;
		
		int paramStartIndex = 0;

		/*
		 * No parameters specified on the command line
		 */
		if ( m_args == null || m_args.length == 0 ) {
			processDefaultConfigurationFile( DEFAULT_CONFIGURATION_FILE_NAME );
			return;
		}

		/*
		 * The parameter and value for the config param specs file was specified
		 */
		if (m_args[0].toLowerCase().equals( ("--" + DEFAULT_CONFIGURATION_FILE).toLowerCase() ) ) {
			processDefaultConfigurationFile( m_args[1] );
			paramStartIndex = 2;
		}

		processCmdLineParams( paramStartIndex );
		
		m_initialized = true;
	}
	
	protected void init(
			String configFile,
			boolean reinit
			) {
		
		if ( m_initialized && !reinit ) {
			return;
		}
		
        m_paramSpecs = new Hashtable<String, ParamSpec>();
        m_paramInfos = new Hashtable<String, AParamInfo>();

        processDefaultConfigurationFile( configFile );
        
		Iterator<Map.Entry<String, ParamSpec>> itr = m_paramSpecs.entrySet().iterator();
		while ( itr.hasNext() ) {
			Map.Entry<String, ParamSpec> entry = itr.next();
			String paramName = entry.getKey();
		    ParamSpec spec = entry.getValue();
		    
	        /*
	         * numValuesExptected = 1 for a parameter with a single value
	         * numValuesExptected != 1 for a parameter that has an array (i.e. list) of values
	         */
		    List<String> paramValues = new LinkedList<String>();
		    String[] defaultValues = ( (String) spec.getDefaultValue()).split( " " );
	        for ( int i = 0; i < defaultValues.length; i++ ) {
	            paramValues.add(  defaultValues[ i ] );
	        }
	
	        AParamInfo info = createParamInfo(
	                spec,
	                paramValues,
	                false
	                );
	        m_paramInfos.put( paramName.toLowerCase(), info );
		}
        
        m_initialized = true;
	}
	
    protected void processDefaultConfigurationFile(
            String configParamSpecsFile
            ) {
        
        try {
            File file = new File( configParamSpecsFile );
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            
            Document doc = db.parse(file);
            doc.getDocumentElement().normalize();
            NodeList nodeList = doc.getElementsByTagName("parameter");
            
            for (int s = 0; s < nodeList.getLength(); s++) {
                Node firstNode = nodeList.item( s );
                if ( firstNode.getNodeType() == Node.ELEMENT_NODE ) {
                     Element firstElement = (Element) firstNode;
                     
                     NodeList paramNameNodeList = firstElement.getElementsByTagName("name");
                     Element paramNameElement = (Element) paramNameNodeList.item(0);
                     NodeList paramNameElementChildNodes = paramNameElement.getChildNodes();
                     String parameterName = ((Node) paramNameElementChildNodes.item(0)).getNodeValue();
                     
                     NodeList paramTypeNodeList = firstElement.getElementsByTagName("type");
                     Element paramTypeElement = (Element) paramTypeNodeList.item(0);
                     NodeList paramTypeElementChildNodes = paramTypeElement.getChildNodes();
                     String parameterType = ((Node) paramTypeElementChildNodes.item(0)).getNodeValue();
                     ConfigParamType type = getConfigParamTypeFromString( parameterType );
                     
                     int parameterNumValues = -1;
                     NodeList paramNumValuesNodeList = firstElement.getElementsByTagName("numValues");
                     if ( paramNumValuesNodeList != null && paramNumValuesNodeList.getLength() > 0 ) {
                         Element paramNumValuesElement = (Element) paramNumValuesNodeList.item(0);
                         NodeList paramNumValuesElementChildNodes = paramNumValuesElement.getChildNodes();
                         parameterNumValues = Integer.parseInt( ((Node) paramNumValuesElementChildNodes.item(0)).getNodeValue() );
                     }
                     
                     Object parameterDefaultValue = null;
                     if ( parameterNumValues != 0 ) {
                         NodeList paramDefaultValueNodeList = firstElement.getElementsByTagName("defaultValue");
                         Element paramDefaultValueElement = (Element) paramDefaultValueNodeList.item(0);
                         NodeList paramDefaultValueElementChildNodes = paramDefaultValueElement.getChildNodes();
                         if ( paramDefaultValueElementChildNodes.getLength() > 0 ) {
                        	 parameterDefaultValue = ((Node) paramDefaultValueElementChildNodes.item(0)).getNodeValue();
                         }
                     }
                     
                     ParamSpec spec = new AParamSpec(
                             parameterName.toLowerCase(),
                             type,
                             parameterNumValues,
                             parameterDefaultValue
                             );
                     m_paramSpecs.put( parameterName.toLowerCase(), spec );
                     
                }
            }
        }
        catch ( Exception e ) {
            System.err.println( e );
        }
        
    }

	private void processCmdLineParams(
			int startIndex
			) {
	    
	    for ( int i = 0; i < m_args.length; i++ ) {
	        if ( m_args[i].length() > 2 && m_args[i].substring( 0, 2 ).equals( "--" ) ) {
	            String paramName = m_args[i].substring( 2 );
	            ParamSpec spec = m_paramSpecs.get( paramName.toLowerCase() );
	            if ( spec == null ) {
                    ErrorHandlingUtils.logWarningMessageAndContinue( "No spec found for command line parameter '" + paramName + "'" );
	                if ( m_args.length == i+1 || ( m_args.length > i+1 && m_args[i+1].length() > 2 && m_args[i+1].substring( 0, 2 ).equals( "--" ) ) ) {
	                    ErrorHandlingUtils.logWarningMessageAndContinue( "Assuming '" + paramName + "' is BOOLEAN parameter with value TRUE since no values are specified." );
                        spec = new AParamSpec(
                                paramName.toLowerCase(),
                                ConfigParamType.BOOLEAN,
                                1,
                                "true"
                                );
	                }
	                else {
	                    ErrorHandlingUtils.logWarningMessageAndContinue( "Assuming '" + paramName + "' is STRING_ARRAY parameter since at least one value is specified." );
	                    spec = new AParamSpec(
	                            paramName.toLowerCase(),
	                            ConfigParamType.STRING_ARRAY,
	                            -1,
	                            ""
	                            );
	                }
                    m_paramSpecs.put( paramName.toLowerCase(), spec );
	            }
	        }
	    }

		Iterator<Map.Entry<String, ParamSpec>> itr = m_paramSpecs.entrySet().iterator();
		while ( itr.hasNext() ) {
			Map.Entry<String, ParamSpec> entry = itr.next();
			String paramName = entry.getKey();
		    ParamSpec spec = entry.getValue();
			processCmdLineParam(
					startIndex,
					paramName,
					(String) spec.getDefaultValue()
					);
		}
	}

	private void processCmdLineParam(
	        int startIndex,
	        String parameterName,
	        String defaultValue
	        ) {

	    boolean paramSpecified = false;
	    List<String> paramValues = new LinkedList<String>();
	    
        ParamSpec spec = m_paramSpecs.get( parameterName );
        int numValuesExpected = spec.getNumValues();

        for (int i = startIndex; i < m_args.length; i++) {

			if ( m_args[i].toLowerCase().equals( ( "--" + parameterName ) ) ) {
			    paramSpecified = true;
			    
			    int numValuesFound = 0;
                for ( int j = i + 1; j < m_args.length; j++ ) {
                    if ( m_args[j].length() > 2 && m_args[j].substring( 0, 2 ).equals( "--" ) ) {
                        break;
                    }
                    numValuesFound++;
                }
				
                /*
                 * numValuesExptected = 1 for a parameter with a single value (i.e., String, Int, Boolean, Double)
                 * numValuesExptected != 1 for a parameter that has an array (i.e. list) of values
                 */
				if ( numValuesExpected == 1 && numValuesFound != numValuesExpected ) {
					System.err.println("Parameter " + parameterName + " has an incorrect number of values specified: " + numValuesExpected + " values expected." );
					continue;
				}

		        for ( int j = i + 1; j < m_args.length; j++ ) {
		            if ( m_args[j].length() > 2 && m_args[j].substring( 0, 2 ).equals( "--" ) ) {
		                break;
		            }
		            paramValues.add(  m_args[j] );
		        }

			}
		}
		
		if ( paramSpecified == false ) {
		    
            /*
             * numValuesExptected = 1 for a parameter with a single value
             * numValuesExptected != 1 for a parameter that has an array (i.e. list) of values
             */
            String[] defaultValues = ( (String) spec.getDefaultValue()).split( " " );
            for ( int i = 0; i < defaultValues.length; i++ ) {
                paramValues.add(  defaultValues[ i ] );
            }
		    
		}

        AParamInfo info = createParamInfo(
                spec,
                paramValues,
                paramSpecified
                );
        m_paramInfos.put( parameterName.toLowerCase(), info );

	}
	
	public static void overrideValuesByThoseSpecifiedInSource(
			ConfigParamProcessor target,
	        ConfigParamProcessor source
	        ) {
	    
	    Iterator<Map.Entry<String, AParamInfo>> itr = source.getAllParamInfos().entrySet().iterator();
	    while ( itr.hasNext() ) {  
	        Map.Entry<String, AParamInfo> entry = itr.next();
	        AParamInfo sourceParamInfo = entry.getValue();
	        String paramName = sourceParamInfo.getName();
	        
	        ParamSpec targetParamSpec = target.getAllParamSpecs().get( entry.getKey() );
	        if ( targetParamSpec == null ) {
	            ErrorHandlingUtils.logSevereMessageAndContinue(
	            		"AConfigParamProcess::overrideValuesInTargetByThoseSpecifiedOnCmdLineInSource - " +
	            		"Cannot override '" + paramName + "' because it is not specified in target"
	            		);
	            continue;
	        }
	        AParamInfo targetParamInfo = target.getAllParamInfos().get( entry.getKey() );
	        if ( targetParamInfo.getWasSpecifiedOnCmdLine() == true ) {
	        	/*
	        	 * A default config file value can be overridden by a value in another config file
	        	 * only if that value was not specified on the command line.
	        	 */
	        	continue;
	        }
	        
	        target.getAllParamInfos().put( paramName.toLowerCase(), sourceParamInfo );
	    }
	    
	}
	

	
}
