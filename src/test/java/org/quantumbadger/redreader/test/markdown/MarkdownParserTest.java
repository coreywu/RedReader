package org.quantumbadger.redreader.test.markdown;

import org.junit.Ignore;
import org.junit.Test;
import org.quantumbadger.redreader.reddit.prepared.markdown.MarkdownParser;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MarkdownParserTest {

	@Test
	public void testTableRegex() {
		assertTrue(MarkdownParser.isValidTableDelimiter(":---|:---|--"));
		assertTrue(MarkdownParser.isValidTableDelimiter(":---:|:---:|:--:"));
		assertTrue(MarkdownParser.isValidTableDelimiter("---|---|--"));
		assertTrue(MarkdownParser.isValidTableDelimiter(":|:|:"));
		assertTrue(MarkdownParser.isValidTableDelimiter("-|-"));
		assertTrue(MarkdownParser.isValidTableDelimiter("-|-|"));
		assertTrue(MarkdownParser.isValidTableDelimiter(":---|:---|"));
		assertTrue(MarkdownParser.isValidTableDelimiter(":-|"));
		assertTrue(MarkdownParser.isValidTableDelimiter("|--|--"));
		assertTrue(MarkdownParser.isValidTableDelimiter("|--|:--"));
		assertTrue(MarkdownParser.isValidTableDelimiter("|--|:--:|--"));
		assertTrue(MarkdownParser.isValidTableDelimiter("-|"));
		assertTrue(MarkdownParser.isValidTableDelimiter("|:-:|:-:|:-:|:-:|:-:|"));
		assertTrue(MarkdownParser.isValidTableDelimiter(":---: |"));
		assertTrue(MarkdownParser.isValidTableDelimiter("---| ---"));
		assertTrue(MarkdownParser.isValidTableDelimiter("---|   ---"));
		assertTrue(MarkdownParser.isValidTableDelimiter("---|---  "));
		assertTrue(MarkdownParser.isValidTableDelimiter("---|---  |"));
		assertTrue(MarkdownParser.isValidTableDelimiter("---|---  |  --"));
		assertFalse(MarkdownParser.isValidTableDelimiter("--||--"));
		assertFalse(MarkdownParser.isValidTableDelimiter("|"));
		assertFalse(MarkdownParser.isValidTableDelimiter("||--"));
		assertFalse(MarkdownParser.isValidTableDelimiter(":-:-:|:---:|:--:"));
		assertFalse(MarkdownParser.isValidTableDelimiter(":---:|:-:-:|:--:"));
		assertFalse(MarkdownParser.isValidTableDelimiter(":---:|:---:|::-:"));
		assertFalse(MarkdownParser.isValidTableDelimiter(":---:|:---:|:-::"));
		assertFalse(MarkdownParser.isValidTableDelimiter("-- -|--"));
	}

	@Ignore
	@Test
	public void testSimpleTable() {
		char[] input = ("Player | 1st OHL Season | 2nd | 3rd\n" +
						"---|---|----|----|\n" +
						"Marner | 59P / 64GP | 126P / 63GP | 137P / 64GP (on pace)\n" +
						"Monahan| 47P / 65GP | 78P / 62GP | 78P / 58GP\n" +
						"Hall | 84P / 63GP | 90P / 63GP | 106P / 57GP \n" +
						"Seguin | 67P / 61GP | 106P / 63GP | N/A").toCharArray();

		System.out.println(MarkdownParser.parse(input));
	}

	@Ignore
	@Test
	public void testComplexTable() {
		char[] input = ("| Team | Time | Player | Penalty (Time) | Streamable\n" +
						"|:-:|:-:|:-:|:-:|:-:|\n" +
                        "| **1st** | **Period** |\n" +
                        "[](/r/leafs) | 08:58 | Jake Gardiner |  Hi-sticking (2 min) | -\n" +
                        "[](/r/coloradoavalanche) | 12:59 | Francois Beauchemin |  Tripping (2 min) | -\n" +
                        "[](/r/coloradoavalanche) | 16:50 | Blake Comeau |  Hooking (2 min) | -\n" +
                        "		| **2nd** | **Period** |\n" +
                        "[](/r/leafs) | 02:07 | Brad Boyes |  Hi-sticking (2 min) | -\n" +
                        "[](/r/coloradoavalanche) | 12:03 | Alex Tanguay |  Holding (2 min) | -\n" +
                        "[](/r/leafs) | 17:06 | Matt Hunwick |  Slashing (2 min) | -\n" +
                        "		| **3rd** | **Period** |\n" +
                        "[](/r/leafs) | 02:01 | Leo Komarov |  Slashing (2 min) | -\n").toCharArray();

		System.out.println(MarkdownParser.parse(input));
	}

	@Ignore
	@Test
	public void testComplexTable2() {
		char[] input = ("| 2016 World Junior Classic\n" +
				":---: |\n" +
				"**Location**: Helsinki, Finland |\n" +
				"**Tournament Length**: December 26th - January 5th |\n" +
				"**Participating Nations**: [](/r/canada) [](/r/denmark) [](/r/sweden) [](/r/switzerland) [](/r/usa) [](/r/belarus) [](/r/czech) [](/r/finland) [](/r/russia) [](/r/slovakia) |\n" +
				"**Arenas**: [Hartwall Arena - cap. 13,506](https://en.wikipedia.org/wiki/Hartwall_Arena) &amp; [Helsinki Ice Hall - cap 8,200](https://en.wikipedia.org/wiki/Helsinki_Ice_Hall) |").toCharArray();
		System.out.println(MarkdownParser.parse(input));
	}

	@Ignore
	@Test
	public void testMultipleTables() {
		char[] input = ("Stats Central|\n" +
						":---:|\n" +
						"\n" +
                        "Goals | | | | | |\n" +
                        ":--:|:--:|:--:|:--:|:--:|:--:|\n" +
                        "| 1 | 2 | 3 | OT | Total |\n" +
                        "[](/r/leafs)| 2 | 1 | 4 | - | 7|\n" +
                        "[](/r/coloradoavalanche) | 1 | 2 | 1 | - | 4 |\n" +
						"	\n" +
                        "		Shots | | | | | |\n" +
                        ":--:|:--:|:--:|:--:|:--:|:--:|\n" +
                        "| 1 | 2 | 3 | OT | Total\n" +
                        "[](/r/leafs)| 8 | 3 | 11 | - | 22 |\n" +
                        "[](/r/coloradoavalanche) | 7 | 7 | 14 | - | 28 |\n" +
						"\n" +
                        "[](/r/leafs) | Team Stats | [](/r/coloradoavalanche) |\n" +
                        ":--:|:--:|:--:\n" +
                        "3/3 | Power Plays | 1/4\n" +
                        "22 | Hits | 20\n" +
                        "31 | Faceoff Wins | 34\n" +
                        "1 | Giveaways | 4\n" +
                        "11 | Takeaways | 7\n" +
                        "8 | Blocked Shots | 16\n" +
                        "8 | Penalty Minutes | 6\n").toCharArray();

		System.out.println(MarkdownParser.parse(input));
	}
}
