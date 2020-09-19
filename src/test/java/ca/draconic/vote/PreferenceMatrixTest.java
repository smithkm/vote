package ca.draconic.vote;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.apache.commons.math3.fraction.Fraction;
import org.apache.commons.math3.fraction.FractionField;
import org.apache.commons.math3.linear.FieldMatrix;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.util.Pair;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class PreferenceMatrixTest {
    
    static FieldMatrix<Fraction> matrix3x3_empty;
    static FieldMatrix<Fraction> matrix3x4_empty;
    static FieldMatrix<Fraction> matrix4x3_empty;
    static FieldMatrix<Fraction> matrix4x4_empty;
    static FieldMatrix<Fraction> matrix2x2_empty;
    static FieldMatrix<Fraction> matrix3x3_valuesOnDiagonal;
    
    static FieldMatrix<Fraction> matrix3x3_test1;
    
    @BeforeAll
    public static void testMatrices() {
        matrix3x3_empty = MatrixUtils.createFieldMatrix(FractionField.getInstance(), 3,3);
        matrix3x4_empty = MatrixUtils.createFieldMatrix(FractionField.getInstance(), 3,4);
        matrix4x3_empty = MatrixUtils.createFieldMatrix(FractionField.getInstance(), 4,3);
        matrix4x4_empty = MatrixUtils.createFieldMatrix(FractionField.getInstance(), 4,4);
        matrix2x2_empty = MatrixUtils.createFieldMatrix(FractionField.getInstance(), 2,2);
        matrix3x3_test1 = MatrixUtils.createFieldMatrix(new Fraction[][] {
            {f(0), f(42), f(9)},
            {f(41), f(0), f(5)},
            {f(9), f(100), f(0)},
        });
        matrix3x3_valuesOnDiagonal = matrix3x3_test1.copy();
        matrix3x3_valuesOnDiagonal.setEntry(1, 1, Fraction.ONE);
    }

    private static Fraction f(int x) {
        return new Fraction(x);
    }
    private static Fraction f(int n, int d) {
        return new Fraction(n,d);
    }
    
    @Test
    public void testEmpty() throws Exception {
        var options = Arrays.asList("A","B","C");
        var unit = new PreferenceMatrix<>(options, FractionField.getInstance());
        
        for(String a : options) {
            for (String b : options) {
                assertEquals(new PreferencePair<>(a, b, f(0), f(0)), unit.get(a, b));
                assertFalse(unit.isWin(a, b));
            }
        }
    }
    
    private void assertPair(String a, String b, Fraction ca, Fraction cb, PreferenceMatrix<String, Fraction> unit) {
        assertEquals(new PreferencePair<>(a, b, ca, cb), unit.get(a, b));
        assertEquals(new PreferencePair<>(b, a, cb, ca), unit.get(b, a));
    }
    
    @Test
    public void testMatrix() throws Exception {
        var options = Arrays.asList("A","B","C");
        var unit = new PreferenceMatrix<>(options, matrix3x3_test1);
        
        assertPair("A","B", f(42), f(41), unit);
        assertPair("A","C", f(9), f(9), unit);
        assertPair("B","C", f(5), f(100), unit);
        
        // A beats B
        assertEquals(true, unit.isWin("A", "B"));
        assertEquals(false, unit.isWin("B", "A"));
        
        // A ties with C
        assertEquals(false, unit.isWin("A", "C"));
        assertEquals(false, unit.isWin("C", "A"));
        
        // C beats B
        assertEquals(true, unit.isWin("C", "B"));
        assertEquals(false, unit.isWin("B", "C"));
        
    }
    
    @Test
    public void testMatrixHasVotesOnDiagonal() throws Exception {
        var options = Arrays.asList("A","B","C");
        assertThrows(IllegalArgumentException.class, 
                ()->new PreferenceMatrix<>(options, matrix3x3_valuesOnDiagonal));
    }
    
    @Test
    public void testMatrixDimensionsMatchOptions() throws Exception {
        var options = Arrays.asList("A","B","C");
        assertThrows(IllegalArgumentException.class, 
                ()->new PreferenceMatrix<>(options, matrix3x4_empty));
        assertThrows(IllegalArgumentException.class, 
                ()->new PreferenceMatrix<>(options, matrix4x3_empty));
        assertThrows(IllegalArgumentException.class, 
                ()->new PreferenceMatrix<>(options, matrix4x4_empty));
        assertThrows(IllegalArgumentException.class, 
                ()->new PreferenceMatrix<>(options, matrix2x2_empty));
    }
    
    @Test
    public void testGetIndex() throws Exception {
        var options = Arrays.asList("A","B","C");
        var unit = new PreferenceMatrix<>(options, FractionField.getInstance());
        
        assertEquals(0, unit.getIndex("A"));
        assertEquals(1, unit.getIndex("B"));
        assertEquals(2, unit.getIndex("C"));
        assertThrows(NoSuchElementException.class, ()->unit.getIndex("DOES NOT EXIST"));
    }
    
    @Test
    public void testGetOption() throws Exception {
        var options = Arrays.asList("A","B","C");
        var unit = new PreferenceMatrix<>(options, FractionField.getInstance());
        
        assertEquals("A", unit.getOption(0));
        assertEquals("B", unit.getOption(1));
        assertEquals("C", unit.getOption(2));
        assertThrows(NoSuchElementException.class, ()->unit.getOption(3));
        assertThrows(NoSuchElementException.class, ()->unit.getOption(-1));
    }
    
    @Test
    public void testWeightedPreferentialTallyWithoutWeight() throws Exception {
        var options = Arrays.asList("A","B","C");
        final var builder = RankedBallot.builder(options, FractionField.getInstance());
        var ballots = List.of(
                List.of(1,2,3),
                List.of(1,2,3),
                List.of(1,2,3),
                
                List.of(1,3,2),
                List.of(1,3,2),
                List.of(1,3,2),
                
                List.of(2,1,1),
                List.of(2,1,1)
                ).stream()
                .map(builder::ballot)
                .collect(Collectors.toList());
        var unit = PreferenceMatrix.weightedPreferential(options, ballots, FractionField.getInstance());
        
        assertPair("A","B", f(6), f(2), unit);
        assertPair("A","C", f(6), f(2), unit);
        assertPair("B","C", f(3), f(3), unit);
    }
    
    @Test
    public void testWeightedPreferentialTally() throws Exception {
        var options = Arrays.asList("A","B","C");
        final var builder = RankedBallot.builder(options, FractionField.getInstance());
        var ballots = List.of(
                new Pair<>(List.of(1,2,3), f(1)),
                new Pair<>(List.of(1,2,3), f(2)),
                new Pair<>(List.of(1,2,3), f(1)),
                
                new Pair<>(List.of(1,3,2), f(1)),
                new Pair<>(List.of(1,3,2), f(1)),
                new Pair<>(List.of(1,3,2), f(1)),
                
                new Pair<>(List.of(2,1,1), f(2)),
                new Pair<>(List.of(2,1,1), f(1))
                ).stream()
                .map(p->builder.ballot(p.getFirst(), p.getSecond()))
                .collect(Collectors.toList());
        var unit = PreferenceMatrix.weightedPreferential(options, ballots, FractionField.getInstance());
        
        assertPair("A","B", f(7), f(3), unit);
        assertPair("A","C", f(7), f(3), unit);
        assertPair("B","C", f(4), f(3), unit);
    }
}
