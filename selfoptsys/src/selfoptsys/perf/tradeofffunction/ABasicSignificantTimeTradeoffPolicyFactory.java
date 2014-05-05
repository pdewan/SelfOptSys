package selfoptsys.perf.tradeofffunction;


public class ABasicSignificantTimeTradeoffPolicyFactory 
    implements RespTimeTradeoffPolicyFactory {

    public RespTimeTradeoffPolicy getRemoteResponseTimeTradeoffSpec() {
        return new ABasicSignificantRespTimeTradeoffPolicy();
    }
    
}
