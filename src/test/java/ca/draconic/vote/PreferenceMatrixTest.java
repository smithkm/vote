package ca.draconic.vote;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.math3.fraction.Fraction;
import org.apache.commons.math3.fraction.FractionField;
import org.apache.commons.math3.linear.FieldMatrix;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.util.Pair;
import org.junit.jupiter.api.Assertions;
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
    static FieldMatrix<Fraction> matrix3x3_test1_margins;
    static FieldMatrix<Fraction> matrix5x5_test2;
    static FieldMatrix<Fraction> matrix5x5_test2_beatpath;
    static int[][] array3x3_test1;
    
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
        array3x3_test1 = new int[][] {
            {0, 42, 9},
            {41, 0, 5},
            {9, 100, 0},
        };
        matrix3x3_test1_margins = MatrixUtils.createFieldMatrix(new Fraction[][] {
            {f(0), f(1), f(0)},
            {f(-1), f(0), f(-95)},
            {f(0), f(95), f(0)},
        });
        matrix3x3_valuesOnDiagonal = matrix3x3_test1.copy();
        matrix3x3_valuesOnDiagonal.setEntry(1, 1, Fraction.ONE);
        
        // Examples from the Wikipedia article https://en.wikipedia.org/wiki/Schulze_method
        matrix5x5_test2 = MatrixUtils.createFieldMatrix(new Fraction[][] {
            {f(0), f(20), f(26), f(30), f(22)},
            {f(25), f(0), f(16), f(33), f(18)},
            {f(19), f(29), f(0), f(17), f(24)},
            {f(15), f(12), f(28), f(0), f(14)},
            {f(23), f(27), f(21), f(31), f(0)},
        });
        matrix5x5_test2_beatpath = MatrixUtils.createFieldMatrix(new Fraction[][] {
            {f(0), f(28), f(28), f(30), f(24)},
            {f(25), f(0), f(28), f(33), f(24)},
            {f(25), f(29), f(0), f(29), f(24)},
            {f(25), f(28), f(28), f(0), f(24)},
            {f(25), f(28), f(28), f(31), f(0)},
        });
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
    public void testFromArray() throws Exception {
        var options = Arrays.asList("A","B","C");
        var unit = PreferenceMatrix.fromArray(options, array3x3_test1);
        
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
    
    @Test
    public void testBeatpath() throws Exception {
        var options = Arrays.asList("A","B","C","D","E");
        var unit = new PreferenceMatrix<>(options, matrix5x5_test2);
        var result = unit.beatPaths();
        
        assertEquals(matrix5x5_test2_beatpath, result.getData());
    }
    
    @Test
    public void testSortOptions() throws Exception {
        var options = Arrays.asList("A","B","C","D","E");
        var unit = new PreferenceMatrix<>(options, matrix5x5_test2_beatpath);
        
        var result = unit.optionsByPreference();
        Assertions.assertIterableEquals(List.of(
                Set.of("E"),
                Set.of("A"),
                Set.of("C"),
                Set.of("B"),
                Set.of("D")
                ), result);
    }
    
    @Test
    public void testMargins() throws Exception {
        var options = Arrays.asList("A","B","C");
        var unit = new PreferenceMatrix<>(options, matrix3x3_test1);
        var result = unit.margins();
        
        assertEquals(matrix3x3_test1_margins, result.getData());
    }

}
