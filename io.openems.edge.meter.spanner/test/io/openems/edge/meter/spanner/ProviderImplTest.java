package io.openems.edge.meter.spanner;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import io.openems.edge.meter.spanner.ProviderImpl;

/*
 * Example JUNit test case
 *
 */

public class ProviderImplTest {

	/*
	 * Example test method
	 */

	@Test
	public void simple() {
		ProviderImpl impl = new ProviderImpl();
		assertNotNull(impl);
	}

}
