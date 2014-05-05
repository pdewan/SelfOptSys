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
public class ASettingsFileConfigParamProcessor 
	extends AConfigParamProcessor {

	protected boolean m_initialized = false;

	private static class ASettingsFileConfigParamProcessorHolder {
		private final static ASettingsFileConfigParamProcessor INSTANCE =
			new ASettingsFileConfigParamProcessor();
	}
	
	public static ConfigParamProcessor getInstance() {
		return ASettingsFileConfigParamProcessorHolder.INSTANCE;
	}
	
	public static ConfigParamProcessor getInstance(
			String settingsFile,
			boolean reinit
			) {
	    ASettingsFileConfigParamProcessorHolder.INSTANCE.init( settingsFile, reinit );
		return ASettingsFileConfigParamProcessorHolder.INSTANCE;
	}
	
	private ASettingsFileConfigParamProcessor() {}

	public void init(
			String settingsFile,
			boolean reinit
			) {

		if ( m_initialized && !reinit ) {
			return;
		}

		m_paramSpecs = new Hashtable<String, ParamSpec>();
		m_paramInfos = new Hashtable<String, AParamInfo>();
		
		processSettingsFile( settingsFile );
		processSettingsFileParams();

		m_initialized = true;
	}
	
    protected void processSettingsFile(
            String settingsFile
            ) {
        
        try {
            File file = new File( settingsFile );
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
                     
                     NodeList paramDefaultValueNodeList = firstElement.getElementsByTagName("value");
                     Element paramDefaultValueElement = (Element) paramDefaultValueNodeList.item(0);
                     NodeList paramDefaultValueElementChildNodes = paramDefaultValueElement.getChildNodes();
                     Object parameterDefaultValue = ((Node) paramDefaultValueElementChildNodes.item(0)).getNodeValue();
                     
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
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    null,
                    e
                    );
        }
        
    }

	private void processSettingsFileParams() {

		Iterator<Map.Entry<String, ParamSpec>> itr = m_paramSpecs.entrySet().iterator();
		while ( itr.hasNext() ) {
			Map.Entry<String, ParamSpec> entry = itr.next();
			
			String paramName = entry.getKey();
		    ParamSpec spec = entry.getValue();
			processCmdLineParam( paramName, (String) spec.getDefaultValue() );
		}
	}

	private void processCmdLineParam(
	        String parameterName,
	        String defaultValue
	        ) {

	    List<String> paramValues = new LinkedList<String>();
	    
        ParamSpec spec = m_paramSpecs.get( parameterName );
		
        /*
         * numValuesExptected = 1 for a parameter with a single value
         * numValuesExptected != 1 for a parameter that has an array (i.e. list) of values
         */
        String[] defaultValues = ( (String) spec.getDefaultValue()).split( " " );
        for ( int i = 0; i < defaultValues.length; i++ ) {
            paramValues.add(  defaultValues[ i ] );
        }

        AParamInfo info = createParamInfo(
                spec,
                paramValues,
                false
                );
        m_paramInfos.put( parameterName.toLowerCase(), info );

	}
	
}
