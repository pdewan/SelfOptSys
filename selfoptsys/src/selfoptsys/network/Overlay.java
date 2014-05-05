package selfoptsys.network;

import java.io.*;
import java.util.List;

public interface Overlay
	extends Serializable {

    int[] getAddOrder();
    
    int[] getParents();
    
    int[] getNumChildren();
    
    List<Integer> getChildrenOf( int index );
    
}
