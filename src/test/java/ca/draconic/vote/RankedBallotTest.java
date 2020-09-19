package ca.draconic.vote;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Map;

import org.apache.commons.math3.fraction.Fraction;
import org.apache.commons.math3.fraction.FractionField;
import org.junit.jupiter.api.Test;

public class RankedBallotTest {
    
    @Test
    public void testMap() {
        var rankings = Map.of(
                "A", 1,
                "B", 2,
                "C", 2
                );
        
        var ballot1 = new RankedBallot<>(rankings, FractionField.getInstance());
        
        assertEquals(Preference.A, ballot1.rank("A", "B").get());
        assertEquals(Preference.B, ballot1.rank("B", "A").get());
        assertEquals(Preference.A, ballot1.rank("A", "C").get());
        assertEquals(Preference.B, ballot1.rank("C", "A").get());
        assertEquals(Preference.NONE, ballot1.rank("B", "C").get());
        assertEquals(Preference.NONE, ballot1.rank("C", "B").get());
        assertEquals(Fraction.ONE, ballot1.getWeight());
        
        var ballot2 = new RankedBallot<>(rankings, Fraction.TWO);
        assertEquals(Fraction.TWO, ballot2.getWeight());
    }
    
    @Test
    public void testOrderedGroups() {
        var rankings = List.of(
                List.of("A"),
                List.of("B","C")
            );
        
        var ballot1 = new RankedBallot<>(rankings, FractionField.getInstance());
        
        assertEquals(Preference.A, ballot1.rank("A", "B").get());
        assertEquals(Preference.B, ballot1.rank("B", "A").get());
        assertEquals(Preference.A, ballot1.rank("A", "C").get());
        assertEquals(Preference.B, ballot1.rank("C", "A").get());
        assertEquals(Preference.NONE, ballot1.rank("B", "C").get());
        assertEquals(Preference.NONE, ballot1.rank("C", "B").get());
        assertEquals(Fraction.ONE, ballot1.getWeight());
        
        var ballot2 = new RankedBallot<>(rankings, Fraction.TWO);
        assertEquals(Fraction.TWO, ballot2.getWeight());
    }
    
    @Test
    public void testOrderedGroupsNotUnique() {
        var rankings = List.of(
            List.of("A","C"),
            List.of("B","C")
        );
        
        assertThrows(IllegalArgumentException.class, 
                ()->new RankedBallot<>(rankings, FractionField.getInstance()));
        
    }
    
    @Test
    public void testBuilder() {
        var builder = RankedBallot.builder(List.of("A", "B", "C"), FractionField.getInstance());
        
        var ballot = builder.ballot(List.of(1,2,2));
        
        assertEquals(Preference.A, ballot.rank("A", "B").get());
        assertEquals(Preference.B, ballot.rank("B", "A").get());
        assertEquals(Preference.A, ballot.rank("A", "C").get());
        assertEquals(Preference.B, ballot.rank("C", "A").get());
        assertEquals(Preference.NONE, ballot.rank("B", "C").get());
        assertEquals(Preference.NONE, ballot.rank("C", "B").get());
        assertEquals(Fraction.ONE, ballot.getWeight());
        
        var ballot2 = builder.ballot(List.of(-1,0,0), new Fraction(2));
        
        assertEquals(Preference.A, ballot2.rank("A", "B").get());
        assertEquals(Preference.B, ballot2.rank("B", "A").get());
        assertEquals(Preference.A, ballot2.rank("A", "C").get());
        assertEquals(Preference.B, ballot2.rank("C", "A").get());
        assertEquals(Preference.NONE, ballot2.rank("B", "C").get());
        assertEquals(Preference.NONE, ballot2.rank("C", "B").get());
        assertEquals(new Fraction(2), ballot2.getWeight());
    }
    
    @Test
    public void testBuilderSizeMissmatch() {
        var builder = RankedBallot.builder(List.of("A", "B", "C"), FractionField.getInstance());
        
        assertThrows(IllegalArgumentException.class, ()->builder.ballot(List.of(1,2)));
        assertThrows(IllegalArgumentException.class, ()->builder.ballot(List.of(1,2,3,4)));
    }
    
    @Test
    public void testBuilderNotUnique() {
        assertThrows(IllegalArgumentException.class, 
                ()->RankedBallot.builder(List.of("A", "B", "A"), FractionField.getInstance()));
    }
    
    @Test
    public void testChangeWeight() {
        var rankings = Map.of(
                "A", 1,
                "B", 2,
                "C", 2
                );
        
        var ballot = new RankedBallot<>(rankings, Fraction.TWO);
        ballot.multiply(Fraction.ONE_THIRD);
        assertEquals(Fraction.TWO_THIRDS, ballot.getWeight());
    }
}
