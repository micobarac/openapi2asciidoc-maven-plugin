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
package com.logate.openapi2asciidoc.swagger2markup.internal.component;

import com.logate.openapi2asciidoc.swagger2markup.Swagger2MarkupConverter;
import com.logate.openapi2asciidoc.swagger2markup.SwaggerLabels;
import com.logate.openapi2asciidoc.swagger2markup.markup.builder.MarkupDocBuilder;
import com.logate.openapi2asciidoc.swagger2markup.spi.MarkupComponent;
import io.swagger.models.Swagger;
import org.apache.commons.lang3.Validate;

import java.util.List;
import java.util.stream.Collectors;

import static com.logate.openapi2asciidoc.swagger2markup.internal.utils.MarkupDocBuilderUtils.copyMarkupDocBuilder;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.join;

public class UriSchemeComponent extends MarkupComponent<UriSchemeComponent.Parameters> {


    public UriSchemeComponent(Swagger2MarkupConverter.SwaggerContext context) {
        super(context);
    }

    public static Parameters parameters(Swagger swagger, int titleLevel) {
        return new Parameters(swagger, titleLevel);
    }

    @Override
    public MarkupDocBuilder apply(MarkupDocBuilder markupDocBuilder, Parameters params) {
        Swagger swagger = params.swagger;
        if (isNotBlank(swagger.getHost()) || isNotBlank(swagger.getBasePath()) || isNotEmpty(swagger.getSchemes())) {
            markupDocBuilder.sectionTitleLevel(params.titleLevel, labels.getLabel(SwaggerLabels.URI_SCHEME));
            MarkupDocBuilder paragraphBuilder = copyMarkupDocBuilder(markupDocBuilder);
            if (isNotBlank(swagger.getHost())) {
                paragraphBuilder.italicText(labels.getLabel(SwaggerLabels.HOST))
                        .textLine(COLON + swagger.getHost());
            }
            if (isNotBlank(swagger.getBasePath())) {
                paragraphBuilder.italicText(labels.getLabel(SwaggerLabels.BASE_PATH))
                        .textLine(COLON + swagger.getBasePath());
            }
            if (isNotEmpty(swagger.getSchemes())) {
                List<String> schemes = swagger.getSchemes().stream()
                        .map(Enum::toString)
                        .collect(Collectors.toList());
                paragraphBuilder.italicText(labels.getLabel(SwaggerLabels.SCHEMES))
                        .textLine(COLON + join(schemes, ", "));
            }
            markupDocBuilder.paragraph(paragraphBuilder.toString(), true);
        }
        return markupDocBuilder;
    }

    public static class Parameters {

        private final int titleLevel;
        private final Swagger swagger;

        public Parameters(Swagger swagger, int titleLevel) {

            this.swagger = Validate.notNull(swagger);
            this.titleLevel = titleLevel;
        }
    }


}
