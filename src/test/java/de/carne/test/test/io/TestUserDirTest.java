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
package de.carne.test.test.io;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import de.carne.test.io.TestUserDir;

/**
 * Test {@linkplain TestUserDir} class.
 */
class TestUserDirTest {

	@Test
	void testChangeUserDir() throws IOException {
		File initialUserDirFile = new File(TestUserDir.getUserDir());
		String testUserDir1 = "test1";
		String userDir1 = TestUserDir.changeUserDir(testUserDir1);
		String testUserDir2 = "./test2";
		String userDir2 = TestUserDir.changeUserDir(testUserDir2);
		String testUserDir3 = "/test3";
		String userDir3 = TestUserDir.changeUserDir(testUserDir3);

		Assertions.assertEquals(new File(initialUserDirFile, testUserDir1).getCanonicalPath(),
				new File(userDir1).getCanonicalPath());
		Assertions.assertEquals(new File(new File(initialUserDirFile, testUserDir1), testUserDir2).getCanonicalPath(),
				new File(userDir2).getCanonicalPath());
		Assertions.assertEquals(new File(testUserDir3).getCanonicalPath(), new File(userDir3).getCanonicalPath());
	}

}
