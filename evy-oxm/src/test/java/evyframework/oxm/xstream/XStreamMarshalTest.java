package evyframework.oxm.xstream;

import static org.custommonkey.xmlunit.XMLAssert.*;

import java.io.ByteArrayOutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;

import com.thoughtworks.xstream.converters.extended.EncodedByteArrayConverter;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver;
import com.thoughtworks.xstream.io.json.JsonHierarchicalStreamDriver;
import com.thoughtworks.xstream.io.json.JsonWriter;
import com.thoughtworks.xstream.io.json.JsonWriter.Format;

public class XStreamMarshalTest {

	private static final String EXPECTED_STRING = "<flight><flightNumber>42</flightNumber></flight>";

	private XStreamMarshallerBuilder builder;

	private Flight flight;

	@Before
	public void createMarshallerBuilder() {
		builder = new XStreamMarshallerBuilder().aliasClass(Flight.class, "flight").endClass();
		flight = new Flight();
		flight.setFlightNumber(42L);
	}

	private XStreamMarshaller createSimpleMarshaller() {
		return builder.build();
	}

	@Test
	public void marshalString() throws Exception {
		XStreamMarshaller marshaller = createSimpleMarshaller();
		String result = marshaller.marshal(flight);
		assertXMLEqual("Marshaller writes invalid String", EXPECTED_STRING, result);
	}

	@Test
	public void marshalWriter() throws Exception {
		XStreamMarshaller marshaller = createSimpleMarshaller();
		StringWriter writer = new StringWriter();
		marshaller.marshal(flight, writer);
		assertXMLEqual("Marshaller writes invalid Writer", EXPECTED_STRING, writer.toString());
	}

	@Test
	public void marshalOutputStream() throws Exception {
		XStreamMarshaller marshaller = createSimpleMarshaller();
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		marshaller.marshal(flight, os);
		String s = new String(os.toByteArray(), "UTF-8");
		assertXMLEqual("Marshaller writes invalid OutputStream", EXPECTED_STRING, s);
	}

	@Test
	public void converters() throws Exception {
		XStreamMarshaller marshaller = builder.registerConverters(new EncodedByteArrayConverter()).build();
		byte[] buf = new byte[] { 0x1, 0x2 };
		Writer writer = new StringWriter();
		marshaller.marshal(buf, writer);
		assertXMLEqual("<byte-array>AQI=</byte-array>", writer.toString());
		Reader reader = new StringReader(writer.toString());
		byte[] bufResult = (byte[]) marshaller.unmarshal(reader);
		assertTrue("Invalid result", Arrays.equals(buf, bufResult));
	}

	@Test
	public void asAttributesForType() throws Exception {
		XStreamMarshaller marshaller = builder.asAttributeForTypes(Long.TYPE).build();
		Writer writer = new StringWriter();
		marshaller.marshal(flight, writer);
		String expected = "<flight flightNumber=\"42\" />";
		assertXMLEqual("Marshaller does not use attributes", expected, writer.toString());
	}

	@Test
	public void asAttributesForFieldAndType() throws Exception {
		XStreamMarshaller marshaller = builder.asAttribute("flightNumber", Long.TYPE).build();
		Writer writer = new StringWriter();
		marshaller.marshal(flight, writer);
		String expected = "<flight flightNumber=\"42\" />";
		assertXMLEqual("Marshaller does not use attributes", expected, writer.toString());
	}

	@Test
	public void asAttributesForField() throws Exception {
		builder.forClass(Flight.class).forField("flightNumber").asAttribute();
		XStreamMarshaller marshaller = builder.build();
		Writer writer = new StringWriter();
		marshaller.marshal(flight, writer);
		String expected = "<flight flightNumber=\"42\" />";
		assertXMLEqual("Marshaller does not use attributes", expected, writer.toString());
	}

	@Test
	public void aliasType() throws Exception {
		FlightSubclass flight = new FlightSubclass();
		flight.setFlightNumber(42);
		builder.aliasType(Flight.class, "flight");
		XStreamMarshaller marshaller = builder.build();

		Writer writer = new StringWriter();
		marshaller.marshal(flight, writer);
		assertXMLEqual("Marshaller does not use attributes", EXPECTED_STRING, writer.toString());
	}

	@Test
	public void aliasTypes() throws Exception {
		FlightSubclass flight = new FlightSubclass();
		flight.setFlightNumber(42);
		builder.aliasTypes(Collections.singletonMap(Flight.class, "flight"));
		XStreamMarshaller marshaller = builder.build();

		Writer writer = new StringWriter();
		marshaller.marshal(flight, writer);
		assertXMLEqual("Marshaller does not use attributes", EXPECTED_STRING, writer.toString());
	}

	@Test
	public void aliasTypeString() throws Exception {
		FlightSubclass flight = new FlightSubclass();
		flight.setFlightNumber(42);
		builder.aliasTypes(Collections.singletonMap(Flight.class.getName(), "flight"));
		XStreamMarshaller marshaller = builder.build();

		Writer writer = new StringWriter();
		marshaller.marshal(flight, writer);
		assertXMLEqual("Marshaller does not use attributes", EXPECTED_STRING, writer.toString());
	}

	@Test
	public void fieldAlias() throws Exception {
		builder.forClass(Flight.class).aliasField("flightNumber", "flightNo");
		XStreamMarshaller marshaller = builder.build();
		Writer writer = new StringWriter();
		marshaller.marshal(flight, writer);
		String expected = "<flight><flightNo>42</flightNo></flight>";
		assertXMLEqual("Marshaller does not use aliases", expected, writer.toString());
	}

	@Test
	public void fieldAliases() throws Exception {
		builder.forClass(Flight.class).aliasFields(Collections.singletonMap("flightNumber", "flightNo"));
		XStreamMarshaller marshaller = builder.build();
		Writer writer = new StringWriter();
		marshaller.marshal(flight, writer);
		String expected = "<flight><flightNo>42</flightNo></flight>";
		assertXMLEqual("Marshaller does not use aliases", expected, writer.toString());
	}

	@Test
	public void omitFields() throws Exception {
		builder.forClass(Flight.class).omitFields("flightNumber");
		XStreamMarshaller marshaller = builder.build();
		Writer writer = new StringWriter();
		marshaller.marshal(flight, writer);
		assertXpathNotExists("/flight/flightNumber", writer.toString());
	}

	@Test
	public void implicitCollections() throws Exception {
		Flights flights = new Flights();
		flights.getFlights().add(flight);
		flights.getStrings().add("42");

		builder.aliasClass(Flight.class, "flight");
		builder.aliasClass(Flights.class, "flights");
		builder.forClass(Flights.class).implicitCollections("flights", "strings");
		XStreamMarshaller marshaller = builder.build();

		Writer writer = new StringWriter();
		marshaller.marshal(flights, writer);
		String result = writer.toString();
		assertXpathNotExists("/flights/flights", result);
		assertXpathExists("/flights/flight", result);
		assertXpathNotExists("/flights/strings", result);
		assertXpathExists("/flights/string", result);
	}

	@Test
	public void jettisonDriver() throws Exception {
		XStreamMarshaller marshaller = builder.build();
		marshaller.setStreamDriver(new JettisonMappedXmlDriver());
		Writer writer = new StringWriter();
		marshaller.marshal(flight, writer);
		assertEquals("Invalid result", "{\"flight\":{\"flightNumber\":42}}", writer.toString());
		Object o = marshaller.unmarshal(new StringReader(writer.toString()));
		assertTrue("Unmarshalled object is not Flights", o instanceof Flight);
		Flight unflight = (Flight) o;
		assertNotNull("Flight is null", unflight);
		assertEquals("Number is invalid", 42L, unflight.getFlightNumber());
	}

	@Test
	public void jsonDriver() throws Exception {
		XStreamMarshaller marshaller = builder.build();
		marshaller.setStreamDriver(new JsonHierarchicalStreamDriver() {
			@Override
			public HierarchicalStreamWriter createWriter(Writer writer) {
				return new JsonWriter(writer, JsonWriter.DROP_ROOT_MODE, new Format(new char[0], "".toCharArray(),
						Format.SPACE_AFTER_LABEL | Format.COMPACT_EMPTY_ELEMENT));
			}
		});

		Writer writer = new StringWriter();
		marshaller.marshal(flight, writer);
		assertEquals("Invalid result", "{\"flightNumber\": 42}", writer.toString());
	}

	@Test
	public void annotatedMarshalStreamResultWriter() throws Exception {
		builder.annotatedClasses(Flight.class);
		XStreamMarshaller marshaller = builder.build();
		StringWriter writer = new StringWriter();
		Flight flight = new Flight();
		flight.setFlightNumber(42);
		marshaller.marshal(flight, writer);
		String expected = "<flight><number>42</number></flight>";
		assertXMLEqual("Marshaller writes invalid StreamResult", expected, writer.toString());
	}
	
	@Test
	public void mapper() throws Exception {
		XStreamMarshaller marshaller = new XStreamMarshallerBuilder().apply(new FlightMapper()).build();
		String result = marshaller.marshal(flight);
		assertXMLEqual("Marshaller writes invalid String", EXPECTED_STRING, result);

	}
}
