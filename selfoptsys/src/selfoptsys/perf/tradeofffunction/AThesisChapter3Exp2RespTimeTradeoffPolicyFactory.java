package selfoptsys.perf.tradeofffunction;


public class AThesisChapter3Exp2RespTimeTradeoffPolicyFactory 
    implements RespTimeTradeoffPolicyFactory {

    public RespTimeTradeoffPolicy getRemoteResponseTimeTradeoffSpec() {
        return new AThesisChapter3Exp2RespTimeTradeoffPolicy();
    }
    
}
