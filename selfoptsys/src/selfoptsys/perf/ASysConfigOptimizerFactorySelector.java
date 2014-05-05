package selfoptsys.perf;


public class ASysConfigOptimizerFactorySelector {

    private static SysConfigOptimizerFactory m_factory = 
        new ASysConfigOptimizerFactory();
    
    public static void setFactory(
            SysConfigOptimizerFactory factory
            ) {
        m_factory = factory;
    }
    public static SysConfigOptimizerFactory getFactory() {
        return m_factory;
    }
    
}
