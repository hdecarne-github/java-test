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
package de.carne.test.mock;

import java.util.function.Function;
import java.util.function.Supplier;

import org.mockito.ScopedMock;

/**
 * Base class used initialize a {@linkplain ScopedMock} instance and associate it to an actual mock instance.
 *
 * @param <M> the actual {@linkplain ScopedMock} derived mock type.
 * @param <T> the type of the associated mock instance.
 */
public abstract class ScopedMockInstance<M extends ScopedMock, T> implements Supplier<T>, AutoCloseable {

	private final M mock;
	private final T instance;

	protected ScopedMockInstance(Function<T, M> initializer, T instance) {
		this.mock = initializer.apply(instance);
		this.instance = instance;
	}

	@Override
	public T get() {
		return this.instance;
	}

	@Override
	public void close() {
		this.mock.close();
	}

}
