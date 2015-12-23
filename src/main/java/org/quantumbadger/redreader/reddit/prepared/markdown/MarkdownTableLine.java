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
import java.util.List;

public final class MarkdownTableLine extends MarkdownLine {

	private final List<Integer> rowEndIndices;

	MarkdownTableLine(CharArrSubstring src, MarkdownParser.MarkdownParagraphType type, int spacesAtStart, int spacesAtEnd,
					  int prefixLength, int level, int number, List<Integer> rowEndIndices) {
		super(src, type, spacesAtStart, spacesAtEnd, prefixLength, level, number);
		this.rowEndIndices = rowEndIndices;
	}

	MarkdownTableLine(MarkdownLine markdownLine, List<Integer> rowEndIndices) {
		super(markdownLine.src, markdownLine.type, markdownLine.spacesAtStart, markdownLine.spacesAtEnd,
				markdownLine.prefixLength, markdownLine.level, markdownLine.number);
		this.rowEndIndices = rowEndIndices;
		this.rowEndIndices.add(markdownLine.src.start + markdownLine.src.length);
	}

	MarkdownTableLine(MarkdownLine markdownLine) {
		super(markdownLine.src, markdownLine.type, markdownLine.spacesAtStart, markdownLine.spacesAtEnd,
				markdownLine.prefixLength, markdownLine.level, markdownLine.number);
		this.rowEndIndices = new ArrayList<>();
		this.rowEndIndices.add(markdownLine.src.start);
		this.rowEndIndices.add(markdownLine.src.start + markdownLine.src.length);
	}

	public MarkdownTableLine createTable(MarkdownLine toAppend) {
		rowEndIndices.add(toAppend.src.start + toAppend.src.length);
		return new MarkdownTableLine(src.rejoin(toAppend.src), MarkdownParser.MarkdownParagraphType.TABLE,
				spacesAtStart, toAppend.spacesAtEnd, prefixLength, level, number, rowEndIndices);
	}

	public MarkdownTableLine normalizeTable() {
		List<Integer> newRowEndIndices = new ArrayList<>();
		Integer start = rowEndIndices.get(0);
		for (Integer rowEndIndex : rowEndIndices) {
			newRowEndIndices.add(rowEndIndex - start);
		}
		return new MarkdownTableLine(this, newRowEndIndices);
	}

	public List<Integer> getRowEndIndices() {
		return rowEndIndices;
	}

	@Override
	public MarkdownParagraph tokenize(final MarkdownParagraph parent) {

		final CharArrSubstring cleanedSrc = prefixLength == 0 ? src : src.substring(prefixLength);

		return new MarkdownTableParagraph(cleanedSrc, parent, type, null, level, number, rowEndIndices);
	}

}
