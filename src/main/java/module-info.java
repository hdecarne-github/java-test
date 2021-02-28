/**
 * module-info
 */
module de.carne.test {
	requires transitive java.net.http;
	requires transitive org.eclipse.jdt.annotation;
	requires transitive org.junit.jupiter.api;

	requires de.carne;
	requires org.junit.platform.commons;
	requires org.mockito;

	exports de.carne.test.annotation.io;
	exports de.carne.test.extension.io;
	exports de.carne.test.helper.diff;
	exports de.carne.test.helper.io;
	exports de.carne.test.mock.net.http;
}
