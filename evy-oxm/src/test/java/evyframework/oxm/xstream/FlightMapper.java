package evyframework.oxm.xstream;

public class FlightMapper implements XStreamMapper {

	@Override
	public void configure(XStreamConfiguration<?> configurator) {
		configurator.aliasClass(Flight.class, "flight");
	}

}
