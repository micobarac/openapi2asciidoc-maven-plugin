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
import io.swagger.models.Info;
import org.apache.commons.lang3.Validate;

import static com.logate.openapi2asciidoc.swagger2markup.internal.utils.MarkupDocBuilderUtils.copyMarkupDocBuilder;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class VersionInfoComponent extends MarkupComponent<VersionInfoComponent.Parameters> {


    public VersionInfoComponent(Swagger2MarkupConverter.SwaggerContext context) {
        super(context);
    }

    public static Parameters parameters(Info info,
                                                             int titleLevel) {
        return new Parameters(info, titleLevel);
    }

    @Override
    public MarkupDocBuilder apply(MarkupDocBuilder markupDocBuilder, Parameters params) {
        String version = params.info.getVersion();
        if (isNotBlank(version)) {
            markupDocBuilder.sectionTitleLevel(params.titleLevel, labels.getLabel(SwaggerLabels.CURRENT_VERSION));
            MarkupDocBuilder paragraphBuilder = copyMarkupDocBuilder(markupDocBuilder);
            paragraphBuilder.italicText(labels.getLabel(SwaggerLabels.VERSION)).textLine(COLON + version);
            markupDocBuilder.paragraph(paragraphBuilder.toString(), true);
        }
        return markupDocBuilder;
    }

    public static class Parameters {

        private final int titleLevel;
        private final Info info;

        public Parameters(
                Info info,
                int titleLevel) {
            this.info = Validate.notNull(info, "Info must not be null");
            this.titleLevel = titleLevel;
        }
    }
}
