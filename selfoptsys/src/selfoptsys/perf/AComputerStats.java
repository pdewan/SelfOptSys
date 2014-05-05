package selfoptsys.perf;

import java.io.*;
import java.util.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import selfoptsys.config.Constants;
import commonutils.basic.*;


public class AComputerStats
    implements ComputerStats, Serializable {
    
    private static final long serialVersionUID = 8851553818067510898L;
    
    protected ComputerInfo m_computerInfo;
    
    protected Map<PerformanceParameterType, Double> m_costs;
    protected Map<PerformanceParameterType, Double> m_costsOld;
    protected Map<PerformanceParameterType, List<Double>> m_costsForCurrentSession;
    protected Map<PerformanceParameterType, Integer> m_numCostsCounters;
    
    protected boolean m_fakeComputerInfo = false;
    
    public AComputerStats(
            ComputerInfo computerInfo
            ) {
        m_computerInfo = computerInfo;
        
        m_costs = new Hashtable<PerformanceParameterType, Double>();
        m_costsOld = new Hashtable<PerformanceParameterType, Double>();
        m_costsForCurrentSession = new Hashtable<PerformanceParameterType, List<Double>>();
        m_numCostsCounters = new Hashtable<PerformanceParameterType, Integer>();
        
        if ( computerInfo.getProcessorName().equals( Constants.FAKE_LOGGABLE_PROCESSOR_NAME ) &&
                computerInfo.getProcessorIdentifier().equals( Constants.FAKE_LOGGABLE_PROCESSOR_IDENTIFIER ) &&
                computerInfo.getProcessorSpeed() == Constants.FAKE_LOGGABLE_PROCESSOR_SPEED ) {
            m_fakeComputerInfo = true;
        }
    }
    
    public ComputerInfo getComputerInfo() {
        return m_computerInfo;
    }
    
    public double getCostEstimate(
            PerformanceParameterType costType,
            boolean estimateCostsBasedOnOtherCosts,
            boolean useOppositeCosts,
            Map<ComputerInfo, ComputerStats> otherComputerStats
            ) {
        
        if ( m_fakeComputerInfo ) {
            return 0.001;
        }
        
        Double costVal = null;
        double costValue = -1;
        
        /*
         * Return the cost value if we have one; otherwise, try to return the
         * value of an equivalent cost ...
         */
        if ( ( costVal = m_costs.get( costType ) ) != null ) {
            costValue = costVal.doubleValue();
        }
        else if ( estimateCostsBasedOnOtherCosts ) {
            if ( ( costVal = getCostEstimateUsingOtherCostEstimatesForThisComputer( costType, useOppositeCosts ) ) != null ) {
                costValue = costVal.doubleValue();
            }
            else if ( otherComputerStats != null ) {
                if ( ( costVal = getCostEstimateUsingCostEstimatesOfOtherComputers( costType, useOppositeCosts, otherComputerStats ) ) != null ) {
                    costValue = costVal.doubleValue();
                }
                else {
                    if ( ( costVal = getCostEstimateUsingOtherCostEstimatesForThisComputer( costType, true ) ) != null ) {
                        costValue = costVal.doubleValue();
                    }
                    else if ( ( costVal = getCostEstimateUsingCostEstimatesOfOtherComputers( costType, true, otherComputerStats ) ) != null ) {
                        costValue = costVal.doubleValue();
                    }
                }
            }
        }
        
        if ( costValue < 0 && 
                estimateCostsBasedOnOtherCosts && 
                otherComputerStats != null ) {
            /*
             * One final attempt if we are considering observed trans costs
             */
            if ( costType == PerformanceParameterType.CENTRALIZED_MASTER_OUTPUT_TRANS_BASED_ON_OBSERVED_TIME ) {
                costValue = getCostEstimate(
                        PerformanceParameterType.CENTRALIZED_MASTER_OUTPUT_TRANS,
                        true,
                        false,
                        otherComputerStats
                        );
            }
            else if ( costType == PerformanceParameterType.CENTRALIZED_SLAVE_INPUT_TRANS_BASED_ON_OBSERVED_TIME ) {
                costValue = getCostEstimate(
                        PerformanceParameterType.CENTRALIZED_SLAVE_INPUT_TRANS,
                        true,
                        false,
                        otherComputerStats
                        );
            }
            else if ( costType == PerformanceParameterType.CENTRALIZED_SLAVE_OUTPUT_TRANS_BASED_ON_OBSERVED_TIME ) {
                costValue = getCostEstimate(
                        PerformanceParameterType.CENTRALIZED_SLAVE_OUTPUT_TRANS,
                        true,
                        false,
                        otherComputerStats
                        );
            }
            else if ( costType == PerformanceParameterType.REPLICATED_INPUTTING_MASTER_INPUT_TRANS_BASED_ON_OBSERVED_TIME ) {
                costValue = getCostEstimate(
                        PerformanceParameterType.REPLICATED_INPUTTING_MASTER_INPUT_TRANS,
                        true,
                        false,
                        otherComputerStats
                        );
            }
            else if ( costType == PerformanceParameterType.REPLICATED_OBSERVING_MASTER_INPUT_TRANS_BASED_ON_OBSERVED_TIME ) {
                costValue = getCostEstimate(
                        PerformanceParameterType.REPLICATED_OBSERVING_MASTER_INPUT_TRANS,
                        true,
                        false,
                        otherComputerStats
                        );
            }
            
            if ( costValue < 0 ) {
                ErrorHandlingUtils.logWarningMessageAndContinue( "Could not estimate '" + costType + "' for '" + m_computerInfo.getUniqueComputerTypeId() + "' computer." );
            }
        }
        
        return costValue;
    }
    public void setCostEstimate(
            PerformanceParameterType costType,
            double value
            ) {
        m_costs.put(
                costType,
                value
                );
    }
    private Double getCostEstimateUsingOtherCostEstimatesForThisComputer(
            PerformanceParameterType costType,
            boolean useOppositeCosts
            ) {
        Double costVal = null;
        
        if ( costType == PerformanceParameterType.REPLICATED_INPUTTING_MASTER_INPUT_PROC || 
                costType == PerformanceParameterType.REPLICATED_OBSERVING_MASTER_INPUT_PROC || 
                costType == PerformanceParameterType.CENTRALIZED_MASTER_INPUT_PROC ) {
            if ( ( costVal = m_costs.get( PerformanceParameterType.REPLICATED_INPUTTING_MASTER_INPUT_PROC ) ) != null || 
                    ( costVal = m_costs.get( PerformanceParameterType.REPLICATED_OBSERVING_MASTER_INPUT_PROC ) ) != null || 
                    ( costVal = m_costs.get( PerformanceParameterType.CENTRALIZED_MASTER_INPUT_PROC ) ) != null ) {
                return costVal;
            }
            else if ( useOppositeCosts ) {
                if ( ( costVal = m_costs.get( PerformanceParameterType.REPLICATED_INPUTTING_MASTER_OUTPUT_PROC ) ) != null || 
                        ( costVal = m_costs.get( PerformanceParameterType.REPLICATED_OBSERVING_MASTER_OUTPUT_PROC ) ) != null || 
                        ( costVal = m_costs.get( PerformanceParameterType.CENTRALIZED_MASTER_OUTPUT_PROC ) ) != null ||
                        ( costVal = m_costs.get( PerformanceParameterType.CENTRALIZED_SLAVE_OUTPUT_PROC ) ) != null ) {
                    return costVal;
                }
            }
        }
        else if ( costType == PerformanceParameterType.REPLICATED_INPUTTING_MASTER_OUTPUT_PROC || 
                costType == PerformanceParameterType.REPLICATED_OBSERVING_MASTER_OUTPUT_PROC || 
                costType == PerformanceParameterType.CENTRALIZED_MASTER_OUTPUT_PROC ||
                costType == PerformanceParameterType.CENTRALIZED_SLAVE_OUTPUT_PROC ) {
            if ( ( costVal = m_costs.get( PerformanceParameterType.REPLICATED_INPUTTING_MASTER_OUTPUT_PROC ) ) != null || 
                    ( costVal = m_costs.get( PerformanceParameterType.REPLICATED_OBSERVING_MASTER_OUTPUT_PROC ) ) != null || 
                    ( costVal = m_costs.get( PerformanceParameterType.CENTRALIZED_MASTER_OUTPUT_PROC ) ) != null || 
                    ( costVal = m_costs.get( PerformanceParameterType.CENTRALIZED_SLAVE_OUTPUT_PROC ) ) != null) {
                return costVal;
            }
            else if ( useOppositeCosts ) {
                if ( ( costVal = m_costs.get( PerformanceParameterType.REPLICATED_INPUTTING_MASTER_INPUT_PROC ) ) != null || 
                        ( costVal = m_costs.get( PerformanceParameterType.REPLICATED_OBSERVING_MASTER_INPUT_PROC ) ) != null || 
                        ( costVal = m_costs.get( PerformanceParameterType.CENTRALIZED_MASTER_INPUT_PROC ) ) != null ) {
                    return costVal;
                }
            }
        }
        else if ( costType == PerformanceParameterType.REPLICATED_INPUTTING_MASTER_INPUT_TRANS || 
                costType == PerformanceParameterType.REPLICATED_OBSERVING_MASTER_INPUT_TRANS || 
                costType == PerformanceParameterType.CENTRALIZED_SLAVE_INPUT_TRANS ) {
            if ( ( costVal = m_costs.get( PerformanceParameterType.REPLICATED_INPUTTING_MASTER_INPUT_TRANS ) ) != null || 
                    ( costVal = m_costs.get( PerformanceParameterType.REPLICATED_OBSERVING_MASTER_INPUT_TRANS ) ) != null || 
                    ( costVal = m_costs.get( PerformanceParameterType.CENTRALIZED_SLAVE_INPUT_TRANS ) ) != null) {
                return costVal;
            }
            else if ( useOppositeCosts ) {
                if ( ( costVal = m_costs.get( PerformanceParameterType.CENTRALIZED_MASTER_OUTPUT_TRANS ) ) != null || 
                        ( costVal = m_costs.get( PerformanceParameterType.CENTRALIZED_SLAVE_OUTPUT_TRANS ) ) != null) {
                    return costVal;
                }
            }
        }
        else if ( costType == PerformanceParameterType.CENTRALIZED_MASTER_OUTPUT_TRANS ||
                costType == PerformanceParameterType.CENTRALIZED_SLAVE_OUTPUT_TRANS ) {
            if ( ( costVal = m_costs.get( PerformanceParameterType.CENTRALIZED_MASTER_OUTPUT_TRANS ) ) != null || 
                    ( costVal = m_costs.get( PerformanceParameterType.CENTRALIZED_SLAVE_OUTPUT_TRANS ) ) != null) {
                return costVal;
            }
            else if ( useOppositeCosts ) {
                if ( ( costVal = m_costs.get( PerformanceParameterType.REPLICATED_INPUTTING_MASTER_INPUT_TRANS ) ) != null || 
                        ( costVal = m_costs.get( PerformanceParameterType.REPLICATED_OBSERVING_MASTER_INPUT_TRANS ) ) != null || 
                        ( costVal = m_costs.get( PerformanceParameterType.CENTRALIZED_SLAVE_INPUT_TRANS ) ) != null) {
                    return costVal;
                }
            }
        }
        else if ( costType == PerformanceParameterType.REPLICATED_INPUTTING_MASTER_INPUT_TRANS_BASED_ON_OBSERVED_TIME || 
                costType == PerformanceParameterType.REPLICATED_OBSERVING_MASTER_INPUT_TRANS_BASED_ON_OBSERVED_TIME || 
                costType == PerformanceParameterType.CENTRALIZED_SLAVE_INPUT_TRANS_BASED_ON_OBSERVED_TIME ) {
            if ( ( costVal = m_costs.get( PerformanceParameterType.REPLICATED_INPUTTING_MASTER_INPUT_TRANS_BASED_ON_OBSERVED_TIME ) ) != null || 
                    ( costVal = m_costs.get( PerformanceParameterType.REPLICATED_OBSERVING_MASTER_INPUT_TRANS_BASED_ON_OBSERVED_TIME ) ) != null || 
                    ( costVal = m_costs.get( PerformanceParameterType.CENTRALIZED_SLAVE_INPUT_TRANS_BASED_ON_OBSERVED_TIME ) ) != null) {
                return costVal;
            }
            else if ( useOppositeCosts ) {
                if ( ( costVal = m_costs.get( PerformanceParameterType.CENTRALIZED_MASTER_OUTPUT_TRANS_BASED_ON_OBSERVED_TIME ) ) != null || 
                        ( costVal = m_costs.get( PerformanceParameterType.CENTRALIZED_SLAVE_OUTPUT_TRANS_BASED_ON_OBSERVED_TIME ) ) != null) {
                    return costVal;
                }
            }
        }
        else if ( costType == PerformanceParameterType.CENTRALIZED_MASTER_OUTPUT_TRANS_BASED_ON_OBSERVED_TIME ||
                costType == PerformanceParameterType.CENTRALIZED_SLAVE_OUTPUT_TRANS_BASED_ON_OBSERVED_TIME ) {
            if ( ( costVal = m_costs.get( PerformanceParameterType.CENTRALIZED_MASTER_OUTPUT_TRANS_BASED_ON_OBSERVED_TIME ) ) != null || 
                    ( costVal = m_costs.get( PerformanceParameterType.CENTRALIZED_SLAVE_OUTPUT_TRANS_BASED_ON_OBSERVED_TIME ) ) != null) {
                return costVal;
            }
            else if ( useOppositeCosts ) {
                if ( ( costVal = m_costs.get( PerformanceParameterType.REPLICATED_INPUTTING_MASTER_INPUT_TRANS_BASED_ON_OBSERVED_TIME ) ) != null || 
                        ( costVal = m_costs.get( PerformanceParameterType.REPLICATED_OBSERVING_MASTER_INPUT_TRANS_BASED_ON_OBSERVED_TIME ) ) != null || 
                        ( costVal = m_costs.get( PerformanceParameterType.CENTRALIZED_SLAVE_INPUT_TRANS_BASED_ON_OBSERVED_TIME ) ) != null) {
                    return costVal;
                }
            }
        }
        
        return costVal;
    }
    
    private Double getCostEstimateUsingCostEstimatesOfOtherComputers(
            PerformanceParameterType costType,
            boolean useOppositeCosts,
            Map<ComputerInfo, ComputerStats> computerStats
            ) {
        Double costVal = null;

        /*
         * These estimates are not comprehensive.
         */
        
        /*
         * Assumption: some reports have been received for each computer. Therefore,
         * each computer will at least have some estimate for output processing costs.
         */
        
        ComputerStats computerWithCost = null;
        double ratio = -1;
        
        Iterator<Map.Entry<ComputerInfo, ComputerStats>> itr = computerStats.entrySet().iterator();
        while ( itr.hasNext() ) {
            Map.Entry<ComputerInfo, ComputerStats> entry = itr.next();
            if ( entry.getKey().getProcessorName().equals( Constants.FAKE_LOGGABLE_PROCESSOR_NAME ) ) {
                continue;
            }
            
            Double costValueOfOtherComp = entry.getValue().getCostEstimate(
                    costType,
                    false,
                    useOppositeCosts,
                    null
                    );
            if ( costValueOfOtherComp != null && costValueOfOtherComp != -1.0 ) {
                computerWithCost = entry.getValue();
                ratio = computerWithCost.getCostEstimate(
                        PerformanceParameterType.REPLICATED_INPUTTING_MASTER_OUTPUT_PROC, true, useOppositeCosts, null ) /
                        costValueOfOtherComp.doubleValue();
                break;
            }
        }
        
        if ( computerWithCost == null ){
            itr = computerStats.entrySet().iterator();
            while ( itr.hasNext() ) {
                Map.Entry<ComputerInfo, ComputerStats> entry = itr.next();
                if ( entry.getKey().getProcessorName().equals( Constants.FAKE_LOGGABLE_PROCESSOR_NAME ) ) {
                    continue;
                }
                
                Double costValueOfOtherComp = entry.getValue().getCostEstimate(
                        costType,
                        true,
                        useOppositeCosts,
                        null
                        );
                if ( costValueOfOtherComp != null && costValueOfOtherComp != -1.0 ) {
                    computerWithCost = entry.getValue();
                    ratio = computerWithCost.getCostEstimate(
                            PerformanceParameterType.REPLICATED_INPUTTING_MASTER_OUTPUT_PROC, true, useOppositeCosts, null ) /
                            costValueOfOtherComp.doubleValue();
                    break;
                }
            }
        }
        if ( computerWithCost != null ) {
            costVal = getCostEstimate( PerformanceParameterType.REPLICATED_INPUTTING_MASTER_OUTPUT_PROC, true, useOppositeCosts, null ) / ratio;
        }
        
        return costVal;
    }

    
    public double getOldCostEstimate(
            PerformanceParameterType costType
            ) {
        Double cost = m_costsOld.get( costType );
        if ( cost == null ) {
            return -1;
        }
        
        return cost;
    }
    
    public List<Double> getCostsForCurrentSession(
            PerformanceParameterType costType
            ) {
        List<Double> costs = m_costsForCurrentSession.get( costType );
        return costs;
    }
    public void appendCostToCurrentSession(
            PerformanceParameterType costType,
            double value
            ) {
        List<Double> costs = m_costsForCurrentSession.get( costType );
        if ( costs == null ) {
            costs = new LinkedList<Double>();
            m_costsForCurrentSession.put(
                    costType,
                    costs
                    );
        }
        costs.add( value );
        
        int costCount = 0;
        Integer curCount = m_numCostsCounters.get( costType );
        if ( curCount != null ) {
            costCount = curCount;
        }
        costCount++;
        m_numCostsCounters.put(
                costType,
                costCount
                );
    }
    
    public Map<PerformanceParameterType, Double> getAllEstimates() {
        return m_costs;
    }
    
    public Map<PerformanceParameterType, List<Double>> getAllReportsForCurrentSession() {
        return m_costsForCurrentSession;
    }
    
    public void calculateEstimatesBasedOnValuesFromCurrentSession(
            int maxNumReportsToUse
            ) {
        
        Iterator<Map.Entry<PerformanceParameterType, List<Double>>> itr = m_costsForCurrentSession.entrySet().iterator();
        while ( itr.hasNext() ) {
            Map.Entry<PerformanceParameterType, List<Double>> entry = itr.next();
            PerformanceParameterType costType = entry.getKey();
            
            double curCostValue = -1;
            if ( m_costs.get( costType ) != null ) {
                curCostValue = m_costs.get( costType );
            }
            m_costsOld.put( 
                    costType,
                    curCostValue
                    );
            
            double newCost = 0;
            List<Double> curSessionCosts = entry.getValue();
            int indexOfOldestReportToUse = 0;
            if ( maxNumReportsToUse > 0 ) {
                indexOfOldestReportToUse = curSessionCosts.size() - maxNumReportsToUse;
                indexOfOldestReportToUse = Math.max( 0, indexOfOldestReportToUse );
            }
            for ( int i = curSessionCosts.size() - 1; i >= indexOfOldestReportToUse; i-- ) {
                newCost += curSessionCosts.get( i );
            }
            if ( curSessionCosts.size() > 0 ) {
                newCost = MathUtils.round( newCost / curSessionCosts.size(), 4 );
            }
            
            if ( newCost == 0 ) {
                newCost = curCostValue;
            }
            else if ( curCostValue != -1 ) {
                newCost = curCostValue * 0.3 + newCost * 0.7;
                newCost = MathUtils.round( newCost, 3 );
            }
            
            m_costs.put(
                    costType,
                    newCost
                    );
        }
        
    }
    
    public void discardLatestEstimates() {
        
        Iterator<Map.Entry<PerformanceParameterType, Double>> itr = m_costsOld.entrySet().iterator();
        while ( itr.hasNext() ) {
            Map.Entry<PerformanceParameterType, Double> entry = itr.next();
            m_costs.put(
                    entry.getKey(),
                    entry.getValue()
                    );
        }
        
    }
    
    public static void saveObjects(
            String statsFile,
            Map<ComputerInfo, ComputerStats> computerStatsCollection
            ) {
        
        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.newDocument();

            Element rootElement = document.createElement( "estimates" );
            document.appendChild( rootElement );
            
            Iterator<Map.Entry<ComputerInfo, ComputerStats>> itr = computerStatsCollection.entrySet().iterator();
            while ( itr.hasNext() ) {
                Map.Entry<ComputerInfo, ComputerStats> entry = itr.next();
                
                if ( entry.getKey().getProcessorName().equals( Constants.FAKE_LOGGABLE_PROCESSOR_NAME ) ) {
                    continue;
                }
                
                entry.getValue().calculateEstimatesBasedOnValuesFromCurrentSession( -1 );
                
                Element statsObjectNode = document.createElement( "StatsObject" );
                Element emParameterNode;

                emParameterNode = createParameterNode(
                        document, "ProcessorName", entry.getKey().getProcessorName()
                        );
                statsObjectNode.appendChild( emParameterNode );
                emParameterNode = createParameterNode(
                        document, "ProcessorIdentifier", entry.getKey().getProcessorIdentifier()
                        );
                statsObjectNode.appendChild( emParameterNode );
                emParameterNode = createParameterNode(
                        document, "ProcessorSpeed", Long.toString( entry.getKey().getProcessorSpeed() )
                        );
                statsObjectNode.appendChild( emParameterNode );

                for ( int i = 0; i < PerformanceParameterType.values().length; i++ ) {
                    PerformanceParameterType costType = PerformanceParameterType.values()[ i ];
                    double costValue = entry.getValue().getCostEstimate(
                            costType,
                            true,
                            false,
                            computerStatsCollection
                            );
                    emParameterNode = createParameterNode(
                            document, costType.toString(), Double.toString( costValue )
                            );
                    statsObjectNode.appendChild( emParameterNode );
                }
               
                rootElement.appendChild( statsObjectNode );
            }
            
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty( OutputKeys.INDENT, "yes" ); 
            transformer.setOutputProperty( "{http://xml.apache.org/xalan}indent-amount", "4" );
            DOMSource source = new DOMSource( document );
            StreamResult result =  new StreamResult( statsFile );
            transformer.transform( source, result );
        }
        catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "AComputerStats: Could not save cost estimates file",
                    e
                    );
        }
    }
    
    private static Element createParameterNode(
            Document document,
            String name,
            String value
            ) {
        Element emParameterNode = document.createElement( "parameter" );
        Element emParameterNodeChild = document.createElement( "name" );
        emParameterNodeChild.setTextContent( name );
        emParameterNode.appendChild( emParameterNodeChild );
        emParameterNodeChild = document.createElement( "value" );
        emParameterNodeChild.setTextContent( value );
        emParameterNode.appendChild( emParameterNodeChild );
        emParameterNode.appendChild( emParameterNodeChild );
        return emParameterNode;

    }
    
    public static Map<ComputerInfo, ComputerStats> loadObjects(
            String statsFile
            ) {

        Map<ComputerInfo, ComputerStats> computerStatsCollection = new Hashtable<ComputerInfo, ComputerStats>();
        
        try {
            File file = new File( statsFile );
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            
            Document doc = db.parse(file);
            doc.getDocumentElement().normalize();
            NodeList statsObjectsNodeList = doc.getElementsByTagName("StatsObject");
            
            for (int s = 0; s < statsObjectsNodeList.getLength(); s++) {
                Map<String, Object> values = new Hashtable<String, Object>();
                
                Node statsObjectsFirstNode = statsObjectsNodeList.item( s );
                if ( statsObjectsFirstNode.getNodeType() == Node.ELEMENT_NODE ) {
                    
                     Element statObjectsFirstNodeListElement = (Element) statsObjectsFirstNode;
                     NodeList statsObjectNodeList = statObjectsFirstNodeListElement.getElementsByTagName( "parameter" );
                     for ( int i = 0; i < statsObjectNodeList.getLength(); i++ ) {
                         Node statsObjectFirstNode = statsObjectNodeList.item( i );
                         if ( statsObjectFirstNode.getNodeType() == Node.ELEMENT_NODE ) {
                         
                             Element firstElement = (Element) statsObjectFirstNode;
                             NodeList paramNameNodeList = firstElement.getElementsByTagName("name");
                             Element paramNameElement = (Element) paramNameNodeList.item(0);
                             NodeList paramNameElementChildNodes = paramNameElement.getChildNodes();
                             String parameterName = ((Node) paramNameElementChildNodes.item(0)).getNodeValue();
                             
                             NodeList paramValueNodeList = firstElement.getElementsByTagName("value");
                             Element paramValueElement = (Element) paramValueNodeList.item(0);
                             NodeList paramValueElementChildNodes = paramValueElement.getChildNodes();
                             Object parametertValue = ((Node) paramValueElementChildNodes.item(0)).getNodeValue();
                             
                             values.put(
                                     parameterName,
                                     parametertValue
                                     );
                         }       
                     }
                                                  
                }
                
                ComputerInfo computerInfo = new AComputerInfo(
                        (String) values.get( "ProcessorName" ),
                        (String) values.get( "ProcessorIdentifier" ),
                        Long.parseLong( (String) values.get( "ProcessorSpeed" ) )
                        );
                ComputerStats computerStats = new AComputerStats( computerInfo );
                for ( int i = 0; i < PerformanceParameterType.values().length; i++ ) {
                    Object value = values.get( PerformanceParameterType.values()[ i ].toString() );
                    double costEstimate = -1;
                    if ( value != null ) {
                        costEstimate = Double.parseDouble( (String ) value );
                    }
                    computerStats.setCostEstimate(
                            PerformanceParameterType.values()[ i ],
                            costEstimate
                            );
                }
                
                computerStatsCollection.put( computerInfo, computerStats );
            }
            
        }
        catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "AComputerStats: Could not load cost estimates file",
                    e
                    );
        }
        
        return computerStatsCollection;
    }
    
    public Map<PerformanceParameterType, Integer> getNumCostsCounters() {
        return m_numCostsCounters;
    }
    
    public void resetNumCostsCounters() {
        m_numCostsCounters.clear();
    }
    
}