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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import de.carne.boot.Exceptions;

/**
 * Utility class providing all kinds of diff operations.
 */
public final class Diff {

	private static final int TEXT_DIFFER_RANGE = 100;

	private Diff() {
		// Prevent instantiation
	}

	/**
	 * Diffs two strings character by character.
	 *
	 * @param string1 the 1st string to diff.
	 * @param string2 the 2nd string to diff.
	 * @return the diff result.
	 */
	public static DiffResult<Character> characters(String string1, String string2) {
		int string1Length = string1.length();
		int string2Length = string2.length();
		int range = Math.max(string1Length, string2Length);
		Differ<Character> differ = Differ.characterDiffer(range);

		for (int charIndex = 0; charIndex < string1Length; charIndex++) {
			differ.feedLeft(Character.valueOf(string1.charAt(charIndex)));
		}

		for (int charIndex = 0; charIndex < string2Length; charIndex++) {
			differ.feedRight(Character.valueOf(string2.charAt(charIndex)));
		}
		differ.run(true);
		return differ.toResult();
	}

	/**
	 * Diffs two files line by line
	 * <p>
	 * This function assumes the files are UTF-8 encoded.
	 * </p>
	 *
	 * @param file1 the 1st file to diff.
	 * @param file2 the 2nd file to diff.
	 * @return the diff result.
	 * @throws IOException if an I/O error occurs.
	 */
	public static DiffResult<String> lines(File file1, File file2) throws IOException {
		return lines(file1, file2, StandardCharsets.UTF_8);

	}

	/**
	 * Diffs two files line by line
	 *
	 * @param file1 the 1st file to diff.
	 * @param file2 the 2nd file to diff.
	 * @param cs the {@linkplain Charset} to use for file decoding.
	 * @return the diff result.
	 * @throws IOException if an I/O error occurs.
	 */
	public static DiffResult<String> lines(File file1, File file2, Charset cs) throws IOException {
		DiffResult<String> result;

		try (BufferedReader reader1 = newReader(file1, cs); BufferedReader reader2 = newReader(file2, cs)) {
			result = runDiffer(reader1, reader2);
		}
		return result;
	}

	private static BufferedReader newReader(File file, Charset cs) throws IOException {
		return new BufferedReader(new InputStreamReader(new FileInputStream(file), cs));
	}

	/**
	 * Diffs two strings line by line
	 *
	 * @param string1 the 1st string to diff.
	 * @param string2 the 2nd string to diff.
	 * @return the diff result.
	 */
	public static DiffResult<String> lines(String string1, String string2) {
		DiffResult<String> result;

		try (BufferedReader reader1 = newReader(string1); BufferedReader reader2 = newReader(string2)) {
			result = runDiffer(reader1, reader2);
		} catch (IOException e) {
			throw Exceptions.toRuntime(e);
		}
		return result;
	}

	private static BufferedReader newReader(String string) {
		return new BufferedReader(new StringReader(string));
	}

	private static DiffResult<String> runDiffer(BufferedReader reader1, BufferedReader reader2) throws IOException {
		Differ<String> differ = Differ.lineDiffer(TEXT_DIFFER_RANGE);
		String reader1Line = reader1.readLine();
		String reader2Line = reader2.readLine();

		while (differ.isRestrained() && (reader1Line != null || reader2Line != null)) {
			while (reader1Line != null) {
				String feedLine = reader1Line;

				reader1Line = reader1.readLine();
				if (!differ.feedLeft(feedLine)) {
					break;
				}
			}
			while (reader2Line != null) {
				String feedLine = reader2Line;

				reader2Line = reader2.readLine();
				if (!differ.feedRight(feedLine)) {
					break;
				}
			}
			differ.run(false);
		}
		differ.run(true);
		return differ.toResult();
	}

}
