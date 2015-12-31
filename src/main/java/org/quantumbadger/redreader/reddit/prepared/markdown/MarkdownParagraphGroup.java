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

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Paint.Style;
import android.graphics.Typeface;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.UnderlineSpan;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.laurencedawson.activetextview.ActiveTextView;

import org.quantumbadger.redreader.R;
import org.quantumbadger.redreader.common.General;
import org.quantumbadger.redreader.views.LinkDetailsView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class MarkdownParagraphGroup {

	private final MarkdownParagraph[] paragraphs;

	public MarkdownParagraphGroup(final MarkdownParagraph[] paragraphs) {
		this.paragraphs = paragraphs;
	}

	@SuppressWarnings("deprecation")
	public ViewGroup buildView(final Activity activity, final Integer textColor, final Float textSize,
							   final boolean showLinkButtons) {

		final float dpScale = activity.getResources().getDisplayMetrics().density;

		final int paragraphSpacing = (int) (dpScale * 6);
		final int cellPadding = (int) (dpScale * 4);
		final int codeLineSpacing = (int) (dpScale * 3);
		final int quoteBarWidth = (int) (dpScale * 3);
		final int maxQuoteLevel = 5;

		final LinearLayout layout = new LinearLayout(activity);
		layout.setOrientation(android.widget.LinearLayout.VERTICAL);

		for(final MarkdownParagraph paragraph : paragraphs) {

			final ActiveTextView tv = new ActiveTextView(activity);
			tv.setText(paragraph.spanned);

			if(textColor != null) tv.setTextColor(textColor);
			if(textSize != null) tv.setTextSize(textSize);

			switch(paragraph.type) {

				case BULLET: {
					final LinearLayout bulletItem = new LinearLayout(activity);
					final int paddingPx = General.dpToPixels(activity, 6);
					bulletItem.setPadding(paddingPx, paddingPx, paddingPx, 0);

					final TextView bullet = new TextView(activity);
					bullet.setText("•   ");
					if(textSize != null) bullet.setTextSize(textSize);

					bulletItem.addView(bullet);
					bulletItem.addView(tv);

					layout.addView(bulletItem);

					((ViewGroup.MarginLayoutParams)bulletItem.getLayoutParams()).leftMargin
							= (int) (dpScale * (paragraph.level == 0 ? 12 : 24));

					break;
				}

				case NUMBERED: {
					final LinearLayout numberedItem = new LinearLayout(activity);
					final int paddingPx = General.dpToPixels(activity, 6);
					numberedItem.setPadding(paddingPx, paddingPx, paddingPx, 0);

					final TextView number = new TextView(activity);
					number.setText(paragraph.number + ".   ");
					if(textSize != null) number.setTextSize(textSize);

					numberedItem.addView(number);
					numberedItem.addView(tv);

					layout.addView(numberedItem);

					((ViewGroup.MarginLayoutParams)numberedItem.getLayoutParams()).leftMargin
							= (int) (dpScale * (paragraph.level == 0 ? 12 : 24));

					break;
				}

				case CODE:
					tv.setTypeface(General.getMonoTypeface(activity));
					tv.setText(paragraph.raw.arr, paragraph.raw.start, paragraph.raw.length);
					layout.addView(tv);

					if(paragraph.parent != null) {
						((ViewGroup.MarginLayoutParams) tv.getLayoutParams()).topMargin
								= paragraph.parent.type == MarkdownParser.MarkdownParagraphType.CODE
								? codeLineSpacing : paragraphSpacing;
					}

					((ViewGroup.MarginLayoutParams) tv.getLayoutParams()).leftMargin = (int) (dpScale * 6);
					break;

				case HEADER:
					final SpannableString underlinedText = new SpannableString(paragraph.spanned);
					underlinedText.setSpan(new UnderlineSpan(), 0, underlinedText.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
					tv.setText(underlinedText);
					layout.addView(tv);
					if(paragraph.parent != null) {
						((ViewGroup.MarginLayoutParams) tv.getLayoutParams()).topMargin = paragraphSpacing;
					}
					break;

				case HLINE: {

					final View hLine = new View(activity);
					layout.addView(hLine);
					final ViewGroup.MarginLayoutParams hLineParams = (ViewGroup.MarginLayoutParams) hLine.getLayoutParams();
					hLineParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
					hLineParams.height = (int) dpScale;
					hLineParams.setMargins((int)(dpScale * 15), paragraphSpacing, (int)(dpScale * 15), 0);
					hLine.setBackgroundColor(Color.rgb(128, 128, 128));
					break;
				}

				case QUOTE: {

					final LinearLayout quoteLayout = new LinearLayout(activity);

					for(int lvl = 0; lvl < Math.min(maxQuoteLevel, paragraph.level); lvl++) {
						final View quoteIndent = new View(activity);
						quoteLayout.addView(quoteIndent);
						quoteIndent.setBackgroundColor(Color.rgb(128, 128, 128));
						quoteIndent.getLayoutParams().width = quoteBarWidth;
						quoteIndent.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
						((ViewGroup.MarginLayoutParams)quoteIndent.getLayoutParams()).rightMargin = quoteBarWidth;
					}

					quoteLayout.addView(tv);
					layout.addView(quoteLayout);

					if(paragraph.parent != null) {
						if(paragraph.parent.type == MarkdownParser.MarkdownParagraphType.QUOTE) {
							((ViewGroup.MarginLayoutParams)tv.getLayoutParams()).topMargin = paragraphSpacing;
						} else {
							((ViewGroup.MarginLayoutParams)quoteLayout.getLayoutParams()).topMargin = paragraphSpacing;
						}
					}

					break;
				}

				case TABLE:
					// Get color for table cell borders
					TypedValue typedValue = new TypedValue();
					activity.getTheme().resolveAttribute(R.attr.rrCommentBodyCol, typedValue, true);
					int borderColor = typedValue.data;

					MarkdownTableParagraph table = (MarkdownTableParagraph) paragraph;
					List<Integer> rowEndIndices = table.getRowEndIndices();

					TableLayout tableLayout = new TableLayout(activity);

					// Get cell justification from delimiter
					String gravityString = table.raw.substring(
							rowEndIndices.get(1), rowEndIndices.get(2) - rowEndIndices.get(1)).toString();
					List<Integer> gravityList = getGravity(gravityString);

					TableRow tableHeader = new TableRow(activity);

					String header = table.raw.substring(0, rowEndIndices.get(1)).toString().trim();

					header = header.trim();
					if (header.charAt(0) == '|') {
						header = header.substring(1);
					}
					if (header.charAt(header.length() - 1) == '|') {
						header = header.substring(0, header.length() - 1);
					}

					String[] headerStrings = header.split("\\|");

					int columns = headerStrings.length;

					for (int i = 0; i < columns; i++) {
						TextView headerTextView = new TextView(activity);
						headerTextView.setText(headerStrings[i].trim());
						headerTextView.setTypeface(null, Typeface.BOLD);
						headerTextView.setGravity(gravityList.get(i));

						// Add border
						ShapeDrawable border = new ShapeDrawable(new RectShape());
						border.getPaint().setStyle(Style.STROKE);
						border.getPaint().setColor(borderColor);
						headerTextView.setBackgroundDrawable(border);
						headerTextView.setPadding(cellPadding, 0, cellPadding, 0);

						tableHeader.addView(headerTextView);
					}
					tableLayout.addView(tableHeader);

					for (int i = 2; i < rowEndIndices.size() - 2; i++) {
						TableRow tableRow = new TableRow(activity);
						String row;

						if (i < rowEndIndices.size() - 3) {
							row = table.raw.substring(
									rowEndIndices.get(i), rowEndIndices.get(i + 1) - rowEndIndices.get(i)).toString();
						} else {
							row = table.raw.substring(rowEndIndices.get(rowEndIndices.size() - 3),
									table.raw.length - rowEndIndices.get(rowEndIndices.size() - 3)).toString();
						}

						row = row.trim();
						if (row.charAt(0) == '|') {
							row = row.substring(1);
						}
						if (row.charAt(row.length() - 1) == '|') {
							row = row.substring(0, row.length() - 1);
						}

						String[] tableRowStrings = row.split("\\|");

						for (int j = 0; j < columns; j++) {
							TextView tableRowTextView = new TextView(activity);
							if (j < tableRowStrings.length) {
								tableRowTextView.setText(tableRowStrings[j].trim());
							}
							tableRowTextView.setGravity(gravityList.get(j));

							// Add border
							ShapeDrawable border = new ShapeDrawable(new RectShape());
							border.getPaint().setStyle(Style.STROKE);
							border.getPaint().setColor(borderColor);
							tableRowTextView.setBackgroundDrawable(border);
							tableRowTextView.setPadding(cellPadding, 0, cellPadding, 0);

							tableRow.addView(tableRowTextView);
						}
						tableLayout.addView(tableRow);
					}

					HorizontalScrollView horizontalScrollView = new HorizontalScrollView(activity);
					HorizontalScrollView.LayoutParams horizontalScrollLayoutParams = new HorizontalScrollView.LayoutParams(
							HorizontalScrollView.LayoutParams.WRAP_CONTENT, HorizontalScrollView.LayoutParams.WRAP_CONTENT);
					horizontalScrollLayoutParams.setMargins(0, paragraphSpacing, 0, 0);
					horizontalScrollView.setLayoutParams(horizontalScrollLayoutParams);

					horizontalScrollView.addView(tableLayout);

					layout.addView(horizontalScrollView);
					break;

				case TEXT:

					layout.addView(tv);
					if(paragraph.parent != null) {
						((ViewGroup.MarginLayoutParams) tv.getLayoutParams()).topMargin = paragraphSpacing;
					}

					break;

				case EMPTY:
					throw new RuntimeException("Internal error: empty paragraph when building view");
			}

			if(showLinkButtons) {
				for(final MarkdownParagraph.Link link : paragraph.links) {

					final LinkDetailsView ldv = new LinkDetailsView(activity, link.title, link.subtitle);
					layout.addView(ldv);

					final int linkMarginPx = Math.round(dpScale * 8);
					((LinearLayout.LayoutParams) ldv.getLayoutParams()).setMargins(0, linkMarginPx, 0, linkMarginPx);
					ldv.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;

					ldv.setOnClickListener(new View.OnClickListener() {
						public void onClick(View v) {
							link.onClicked(activity);
						}
					});
				}
			}
		}

		return layout;
	}

	private List<Integer> getGravity(String justificationString) {
		justificationString = justificationString.trim();
		if (justificationString.charAt(0) == '|') {
			justificationString = justificationString.substring(1);
		}
		if (justificationString.charAt(justificationString.length() - 1) == '|') {
			justificationString = justificationString.substring(0, justificationString.length() - 1);
		}
		List<Integer> gravityList = new ArrayList<>();
		for (String justification : justificationString.split("\\|")) {
			justification = justification.trim();
			if (MarkdownParser.countCharInString(justification, ':') == 2) {
				gravityList.add(Gravity.CENTER);
			} else if (justification.charAt(0) == ':' || MarkdownParser.countCharInString(justification, ':') == 0) {
				gravityList.add(Gravity.LEFT);
			} else {
				gravityList.add(Gravity.RIGHT);
			}
		}
		return gravityList;
	}

	@Override
	public String toString() {
		return "MarkdownParagraphGroup: [ paragraphs: " + Arrays.toString(paragraphs) + " ]";
	}
}
