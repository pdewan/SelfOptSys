package selfoptsys.perf.tradeofffunction;


public class ABasicRespTimeTradeoffPolicyFactory 
    implements RespTimeTradeoffPolicyFactory {

    public RespTimeTradeoffPolicy getRemoteResponseTimeTradeoffSpec() {
        return new ABasicLazyRespTimeTradeoffPolicy();
    }
    
}
