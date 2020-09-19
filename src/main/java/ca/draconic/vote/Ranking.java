package ca.draconic.vote;

import java.util.Comparator;
import java.util.Optional;

/**
 * 
 * @author Kevin Smith <smithkm@draconic.ca>
 *
 * @param <Option>
 */
public interface Ranking<Option> extends Comparator<Option> {
    
    public Optional<Preference> rank(Option a, Option b);
    
    @Override
    default int compare(Option a, Option b) {
        return rank(a,b).get().comparison;
    }
}
