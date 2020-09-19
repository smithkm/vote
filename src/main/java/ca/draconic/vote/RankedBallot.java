package ca.draconic.vote;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.math3.Field;
import org.apache.commons.math3.FieldElement;
import org.apache.commons.math3.util.Pair;

/**
 * 
 * @author Kevin Smith <smithkm@draconic.ca>
 *
 */
public class RankedBallot<Option, Count extends FieldElement<Count> & Comparable<Count>>
    implements Ranking<Option>, WeightedBallot<Count> {
    
    Map<Option, Integer> ranking;
    private Count weight;
    
    /**
     * Takes a map of Options to rankings (lower numbers are more preferred)
     * @param ranking
     */
    public RankedBallot(Map<Option, Integer> ranking, Count weight) {
        this.ranking = Map.copyOf(ranking);
        this.weight = weight;
    }
    
    /**
     * Takes a map of Options to rankings (lower numbers are more preferred)
     * @param ranking
     */
    public RankedBallot(Map<Option, Integer> ranking, Field<Count> field) {
        this(ranking, field.getOne());
    }
    
    /**
     * Takes a list in order of decreasing preference. Each entry is a collection of equally preferred options.
     * @param ranking
     */
    public RankedBallot(List<? extends Collection<Option>> ranking, Field<Count> field) {
        this(toRankMap(ranking), field.getOne());
    }
    
    /**
     * Takes a list in order of decreasing preference. Each entry is a collection of equally preferred options.
     * @param ranking
     */
    public RankedBallot(List<? extends Collection<Option>> ranking, Count weight) {
        this(toRankMap(ranking), weight);
    }
    
    private static <Option> Map<Option, Integer> toRankMap(List<? extends Collection<Option>> ranking) {
        int r = 0;
        var result = new HashMap<Option, Integer>();
        for(var rank: ranking) {
            r++;
            for(Option option: rank) {
                if(result.putIfAbsent(option, r)!=null) {
                    throw new IllegalArgumentException("Option repeated in ranking");
                }
            }
        }
        return result;
    }
    
    @Override
    public Optional<Preference> rank(Option a, Option b) {
        var rankA = Optional.ofNullable(ranking.get(a));
        var rankB = Optional.ofNullable(ranking.get(b));
        return rankA.flatMap(x->rankB.map(y->Preference.fromRanks(x,y)));
    }

    @Override
    public Count getWeight() {
        return weight;
    }

    @Override
    public void multiply(Count multiplier) {
        this.weight=this.weight.multiply(multiplier);
    }
    
    private static <T, U> Iterable<Pair<T,U>> zip(Iterable<T> o1, Iterable<U> o2){
        return new Iterable<>() {

            @Override
            public Iterator<Pair<T, U>> iterator() {
                var i1 = o1.iterator();
                var i2 = o2.iterator();
                
                return new Iterator<>() {

                    @Override
                    public boolean hasNext() {
                        return i1.hasNext() && i2.hasNext();
                    }

                    @Override
                    public Pair<T, U> next() {
                        return new Pair<>(i1.next(), i2.next());
                    }
                    
                };
            }
            
        };
    }
    
    public static class Builder<Option, Count extends FieldElement<Count> & Comparable<Count>> {
        private final List<Option> options;
        private final Field<Count> field;

        private Builder(List<Option> options, Field<Count> field) {
            super();
            this.options = options;
            this.field = field;
        }
        
        public RankedBallot<Option, Count> ballot(List<Integer> ranks, Count weight) {
            if(ranks.size()!=options.size()) {
                throw new IllegalArgumentException("ranks must have same size as options");
            }
            var map = new HashMap<Option, Integer>();
            for(var pair: zip(options, ranks)) {
                map.put(pair.getKey(), pair.getValue());
            }
            return new RankedBallot<Option, Count>(map, weight);
        }
        
        public RankedBallot<Option, Count> ballot(List<Integer> ranks) {
            return ballot(ranks, field.getOne());
        }
    }
    
    public static <Option, Count extends FieldElement<Count> & Comparable<Count>> 
    Builder<Option, Count> builder(List<Option> options, Field<Count> field) {
        if(new HashSet<>(options).size()!=options.size())
            throw new IllegalArgumentException("Options must be unique");
        return new Builder<>(options, field);
    }
}
