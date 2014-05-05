package commonutils.basic;

import java.util.*;

class OnIntSortHolder implements Comparable<OnIntSortHolder> {
    public int SortValue;
    public int KeepInSyncValue;
    
    public final int compareTo( OnIntSortHolder otherHolder ) {
        if ( SortValue < otherHolder.SortValue ) {
            return -1;
        } else if ( SortValue > otherHolder.SortValue ) {
            return 1;
        } else {
            return 0;
        }
    }
}

class OnDoubleSortHolder implements Comparable<OnDoubleSortHolder> {
    public double SortValue;
    public int KeepInSyncValue;
    
    public final int compareTo( OnDoubleSortHolder otherHolder ) {
        if ( SortValue < otherHolder.SortValue ) {
            return -1;
        } else if ( SortValue > otherHolder.SortValue ) {
            return 1;
        } else {
            return 0;
        }
    }
}

public class DualArraySorter {

    public static void sortOnIntArray( int[] sortOn, int[] keepInSync ) {
        
        int numEntries = sortOn.length;

        OnIntSortHolder[] holder = new OnIntSortHolder[numEntries];
        for ( int i = 0; i < numEntries; i++ ) {
            holder[i] = new OnIntSortHolder();
        }

        for ( int i = 0; i < numEntries; i++ ) {
            holder[i].SortValue = sortOn[i];
            holder[i].KeepInSyncValue = (Integer) keepInSync[i];
        }
        
        Arrays.sort( holder );
        
        for ( int i = numEntries - 1; i >= 0; i-- ) {
            sortOn[numEntries - i - 1] = holder[i].SortValue;
            keepInSync[numEntries - i - 1] = holder[i].KeepInSyncValue;
        }
    }
    
    public static void sortOnDoubleArray( double[] sortOn, int[] keepInSync ) {
        
        int numEntries = sortOn.length;
        
        OnDoubleSortHolder[] holder = new OnDoubleSortHolder[numEntries];
        for ( int i = 0; i < numEntries; i++ ) {
            holder[i] = new OnDoubleSortHolder();
        }
        
        for ( int i = 0; i < numEntries; i++ ) {
            holder[i].SortValue = sortOn[i];
            holder[i].KeepInSyncValue = (Integer) keepInSync[i];
        }
        
        Arrays.sort( holder );
        
        for ( int i = numEntries - 1; i >= 0; i-- ) {
            sortOn[numEntries - i - 1] = holder[i].SortValue;
            keepInSync[numEntries - i - 1] = holder[i].KeepInSyncValue;
        }
    }
}
