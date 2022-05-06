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
import io.swagger.models.Tag;
import org.apache.commons.lang3.Validate;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class TagsComponent extends MarkupComponent<TagsComponent.Parameters> {

    public TagsComponent(Swagger2MarkupConverter.SwaggerContext context) {
        super(context);
    }

    public static Parameters parameters(List<Tag> tags,
                                                      int titleLevel) {
        return new Parameters(tags, titleLevel);
    }

    @Override
    public MarkupDocBuilder apply(MarkupDocBuilder markupDocBuilder, Parameters params) {
        markupDocBuilder.sectionTitleLevel(params.titleLevel, labels.getLabel(SwaggerLabels.TAGS));

        List<String> tagsList = params.tags.stream()
                .map(this::mapToString).collect(Collectors.toList());

        if (config.getTagOrdering() != null)
            Collections.sort(tagsList, config.getTagOrdering());

        markupDocBuilder.unorderedList(tagsList);

        return markupDocBuilder;
    }

    private String mapToString(Tag tag) {
        String name = tag.getName();
        String description = tag.getDescription();
        if (isNotBlank(description)) {
            return name + COLON + description;
        } else {
            return name;
        }
    }

    public static class Parameters {

        private final List<Tag> tags;
        private final int titleLevel;

        public Parameters(List<Tag> tags,
                          int titleLevel) {
            this.tags = Validate.notNull(tags, "Tags must not be null");
            this.titleLevel = titleLevel;
        }
    }
}
