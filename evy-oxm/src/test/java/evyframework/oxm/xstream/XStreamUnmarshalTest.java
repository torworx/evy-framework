package evyframework.oxm.xstream;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class XStreamUnmarshalTest {
	
	private static final String INPUT_STRING = "<flight><flightNumber>42</flightNumber></flight>";

	private XStreamMarshaller marshaller;

	private Flight flight;

	@Before
	public void createMarshallerBuilder() {
		marshaller = new XStreamMarshallerBuilder().aliasClass(Flight.class,
				"flight").endClass().build();
		flight = new Flight();
		flight.setFlightNumber(42L);
	}

	private void testFlight(Object o) {
		assertTrue("Unmarshalled object is not Flights", o instanceof Flight);
		Flight flight = (Flight) o;
		assertNotNull("Flight is null", flight);
		assertEquals("Number is invalid", 42L, flight.getFlightNumber());
	}
	
	@Test
	public void unmarshalInputString() throws Exception {
		Object flights = marshaller.unmarshal(INPUT_STRING);
		testFlight(flights);
	}

	@Test
	public void unmarshalInputStream() throws Exception {
		InputStream source = new ByteArrayInputStream(INPUT_STRING.getBytes("UTF-8"));
		Object flights = marshaller.unmarshal(source);
		testFlight(flights);
	}

	@Test
	public void unmarshalReader() throws Exception {
		Reader source = new StringReader(INPUT_STRING);
		Object flights = marshaller.unmarshal(source);
		testFlight(flights);
	}
	
	@Test
	public void unmarshalInputStringWithRoot() throws Exception {
		Flight flight = new Flight();
		marshaller.unmarshal(INPUT_STRING, flight);
		testFlight(flight);
	}

	@Test
	public void unmarshalInputStreamWithRoot() throws Exception {
		InputStream source = new ByteArrayInputStream(INPUT_STRING.getBytes("UTF-8"));
		Flight flight = new Flight();
		marshaller.unmarshal(source, flight);
		testFlight(flight);
	}

	@Test
	public void unmarshalReaderWithRoot() throws Exception {
		Reader source = new StringReader(INPUT_STRING);
		Flight flight = new Flight();
		marshaller.unmarshal(source, flight);
		testFlight(flight);
	}

}
