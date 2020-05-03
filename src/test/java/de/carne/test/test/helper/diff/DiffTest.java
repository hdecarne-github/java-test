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
package de.carne.test.test.helper.diff;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import de.carne.test.annotation.io.TempDir;
import de.carne.test.extension.io.TempPathExtension;
import de.carne.test.helper.diff.Diff;
import de.carne.test.helper.diff.DiffResult;
import de.carne.test.helper.io.RemoteTestFile;
import de.carne.test.helper.io.TestFile;
import de.carne.util.logging.Log;

/**
 * Test {@linkplain Diff} class.
 */
@ExtendWith(TempPathExtension.class)
class DiffTest {

	private static final Log LOG = new Log();

	private static final String CHARACTERS_1A = "abcdefghijklmnopqrstuvwxyz";
	private static final String CHARACTERS_1B = "zbcdefghHijklmnopqrstuvwxya ABCD";

	private static final String FILE_1A = Objects.requireNonNull(DiffTest.class.getResource("DiffTest1a.txt"))
			.toExternalForm();
	private static final String FILE_1B = Objects.requireNonNull(DiffTest.class.getResource("DiffTest1b.txt"))
			.toExternalForm();
	private static final String FILE_1C = Objects.requireNonNull(DiffTest.class.getResource("DiffTest1c.txt"))
			.toExternalForm();
	private static final String FILE_1D = Objects.requireNonNull(DiffTest.class.getResource("DiffTest1d.txt"))
			.toExternalForm();

	@Test
	void testCharacterDiff() {
		DiffResult<Character> diffResult1 = diffCharacters("", "");

		Assertions.assertEquals(DiffResult.characterMatch(), diffResult1);

		DiffResult<Character> diffResult2 = diffCharacters(CHARACTERS_1A, "");

		Assertions.assertEquals(CHARACTERS_1A.length(), diffResult2.size());

		DiffResult<Character> diffResult3 = diffCharacters("", CHARACTERS_1A);

		Assertions.assertEquals(CHARACTERS_1A.length(), diffResult3.size());

		DiffResult<Character> diffResult4 = diffCharacters(CHARACTERS_1A, CHARACTERS_1A);

		Assertions.assertEquals(DiffResult.characterMatch(), diffResult4);

		DiffResult<Character> diffResult5 = diffCharacters(CHARACTERS_1A, CHARACTERS_1B);

		Assertions.assertEquals(10, diffResult5.size());

		Assertions.assertEquals("@0:+z", diffResult5.entryAt(0).toString());
		Assertions.assertEquals("@0:-a", diffResult5.entryAt(1).toString());
		Assertions.assertEquals("@8:+H", diffResult5.entryAt(2).toString());
		Assertions.assertEquals("@25:+a", diffResult5.entryAt(3).toString());
		Assertions.assertEquals("@25:+ ", diffResult5.entryAt(4).toString());
		Assertions.assertEquals("@25:+A", diffResult5.entryAt(5).toString());
		Assertions.assertEquals("@25:+B", diffResult5.entryAt(6).toString());
		Assertions.assertEquals("@25:+C", diffResult5.entryAt(7).toString());
		Assertions.assertEquals("@25:+D", diffResult5.entryAt(8).toString());
		Assertions.assertEquals("@25:-z", diffResult5.entryAt(9).toString());
	}

	private DiffResult<Character> diffCharacters(String string1, String string2) {
		LOG.info("Comparing \"{0}\" to \"{1}\"", string1, string2);

		DiffResult<Character> result = Diff.characters(string1, string2);

		LOG.info("Result:\n{0}", result);

		return result;
	}

	@Test
	void testFileDiff1(@TempDir Path tmpDir) throws IOException {
		TestFile file1a = new RemoteTestFile(tmpDir, FILE_1A);
		TestFile file1b = new RemoteTestFile(tmpDir, FILE_1B);
		TestFile file1c = new RemoteTestFile(tmpDir, FILE_1C);
		TestFile file1d = new RemoteTestFile(tmpDir, FILE_1D);

		DiffResult<String> diffResult1 = diffLines(file1a.getFile(), file1a.getFile());

		Assertions.assertEquals(DiffResult.lineMatch(), diffResult1);

		DiffResult<String> diffResult2 = diffLines(file1a.getFile(), file1b.getFile());

		Assertions.assertEquals(100, diffResult2.size());

		DiffResult<String> diffResult3 = diffLines(file1b.getFile(), file1a.getFile());

		Assertions.assertEquals(100, diffResult3.size());

		DiffResult<String> diffResult4 = diffLines(file1b.getFile(), file1b.getFile());

		Assertions.assertEquals(DiffResult.lineMatch(), diffResult4);

		DiffResult<String> diffResult5 = diffLines(file1b.getFile(), file1c.getFile());

		Assertions.assertEquals(10, diffResult5.size());
		Assertions.assertEquals("@0:+1?", diffResult5.entryAt(0).toString());
		Assertions.assertEquals("@0:-1", diffResult5.entryAt(1).toString());
		Assertions.assertEquals("@98:+100?", diffResult5.entryAt(3).toString());
		Assertions.assertEquals("@98:+99?", diffResult5.entryAt(2).toString());
		Assertions.assertEquals("@98:+101?", diffResult5.entryAt(4).toString());
		Assertions.assertEquals("@98:+102?", diffResult5.entryAt(5).toString());
		Assertions.assertEquals("@98:-99", diffResult5.entryAt(6).toString());
		Assertions.assertEquals("@99:-100", diffResult5.entryAt(7).toString());
		Assertions.assertEquals("@100:-101", diffResult5.entryAt(8).toString());
		Assertions.assertEquals("@101:-102", diffResult5.entryAt(9).toString());

		DiffResult<String> diffResult6 = diffLines(file1b.getFile(), file1d.getFile());

		Assertions.assertFalse(diffResult6.isRestrained());
		Assertions.assertEquals(200, diffResult6.size());
	}

	private DiffResult<String> diffLines(File file1, File file2) throws IOException {
		LOG.info("Comparing file \"{0}\" to \"{1}\"", file1, file2);

		DiffResult<String> result = Diff.lines(file1, file2);

		LOG.info("Result:\n{0}", result);

		return result;
	}

}
