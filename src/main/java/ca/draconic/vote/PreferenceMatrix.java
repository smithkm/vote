package ca.draconic.vote;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.math3.Field;
import org.apache.commons.math3.FieldElement;
import org.apache.commons.math3.fraction.Fraction;
import org.apache.commons.math3.linear.Array2DRowFieldMatrix;
import org.apache.commons.math3.linear.FieldMatrix;
import org.apache.commons.math3.linear.MatrixUtils;

public class PreferenceMatrix<Option, Count extends FieldElement<Count> & Comparable<Count>> {
    
    final List<Option> order;
    final Map<Option, Integer> index ;
    final FieldMatrix<Count> count;
    
    public PreferenceMatrix(Collection<Option> options, Field<Count> field) {
        this(options, new Array2DRowFieldMatrix<>(field, options.size(), options.size()));
    }
    
    public PreferenceMatrix(Collection<Option> options, FieldMatrix<Count> count) {
        order = List.copyOf(options);
        final int C = order.size();
        index=new HashMap<>(C);
        {
            int i=0;
            for(Option option : options) {
                index.put(option, i++);
            }
        }
        for(int i=0; i<C; i++) {
            if(!count.getEntry(i, i).equals(count.getField().getZero()))
                throw new IllegalArgumentException("Preference matrix must be 0 on the diagonal");
        }
        if(count.getColumnDimension()!=C || count.getRowDimension()!=C) {
            throw new IllegalArgumentException("Matrix must have dimensions equal to number of options");
        }
        this.count = count;
    }
    
    static <Option> PreferenceMatrix<Option,Fraction> fromArray(Collection<Option> options, int[][] count) {
        var converted = Arrays.stream(count)
            .map(Arrays::stream)
            .map(row->row
                    .mapToObj(Fraction::new)
                    .toArray(Fraction[]::new))
            .toArray(Fraction[][]::new);
        return new PreferenceMatrix<>(options, MatrixUtils.createFieldMatrix(converted));
    }
    
    public PreferencePair<Option, Count> get(Option optionA, Option optionB) {
        int i = getIndex(optionA);
        int j = getIndex(optionB);
        
        return get(optionA, optionB, i, j);
    }
    
    public PreferencePair<Option, Count> get(int i, int j) {
        Option optionA = getOption(i);
        Option optionB = getOption(j);
        
        return get(optionA, optionB, i, j);
    }
    
    private PreferencePair<Option, Count> get(Option optionA, Option optionB, int i, int j) {
        
        Count preferA = count.getEntry(i, j);
        Count preferB = count.getEntry(j, i);
        
        return new PreferencePair<Option, Count>(optionA, optionB, preferA, preferB);
    }
            
    private Stream<Integer> indexStream() {
        return IntStream.range(0, order.size())
                .mapToObj(i->i);
    }
    
    public Stream<PreferencePair<Option, Count>> stream(){
        return indexStream()
            .flatMap(i->indexStream()
                .filter(j->j!=i)
                .map(j->new PreferencePair<Option, Count>(
                    getOption(i),
                    getOption(j),
                    count.getEntry(i, j),
                    count.getEntry(j, i)
                )));
    }
    
    public Option getOption(int i) {
        try {
            return order.get(i);
        } catch (ArrayIndexOutOfBoundsException ex) {
            throw new NoSuchElementException(i+" is not a valid index");
        }
    }
    
    public int getIndex(Option option) {
        Integer i = index.get(option);
        if (Objects.isNull(i)) {
            throw new NoSuchElementException("Option "+option+" is unknown");
        }
        return i;
    }
    
    private Count min(Count a, Count b) {
        return a.compareTo(b)<0 ? a : b;
    }
    
    private Count max(Count a, Count b) {
        return a.compareTo(b)>0 ? a : b;
    }
    
    private Count ifBigger(Count a, Count b) {
        return a.compareTo(b)>0 ? a : a.getField().getZero();
    }
    
    public PreferenceMatrix<Option, Count> beatPaths() {
        final int C = order.size(); 
        var result = count.createMatrix(C, C);
        
        for(int i = 0; i<C; i++) {
            for(int j = 0; j<C; j++) {
                if(i!=j) {
                    result.setEntry(i, j, ifBigger(count.getEntry(i,j),count.getEntry(j,i)));
                }
            }
        }
        
        for(int i = 0; i<C; i++) {
            for(int j = 0; j<C; j++) {
                if(i!=j) {
                    for(int k = 0; k<C; k++) {
                        if(i!=k  && j!=k) {
                            result.setEntry(j,k, max(result.getEntry(j,k), min(result.getEntry(j,i), result.getEntry(i,k))));
                        }
                    }
                }
            }
        }
        return new PreferenceMatrix<Option, Count>(order, result);
    }
    
    
    
    public boolean isWin(int i, int j) {
        return count.getEntry(i,j).compareTo(count.getEntry(j,i))>0;
    }
    public boolean isWin(Option a, Option b) {
        int i = getIndex(a);
        int j = getIndex(b);
        return isWin(i,j);
    }
    
    private static 
    <Option, 
     Count extends FieldElement<Count> & Comparable<Count>,
     Ballot extends Ranking<Option> >
    FieldMatrix<Count> unweightedRankedBallotMatrix(Collection<Option> options, Ballot vote, Field<Count> field) {
        final int C = options.size();
        FieldMatrix<Count> result = new Array2DRowFieldMatrix<>(field, C, C);
        int i = -1;
        for(Option a: options) {
            i++;
            int j=-1;
            for(Option b: options) {
                j++;
                
                if (vote.rank(a, b).get()==Preference.A) {
                    result.addToEntry(i, j, field.getOne());
                }
            }
        }
        return result;
    }
    
    private static 
    <Option, 
     Count extends FieldElement<Count> & Comparable<Count>,
     Ballot extends Ranking<Option> & WeightedBallot<Count> > 
    FieldMatrix<Count> weightedRankedBallotMatrix(Collection<Option> options, Ballot vote, Field<Count> field) {
        FieldMatrix<Count> result = unweightedRankedBallotMatrix(options, vote, field);
        return result.scalarMultiply(vote.getWeight());
    }
    
    public static 
    <Option, 
     Count extends FieldElement<Count> & Comparable<Count>,
     Ballot extends Ranking<Option> & WeightedBallot<Count> > 
    PreferenceMatrix<Option, Count> weightedPreferential(Collection<Option> options, Collection<Ballot> votes, Field<Count> field) {
        final int C = options.size();
        FieldMatrix<Count> identity = new Array2DRowFieldMatrix<>(field, C, C);
        var matrix = votes.stream()
            .map(vote->weightedRankedBallotMatrix(options, vote, field))
            .reduce(identity,FieldMatrix::add);
        return new PreferenceMatrix<Option, Count>(options, matrix);
    }
    
    public FieldMatrix<Count> getData() {
        return count;
    }
}
