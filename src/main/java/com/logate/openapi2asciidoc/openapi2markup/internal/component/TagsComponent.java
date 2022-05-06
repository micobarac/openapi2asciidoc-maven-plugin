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
package com.logate.openapi2asciidoc.openapi2markup.internal.component;

import com.logate.openapi2asciidoc.openapi2markup.OpenAPI2MarkupConverter;
import com.logate.openapi2asciidoc.openapi2markup.extension.MarkupComponent;
import com.logate.openapi2asciidoc.swagger2markup.adoc.ast.impl.DescriptionListEntryImpl;
import com.logate.openapi2asciidoc.swagger2markup.adoc.ast.impl.DescriptionListImpl;
import com.logate.openapi2asciidoc.swagger2markup.adoc.ast.impl.ListItemImpl;
import com.logate.openapi2asciidoc.swagger2markup.adoc.ast.impl.SectionImpl;
import io.swagger.v3.oas.models.tags.Tag;
import org.apache.commons.lang3.StringUtils;
import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.Section;

import java.util.Collections;
import java.util.List;

import static com.logate.openapi2asciidoc.openapi2markup.config.OpenAPILabels.SECTION_TITLE_TAGS;


public class TagsComponent extends MarkupComponent<Document, TagsComponent.Parameters, Document> {

    private final ExternalDocumentationComponent externalDocumentationComponent;

    public TagsComponent(OpenAPI2MarkupConverter.OpenAPIContext context) {
        super(context);
        this.externalDocumentationComponent = new ExternalDocumentationComponent(context);
    }

    public static Parameters parameters(List<Tag> tags) {
        return new Parameters(tags);
    }

    public Document apply(Document document, List<Tag> tags) {
        return apply(document, parameters(tags));
    }

    @Override
    public Document apply(Document document, Parameters parameters) {
        List<Tag> openAPITags = parameters.tags;
        if (null == openAPITags || openAPITags.isEmpty()) return document;

        Section tagsSection = new SectionImpl(document);
        tagsSection.setTitle(labels.getLabel(SECTION_TITLE_TAGS));

        DescriptionListImpl tagsList = new DescriptionListImpl(tagsSection);
        openAPITags.forEach(tag -> {
            DescriptionListEntryImpl tagEntry = new DescriptionListEntryImpl(tagsList, Collections.singletonList(new ListItemImpl(tagsList, tag.getName())));
            String description = tag.getDescription();
            if(StringUtils.isNotBlank(description)){
                ListItemImpl tagDesc = new ListItemImpl(tagEntry, "");
                tagDesc.setSource(description);
                externalDocumentationComponent.apply(tagDesc, tag.getExternalDocs());
                tagEntry.setDescription(tagDesc);
            }
            tagsList.addEntry(tagEntry);
        });

        tagsSection.append(tagsList);
        document.append(tagsSection);
        return document;
    }

    public static class Parameters {

        private final List<Tag> tags;

        public Parameters(List<Tag> tags) {
            this.tags = tags;
        }
    }
}
