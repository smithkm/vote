package ca.draconic.vote;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.NoSuchElementException;

import org.apache.commons.math3.fraction.Fraction;
import org.apache.commons.math3.fraction.FractionField;
import org.apache.commons.math3.linear.Array2DRowFieldMatrix;
import org.junit.jupiter.api.Test;

public class PreferenceMatrixTest {
    
    @Test
    public void testEmpty() throws Exception {
        var options = Arrays.asList("A","B","C");
        var unit = new PreferenceMatrix<>(options, FractionField.getInstance());
        
        assertEquals(0, unit.getWins("A"));
        assertEquals(0, unit.getWins("B"));
        assertEquals(0, unit.getWins("C"));
    }
    
    @Test
    public void testMatrix() throws Exception {
        var options = Arrays.asList("A","B","C");
        var matrix = new Array2DRowFieldMatrix<>(FractionField.getInstance(), 3,3);
        matrix.setEntry(0, 1, new Fraction(42)); // 42 prefer A over B
        matrix.setEntry(1, 0, new Fraction(41)); // 41 prefer B over A
        var unit = new PreferenceMatrix<>(options, matrix);
        assertEquals(true, unit.isWin("A", "B"));
        assertEquals(false, unit.isWin("B", "A"));
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
