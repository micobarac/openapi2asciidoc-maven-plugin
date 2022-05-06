/*
 * Copyright 2017 Robert Winkler
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.logate.openapi2asciidoc.swagger2markup.internal.resolver;

import com.logate.openapi2asciidoc.swagger2markup.Swagger2MarkupConverter;
import com.logate.openapi2asciidoc.swagger2markup.utils.IOUtils;

import static org.apache.commons.lang3.StringUtils.defaultString;

/**
 * Overrides definition document resolver functor for inter-document cross-references from definitions files.
 * This implementation simplifies the path between two definitions because all definitions are in the same path.
 */
public class DefinitionDocumentResolverFromDefinition extends DefinitionDocumentResolverDefault {

    public DefinitionDocumentResolverFromDefinition(Swagger2MarkupConverter.SwaggerContext context) {
        super(context);
    }

    public String apply(String definitionName) {
        String defaultResolver = super.apply(definitionName);

        if (defaultResolver != null && config.isSeparatedDefinitionsEnabled())
            return defaultString(config.getInterDocumentCrossReferencesPrefix()) + markupDocBuilder.addFileExtension(IOUtils.normalizeName(definitionName));
        else
            return defaultResolver;
    }
}