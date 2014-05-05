package commonutils.basic2;

import java.util.*;
import commonutils.basic.*;

/*
 * We manually do the check to make sure there are no cast exceptions
 */
@SuppressWarnings("unchecked")
public abstract class AConfigParamProcessor 
	implements ConfigParamProcessor {

	protected Map<String, AParamInfo> m_paramInfos = new Hashtable<String, AParamInfo>();
	protected Map<String, ParamSpec> m_paramSpecs = new Hashtable<String, ParamSpec>();
	
    public AConfigParamProcessor() {}
    
    public Map<String, AParamInfo> getAllParamInfos() {
        return m_paramInfos;
    }
    
    public Map<String, ParamSpec> getAllParamSpecs() {
        return m_paramSpecs;
    }
    
	protected ConfigParamType getConfigParamTypeFromString(
			String parameterTypeString
			) {
		ConfigParamType type = ConfigParamType.STRING;
		
		if ( ConfigParamType.STRING.toString().equals( parameterTypeString.toUpperCase() ) ) {
			return ConfigParamType.STRING;
		}
		else if ( ConfigParamType.STRING_ARRAY.toString().equals( parameterTypeString.toUpperCase() ) ) {
			return ConfigParamType.STRING_ARRAY;
		}
		else if ( ConfigParamType.BOOLEAN.toString().equals( parameterTypeString.toUpperCase() ) ) {
			return ConfigParamType.BOOLEAN;
		}
		else if ( ConfigParamType.BOOLEAN_ARRAY.toString().equals( parameterTypeString.toUpperCase() ) ) {
			return ConfigParamType.BOOLEAN_ARRAY;
		}
		else if ( ConfigParamType.INT.toString().equals( parameterTypeString.toUpperCase() ) ) {
			return ConfigParamType.INT;
		}
		else if ( ConfigParamType.INT_ARRAY.toString().equals( parameterTypeString.toUpperCase() ) ) {
			return ConfigParamType.INT_ARRAY;
		}
		else if ( ConfigParamType.DOUBLE.toString().equals( parameterTypeString.toUpperCase() ) ) {
			return ConfigParamType.DOUBLE;
		}
		else if ( ConfigParamType.DOUBLE_ARRAY.toString().equals( parameterTypeString.toUpperCase() ) ) {
			return ConfigParamType.DOUBLE_ARRAY;
		}
		
		return type;
	}
	
    public static AParamInfo createParamInfo(
            ParamSpec spec,
            List<String> paramValues,
            boolean paramSpecified
            ) {
        AParamInfo info = null;
        
        if ( spec.getConfigParamType() == ConfigParamType.STRING ) {
            String pVal = "";
            for ( int i = 0; i < paramValues.size(); i++ ) {
                pVal += paramValues.get( i );
                if ( i < paramValues.size() - 1 ) {
                    pVal += " ";
                }
            }
            info = new AParamInfo<String>(spec.getName(), pVal, paramSpecified );
        } 
        else if ( spec.getConfigParamType() == ConfigParamType.STRING_ARRAY ) {
            String[] paramValueArray = new String[ paramValues.size() ];
            for ( int j = 0; j < paramValues.size(); j++ ) {
                paramValueArray[ j ] = paramValues.get( j );
            }
            info = new AParamInfo<String[]>( spec.getName(), paramValueArray, paramSpecified );
        } 
        else if ( spec.getConfigParamType() == ConfigParamType.BOOLEAN ) {
            info = new AParamInfo<Boolean>(spec.getName(), Boolean.parseBoolean( paramValues.get( 0 ) ), paramSpecified );
        } 
        else if ( spec.getConfigParamType() == ConfigParamType.BOOLEAN_ARRAY ) {
            Boolean[] paramValueArray = new Boolean[ paramValues.size() ];
            for ( int j = 0; j < paramValues.size(); j++ ) {
                paramValueArray[ j ] = Boolean.parseBoolean(  paramValues.get( j ) );
            }
            info = new AParamInfo<Boolean[]>( spec.getName(), paramValueArray, paramSpecified );
        } 
        else if ( spec.getConfigParamType() == ConfigParamType.INT ) {
            info = new AParamInfo<Integer>(spec.getName(), Integer.parseInt( paramValues.get( 0 ) ), paramSpecified );
        } 
        else if ( spec.getConfigParamType() == ConfigParamType.INT_ARRAY ) {
            Integer[] paramValueArray = new Integer[ paramValues.size() ];
            for ( int j = 0; j < paramValues.size(); j++ ) {
                paramValueArray[ j ] = Integer.parseInt(  paramValues.get( j ) );
            }
            info = new AParamInfo<Integer[]>( spec.getName(), paramValueArray, paramSpecified );
        } 
        else if ( spec.getConfigParamType() == ConfigParamType.DOUBLE ) {
            info = new AParamInfo<Double>( spec.getName(), Double.parseDouble( paramValues.get( 0 ) ), paramSpecified );
        }
        else if ( spec.getConfigParamType() == ConfigParamType.DOUBLE_ARRAY ) {
            Double[] paramValueArray = new Double[ paramValues.size() ];
            for ( int j = 0; j < paramValues.size(); j++ ) {
                paramValueArray[ j ] = Double.parseDouble(  paramValues.get( j ) );
            }
            info = new AParamInfo<Double[]>( spec.getName(), paramValueArray, paramSpecified );
        }
        
        return info;
    }
	
    public String getStringParam(
			String paramName
			) {
        ParamSpec spec = m_paramSpecs.get( paramName.toLowerCase() );
        if ( spec == null ) {
            ErrorHandlingUtils.logSevereMessageAndContinue( "Parameter '" + paramName + "' is not recognized" );
        }
            
        if ( spec.getConfigParamType() != ConfigParamType.STRING ) {
			return "";
		}
		
		AParamInfo<String> info = m_paramInfos.get( paramName.toLowerCase() );
		return info.getValue();
	}
	public String getStringParam(
			String paramName,
			String defaultValueIfNotParamSpecified
			) {
		return "";
	}
	
    public String[] getStringArrayParam(
			String paramName
			) {
        ParamSpec spec = m_paramSpecs.get( paramName.toLowerCase() );
        if ( spec == null ) {
            ErrorHandlingUtils.logSevereMessageAndContinue( "Parameter '" + paramName + "' is not recognized" );
        }
            
        if ( spec.getConfigParamType() != ConfigParamType.STRING_ARRAY ) {
            return null;
        }
        
        AParamInfo<String[]> info = m_paramInfos.get( paramName.toLowerCase() );
        return info.getValue();
	}
	public String[] getStringArrayParam(
			String paramName,
			String[] defaultValueIfNotParamSpecified
			) {
	    return null;
	}

	public boolean getBooleanParam( 
			String paramName
			) {
		ParamSpec spec = m_paramSpecs.get( paramName.toLowerCase() );
        if ( spec == null ) {
            ErrorHandlingUtils.logSevereMessageAndContinue( "Parameter '" + paramName + "' is not recognized" );
        }
            
		if ( spec.getConfigParamType() != ConfigParamType.BOOLEAN ) {
			return false;
		}
		
		AParamInfo<Boolean> info = m_paramInfos.get( paramName.toLowerCase() );
		return info.getValue();
	}
	public boolean getBooleanParam(
			String paramName,
			boolean defaultValueIfNotParamSpecified
			) {
		return false;
	}
	public boolean[] getBooleanArrayParam( 
			String paramName
			) {
		return null;
	}
	public boolean[] getBooleanArrayParam( 
			String paramName,
			boolean[] defaultValueIfNotParamSpecified
			) {
		return null;
	}

	public int getIntParam(
			String paramName
			) {
		ParamSpec spec = m_paramSpecs.get( paramName.toLowerCase() );
        if ( spec == null ) {
            ErrorHandlingUtils.logSevereMessageAndContinue( "Parameter '" + paramName + "' is not recognized" );
        }
            
		if ( spec.getConfigParamType() != ConfigParamType.INT ) {
			return 0;
		}
		
		AParamInfo<Integer> info = m_paramInfos.get( paramName.toLowerCase() );
		return info.getValue();
	}
	public int getIntParam(
			String paramName,
			int defaultValueIfNotParamSpecified
			) {
		return 0;
	}
	public int[] getIntArrayParam(
			String paramName
			) {
        ParamSpec spec = m_paramSpecs.get( paramName.toLowerCase() );
        if ( spec == null ) {
            ErrorHandlingUtils.logSevereMessageAndContinue( "Parameter '" + paramName + "' is not recognized" );
        }
            
        if ( spec.getConfigParamType() != ConfigParamType.INT_ARRAY ) {
            return null;
        }
        
        AParamInfo<Integer[]> info = m_paramInfos.get( paramName.toLowerCase() );
        int[] intArray = new int[ info.getValue().length ];
        for ( int i = 0; i < intArray.length; i++ ) {
            intArray[i] = info.getValue()[i];
        }
        return intArray;
	}
	public int[] getIntArrayParam(
			String paramName,
			int[] defaultValueIfNotParamSpecified
			) {
		return null;
	}

	public double getDoubleParam(
			String paramName
			) {
		ParamSpec spec = m_paramSpecs.get( paramName.toLowerCase() );
        if ( spec == null ) {
            ErrorHandlingUtils.logSevereMessageAndContinue( "Parameter '" + paramName + "' is not recognized" );
        }
            
		if ( spec.getConfigParamType() != ConfigParamType.DOUBLE ) {
			return 0;
		}
		
		AParamInfo<Double> info = m_paramInfos.get( paramName.toLowerCase() );
		return info.getValue();
	}
	public double getDoubleParam(
			String paramName,
			double defaultValueIfNotParamSpecified
			) {
		return 0;		
	}
	public double[] getDoubleArrayParam(
			String paramName
			) {
        ParamSpec spec = m_paramSpecs.get( paramName.toLowerCase() );
        if ( spec == null ) {
            ErrorHandlingUtils.logSevereMessageAndContinue( "Parameter '" + paramName + "' is not recognized" );
        }
            
        if ( spec.getConfigParamType() != ConfigParamType.DOUBLE_ARRAY ) {
            return null;
        }
        
        AParamInfo<Double[]> info = m_paramInfos.get( paramName.toLowerCase() );
        double[] doubleArray = new double[ info.getValue().length ];
        for ( int i = 0; i < doubleArray.length; i++ ) {
            doubleArray[i] = info.getValue()[i];
        }
        return doubleArray;
	}
	public double[] getDoubleArrayParam(
			String paramName,
			double[] defaultValueIfNotParamSpecified
			) {
		return null;
	}
	
}
