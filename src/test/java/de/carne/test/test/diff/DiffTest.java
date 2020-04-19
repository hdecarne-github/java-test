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
package de.carne.test.test.diff;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import de.carne.test.api.io.TempDir;
import de.carne.test.diff.Diff;
import de.carne.test.diff.DiffResult;
import de.carne.test.extension.TempPathExtension;
import de.carne.test.io.RemoteTestFile;
import de.carne.test.io.TestFile;

/**
 * Test {@linkplain Diff} class.
 */
@ExtendWith(TempPathExtension.class)
class DiffTest {

	private static final String CHARACTERS_1A = "abcdefghijklmnopqrstuvwxyz";
	private static final String CHARACTERS_1B = "zbcdefghHijklmnopqrstuvwxya ABCD";

	private static final String FILE_1A = Objects.requireNonNull(DiffTest.class.getResource("DiffTest1a.txt"))
			.toExternalForm();
	private static final String FILE_1B = Objects.requireNonNull(DiffTest.class.getResource("DiffTest1b.txt"))
			.toExternalForm();
	private static final String FILE_1C = Objects.requireNonNull(DiffTest.class.getResource("DiffTest1c.txt"))
			.toExternalForm();

	@Test
	void testCharacterDiff() {
		DiffResult<Character> matchResult = Diff.characters(CHARACTERS_1A, CHARACTERS_1A);

		Assertions.assertEquals(DiffResult.characterMatch(), matchResult);

		DiffResult<Character> diffResult = Diff.characters(CHARACTERS_1A, CHARACTERS_1B);

		Assertions.assertEquals(10, diffResult.size());
		Assertions.assertEquals("0:+z", diffResult.entryAt(0).toString());
		Assertions.assertEquals("0:-a", diffResult.entryAt(1).toString());
		Assertions.assertEquals("8:+H", diffResult.entryAt(2).toString());
		Assertions.assertEquals("25:+a", diffResult.entryAt(3).toString());
		Assertions.assertEquals("25:+ ", diffResult.entryAt(4).toString());
		Assertions.assertEquals("25:+A", diffResult.entryAt(5).toString());
		Assertions.assertEquals("25:+B", diffResult.entryAt(6).toString());
		Assertions.assertEquals("25:+C", diffResult.entryAt(7).toString());
		Assertions.assertEquals("25:+D", diffResult.entryAt(8).toString());
		Assertions.assertEquals("25:-z", diffResult.entryAt(9).toString());
	}

	@Test
	void testFileDiff1(@TempDir Path tmpDir) throws IOException {
		TestFile file1a = new RemoteTestFile(tmpDir, FILE_1A);
		TestFile file1b = new RemoteTestFile(tmpDir, FILE_1B);
		TestFile file1c = new RemoteTestFile(tmpDir, FILE_1C);

		DiffResult<String> matchResult = Diff.lines(file1a.getFile(), file1a.getFile());

		Assertions.assertEquals(DiffResult.lineMatch(), matchResult);

		DiffResult<String> diffResult = Diff.lines(file1a.getFile(), file1b.getFile());

		Assertions.assertEquals(10, diffResult.size());
		Assertions.assertEquals("0:+1?", diffResult.entryAt(0).toString());
		Assertions.assertEquals("0:-1", diffResult.entryAt(1).toString());
		Assertions.assertEquals("98:+99?", diffResult.entryAt(2).toString());
		Assertions.assertEquals("98:+100?", diffResult.entryAt(3).toString());
		Assertions.assertEquals("98:+101?", diffResult.entryAt(4).toString());
		Assertions.assertEquals("98:+102?", diffResult.entryAt(5).toString());
		Assertions.assertEquals("98:-99", diffResult.entryAt(6).toString());
		Assertions.assertEquals("99:-100", diffResult.entryAt(7).toString());
		Assertions.assertEquals("100:-101", diffResult.entryAt(8).toString());
		Assertions.assertEquals("101:-102", diffResult.entryAt(9).toString());

		DiffResult<String> excessiveDiffResult = Diff.lines(file1a.getFile(), file1c.getFile());

		Assertions.assertFalse(excessiveDiffResult.isRestrained());
		Assertions.assertEquals(200, excessiveDiffResult.size());
	}

}
