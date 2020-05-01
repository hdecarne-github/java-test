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
package de.carne.test.helper.diff;

import java.util.LinkedList;
import java.util.Objects;

import org.eclipse.jdt.annotation.Nullable;

import de.carne.test.helper.diff.DiffEntry.Type;
import de.carne.util.Check;

class Differ<T> {

	private final int range;
	private final @Nullable T[] left;
	private final @Nullable T[] right;
	private int leftLength = 0;
	private int rightLength = 0;
	private boolean restrained = true;
	private int position = 0;
	private int maxMatchPosition = -1;
	private LinkedList<DiffEntry<T>> diffs = new LinkedList<>();
	private final int[] forwardTrace;
	private final int[] reverseTrace;

	private Differ(int range, @Nullable T[] left, @Nullable T[] right) {
		this.range = range;
		this.left = left;
		this.right = right;
		this.forwardTrace = new int[(this.range << 1) + 2];
		this.reverseTrace = new int[this.forwardTrace.length];
	}

	public static Differ<Character> characterDiffer(int range) {
		return new Differ<>(range, new @Nullable Character[range], new @Nullable Character[range]);
	}

	public static Differ<String> lineDiffer(int range) {
		return new Differ<>(range, new @Nullable String[range], new @Nullable String[range]);
	}

	public boolean isRestrained() {
		return this.restrained;
	}

	public boolean feedLeft(T entry) {
		Check.isTrue(this.leftLength < this.range);

		this.left[this.leftLength] = entry;
		this.leftLength++;
		return this.leftLength < this.range;
	}

	public boolean feedLeft(T[] entries) {
		for (T entry : entries) {
			if (!feedLeft(entry)) {
				break;
			}
		}
		return this.leftLength < this.range;
	}

	public boolean feedLeft(Iterable<T> entries) {
		for (T entry : entries) {
			if (!feedLeft(entry)) {
				break;
			}
		}
		return this.leftLength < this.range;
	}

	public boolean feedRight(T entry) {
		Check.isTrue(this.rightLength < this.range);

		this.right[this.rightLength] = entry;
		this.rightLength++;
		return this.rightLength < this.range;
	}

	public boolean feedRight(T[] entries) {
		for (T entry : entries) {
			if (!feedRight(entry)) {
				break;
			}
		}
		return this.rightLength < this.range;
	}

	public boolean feedRight(Iterable<T> entries) {
		for (T entry : entries) {
			if (!feedRight(entry)) {
				break;
			}
		}
		return this.rightLength < this.range;
	}

	public DiffResult<T> toResult() {
		return new DiffResult<>(this.diffs, isRestrained());
	}

	public void run(boolean finish) {
		if (this.restrained) {
			run(0, this.leftLength, 0, this.rightLength);
		}
		if (finish || this.maxMatchPosition < 0) {
			this.position += this.leftLength;
			this.leftLength = 0;
			this.rightLength = 0;
			this.restrained = this.maxMatchPosition >= 0;
		} else {
			DiffEntry<T> lastEntry;
			int leftRemaining = 0;
			int rightRemaining = 0;

			while ((lastEntry = this.diffs.peekLast()) != null && lastEntry.position() > this.maxMatchPosition) {
				if (lastEntry.type() == DiffEntry.Type.DELETE) {
					leftRemaining++;
				} else {
					rightRemaining++;
				}
				this.diffs.removeLast();
			}
			System.arraycopy(this.left, this.leftLength - leftRemaining, this.left, 0, leftRemaining);
			System.arraycopy(this.right, this.rightLength - rightRemaining, this.right, 0, rightRemaining);
			this.position += this.leftLength - leftRemaining;
			this.leftLength = leftRemaining;
			this.rightLength = leftRemaining;
			this.restrained = this.leftLength < this.range && this.rightLength < this.range;
		}
	}

	private void run(int leftStart, int leftEnd, int rightStart, int rightEnd) {
		Snake snake = findSnake(leftStart, leftEnd, rightStart, rightEnd);

		if (snake == null || (snake.start() == leftEnd && snake.diag() == leftEnd - rightEnd)
				|| (snake.end() == leftStart && snake.diag() == leftStart - rightStart)) {
			int l = leftStart;
			int r = rightStart;

			while (l < leftEnd || r < rightEnd) {
				if (l < leftEnd && r < rightEnd && lrEquals(l, r)) {
					l++;
					r++;
					this.maxMatchPosition = Math.max(this.maxMatchPosition, this.position + l);
				} else if (leftEnd - leftStart > rightEnd - rightStart) {
					delete(l);
					l++;
				} else {
					insert(l, r);
					r++;
				}
			}
		} else {
			run(leftStart, snake.start(), rightStart, snake.start() - snake.diag());
			run(snake.end(), leftEnd, snake.end() - snake.diag(), rightEnd);

			int matchCount = snake.end() - snake.start();

			if (matchCount > 0) {
				this.maxMatchPosition = Math.max(this.maxMatchPosition, this.position + matchCount);
			}
		}
	}

	private void delete(int l) {
		this.diffs.add(new DiffEntry<>(this.position + l, Type.DELETE, Objects.requireNonNull(this.left[l])));
	}

	private void insert(int l, int r) {
		this.diffs.add(new DiffEntry<>(this.position + l, Type.INSERT, Objects.requireNonNull(this.right[r])));
	}

	@SuppressWarnings("java:S3776")
	@Nullable
	private Snake findSnake(int leftStart, int leftEnd, int rightStart, int rightEnd) {
		Snake snake = null;
		int leftRange = leftEnd - leftStart;
		int rightRange = rightEnd - rightStart;

		if (leftRange > 0 && rightRange > 0) {
			int delta = leftRange - rightRange;
			int sum = leftRange + rightRange;
			int offset = (sum % 2 == 0 ? sum : sum + 1) >> 1;

			this.forwardTrace[1 + offset] = leftStart;
			this.reverseTrace[1 + offset] = leftEnd + 1;
			for (int d = 0; d <= offset && snake == null; d++) {
				for (int k = -d; k <= d && snake == null; k += 2) {
					int t = k + offset;

					if (k == -d || (k != d && this.forwardTrace[t - 1] < this.forwardTrace[t + 1])) {
						this.forwardTrace[t] = this.forwardTrace[t + 1];
					} else {
						this.forwardTrace[t] = this.forwardTrace[t - 1] + 1;
					}

					int l = this.forwardTrace[t];
					int r = l - leftStart + rightStart - k;

					while (l < leftEnd && r < rightEnd && lrEquals(l, r)) {
						l++;
						r++;
						this.forwardTrace[t] = l;
					}
					if (delta % 2 != 0 && delta - d <= k && k <= delta + d
							&& this.reverseTrace[t - delta] <= this.forwardTrace[t]) {
						snake = getSnake(this.reverseTrace[t - delta], k + leftStart - rightStart, leftEnd, rightEnd);
					}
				}
				for (int k = delta - d; k <= delta + d && snake == null; k += 2) {
					int t = k + offset - delta;

					if (k == delta - d || (k != delta + d && this.reverseTrace[t + 1] <= this.reverseTrace[t - 1])) {
						this.reverseTrace[t] = this.reverseTrace[t + 1] - 1;
					} else {
						this.reverseTrace[t] = this.reverseTrace[t - 1];
					}

					int l = this.reverseTrace[t] - 1;
					int r = l - leftStart + rightStart - k;

					while (l >= leftStart && r >= rightStart && lrEquals(l, r)) {
						this.reverseTrace[t] = l;
						l--;
						r--;
					}
					if (delta % 2 == 0 && -d <= k && k <= d && this.reverseTrace[t] <= this.forwardTrace[t + delta]) {
						snake = getSnake(this.reverseTrace[t], k + leftStart - rightStart, leftEnd, rightEnd);
					}
				}
			}
		}
		return snake;
	}

	private Snake getSnake(int start, int diag, int leftEnd, int rightEnd) {
		int end = start;

		while (end - diag < rightEnd && end < leftEnd && lrEquals(end, end - diag)) {
			end++;
		}
		return new Snake(start, end, diag);
	}

	private boolean lrEquals(int l, int r) {
		return Objects.requireNonNull(this.left[l]).equals(Objects.requireNonNull(this.right[r]));
	}

	private static final class Snake {

		private final int start;
		private final int end;
		private final int diag;

		Snake(int start, int end, int diag) {
			this.start = start;
			this.end = end;
			this.diag = diag;
		}

		public int start() {
			return this.start;
		}

		public int end() {
			return this.end;
		}

		public int diag() {
			return this.diag;
		}

		@Override
		public String toString() {
			return this.start + "-" + this.end + ":" + this.diag;
		}

	}

}
