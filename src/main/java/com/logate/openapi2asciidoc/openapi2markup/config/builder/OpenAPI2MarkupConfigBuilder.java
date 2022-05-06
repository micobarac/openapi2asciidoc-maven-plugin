package com.logate.openapi2asciidoc.openapi2markup.config.builder;

import com.logate.openapi2asciidoc.openapi2markup.OpenAPI2MarkupProperties;
import com.logate.openapi2asciidoc.openapi2markup.OpenSchema2MarkupConfig;
import com.logate.openapi2asciidoc.swagger2markup.core.config.builder.Schema2MarkupConfigBuilder;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.ConfigurationConverter;
import org.apache.commons.configuration2.MapConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;

import java.util.Map;
import java.util.Properties;

public class OpenAPI2MarkupConfigBuilder extends Schema2MarkupConfigBuilder<OpenAPI2MarkupConfigBuilder, OpenSchema2MarkupConfig> {

    public OpenAPI2MarkupConfigBuilder() {
        this(new PropertiesConfiguration());
    }

    public OpenAPI2MarkupConfigBuilder(Properties properties) {
        this(ConfigurationConverter.getConfiguration(properties));
    }

    public OpenAPI2MarkupConfigBuilder(Map<String, String> map) {
        this(new MapConfiguration(map));
    }

    private OpenAPI2MarkupConfigBuilder(Configuration configuration) {
        super(OpenAPI2MarkupConfigBuilder.class,
                new OpenSchema2MarkupConfig(),
                new OpenAPI2MarkupProperties(getCompositeConfiguration(configuration)), configuration);
    }

    @Override
    public OpenSchema2MarkupConfig build() {
        buildNaturalOrdering();
        return config;
    }
}
