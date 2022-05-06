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
import io.swagger.models.ExternalDocs;
import org.apache.commons.lang3.Validate;

import static com.logate.openapi2asciidoc.swagger2markup.internal.utils.MarkupDocBuilderUtils.copyMarkupDocBuilder;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class ExternalDocsComponent extends MarkupComponent<ExternalDocsComponent.Parameters> {

    public ExternalDocsComponent(Swagger2MarkupConverter.SwaggerContext context) {
        super(context);
    }

    public static Parameters parameters(ExternalDocs externalDocs, int titleLevel) {
        return new Parameters(externalDocs, titleLevel);
    }

    @Override
    public MarkupDocBuilder apply(MarkupDocBuilder markupDocBuilder, Parameters params) {
        ExternalDocs externalDocs = params.externalDocs;
        String description = externalDocs.getDescription();
        String url = externalDocs.getUrl();
        if ((description != null && (isNotBlank(description) || (url != null && isNotBlank(url))))) {
            markupDocBuilder.sectionTitleLevel(params.titleLevel, labels.getLabel(SwaggerLabels.EXTERNAL_DOCS));
            MarkupDocBuilder paragraph = copyMarkupDocBuilder(markupDocBuilder);

            if (isNotBlank(description)) {
                paragraph.italicText(labels.getLabel(SwaggerLabels.EXTERNAL_DOCS_DESC)).textLine(COLON + description);
            }
            if (isNotBlank(url)) {
                paragraph.italicText(labels.getLabel(SwaggerLabels.EXTERNAL_DOCS_URL)).textLine(COLON + url);
            }

            markupDocBuilder.paragraph(paragraph.toString(), true);
        }

        return markupDocBuilder;
    }

    public static class Parameters {
        private final int titleLevel;
        private final ExternalDocs externalDocs;

        public Parameters(ExternalDocs externalDocs,
                          int titleLevel) {
            this.externalDocs = Validate.notNull(externalDocs, "ExternalDocs must not be null");
            this.titleLevel = titleLevel;
        }
    }
}
