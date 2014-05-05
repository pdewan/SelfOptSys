package selfoptsys;

import java.util.*;

public interface TimeServerReporter {
    
    void setTimingData(
            int[] userTurns,
            Map<Integer, ATimeServerUserInfo> userInfos,
            List<Long> configurationChangeStartTimes,
            List<Long> configurationChangeEndTimes
            );
    
}