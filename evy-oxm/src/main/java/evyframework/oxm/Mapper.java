package evyframework.oxm;

public interface Mapper<T extends Configuration> {

	void configure(T configuration);
}
