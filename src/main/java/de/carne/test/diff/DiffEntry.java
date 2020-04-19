/*
 * Copyright (c) 2018-2020 Holger de Carne and contributors, All Rights Reserved.
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
package de.carne.test.diff;

import org.eclipse.jdt.annotation.NonNull;

import de.carne.util.Strings;

/**
 * A single diff.
 *
 * @param <T> actual type of the diffed values.
 */
public final class DiffEntry<T> {

	/**
	 * The type of diff.
	 */
	public enum Type {

		/**
		 * Entry has been deleted.
		 */
		DELETE,

		/**
		 * Entry has been inserted.
		 */
		INSERT

	}

	private final int position;
	private final Type type;
	private final @NonNull T value;

	DiffEntry(int position, Type type, @NonNull T value) {
		this.position = position;
		this.type = type;
		this.value = value;
	}

	/**
	 * Gets this entry's position.
	 *
	 * @return this entry's position.
	 */
	public int position() {
		return this.position;
	}

	/**
	 * Gets this entry's type.
	 *
	 * @return this entry's type.
	 */
	public Type type() {
		return this.type;
	}

	/**
	 * Gets this entry's value.
	 *
	 * @return this entry's value.
	 */
	public @NonNull T value() {
		return this.value;
	}

	@Override
	public String toString() {
		StringBuilder buffer = new StringBuilder();

		buffer.append(this.position).append(this.type == Type.DELETE ? ":-" : ":+");
		Strings.encode(buffer, this.value.toString());
		return buffer.toString();
	}

}
