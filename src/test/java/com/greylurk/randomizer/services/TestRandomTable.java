package com.greylurk.randomizer.services;

import static org.hamcrest.Matchers.isIn;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.Test;

public class TestRandomTable {
    private Map<String, Object> buildSampleData() throws Exception {
        ClassLoader classLoader = TestRandomTable.class.getClassLoader();
        TypeReference<HashMap<String,Object>> outputType = new TypeReference<HashMap<String,Object>>() {};
        Map<String, Object> table = new ObjectMapper().readValue(classLoader.getResourceAsStream("testData.json"), outputType);
        return table;
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testRandomTable() throws Exception {
        Map<String, Object> data = buildSampleData();
        RandomTable table = new RandomTable(data);

        String result = table.expandValue("dice-table");
        assertThat(result, isIn(((Map<String,String>)data.get("dice-table")).values()));

    }

    @Test
    @SuppressWarnings("unchecked")
    public void testRandomList() throws Exception {
        Map<String, Object> data = buildSampleData();
        RandomTable table = new RandomTable(data);

        String result = table.expandValue("some-random-love");
        assertThat(result, isIn((List<String>)data.get("some-random-love")));

    }

    @Test
    public void testExpandString() throws Exception {
        RandomTable table = new RandomTable(buildSampleData());
        assertEquals(table.expandValue("what-we-love"), "We love Dungeons and Dragons");
    }

    @Test
    public void testSimpleString() throws Exception {
        RandomTable table = new RandomTable(buildSampleData());
        assertEquals(table.expandValue("dnd"), "Dungeons and Dragons");
    }
}