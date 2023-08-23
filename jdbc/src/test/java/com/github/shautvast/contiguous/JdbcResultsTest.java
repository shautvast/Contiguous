package com.github.shautvast.contiguous;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.when;

class JdbcResultsTest {

    @Test
    public void testListOfString() throws SQLException {
        ResultSet mockResults = Mockito.mock(ResultSet.class);
        when(mockResults.next()).thenReturn(true, false);
        when(mockResults.getObject(1)).thenReturn("Zaphod");

        List<String> presidents = JdbcResults.toList(mockResults, String.class);
        assertFalse(presidents.isEmpty());
        assertEquals(1, presidents.size());

        String president = presidents.get(0);
        assertEquals("Zaphod", president);

    }

    @Test
    public void testListOfBean() throws SQLException {
        ResultSet mockResults = Mockito.mock(ResultSet.class);
        when(mockResults.next()).thenReturn(true, false);
        // the shape of the result equals that of the result (name:String, age:int)
        when(mockResults.getObject("name")).thenReturn("Zaphod");
        when(mockResults.getObject("age")).thenReturn(42); // coincidence?

        List<President> presidents = JdbcResults.toList(mockResults, President.class);
        assertFalse(presidents.isEmpty());
        assertEquals(1, presidents.size());

        President president = presidents.get(0);
        assertEquals("Zaphod", president.getName());
        assertEquals(42, president.getAge());
    }

    @Test
    public void testNameMapping() throws SQLException {
        ResultSet mockResults = Mockito.mock(ResultSet.class);
        when(mockResults.next()).thenReturn(true, false);
        when(mockResults.getObject("name")).thenReturn("Trillian");
        when(mockResults.getObject("realName")).thenReturn("Tricia MacMillan");

        Map<String, String> nameMapping = Map.of("earthName", "realName");
        List<Scientist> scientists = JdbcResults.toList(mockResults, Scientist.class, nameMapping::get);

        assertFalse(scientists.isEmpty());
        assertEquals(1, scientists.size());

        Scientist scientist = scientists.get(0);
        assertEquals("Trillian", scientist.getName());
        assertEquals("Tricia MacMillan", scientist.getEarthName());
    }
}