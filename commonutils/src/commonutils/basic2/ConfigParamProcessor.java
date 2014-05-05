package commonutils.basic2;

import java.util.Map;

public interface ConfigParamProcessor {

	@SuppressWarnings("unchecked")
	Map<String, AParamInfo> getAllParamInfos();
	
    Map<String, ParamSpec> getAllParamSpecs();
    
	String getStringParam(
			String paramName
			);
	String[] getStringArrayParam(
			String paramName
			);

	boolean getBooleanParam( 
			String paramName
			);
	boolean[] getBooleanArrayParam( 
			String paramName
			);

	int getIntParam(
			String paramName
			);
	int[] getIntArrayParam(
			String paramName
			);

	double getDoubleParam(
			String paramName
			);
	double[] getDoubleArrayParam(
			String paramName
			);
	
}
