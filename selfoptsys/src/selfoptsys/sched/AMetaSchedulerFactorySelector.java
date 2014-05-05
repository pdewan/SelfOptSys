package selfoptsys.sched;

public class AMetaSchedulerFactorySelector {

	private static MetaSchedulerFactory m_factory = new ADefaultMetaSchedulerFactory();
	
	public static void setFactory(
			MetaSchedulerFactory factory
			) {
		m_factory = factory;
	}
	
	public static MetaSchedulerFactory getFactory() {
		return m_factory;
	}
	
}
