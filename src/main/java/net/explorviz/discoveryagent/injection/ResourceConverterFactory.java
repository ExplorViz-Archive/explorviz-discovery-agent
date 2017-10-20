package net.explorviz.discoveryagent.injection;

import com.github.jasminb.jsonapi.ResourceConverter;
import com.github.jasminb.jsonapi.SerializationFeature;
import org.glassfish.hk2.api.Factory;
import net.explorviz.discoveryagent.process.Process;


public class ResourceConverterFactory implements Factory<ResourceConverter> {
  private ResourceConverter converter;
  
  public ResourceConverterFactory() {
    ResourceConverter resourceConverter = new ResourceConverter(Process.class);
    this.converter = resourceConverter;
    this.converter.enableSerializationOption(SerializationFeature.INCLUDE_RELATIONSHIP_ATTRIBUTES);
  }
  
  public void dispose(final ResourceConverter arg0) {
  }
  
  public ResourceConverter provide() {
    return this.converter;
  }
}
