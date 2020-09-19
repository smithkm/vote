package ca.draconic.vote;

import org.apache.commons.math3.FieldElement;

public interface WeightedBallot<Count extends FieldElement<Count> & Comparable<Count>> {
    
    /**
     * Weight to apply to the ballot
     * @return
     */
    public Count getWeight();
    
    public void multiply(Count multiplier);
}
