package selfoptsys.perf.tradeofffunction;


public class ABasicLazyRespTimeTradeoffPolicyFactory 
    implements RespTimeTradeoffPolicyFactory {

    public RespTimeTradeoffPolicy getRemoteResponseTimeTradeoffSpec() {
        return new ABasicLazyRespTimeTradeoffPolicy();
    }
    
}
