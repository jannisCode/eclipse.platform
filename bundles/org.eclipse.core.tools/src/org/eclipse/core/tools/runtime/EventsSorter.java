/**********************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.tools.runtime;

import java.text.Collator;
import java.util.Arrays;
import java.util.Comparator;
import org.eclipse.core.runtime.PerformanceStats;
import org.eclipse.core.tools.ISorter;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

public class EventsSorter extends ViewerSorter implements ISorter {
	protected boolean reversed = false;
	protected int columnNumber;

	protected int[][] SORT_ORDERS_BY_COLUMN = {
			// Event
			{EventsView.COLUMN_FAILURES, EventsView.COLUMN_EVENT, EventsView.COLUMN_BLAME, EventsView.COLUMN_CONTEXT},
			// Blame
			{EventsView.COLUMN_FAILURES, EventsView.COLUMN_BLAME, EventsView.COLUMN_EVENT, EventsView.COLUMN_CONTEXT},
			// Context
			{EventsView.COLUMN_FAILURES, EventsView.COLUMN_CONTEXT, EventsView.COLUMN_EVENT, EventsView.COLUMN_BLAME},
			// Failures
			{EventsView.COLUMN_FAILURES, EventsView.COLUMN_EVENT, EventsView.COLUMN_BLAME, EventsView.COLUMN_CONTEXT},
			// Count
			{EventsView.COLUMN_FAILURES, EventsView.COLUMN_COUNT, EventsView.COLUMN_EVENT, EventsView.COLUMN_BLAME, EventsView.COLUMN_CONTEXT},
			// Time
			{EventsView.COLUMN_FAILURES, EventsView.COLUMN_TIME, EventsView.COLUMN_EVENT, EventsView.COLUMN_BLAME, EventsView.COLUMN_CONTEXT}};

	public EventsSorter(int columnNumber) {
		this.columnNumber = columnNumber;
	}

	/**
	 * Returns the number of the column by which this is sorting.
	 */
	public int getColumnNumber() {
		return columnNumber;
	}

	/**
	 * Returns true for descending, or false for ascending sorting order.
	 */
	public boolean isReversed() {
		return reversed;
	}

	/**
	 * Sets the sorting order.
	 */
	public void setReversed(boolean newReversed) {
		reversed = newReversed;
	}

	/*
	 * Overrides method from ViewerSorter
	 */
	public void sort(final Viewer viewer, Object[] elements) {
		Comparator comparator = new Comparator() {
			Collator c = Collator.getInstance();

			/**
			 * Compares two stats objects, sorting first by the main column of this sorter,
			 * then by subsequent columns, depending on the column sort order.
			 */
			public int compare(Object o1, Object o2) {
				PerformanceStats s1 = (PerformanceStats) o1;
				PerformanceStats s2 = (PerformanceStats) o2;
				int[] columnSortOrder = SORT_ORDERS_BY_COLUMN[columnNumber];
				int result = 0;
				for (int i = 0; i < columnSortOrder.length; ++i) {
					result = compareColumnValue(columnSortOrder[i], s1, s2);
					if (result != 0)
						break;
				}
				if (reversed)
					result = -result;
				return result;
			}

			/**
			 * Compares two markers, based only on the value of the specified column.
			 */
			int compareColumnValue(int column, PerformanceStats s1, PerformanceStats s2) {
				switch (column) {
					case EventsView.COLUMN_EVENT:
						return c.compare(s1.getEvent(), s2.getEvent());
					case EventsView.COLUMN_BLAME:
						return c.compare(s1.getBlameString(), s2.getBlameString());
					case EventsView.COLUMN_CONTEXT:
						String name1 = s1.getContext() == null ? "" : s1.getContext(); //$NON-NLS-1$
						String name2 = s2.getContext() == null ? "" : s2.getContext(); //$NON-NLS-1$
						return c.compare(name1, name2);
					case EventsView.COLUMN_FAILURES:
						return s2.getFailureCount() - s1.getFailureCount();
					case EventsView.COLUMN_COUNT:
						return s2.getRunCount() - s1.getRunCount();
					case EventsView.COLUMN_TIME:
						return (int)(s2.getRunningTime() - s1.getRunningTime());
				}
				return 0;
			}
		};
		Arrays.sort(elements, comparator);
	}

	public int states() {
		return 2;
	}
}