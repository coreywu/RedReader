/*******************************************************************************
 * This file is part of RedReader.
 *
 * RedReader is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * RedReader is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with RedReader.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/

package org.quantumbadger.redreader.reddit.prepared.markdown;

import java.util.ArrayList;

public final class MarkdownParser {

	public enum MarkdownParagraphType {
		TEXT, CODE, BULLET, NUMBERED, QUOTE, HEADER, HLINE, EMPTY, TABLE_DELIMITER, TABLE
	}

	public static MarkdownParagraphGroup parse(final char[] raw) {

		final CharArrSubstring[] rawLines = CharArrSubstring.generateFromLines(raw);

		final MarkdownLine[] lines = new MarkdownLine[rawLines.length];

		for(int i = 0; i < rawLines.length; i++) {
			lines[i] = MarkdownLine.generate(rawLines[i]);
		}

		final ArrayList<MarkdownLine> mergedLines = new ArrayList<MarkdownLine>(rawLines.length);
		MarkdownLine currentLine = null;

		for(int i = 0; i < lines.length; i++) {

			if(currentLine != null) {

				switch(lines[i].type) {
					case BULLET:
					case NUMBERED:
					case HEADER:
					case CODE:
					case HLINE:
					case QUOTE:

						mergedLines.add(currentLine);
						currentLine = lines[i];
						break;

					case EMPTY:
						mergedLines.add(currentLine);
						currentLine = null;
						break;

					case TABLE_DELIMITER:
						if (i > 0 && isValidTableDelimiter(lines[i].src.toString())) {
							// Build the entire table from delimiter, header and remaining lines

							currentLine = new MarkdownTableLine(currentLine);
							currentLine = ((MarkdownTableLine) currentLine).createTable(lines[i]);

							if (i + 1 < lines.length && lines[i + 1].src != null) {
								String line = lines[i + 1].src.toString();
								while (line.contains("|") && i < lines.length - 2) {
									currentLine = ((MarkdownTableLine) currentLine).createTable(lines[i + 1]);
									i++;
									if (lines[i + 1].src == null) break;
									line = lines[i + 1].src.toString();
								}
								i++;
							}

							currentLine = ((MarkdownTableLine) currentLine).normalizeTable();
							mergedLines.add(currentLine);
							currentLine = null;

							break;
						}
						// Else, treat the line as normal text

					case TEXT:

						if(i < 1) {
							throw new RuntimeException("Internal error: invalid paragrapher state");
						}

						switch(lines[i - 1].type) {
							case QUOTE:
							case BULLET:
							case NUMBERED:
							case TEXT:

								if(lines[i - 1].spacesAtEnd >= 2) {
									mergedLines.add(currentLine);
									currentLine = lines[i];

								} else {
									currentLine = currentLine.rejoin(lines[i]);
								}
								break;

							case CODE:
							case HEADER:
							case HLINE:
								mergedLines.add(currentLine);
								currentLine = lines[i];
								break;
						}

						break;

				}
			} else if(lines[i].type != MarkdownParagraphType.EMPTY) {
				currentLine = lines[i];
			}
		}

		if(currentLine != null) {
			mergedLines.add(currentLine);
		}

		final ArrayList<MarkdownParagraph> outputParagraphs = new ArrayList<MarkdownParagraph>(mergedLines.size());

		for(final MarkdownLine line : mergedLines) {
			final MarkdownParagraph lastParagraph = outputParagraphs.isEmpty() ? null : outputParagraphs.get(outputParagraphs.size() - 1);
			final MarkdownParagraph paragraph = line.tokenize(lastParagraph);
			if(!paragraph.isEmpty()) outputParagraphs.add(paragraph);
		}

		return new MarkdownParagraphGroup(outputParagraphs.toArray(new MarkdownParagraph[outputParagraphs.size()]));
	}

	public static boolean isValidTableDelimiter(String line) {
		if (line.length() == 1) {
			return false;
		} else if (line.contains("||")) {
			return false;
		} else if (line.contains("::-") || line.contains("-::") || line.contains(":::")) {
			return false;
		} else if (line.contains("- -")) {
			return false;
		}

		String[] splitLines = line.split("\\|");

		for (String splitLine : splitLines) {
			if (countCharInString(splitLine, ':') > 2) {
				return false;
			}
		}

		return true;
	}

	public static int countCharInString(String string, char c) {
		return string.length() - string.replaceAll(String.valueOf(c), "").length();
	}
}
