package ca.draconic.vote;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.NoSuchElementException;

import org.apache.commons.math3.fraction.Fraction;
import org.apache.commons.math3.fraction.FractionField;
import org.apache.commons.math3.linear.FieldMatrix;
import org.apache.commons.math3.linear.MatrixUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class PreferenceMatrixTest {
    
    static FieldMatrix<Fraction> matrix3x3_empty;
    static FieldMatrix<Fraction> matrix3x3_valuesOnDiagonal;
    
    static FieldMatrix<Fraction> matrix3x3_test1;
    
    @BeforeAll
    public static void testMatrices() {
        matrix3x3_empty = MatrixUtils.createFieldMatrix(FractionField.getInstance(), 3,3);
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
}
