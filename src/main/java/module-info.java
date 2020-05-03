/**
 * module-info
 */
module de.carne.test {
	requires transitive org.junit.jupiter.api;

	requires de.carne;
	requires org.eclipse.jdt.annotation;
	requires org.junit.platform.commons;

	exports de.carne.test.annotation.io;
	exports de.carne.test.extension.io;
	exports de.carne.test.helper.diff;
	exports de.carne.test.helper.io;
}
