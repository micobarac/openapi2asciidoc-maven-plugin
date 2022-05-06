package com.logate.openapi2asciidoc.swagger2markup.internal.resolver;

import com.logate.openapi2asciidoc.swagger2markup.Swagger2MarkupConverter;

import java.io.File;

import static org.apache.commons.lang3.StringUtils.defaultString;

/**
 * Overrides definition document resolver functor for inter-document cross-references from operations files.
 * This implementation adapt the relative paths to definitions files
 */
public class DefinitionDocumentResolverFromOperation extends DefinitionDocumentResolverDefault {

    public DefinitionDocumentResolverFromOperation(Swagger2MarkupConverter.SwaggerContext context) {
        super(context);
    }

    public String apply(String definitionName) {
        String defaultResolver = super.apply(definitionName);

        if (defaultResolver != null && config.isSeparatedOperationsEnabled())
            return defaultString(config.getInterDocumentCrossReferencesPrefix()) + new File("..", defaultResolver).getPath();
        else
            return defaultResolver;
    }
}