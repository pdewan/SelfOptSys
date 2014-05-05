package selfoptsys;

public interface LocalTimeServer {

    void startTimeServer();
    
    void startExperimentReplay();
    
    void joinUsersAsNecessary();
	
    void registerResultsPrinter(
    		ResultsPrinter resultsPrinter
    		);
    
}
