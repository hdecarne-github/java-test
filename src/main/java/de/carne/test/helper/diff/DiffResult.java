/*
 * Copyright (c) 2018-2022 Holger de Carne and contributors, All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.carne.test.helper.diff;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jdt.annotation.Nullable;

import de.carne.util.Strings;

/**
 * A diff result.
 *
 * @param <T> actual type of the diffed values.
 */
public final class DiffResult<T> implements Iterable<DiffEntry<T>> {

	private static final DiffResult<Character> CHARACTER_MATCH = new DiffResult<>(Collections.emptyList(), true);
	private static final DiffResult<String> LINE_MATCH = new DiffResult<>(Collections.emptyList(), true);

	private final List<DiffEntry<T>> diffs;
	private final boolean restrained;

	DiffResult(List<DiffEntry<T>> diffs, boolean restrained) {
		this.diffs = diffs;
		this.restrained = restrained;
	}

	/**
	 * Gets the {@linkplain DiffResult} instance representing a match for character based diff.
	 *
	 * @return the {@linkplain DiffResult} instance representing a match for character based diff.
	 */
	public static DiffResult<Character> characterMatch() {
		return CHARACTER_MATCH;
	}

	/**
	 * Gets the {@linkplain DiffResult} instance representing a match for line based diff.
	 *
	 * @return the {@linkplain DiffResult} instance representing a match for line based diff.
	 */
	public static DiffResult<String> lineMatch() {
		return LINE_MATCH;
	}

	/**
	 * Checks whether this result instance contains all diffs or only a subset in case the diffs exceeded the diff
	 * range.
	 *
	 * @return {@code true} this result instance contains all diffs. {@code false} if the number diffs exceeded the diff
	 * range.
	 */
	public boolean isRestrained() {
		return this.restrained;
	}

	@Override
	public Iterator<DiffEntry<T>> iterator() {
		return this.diffs.iterator();
	}

	/**
	 * Gets the number entries in this result instance.
	 *
	 * @return the number entries in this result instance.
	 */
	public int size() {
		return this.diffs.size();
	}

	/**
	 * Gets the entry at the given position in this result instance.
	 *
	 * @param index the position of the entry to get.
	 * @return the entry at the given position in this result instance.
	 */
	public DiffEntry<T> entryAt(int index) {
		return this.diffs.get(index);
	}

	@Override
	public int hashCode() {
		return this.diffs.hashCode();
	}

	@Override
	public boolean equals(@Nullable Object obj) {
		return obj instanceof DiffResult<?> && this.diffs.equals(((DiffResult<?>) obj).diffs);
	}

	@Override
	public String toString() {
		StringWriter buffer = new StringWriter();
		PrintWriter writer = new PrintWriter(buffer);
		Deque<DiffEntry<T>> previousEntries = new LinkedList<>();

		for (DiffEntry<T> entry : this) {
			DiffEntry<T> previousEntry = previousEntries.peekLast();

			if (previousEntry != null && (entry.position() - previousEntry.position()) > 1) {
				toStringHelper(writer, previousEntries);
				previousEntries.clear();
			}
			previousEntries.add(entry);
		}
		if (!previousEntries.isEmpty()) {
			toStringHelper(writer, previousEntries);
		}
		if (!this.restrained) {
			writer.println("...");
		}
		writer.flush();
		return buffer.toString();
	}

	@SuppressWarnings("null")
	private void toStringHelper(PrintWriter writer, Deque<DiffEntry<T>> previousEntries) {
		int deleteCount = 0;
		int insertCount = 0;

		writer.println("@" + previousEntries.peekFirst().position());
		for (DiffEntry<T> previousEntry : previousEntries) {
			if (previousEntry.type() == DiffEntry.Type.DELETE) {
				writer.println("< " + Strings.encode(previousEntry.value().toString()));
				deleteCount++;
			}
		}
		for (DiffEntry<T> previousEntry : previousEntries) {
			if (previousEntry.type() == DiffEntry.Type.INSERT) {
				if (insertCount == 0 && deleteCount > 0) {
					writer.println("---");
				}
				writer.println("> " + Strings.encode(previousEntry.value().toString()));
				insertCount++;
			}
		}
	}

}
