package selfoptsysapps.ppt;


/*
 * ALL_AT_ONCE: For Replicated Architectures
 * ON_DEMAND: For Centralized Architectures
 * HYBRID: For Hybrid Architectures
 *    For masters, this has same behaviour as ALL_AT_ONCE
 *    For slaves, this has same bahaviour as ON_DEMAND
 */
public enum PptPrefetchMode {

    ALL_AT_ONCE() { public String toString() { return "ALL_AT_ONCE"; } },
    ON_DEMAND() { public String toString() { return "ON_DEMAND"; } },
    HYBRID() { public String toString() { return "HYBRID"; } }
    
}
