package net.explorviz.discoveryagent.util;

import com.github.jasminb.jsonapi.ResourceConverter;
import com.github.jasminb.jsonapi.SerializationFeature;
import net.explorviz.discoveryagent.services.TypeService;
import org.glassfish.hk2.api.Factory;

public class ResourceConverterFactory implements Factory<ResourceConverter> {
  private final ResourceConverter converter;

  public ResourceConverterFactory() {
    final ResourceConverter resourceConverter = new ResourceConverter();

    TypeService.typeMap.forEach((name, classType) -> {
      resourceConverter.registerType(classType);
    });

    this.converter = resourceConverter;
    this.converter.enableSerializationOption(SerializationFeature.INCLUDE_RELATIONSHIP_ATTRIBUTES);
  }

  @Override
  public void dispose(final ResourceConverter arg0) {
    // Nothing to dispose
  }

  @Override
  public ResourceConverter provide() {
    return this.converter;
  }
}
