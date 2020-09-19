package ca.draconic.vote;

import org.apache.commons.math3.FieldElement;

/**
 * 
 * @author Kevin Smith <smithkm@draconic.ca>
 *
 */
public enum Preference {
    A(-1),
    B(1),
    NONE(0)
    ;
    final public int comparison;
    
    private Preference(int comparison) {
        this.comparison = comparison;
    }
    
    static public <Count extends FieldElement<Count> & Comparable<Count>> Preference fromVotes(Count preferA, Count preferB) {
        return fromComparison(preferB.compareTo(preferA));
    }
    
    static public Preference fromRanks(int rankA, int rankB) {
        return fromComparison(rankA-rankB);
    }
    
    static final private Preference[] COMPARISSON_LOOKUP = {A, NONE, B};
    
    static private Preference fromComparison(int comparisson){
        return COMPARISSON_LOOKUP[Integer.signum(comparisson)+1];
    }
}
