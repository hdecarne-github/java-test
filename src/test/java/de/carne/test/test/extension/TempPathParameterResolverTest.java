/*
 * Copyright (c) 2018-2019 Holger de Carne and contributors, All Rights Reserved.
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
package de.carne.test.test.extension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer.Alphanumeric;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;

import de.carne.nio.file.FileUtil;
import de.carne.test.extension.TempPath;
import de.carne.test.extension.TempPathParameterResolver;

/**
 * Test {@linkplain TempPathParameterResolver} class.
 */
@ExtendWith(TempPathParameterResolver.class)
@TestMethodOrder(Alphanumeric.class)
class TempPathParameterResolverTest {

	private static final String TEST_FILE1 = "file1.tmp";

	@Test
	void test1stAccess(TempPath tempPath) throws IOException {
		Path testFile1 = tempPath.get().resolve(TEST_FILE1);

		Assertions.assertFalse(Files.exists(testFile1, LinkOption.NOFOLLOW_LINKS));

		FileUtil.touch(testFile1);

		Assertions.assertTrue(Files.exists(testFile1, LinkOption.NOFOLLOW_LINKS));
	}

	@Test
	void test2ndtAccess(TempPath tempPath) {
		Path testFile1 = tempPath.get().resolve(TEST_FILE1);

		Assertions.assertTrue(Files.exists(testFile1, LinkOption.NOFOLLOW_LINKS));
	}

}
