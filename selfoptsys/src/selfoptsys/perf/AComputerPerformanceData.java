package selfoptsys.perf;

import java.io.File;
import java.io.Serializable;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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

public class AComputerPerformanceData 
    implements ComputerPerformanceData, Serializable {

    private static final long serialVersionUID = 1158769989393913044L;
    
    protected ComputerInfo m_computerInfo;
    
    protected Map<PerformanceParameterType, Double> m_latestParameterEstimatesBasedOnRealData;
    protected Map<PerformanceParameterType, Double> m_previousParameterEstimatesBasedOnRealData;
    protected Map<PerformanceParameterType, Double> m_initialParameterEstimatesBasedOnRealData;
    
    protected Map<PerformanceParameterType, Double> m_latestParameterEstimatesBasedOnOtherParameters;

    protected Map<PerformanceParameterType, List<Double>> m_parameterValues;
    protected Map<PerformanceParameterType, Integer> m_parameterValuesAddedSinceLastEstimate;
    
    protected PerformanceParameterEstimatingFunction m_estimatingFunction;
    protected double m_previousDataWeight;
    protected double m_newDataWeight;
    
    protected static Map<PerformanceParameterType, List<PerformanceParameterType>> m_mostRelatedParameters = 
        getMostRelatedParameters();
    protected static Map<PerformanceParameterType, List<PerformanceParameterType>> m_secondMostRelatedParameters
        = getSecondMostRelatedParameters();
    protected static Map<PerformanceParameterType, List<PerformanceParameterType>> m_thirdMostRelatedParameters =
        getThirdMostRelatedParameters();
    protected static Map<PerformanceParameterType, List<PerformanceParameterType>> m_pairWiseRelatedParameters = 
        getPairWiseRelatedParameters();
    
    public AComputerPerformanceData(
            ComputerInfo computerInfo
            ) {
        m_computerInfo = computerInfo;
        
        m_latestParameterEstimatesBasedOnRealData = new Hashtable<PerformanceParameterType, Double>();
        m_previousParameterEstimatesBasedOnRealData = new Hashtable<PerformanceParameterType, Double>();
        m_initialParameterEstimatesBasedOnRealData = new Hashtable<PerformanceParameterType, Double>();
        
        m_latestParameterEstimatesBasedOnOtherParameters = new Hashtable<PerformanceParameterType, Double>();
        
        m_parameterValues = new Hashtable<PerformanceParameterType, List<Double>>();
        for ( PerformanceParameterType parameterType: PerformanceParameterType.values() ) {
            m_parameterValues.put(
                    parameterType,
                    new LinkedList<Double>()
                    );
        }

        m_parameterValuesAddedSinceLastEstimate = new Hashtable<PerformanceParameterType, Integer>();
        for ( PerformanceParameterType parameterType: PerformanceParameterType.values() ) {
            m_parameterValuesAddedSinceLastEstimate.put(
                    parameterType,
                    0
                    );
        }
        
        /*
         * TODO: Need to let users define a custom function. Add a configuration file parameter
         * that allows them to specify the class implementing the function.
         */
        m_estimatingFunction = new APerformanceParameterEstimatingFunction();
        
        /*
         * TODO: Need to let users specify these values in configuration file.
         */
        m_previousDataWeight = 0.3;
        m_newDataWeight = 0.7;
        
    }

    public ComputerInfo getComputerInfo() {
        return m_computerInfo;
    }
    
    public double getParameterEstimate(
            PerformanceParameterType parameterType
            ) {
        double value = getParameterEstimateBasedOnRealData( parameterType );
        if ( value == -1 ) {
            value = getParameterEstimateBasedOnOtherParameters( parameterType );
        }
        return value;
    }

    public double getParameterEstimateBasedOnRealData(
            PerformanceParameterType parameterType
            ) {
        if ( m_latestParameterEstimatesBasedOnRealData.get( parameterType ) != null ) {
            return m_latestParameterEstimatesBasedOnRealData.get( parameterType );
        }
        return -1;
    }
    
    public double getParameterEstimateBasedOnOtherParameters(
            PerformanceParameterType parameterType
            ) {
        if ( m_latestParameterEstimatesBasedOnOtherParameters.get( parameterType ) != null ) {
            return m_latestParameterEstimatesBasedOnOtherParameters.get( parameterType );
        }
        return -1;
    }

    
    public void addPerformanceParameterValue(
            PerformanceParameterType parameterType,
            double value
            ) {
        m_parameterValues.get( parameterType ).add( value );
        m_parameterValuesAddedSinceLastEstimate.put(
                parameterType,
                m_parameterValuesAddedSinceLastEstimate.get( parameterType ) + 1
                );
    }
    
    public void setInitialPerformanceParameterEstimate(
            PerformanceParameterType parameterType,
            double estimate
            ) {
        m_initialParameterEstimatesBasedOnRealData.put(
                parameterType,
                estimate
                );
    }
    
    public void estimateValuesOfAllPerformanceParameters(
            List<ComputerPerformanceData> allKnownComputerPerformanceData
            ) {
        
        /*
         * Get estimates based on initial data or received reports
         */
        for ( PerformanceParameterType parameterType: PerformanceParameterType.values() ) {
            estimatePerformanceParameterBasedOnRealData( parameterType );
        }
        
        /*
         * Get estimates based on values of other computers if necessary and if possible
         */
        if ( allKnownComputerPerformanceData != null ) {
            for ( PerformanceParameterType parameterType: PerformanceParameterType.values() ) {
                if ( m_latestParameterEstimatesBasedOnRealData.get( parameterType ) == null ) {
                    estimatePerformanceParameterBasedOnOtherParameters(
                            parameterType,
                            allKnownComputerPerformanceData
                            );
                    
                    if ( m_latestParameterEstimatesBasedOnOtherParameters.get( parameterType ) == null ) {
                        ErrorHandlingUtils.logSevereMessageAndContinue(
                                "ERROR: Should not have reached this point! Could not estimate value of performance parameter!"
                                );
                        assert( true );
                    }
                }
            }
        }
    }
    
    private void estimatePerformanceParameterBasedOnRealData(
            PerformanceParameterType parameterType
            ) {
        double previousEstimate = -1;
        if ( m_previousParameterEstimatesBasedOnRealData.get( parameterType ) != null ) {
            previousEstimate = m_previousParameterEstimatesBasedOnRealData.get( parameterType );
        }
        
        double initialEstimate = -1;
        if ( m_initialParameterEstimatesBasedOnRealData.get( parameterType ) != null ) {
            initialEstimate = m_initialParameterEstimatesBasedOnRealData.get( parameterType );
        }
        
        double estimate = m_estimatingFunction.getPerformanceParameterEstimate(
                m_parameterValues.get( parameterType ),
                previousEstimate,
                m_parameterValuesAddedSinceLastEstimate.get( parameterType ),
                initialEstimate,
                m_previousDataWeight,
                m_newDataWeight
                );
        
        if ( estimate != -1 ) {
            if ( m_latestParameterEstimatesBasedOnRealData.get( parameterType ) != null ) {
                m_previousParameterEstimatesBasedOnRealData.put(
                        parameterType,
                        m_latestParameterEstimatesBasedOnRealData.get( parameterType )
                        );
            }
            m_latestParameterEstimatesBasedOnRealData.put(
                    parameterType,
                    estimate
                    );
            m_parameterValuesAddedSinceLastEstimate.put(
                    parameterType,
                    0
                    );
        }
        
    }
    
    private void estimatePerformanceParameterBasedOnOtherParameters(
            PerformanceParameterType parameterType,
            List<ComputerPerformanceData> allKnownComputerPerformanceData
            ) {
        m_latestParameterEstimatesBasedOnOtherParameters.remove( parameterType );
        
        estimatePerformanceParameterBasedOnParametersOfOtherComputers(
                parameterType,
                allKnownComputerPerformanceData,
                m_mostRelatedParameters.get( parameterType )
                );
        if ( m_latestParameterEstimatesBasedOnOtherParameters.get( parameterType ) != null ) {
            return;
        }
        
        estimatePerformanceParameterBasedOnParametersOfOtherComputers(
                parameterType,
                allKnownComputerPerformanceData,
                m_secondMostRelatedParameters.get( parameterType )
                );
        if ( m_latestParameterEstimatesBasedOnOtherParameters.get( parameterType ) != null ) {
            return;
        }
        
        estimatePerformanceParameterBasedOnParametersOfOtherComputers(
                parameterType,
                allKnownComputerPerformanceData,
                m_thirdMostRelatedParameters.get( parameterType )
                );
        if ( m_latestParameterEstimatesBasedOnOtherParameters.get( parameterType ) != null ) {
            return;
        }
        
        estimatePerformanceParameterBasedOnOwnParameters(
                parameterType,
                m_pairWiseRelatedParameters.get( parameterType )
                );
        
        if ( m_latestParameterEstimatesBasedOnOtherParameters.get( parameterType ) == null ) {
            ErrorHandlingUtils.logSevereMessageAndContinue(
                    "AComputerPerformanceData::estimatePerformanceBasedOnOtherParameters: " +
                    "Could not estimate value of parameter '" + parameterType.toString() + "'"
                    );
        }
    }
    
    private void estimatePerformanceParameterBasedOnParametersOfOtherComputers(
            PerformanceParameterType parameterType,
            List<ComputerPerformanceData> allKnownComputerPerformanceData,
            List<PerformanceParameterType> relatedParameters
            ) {
        for ( int i = 0; i < relatedParameters.size(); i++ ) {
            
            if ( getParameterEstimateBasedOnRealData( relatedParameters.get( i ) ) == -1 ) {
                /*
                 * We need a related parameter for which we have an estimate based on real data
                 */
                continue;
            }
            
            for ( ComputerPerformanceData otherComputerPerformanceData : allKnownComputerPerformanceData ) {
                if ( otherComputerPerformanceData.getComputerInfo().equals( m_computerInfo ) ) {
                    /*
                     * Use parameters from other computer types to estimate parameter values for this computer type
                     */
                    continue;
                }
                
                if ( otherComputerPerformanceData.getParameterEstimateBasedOnRealData( parameterType ) != -1 ) {
                    if ( otherComputerPerformanceData.getParameterEstimateBasedOnRealData( relatedParameters.get( i ) ) != -1 ) {
                        double ratio = 
                            getParameterEstimateBasedOnRealData( relatedParameters.get( i ) ) / 
                            otherComputerPerformanceData.getParameterEstimateBasedOnRealData( relatedParameters.get( i ) );
                        double estimate = otherComputerPerformanceData.getParameterEstimateBasedOnRealData( parameterType ) * ratio;
                        estimate = MathUtils.round( estimate, 3 );
                        m_latestParameterEstimatesBasedOnOtherParameters.put(
                                parameterType,
                                estimate
                                );
                        return;
                    }
                }
            }
        }
    }
    
    private void estimatePerformanceParameterBasedOnOwnParameters(
            PerformanceParameterType parameterType,
            List<PerformanceParameterType> relatedParameters
            ) {
        for ( int i = 0; i < relatedParameters.size(); i++ ) {
            
            if ( getParameterEstimateBasedOnRealData( relatedParameters.get( i ) ) == -1 ) {
                /*
                 * We need a related parameter for which we have an estimate based on real data
                 */
                continue;
            }
            
            m_latestParameterEstimatesBasedOnOtherParameters.put(
                    parameterType,
                    getParameterEstimateBasedOnRealData( relatedParameters.get( i ) )
                    );
            return;
        }
    }

    private static Map<PerformanceParameterType, List<PerformanceParameterType>> getMostRelatedParameters() {
        
        Map<PerformanceParameterType, List<PerformanceParameterType>> mostRelatedParametersCollection =
            new Hashtable<PerformanceParameterType, List<PerformanceParameterType>>();
        
        for ( PerformanceParameterType parameterType : PerformanceParameterType.values() ) {
            List<PerformanceParameterType> mostRelatedParameters = new LinkedList<PerformanceParameterType>();
            
            if ( parameterType == PerformanceParameterType.REPLICATED_INPUTTING_MASTER_INPUT_PROC ||
                    parameterType == PerformanceParameterType.REPLICATED_OBSERVING_MASTER_INPUT_PROC ||
                    parameterType == PerformanceParameterType.REPLICATED_INPUTTING_MASTER_OUTPUT_PROC ||
                    parameterType == PerformanceParameterType.REPLICATED_OBSERVING_MASTER_OUTPUT_PROC
                    ) {
                mostRelatedParameters.add( PerformanceParameterType.REPLICATED_INPUTTING_MASTER_INPUT_PROC );
                mostRelatedParameters.add( PerformanceParameterType.REPLICATED_OBSERVING_MASTER_INPUT_PROC );
                mostRelatedParameters.add( PerformanceParameterType.REPLICATED_INPUTTING_MASTER_OUTPUT_PROC );
                mostRelatedParameters.add( PerformanceParameterType.REPLICATED_OBSERVING_MASTER_OUTPUT_PROC );
            }
            
            if ( parameterType == PerformanceParameterType.CENTRALIZED_MASTER_INPUT_PROC ||
                    parameterType == PerformanceParameterType.CENTRALIZED_MASTER_OUTPUT_PROC ||
                    parameterType == PerformanceParameterType.CENTRALIZED_SLAVE_OUTPUT_PROC
                    ) {
                mostRelatedParameters.add( PerformanceParameterType.CENTRALIZED_MASTER_INPUT_PROC );
                mostRelatedParameters.add( PerformanceParameterType.CENTRALIZED_MASTER_OUTPUT_PROC );
                mostRelatedParameters.add( PerformanceParameterType.CENTRALIZED_SLAVE_OUTPUT_PROC );
            }
            
            if ( parameterType == PerformanceParameterType.REPLICATED_INPUTTING_MASTER_INPUT_TRANS ||
                    parameterType == PerformanceParameterType.REPLICATED_OBSERVING_MASTER_INPUT_TRANS ||
                    parameterType == PerformanceParameterType.CENTRALIZED_SLAVE_INPUT_TRANS
                    ) {
                mostRelatedParameters.add( PerformanceParameterType.REPLICATED_INPUTTING_MASTER_INPUT_TRANS );
                mostRelatedParameters.add( PerformanceParameterType.REPLICATED_OBSERVING_MASTER_INPUT_TRANS );
                mostRelatedParameters.add( PerformanceParameterType.CENTRALIZED_SLAVE_INPUT_TRANS );
            }
            
            if ( parameterType == PerformanceParameterType.REPLICATED_INPUTTING_MASTER_INPUT_TRANS_BASED_ON_OBSERVED_TIME || 
                    parameterType == PerformanceParameterType.REPLICATED_OBSERVING_MASTER_INPUT_TRANS_BASED_ON_OBSERVED_TIME ||
                    parameterType == PerformanceParameterType.CENTRALIZED_SLAVE_INPUT_TRANS_BASED_ON_OBSERVED_TIME
                    ) {
                mostRelatedParameters.add( PerformanceParameterType.REPLICATED_INPUTTING_MASTER_INPUT_TRANS_BASED_ON_OBSERVED_TIME );
                mostRelatedParameters.add( PerformanceParameterType.REPLICATED_OBSERVING_MASTER_INPUT_TRANS_BASED_ON_OBSERVED_TIME );
                mostRelatedParameters.add( PerformanceParameterType.CENTRALIZED_SLAVE_INPUT_TRANS_BASED_ON_OBSERVED_TIME );
            }
            
            if ( parameterType == PerformanceParameterType.REPLICATED_INPUTTING_MASTER_INPUT_TRANS_TO_FIRST_DEST || 
                    parameterType == PerformanceParameterType.REPLICATED_OBSERVING_MASTER_INPUT_TRANS_TO_FIRST_DEST ||
                    parameterType == PerformanceParameterType.CENTRALIZED_SLAVE_INPUT_TRANS_TO_FIRST_DEST
                    ) {
                mostRelatedParameters.add( PerformanceParameterType.REPLICATED_INPUTTING_MASTER_INPUT_TRANS_TO_FIRST_DEST );
                mostRelatedParameters.add( PerformanceParameterType.REPLICATED_OBSERVING_MASTER_INPUT_TRANS_TO_FIRST_DEST );
                mostRelatedParameters.add( PerformanceParameterType.CENTRALIZED_SLAVE_INPUT_TRANS_TO_FIRST_DEST );
            }
            
            if ( parameterType == PerformanceParameterType.REPLICATED_INPUTTING_MASTER_INPUT_TRANS_TO_FIRST_DEST_BASED_ON_OBSERVED_TIME || 
                    parameterType == PerformanceParameterType.REPLICATED_OBSERVING_MASTER_INPUT_TRANS_TO_FIRST_DEST_BASED_ON_OBSERVED_TIME ||
                    parameterType == PerformanceParameterType.CENTRALIZED_SLAVE_INPUT_TRANS_TO_FIRST_DEST_BASED_ON_OBSERVED_TIME
                    ) {
                mostRelatedParameters.add( PerformanceParameterType.REPLICATED_INPUTTING_MASTER_INPUT_TRANS_TO_FIRST_DEST_BASED_ON_OBSERVED_TIME );
                mostRelatedParameters.add( PerformanceParameterType.REPLICATED_OBSERVING_MASTER_INPUT_TRANS_TO_FIRST_DEST_BASED_ON_OBSERVED_TIME );
                mostRelatedParameters.add( PerformanceParameterType.CENTRALIZED_SLAVE_INPUT_TRANS_TO_FIRST_DEST_BASED_ON_OBSERVED_TIME );
            }
            
            if ( parameterType == PerformanceParameterType.CENTRALIZED_MASTER_OUTPUT_TRANS ||
                    parameterType == PerformanceParameterType.CENTRALIZED_SLAVE_OUTPUT_TRANS
                    ) {
                mostRelatedParameters.add( PerformanceParameterType.CENTRALIZED_MASTER_OUTPUT_TRANS );
                mostRelatedParameters.add( PerformanceParameterType.CENTRALIZED_SLAVE_OUTPUT_TRANS );
            }
            
            if ( parameterType == PerformanceParameterType.CENTRALIZED_MASTER_OUTPUT_TRANS_BASED_ON_OBSERVED_TIME ||
                    parameterType == PerformanceParameterType.CENTRALIZED_SLAVE_OUTPUT_TRANS_BASED_ON_OBSERVED_TIME
                    ) {
                mostRelatedParameters.add( PerformanceParameterType.CENTRALIZED_MASTER_OUTPUT_TRANS_BASED_ON_OBSERVED_TIME );
                mostRelatedParameters.add( PerformanceParameterType.CENTRALIZED_SLAVE_OUTPUT_TRANS_BASED_ON_OBSERVED_TIME );
            }
            
            if ( parameterType == PerformanceParameterType.CENTRALIZED_MASTER_OUTPUT_TRANS_TO_FIRST_DEST ||
                    parameterType == PerformanceParameterType.CENTRALIZED_SLAVE_OUTPUT_TRANS_TO_FIRST_DEST
                    ) {
                mostRelatedParameters.add( PerformanceParameterType.CENTRALIZED_MASTER_OUTPUT_TRANS_TO_FIRST_DEST );
                mostRelatedParameters.add( PerformanceParameterType.CENTRALIZED_SLAVE_OUTPUT_TRANS_TO_FIRST_DEST );
            }

            if ( parameterType == PerformanceParameterType.CENTRALIZED_MASTER_OUTPUT_TRANS_TO_FIRST_DEST_BASED_ON_OBSERVED_TIME ||
                    parameterType == PerformanceParameterType.CENTRALIZED_SLAVE_OUTPUT_TRANS_TO_FIRST_DEST_BASED_ON_OBSERVED_TIME
                    ) {
                mostRelatedParameters.add( PerformanceParameterType.CENTRALIZED_MASTER_OUTPUT_TRANS_TO_FIRST_DEST_BASED_ON_OBSERVED_TIME );
                mostRelatedParameters.add( PerformanceParameterType.CENTRALIZED_SLAVE_OUTPUT_TRANS_TO_FIRST_DEST_BASED_ON_OBSERVED_TIME );
            }

            mostRelatedParametersCollection.put(
                    parameterType,
                    mostRelatedParameters
                    );
        }
        
        return mostRelatedParametersCollection;
    }
    
    private static Map<PerformanceParameterType, List<PerformanceParameterType>> getSecondMostRelatedParameters() {
        
        Map<PerformanceParameterType, List<PerformanceParameterType>> secondMostRelatedParametersCollection =
            new Hashtable<PerformanceParameterType, List<PerformanceParameterType>>();
        
        for ( PerformanceParameterType parameterType : PerformanceParameterType.values() ) {
            List<PerformanceParameterType> secondMostRelatedParameters = new LinkedList<PerformanceParameterType>();

            if ( parameterType == PerformanceParameterType.REPLICATED_INPUTTING_MASTER_INPUT_PROC ||
                    parameterType == PerformanceParameterType.REPLICATED_OBSERVING_MASTER_INPUT_PROC ||
                    parameterType == PerformanceParameterType.REPLICATED_INPUTTING_MASTER_OUTPUT_PROC ||
                    parameterType == PerformanceParameterType.REPLICATED_OBSERVING_MASTER_OUTPUT_PROC
                    ) {
                secondMostRelatedParameters.add( PerformanceParameterType.CENTRALIZED_MASTER_INPUT_PROC );
                secondMostRelatedParameters.add( PerformanceParameterType.CENTRALIZED_MASTER_OUTPUT_PROC );
                secondMostRelatedParameters.add( PerformanceParameterType.CENTRALIZED_SLAVE_OUTPUT_PROC );
            }
            
            if ( parameterType == PerformanceParameterType.CENTRALIZED_MASTER_INPUT_PROC ||
                    parameterType == PerformanceParameterType.CENTRALIZED_MASTER_OUTPUT_PROC ||
                    parameterType == PerformanceParameterType.CENTRALIZED_SLAVE_OUTPUT_PROC
                    ) {
                secondMostRelatedParameters.add( PerformanceParameterType.REPLICATED_INPUTTING_MASTER_INPUT_PROC );
                secondMostRelatedParameters.add( PerformanceParameterType.REPLICATED_OBSERVING_MASTER_INPUT_PROC );
                secondMostRelatedParameters.add( PerformanceParameterType.REPLICATED_INPUTTING_MASTER_OUTPUT_PROC );
                secondMostRelatedParameters.add( PerformanceParameterType.REPLICATED_OBSERVING_MASTER_OUTPUT_PROC );
            }
            
            if ( parameterType == PerformanceParameterType.REPLICATED_INPUTTING_MASTER_INPUT_TRANS ||
                    parameterType == PerformanceParameterType.REPLICATED_OBSERVING_MASTER_INPUT_TRANS ||
                    parameterType == PerformanceParameterType.CENTRALIZED_SLAVE_INPUT_TRANS
                    ) {
                secondMostRelatedParameters.add( PerformanceParameterType.CENTRALIZED_MASTER_OUTPUT_TRANS );
                secondMostRelatedParameters.add( PerformanceParameterType.CENTRALIZED_SLAVE_OUTPUT_TRANS );
            }
            
            if ( parameterType == PerformanceParameterType.REPLICATED_INPUTTING_MASTER_INPUT_TRANS_BASED_ON_OBSERVED_TIME || 
                    parameterType == PerformanceParameterType.REPLICATED_OBSERVING_MASTER_INPUT_TRANS_BASED_ON_OBSERVED_TIME ||
                    parameterType == PerformanceParameterType.CENTRALIZED_SLAVE_INPUT_TRANS_BASED_ON_OBSERVED_TIME
                    ) {
                secondMostRelatedParameters.add( PerformanceParameterType.CENTRALIZED_MASTER_OUTPUT_TRANS_BASED_ON_OBSERVED_TIME );
                secondMostRelatedParameters.add( PerformanceParameterType.CENTRALIZED_SLAVE_OUTPUT_TRANS_BASED_ON_OBSERVED_TIME );
            }
            
            if ( parameterType == PerformanceParameterType.REPLICATED_INPUTTING_MASTER_INPUT_TRANS_TO_FIRST_DEST || 
                    parameterType == PerformanceParameterType.REPLICATED_OBSERVING_MASTER_INPUT_TRANS_TO_FIRST_DEST ||
                    parameterType == PerformanceParameterType.CENTRALIZED_SLAVE_INPUT_TRANS_TO_FIRST_DEST
                    ) {
                secondMostRelatedParameters.add( PerformanceParameterType.CENTRALIZED_MASTER_OUTPUT_TRANS_TO_FIRST_DEST );
                secondMostRelatedParameters.add( PerformanceParameterType.CENTRALIZED_SLAVE_OUTPUT_TRANS_TO_FIRST_DEST );
            }
            
            if ( parameterType == PerformanceParameterType.REPLICATED_INPUTTING_MASTER_INPUT_TRANS_TO_FIRST_DEST_BASED_ON_OBSERVED_TIME || 
                    parameterType == PerformanceParameterType.REPLICATED_OBSERVING_MASTER_INPUT_TRANS_TO_FIRST_DEST_BASED_ON_OBSERVED_TIME ||
                    parameterType == PerformanceParameterType.CENTRALIZED_SLAVE_INPUT_TRANS_TO_FIRST_DEST_BASED_ON_OBSERVED_TIME
                    ) {
                secondMostRelatedParameters.add( PerformanceParameterType.CENTRALIZED_MASTER_OUTPUT_TRANS_TO_FIRST_DEST_BASED_ON_OBSERVED_TIME );
                secondMostRelatedParameters.add( PerformanceParameterType.CENTRALIZED_SLAVE_OUTPUT_TRANS_TO_FIRST_DEST_BASED_ON_OBSERVED_TIME );
            }
            
            if ( parameterType == PerformanceParameterType.CENTRALIZED_MASTER_OUTPUT_TRANS ||
                    parameterType == PerformanceParameterType.CENTRALIZED_SLAVE_OUTPUT_TRANS
                    ) {
                secondMostRelatedParameters.add( PerformanceParameterType.REPLICATED_INPUTTING_MASTER_INPUT_TRANS );
                secondMostRelatedParameters.add( PerformanceParameterType.REPLICATED_OBSERVING_MASTER_INPUT_TRANS );
                secondMostRelatedParameters.add( PerformanceParameterType.CENTRALIZED_SLAVE_INPUT_TRANS );
            }
            
            if ( parameterType == PerformanceParameterType.CENTRALIZED_MASTER_OUTPUT_TRANS_BASED_ON_OBSERVED_TIME ||
                    parameterType == PerformanceParameterType.CENTRALIZED_SLAVE_OUTPUT_TRANS_BASED_ON_OBSERVED_TIME
                    ) {
                secondMostRelatedParameters.add( PerformanceParameterType.REPLICATED_INPUTTING_MASTER_INPUT_TRANS_BASED_ON_OBSERVED_TIME );
                secondMostRelatedParameters.add( PerformanceParameterType.REPLICATED_OBSERVING_MASTER_INPUT_TRANS_BASED_ON_OBSERVED_TIME );
                secondMostRelatedParameters.add( PerformanceParameterType.CENTRALIZED_SLAVE_INPUT_TRANS_BASED_ON_OBSERVED_TIME );
            }
            
            if ( parameterType == PerformanceParameterType.CENTRALIZED_MASTER_OUTPUT_TRANS_TO_FIRST_DEST ||
                    parameterType == PerformanceParameterType.CENTRALIZED_SLAVE_OUTPUT_TRANS_TO_FIRST_DEST
                    ) {
                secondMostRelatedParameters.add( PerformanceParameterType.REPLICATED_INPUTTING_MASTER_INPUT_TRANS_TO_FIRST_DEST );
                secondMostRelatedParameters.add( PerformanceParameterType.REPLICATED_OBSERVING_MASTER_INPUT_TRANS_TO_FIRST_DEST );
                secondMostRelatedParameters.add( PerformanceParameterType.CENTRALIZED_SLAVE_INPUT_TRANS_TO_FIRST_DEST );
            }

            if ( parameterType == PerformanceParameterType.CENTRALIZED_MASTER_OUTPUT_TRANS_TO_FIRST_DEST_BASED_ON_OBSERVED_TIME ||
                    parameterType == PerformanceParameterType.CENTRALIZED_SLAVE_OUTPUT_TRANS_TO_FIRST_DEST_BASED_ON_OBSERVED_TIME
                    ) {
                secondMostRelatedParameters.add( PerformanceParameterType.REPLICATED_INPUTTING_MASTER_INPUT_TRANS_TO_FIRST_DEST_BASED_ON_OBSERVED_TIME );
                secondMostRelatedParameters.add( PerformanceParameterType.REPLICATED_OBSERVING_MASTER_INPUT_TRANS_TO_FIRST_DEST_BASED_ON_OBSERVED_TIME );
                secondMostRelatedParameters.add( PerformanceParameterType.CENTRALIZED_SLAVE_INPUT_TRANS_TO_FIRST_DEST_BASED_ON_OBSERVED_TIME );
            }

            secondMostRelatedParametersCollection.put(
                    parameterType,
                    secondMostRelatedParameters
                    );
        }
        
        return secondMostRelatedParametersCollection;
    }
    
    private static Map<PerformanceParameterType, List<PerformanceParameterType>> getThirdMostRelatedParameters() {

        Map<PerformanceParameterType, List<PerformanceParameterType>> thirdMostRelatedParametersCollection =
            new Hashtable<PerformanceParameterType, List<PerformanceParameterType>>();
        
        for ( PerformanceParameterType parameterType : PerformanceParameterType.values() ) {
            List<PerformanceParameterType> thirdMostRelatedParameters = new LinkedList<PerformanceParameterType>();

            thirdMostRelatedParameters.add( PerformanceParameterType.REPLICATED_INPUTTING_MASTER_OUTPUT_PROC );
            thirdMostRelatedParameters.add( PerformanceParameterType.REPLICATED_OBSERVING_MASTER_OUTPUT_PROC );
            thirdMostRelatedParameters.add( PerformanceParameterType.CENTRALIZED_MASTER_OUTPUT_PROC );
            thirdMostRelatedParameters.add( PerformanceParameterType.CENTRALIZED_SLAVE_OUTPUT_PROC );
            
            thirdMostRelatedParametersCollection.put(
                    parameterType,
                    thirdMostRelatedParameters
                    );
        }

        return thirdMostRelatedParametersCollection;
    }
    
    private static Map<PerformanceParameterType, List<PerformanceParameterType>> getPairWiseRelatedParameters() {

        Map<PerformanceParameterType, List<PerformanceParameterType>> pairWiseRelatedParametersCollection =
            new Hashtable<PerformanceParameterType, List<PerformanceParameterType>>();
        
        for ( PerformanceParameterType parameterType : PerformanceParameterType.values() ) {
            List<PerformanceParameterType> pairWiseRelatedParameters = new LinkedList<PerformanceParameterType>();

            if ( parameterType == PerformanceParameterType.REPLICATED_INPUTTING_MASTER_INPUT_PROC ) {
                pairWiseRelatedParameters.add( PerformanceParameterType.REPLICATED_OBSERVING_MASTER_INPUT_PROC );
                pairWiseRelatedParameters.add( PerformanceParameterType.CENTRALIZED_MASTER_INPUT_PROC );
            }
            if ( parameterType == PerformanceParameterType.REPLICATED_OBSERVING_MASTER_INPUT_PROC ) {
                pairWiseRelatedParameters.add( PerformanceParameterType.REPLICATED_INPUTTING_MASTER_INPUT_PROC );
                pairWiseRelatedParameters.add( PerformanceParameterType.CENTRALIZED_MASTER_INPUT_PROC );
            }
            if ( parameterType == PerformanceParameterType.CENTRALIZED_MASTER_INPUT_PROC ) {
                pairWiseRelatedParameters.add( PerformanceParameterType.REPLICATED_INPUTTING_MASTER_INPUT_PROC );
                pairWiseRelatedParameters.add( PerformanceParameterType.REPLICATED_OBSERVING_MASTER_INPUT_PROC );
            }
            
            if ( parameterType == PerformanceParameterType.REPLICATED_INPUTTING_MASTER_OUTPUT_PROC ) {
                pairWiseRelatedParameters.add( PerformanceParameterType.REPLICATED_OBSERVING_MASTER_OUTPUT_PROC );
                pairWiseRelatedParameters.add( PerformanceParameterType.CENTRALIZED_MASTER_OUTPUT_PROC );
                pairWiseRelatedParameters.add( PerformanceParameterType.CENTRALIZED_SLAVE_OUTPUT_PROC );
            }
            if ( parameterType == PerformanceParameterType.REPLICATED_OBSERVING_MASTER_OUTPUT_PROC ) {
                pairWiseRelatedParameters.add( PerformanceParameterType.REPLICATED_INPUTTING_MASTER_OUTPUT_PROC );
                pairWiseRelatedParameters.add( PerformanceParameterType.CENTRALIZED_MASTER_OUTPUT_PROC );
                pairWiseRelatedParameters.add( PerformanceParameterType.CENTRALIZED_SLAVE_OUTPUT_PROC );
            }
            if ( parameterType == PerformanceParameterType.CENTRALIZED_MASTER_OUTPUT_PROC ) {
                pairWiseRelatedParameters.add( PerformanceParameterType.CENTRALIZED_SLAVE_OUTPUT_PROC );
                pairWiseRelatedParameters.add( PerformanceParameterType.REPLICATED_INPUTTING_MASTER_OUTPUT_PROC );
                pairWiseRelatedParameters.add( PerformanceParameterType.REPLICATED_OBSERVING_MASTER_OUTPUT_PROC );
            }
            if ( parameterType == PerformanceParameterType.CENTRALIZED_SLAVE_OUTPUT_PROC ) {
                pairWiseRelatedParameters.add( PerformanceParameterType.CENTRALIZED_MASTER_OUTPUT_PROC );
                pairWiseRelatedParameters.add( PerformanceParameterType.REPLICATED_INPUTTING_MASTER_OUTPUT_PROC );
                pairWiseRelatedParameters.add( PerformanceParameterType.REPLICATED_OBSERVING_MASTER_OUTPUT_PROC );
            }
            
            if ( parameterType == PerformanceParameterType.REPLICATED_INPUTTING_MASTER_INPUT_TRANS ) {
                pairWiseRelatedParameters.add( PerformanceParameterType.REPLICATED_OBSERVING_MASTER_INPUT_TRANS );
                pairWiseRelatedParameters.add( PerformanceParameterType.CENTRALIZED_SLAVE_INPUT_TRANS );
                pairWiseRelatedParameters.add( PerformanceParameterType.CENTRALIZED_MASTER_OUTPUT_TRANS );
                pairWiseRelatedParameters.add( PerformanceParameterType.CENTRALIZED_SLAVE_OUTPUT_TRANS );
            }
            if ( parameterType == PerformanceParameterType.REPLICATED_OBSERVING_MASTER_INPUT_TRANS ) {
                pairWiseRelatedParameters.add( PerformanceParameterType.REPLICATED_INPUTTING_MASTER_INPUT_TRANS );
                pairWiseRelatedParameters.add( PerformanceParameterType.CENTRALIZED_SLAVE_INPUT_TRANS );
                pairWiseRelatedParameters.add( PerformanceParameterType.CENTRALIZED_MASTER_OUTPUT_TRANS );
                pairWiseRelatedParameters.add( PerformanceParameterType.CENTRALIZED_SLAVE_OUTPUT_TRANS );
            }
            if ( parameterType == PerformanceParameterType.CENTRALIZED_SLAVE_INPUT_TRANS ) {
                pairWiseRelatedParameters.add( PerformanceParameterType.REPLICATED_INPUTTING_MASTER_INPUT_TRANS );
                pairWiseRelatedParameters.add( PerformanceParameterType.REPLICATED_OBSERVING_MASTER_INPUT_TRANS );
                pairWiseRelatedParameters.add( PerformanceParameterType.CENTRALIZED_MASTER_OUTPUT_TRANS );
                pairWiseRelatedParameters.add( PerformanceParameterType.CENTRALIZED_SLAVE_OUTPUT_TRANS );
            }

            if ( parameterType == PerformanceParameterType.REPLICATED_INPUTTING_MASTER_INPUT_TRANS_BASED_ON_OBSERVED_TIME ) {
                pairWiseRelatedParameters.add( PerformanceParameterType.REPLICATED_OBSERVING_MASTER_INPUT_TRANS_BASED_ON_OBSERVED_TIME );
                pairWiseRelatedParameters.add( PerformanceParameterType.CENTRALIZED_SLAVE_INPUT_TRANS_BASED_ON_OBSERVED_TIME );
            }
            if ( parameterType == PerformanceParameterType.REPLICATED_OBSERVING_MASTER_INPUT_TRANS_BASED_ON_OBSERVED_TIME ) {
                pairWiseRelatedParameters.add( PerformanceParameterType.REPLICATED_INPUTTING_MASTER_INPUT_TRANS_BASED_ON_OBSERVED_TIME );
                pairWiseRelatedParameters.add( PerformanceParameterType.CENTRALIZED_SLAVE_INPUT_TRANS_BASED_ON_OBSERVED_TIME );
                pairWiseRelatedParameters.add( PerformanceParameterType.CENTRALIZED_MASTER_OUTPUT_TRANS_BASED_ON_OBSERVED_TIME );
                pairWiseRelatedParameters.add( PerformanceParameterType.CENTRALIZED_SLAVE_OUTPUT_TRANS_BASED_ON_OBSERVED_TIME );
            }
            if ( parameterType == PerformanceParameterType.CENTRALIZED_SLAVE_INPUT_TRANS_BASED_ON_OBSERVED_TIME ) {
                pairWiseRelatedParameters.add( PerformanceParameterType.REPLICATED_INPUTTING_MASTER_INPUT_TRANS_BASED_ON_OBSERVED_TIME );
                pairWiseRelatedParameters.add( PerformanceParameterType.REPLICATED_OBSERVING_MASTER_INPUT_TRANS_BASED_ON_OBSERVED_TIME );
                pairWiseRelatedParameters.add( PerformanceParameterType.CENTRALIZED_MASTER_OUTPUT_TRANS_BASED_ON_OBSERVED_TIME );
                pairWiseRelatedParameters.add( PerformanceParameterType.CENTRALIZED_SLAVE_OUTPUT_TRANS_BASED_ON_OBSERVED_TIME );
            }

            if ( parameterType == PerformanceParameterType.REPLICATED_INPUTTING_MASTER_INPUT_TRANS_TO_FIRST_DEST ) {
                pairWiseRelatedParameters.add( PerformanceParameterType.REPLICATED_OBSERVING_MASTER_INPUT_TRANS_TO_FIRST_DEST );
                pairWiseRelatedParameters.add( PerformanceParameterType.CENTRALIZED_SLAVE_INPUT_TRANS_TO_FIRST_DEST );
                pairWiseRelatedParameters.add( PerformanceParameterType.CENTRALIZED_MASTER_OUTPUT_TRANS_TO_FIRST_DEST );
                pairWiseRelatedParameters.add( PerformanceParameterType.CENTRALIZED_SLAVE_OUTPUT_TRANS_TO_FIRST_DEST );
            }
            if ( parameterType == PerformanceParameterType.REPLICATED_OBSERVING_MASTER_INPUT_TRANS_TO_FIRST_DEST ) {
                pairWiseRelatedParameters.add( PerformanceParameterType.REPLICATED_INPUTTING_MASTER_INPUT_TRANS_TO_FIRST_DEST );
                pairWiseRelatedParameters.add( PerformanceParameterType.CENTRALIZED_SLAVE_INPUT_TRANS_TO_FIRST_DEST );
                pairWiseRelatedParameters.add( PerformanceParameterType.CENTRALIZED_MASTER_OUTPUT_TRANS_TO_FIRST_DEST );
                pairWiseRelatedParameters.add( PerformanceParameterType.CENTRALIZED_SLAVE_OUTPUT_TRANS_TO_FIRST_DEST );
            }
            if ( parameterType == PerformanceParameterType.CENTRALIZED_SLAVE_INPUT_TRANS_TO_FIRST_DEST ) {
                pairWiseRelatedParameters.add( PerformanceParameterType.REPLICATED_INPUTTING_MASTER_INPUT_TRANS_TO_FIRST_DEST );
                pairWiseRelatedParameters.add( PerformanceParameterType.REPLICATED_OBSERVING_MASTER_INPUT_TRANS_TO_FIRST_DEST );
                pairWiseRelatedParameters.add( PerformanceParameterType.CENTRALIZED_MASTER_OUTPUT_TRANS_TO_FIRST_DEST );
                pairWiseRelatedParameters.add( PerformanceParameterType.CENTRALIZED_SLAVE_OUTPUT_TRANS_TO_FIRST_DEST );
            }

            if ( parameterType == PerformanceParameterType.REPLICATED_INPUTTING_MASTER_INPUT_TRANS_TO_FIRST_DEST_BASED_ON_OBSERVED_TIME ) {
                pairWiseRelatedParameters.add( PerformanceParameterType.REPLICATED_OBSERVING_MASTER_INPUT_TRANS_TO_FIRST_DEST_BASED_ON_OBSERVED_TIME );
                pairWiseRelatedParameters.add( PerformanceParameterType.CENTRALIZED_SLAVE_INPUT_TRANS_TO_FIRST_DEST_BASED_ON_OBSERVED_TIME );
                pairWiseRelatedParameters.add( PerformanceParameterType.CENTRALIZED_MASTER_OUTPUT_TRANS_TO_FIRST_DEST_BASED_ON_OBSERVED_TIME );
                pairWiseRelatedParameters.add( PerformanceParameterType.CENTRALIZED_SLAVE_OUTPUT_TRANS_TO_FIRST_DEST_BASED_ON_OBSERVED_TIME );
            }
            if ( parameterType == PerformanceParameterType.REPLICATED_OBSERVING_MASTER_INPUT_TRANS_TO_FIRST_DEST_BASED_ON_OBSERVED_TIME ) {
                pairWiseRelatedParameters.add( PerformanceParameterType.REPLICATED_INPUTTING_MASTER_INPUT_TRANS_TO_FIRST_DEST_BASED_ON_OBSERVED_TIME );
                pairWiseRelatedParameters.add( PerformanceParameterType.CENTRALIZED_SLAVE_INPUT_TRANS_TO_FIRST_DEST_BASED_ON_OBSERVED_TIME );
                pairWiseRelatedParameters.add( PerformanceParameterType.CENTRALIZED_MASTER_OUTPUT_TRANS_TO_FIRST_DEST_BASED_ON_OBSERVED_TIME );
                pairWiseRelatedParameters.add( PerformanceParameterType.CENTRALIZED_SLAVE_OUTPUT_TRANS_TO_FIRST_DEST_BASED_ON_OBSERVED_TIME );
            }
            if ( parameterType == PerformanceParameterType.CENTRALIZED_SLAVE_INPUT_TRANS_TO_FIRST_DEST_BASED_ON_OBSERVED_TIME ) {
                pairWiseRelatedParameters.add( PerformanceParameterType.REPLICATED_INPUTTING_MASTER_INPUT_TRANS_TO_FIRST_DEST_BASED_ON_OBSERVED_TIME );
                pairWiseRelatedParameters.add( PerformanceParameterType.REPLICATED_OBSERVING_MASTER_INPUT_TRANS_TO_FIRST_DEST_BASED_ON_OBSERVED_TIME );
                pairWiseRelatedParameters.add( PerformanceParameterType.CENTRALIZED_MASTER_OUTPUT_TRANS_TO_FIRST_DEST_BASED_ON_OBSERVED_TIME );
                pairWiseRelatedParameters.add( PerformanceParameterType.CENTRALIZED_SLAVE_OUTPUT_TRANS_TO_FIRST_DEST_BASED_ON_OBSERVED_TIME );
            }

            if ( parameterType == PerformanceParameterType.CENTRALIZED_MASTER_OUTPUT_TRANS ) {
                pairWiseRelatedParameters.add( PerformanceParameterType.CENTRALIZED_SLAVE_OUTPUT_TRANS );
                pairWiseRelatedParameters.add( PerformanceParameterType.REPLICATED_INPUTTING_MASTER_INPUT_TRANS );
                pairWiseRelatedParameters.add( PerformanceParameterType.REPLICATED_OBSERVING_MASTER_INPUT_TRANS );
                pairWiseRelatedParameters.add( PerformanceParameterType.CENTRALIZED_SLAVE_INPUT_TRANS );
            }
            if ( parameterType == PerformanceParameterType.CENTRALIZED_SLAVE_OUTPUT_TRANS ) {
                pairWiseRelatedParameters.add( PerformanceParameterType.CENTRALIZED_MASTER_OUTPUT_TRANS );
                pairWiseRelatedParameters.add( PerformanceParameterType.REPLICATED_INPUTTING_MASTER_INPUT_TRANS );
                pairWiseRelatedParameters.add( PerformanceParameterType.REPLICATED_OBSERVING_MASTER_INPUT_TRANS );
                pairWiseRelatedParameters.add( PerformanceParameterType.CENTRALIZED_SLAVE_INPUT_TRANS );
            }

            if ( parameterType == PerformanceParameterType.CENTRALIZED_MASTER_OUTPUT_TRANS_BASED_ON_OBSERVED_TIME ) {
                pairWiseRelatedParameters.add( PerformanceParameterType.CENTRALIZED_SLAVE_OUTPUT_TRANS_BASED_ON_OBSERVED_TIME );
                pairWiseRelatedParameters.add( PerformanceParameterType.REPLICATED_INPUTTING_MASTER_INPUT_TRANS_BASED_ON_OBSERVED_TIME );
                pairWiseRelatedParameters.add( PerformanceParameterType.REPLICATED_OBSERVING_MASTER_INPUT_TRANS_BASED_ON_OBSERVED_TIME );
                pairWiseRelatedParameters.add( PerformanceParameterType.CENTRALIZED_SLAVE_INPUT_TRANS_BASED_ON_OBSERVED_TIME );
            }
            if ( parameterType == PerformanceParameterType.CENTRALIZED_SLAVE_OUTPUT_TRANS_BASED_ON_OBSERVED_TIME ) {
                pairWiseRelatedParameters.add( PerformanceParameterType.CENTRALIZED_MASTER_OUTPUT_TRANS_BASED_ON_OBSERVED_TIME );
                pairWiseRelatedParameters.add( PerformanceParameterType.REPLICATED_INPUTTING_MASTER_INPUT_TRANS_BASED_ON_OBSERVED_TIME );
                pairWiseRelatedParameters.add( PerformanceParameterType.REPLICATED_OBSERVING_MASTER_INPUT_TRANS_BASED_ON_OBSERVED_TIME );
                pairWiseRelatedParameters.add( PerformanceParameterType.CENTRALIZED_SLAVE_INPUT_TRANS_BASED_ON_OBSERVED_TIME );
            }

            if ( parameterType == PerformanceParameterType.CENTRALIZED_MASTER_OUTPUT_TRANS_TO_FIRST_DEST ) {
                pairWiseRelatedParameters.add( PerformanceParameterType.CENTRALIZED_SLAVE_OUTPUT_TRANS_TO_FIRST_DEST );
                pairWiseRelatedParameters.add( PerformanceParameterType.REPLICATED_INPUTTING_MASTER_INPUT_TRANS_TO_FIRST_DEST );
                pairWiseRelatedParameters.add( PerformanceParameterType.REPLICATED_OBSERVING_MASTER_INPUT_TRANS_TO_FIRST_DEST );
                pairWiseRelatedParameters.add( PerformanceParameterType.CENTRALIZED_SLAVE_INPUT_TRANS_TO_FIRST_DEST );
            }
            if ( parameterType == PerformanceParameterType.CENTRALIZED_SLAVE_OUTPUT_TRANS_TO_FIRST_DEST ) {
                pairWiseRelatedParameters.add( PerformanceParameterType.CENTRALIZED_MASTER_OUTPUT_TRANS_TO_FIRST_DEST );
                pairWiseRelatedParameters.add( PerformanceParameterType.REPLICATED_INPUTTING_MASTER_INPUT_TRANS_TO_FIRST_DEST );
                pairWiseRelatedParameters.add( PerformanceParameterType.REPLICATED_OBSERVING_MASTER_INPUT_TRANS_TO_FIRST_DEST );
                pairWiseRelatedParameters.add( PerformanceParameterType.CENTRALIZED_SLAVE_INPUT_TRANS_TO_FIRST_DEST );
            }

            if ( parameterType == PerformanceParameterType.CENTRALIZED_MASTER_OUTPUT_TRANS_TO_FIRST_DEST_BASED_ON_OBSERVED_TIME ) {
                pairWiseRelatedParameters.add( PerformanceParameterType.CENTRALIZED_SLAVE_OUTPUT_TRANS_TO_FIRST_DEST_BASED_ON_OBSERVED_TIME );
                pairWiseRelatedParameters.add( PerformanceParameterType.REPLICATED_INPUTTING_MASTER_INPUT_TRANS_TO_FIRST_DEST_BASED_ON_OBSERVED_TIME );
                pairWiseRelatedParameters.add( PerformanceParameterType.REPLICATED_OBSERVING_MASTER_INPUT_TRANS_TO_FIRST_DEST_BASED_ON_OBSERVED_TIME );
                pairWiseRelatedParameters.add( PerformanceParameterType.CENTRALIZED_SLAVE_INPUT_TRANS_TO_FIRST_DEST_BASED_ON_OBSERVED_TIME );
            }
            if ( parameterType == PerformanceParameterType.CENTRALIZED_SLAVE_OUTPUT_TRANS_TO_FIRST_DEST_BASED_ON_OBSERVED_TIME ) {
                pairWiseRelatedParameters.add( PerformanceParameterType.CENTRALIZED_MASTER_OUTPUT_TRANS_TO_FIRST_DEST_BASED_ON_OBSERVED_TIME );
                pairWiseRelatedParameters.add( PerformanceParameterType.REPLICATED_INPUTTING_MASTER_INPUT_TRANS_TO_FIRST_DEST_BASED_ON_OBSERVED_TIME );
                pairWiseRelatedParameters.add( PerformanceParameterType.REPLICATED_OBSERVING_MASTER_INPUT_TRANS_TO_FIRST_DEST_BASED_ON_OBSERVED_TIME );
                pairWiseRelatedParameters.add( PerformanceParameterType.CENTRALIZED_SLAVE_INPUT_TRANS_TO_FIRST_DEST_BASED_ON_OBSERVED_TIME );
            }

            pairWiseRelatedParametersCollection.put(
                    parameterType,
                    pairWiseRelatedParameters
                    );
        }

        return pairWiseRelatedParametersCollection;
    }
    
    public static Map<ComputerInfo, ComputerPerformanceData> loadComputerPerformanceData(
            String performanceEstimatesFile
            ) {
        Map<ComputerInfo, ComputerPerformanceData> computerPerformanceEstimateCollection = 
            new Hashtable<ComputerInfo, ComputerPerformanceData>();
        
        try {
            File file = new File( performanceEstimatesFile );
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
                ComputerPerformanceData computerPerformanceData = new AComputerPerformanceData( computerInfo );
                for ( int i = 0; i < PerformanceParameterType.values().length; i++ ) {
                    Object value = values.get( PerformanceParameterType.values()[ i ].toString() );
                    double costEstimate = -1;
                    if ( value != null ) {
                        costEstimate = Double.parseDouble( (String ) value );
                    }
                    computerPerformanceData.setInitialPerformanceParameterEstimate(
                            PerformanceParameterType.values()[ i ],
                            costEstimate
                            );
                }
                
                computerPerformanceEstimateCollection.put( computerInfo, computerPerformanceData );
            }
            
        }
        catch ( Exception e ) {
            ErrorHandlingUtils.logSevereExceptionAndContinue(
                    "AComputerStats: Could not load cost estimates file",
                    e
                    );
        }
        
        return computerPerformanceEstimateCollection;
    }
    
    public static void savePerformanceParameterEstimates(
            String statsFile,
            Map<ComputerInfo, ComputerPerformanceData> computerPerformanceDataCollection
            ) {
        
        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.newDocument();

            Element rootElement = document.createElement( "estimates" );
            document.appendChild( rootElement );
            
            Iterator<Map.Entry<ComputerInfo, ComputerPerformanceData>> itr = computerPerformanceDataCollection.entrySet().iterator();
            while ( itr.hasNext() ) {
                Map.Entry<ComputerInfo, ComputerPerformanceData> entry = itr.next();
                
                if ( entry.getKey().getProcessorName().equals( Constants.FAKE_LOGGABLE_PROCESSOR_NAME ) ) {
                    continue;
                }
                
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
                    PerformanceParameterType parameterType = PerformanceParameterType.values()[ i ];
                    double parameterValue = entry.getValue().getParameterEstimateBasedOnRealData( parameterType );
                    emParameterNode = createParameterNode(
                            document, parameterType.toString(), Double.toString( parameterValue )
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
        emParameterNode.setAttribute( "name", name );
        emParameterNode.setAttribute( "value", value );
//        Element emParameterNodeChild = document.createElement( "name" );
//        emParameterNodeChild.setTextContent( name );
//        emParameterNode.appendChild( emParameterNodeChild );
//        emParameterNodeChild = document.createElement( "value" );
//        emParameterNodeChild.setTextContent( value );
//        emParameterNode.appendChild( emParameterNodeChild );
//        emParameterNode.appendChild( emParameterNodeChild );
        return emParameterNode;

    }
    
}
