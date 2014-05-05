package selfoptsys;

import java.util.*;

public interface ResultsPrinter {

	void printTimingInformation(
			String outputFileName,
			int[] userTurns,
            Map<Integer, ATimeServerUserInfo> userInfos,
            List<Long> configurationChangeStartTimes,
            List<Long> configurationChangeEndTimes
            );
	
}
