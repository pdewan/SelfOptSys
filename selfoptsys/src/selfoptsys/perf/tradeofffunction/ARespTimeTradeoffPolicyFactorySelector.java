package selfoptsys.perf.tradeofffunction;

public class ARespTimeTradeoffPolicyFactorySelector {

    private static RespTimeTradeoffPolicyFactory m_responseTimeTradeoffSpecFactory = 
        new ABasicRespTimeTradeoffPolicyFactory();
    
    public static void setFactory(
            RespTimeTradeoffPolicyFactory factory
            ) {
        m_responseTimeTradeoffSpecFactory = factory;
    }
    
    public static RespTimeTradeoffPolicyFactory getFactory() {
        return m_responseTimeTradeoffSpecFactory;
    }
    

}
