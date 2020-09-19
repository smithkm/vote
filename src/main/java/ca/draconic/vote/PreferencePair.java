package ca.draconic.vote;

import org.apache.commons.math3.FieldElement;

public class PreferencePair<Option, Count extends FieldElement<Count> & Comparable<Count>> {
    private final Count preferA;
    private final Count preferB;
    private final Option optionA;
    private final Option optionB;
    
    public PreferencePair(Option optionA, Option optionB, Count preferA, Count preferB) {
        super();
        this.preferA = preferA;
        this.preferB = preferB;
        this.optionA = optionA;
        this.optionB = optionB;
    }
    
    public Count getPreferA() {
        return preferA;
    }
    
    public Count getPreferB() {
        return preferB;
    }
    
    public Option getOptionA() {
        return optionA;
    }

    public Option getOptionB() {
        return optionB;
    }

    public Preference getPreference() {
        return Preference.fromVotes(preferA, preferB);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((optionA == null) ? 0 : optionA.hashCode());
        result = prime * result + ((optionB == null) ? 0 : optionB.hashCode());
        result = prime * result + ((preferA == null) ? 0 : preferA.hashCode());
        result = prime * result + ((preferB == null) ? 0 : preferB.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        PreferencePair<?, ?> other = (PreferencePair<?,?>) obj;
        if (optionA == null) {
            if (other.optionA != null)
                return false;
        } else if (!optionA.equals(other.optionA))
            return false;
        if (optionB == null) {
            if (other.optionB != null)
                return false;
        } else if (!optionB.equals(other.optionB))
            return false;
        if (preferA == null) {
            if (other.preferA != null)
                return false;
        } else if (!preferA.equals(other.preferA))
            return false;
        if (preferB == null) {
            if (other.preferB != null)
                return false;
        } else if (!preferB.equals(other.preferB))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "PreferencePair ["+optionA+":" +preferA+", "+optionB+":" +preferB+"]";
    }
    
    
}