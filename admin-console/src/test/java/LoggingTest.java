import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class LoggingTest {

	private Logger log = LoggerFactory.getLogger(LoggingTest.class);

	@Test
	public void test() {
		log.debug("DEBUG LOG");

		log.info("INFO LOG");

		log.warn("WARN LOG");
	}

}
