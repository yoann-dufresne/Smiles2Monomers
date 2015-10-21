package io.parser;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import io.parsers.CSVParser;

import java.util.Map;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class CSVParserTests {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void parseLineCorrect() {
		String[] names = {"id", "value", "opt"};
		String line = "a;4;null";
		
		Map<String, String> parsingResult = null;
		try {
			parsingResult = CSVParser.parseLine(names, line);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		Assert.assertEquals(3, parsingResult.size());
		
		Assert.assertEquals("a", parsingResult.get("id"));
		Assert.assertEquals("4", parsingResult.get("value"));
		Assert.assertEquals("null", parsingResult.get("opt"));
	}
	
	@Test
	public void parseLineIncorrect() {
		String[] names = {"id", "value", "opt"};
		String line = "a;4;null;n";
		
		try {
			@SuppressWarnings("unused")
			Map<String, String> parsingResult = CSVParser.parseLine(names, line);
			fail("InvalidFormatException not thrown.");
		} catch (Exception e) {
			assertNotNull(e);
		}
	}

}
