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
import com.logate.openapi2asciidoc.swagger2markup.adoc.ast.impl.ParagraphBlockImpl;
import com.logate.openapi2asciidoc.swagger2markup.core.Schema2MarkupProperties;
import io.swagger.v3.oas.models.media.Encoding;
import org.apache.commons.lang3.StringUtils;
import org.asciidoctor.ast.StructuralNode;

import java.util.Collections;
import java.util.Map;

import static com.logate.openapi2asciidoc.openapi2markup.config.OpenAPILabels.LABEL_EXAMPLES;
import static com.logate.openapi2asciidoc.openapi2markup.internal.helper.OpenApiHelpers.italicUnconstrained;
import static com.logate.openapi2asciidoc.swagger2markup.adoc.converter.internal.Delimiters.LINE_SEPARATOR;

public class EncodingComponent extends MarkupComponent<StructuralNode, EncodingComponent.Parameters, StructuralNode> {

    private final HeadersComponent headersComponent;

    public EncodingComponent(OpenAPI2MarkupConverter.OpenAPIContext context) {
        super(context);
        this.headersComponent = new HeadersComponent(context);
    }

    public static Parameters parameters(Map<String, Encoding> encodings) {
        return new Parameters(encodings);
    }

    public StructuralNode apply(StructuralNode node, Map<String, Encoding> encodings) {
        return apply(node, parameters(encodings));
    }

    @Override
    public StructuralNode apply(StructuralNode node, Parameters parameters) {
        Map<String, Encoding> encodings = parameters.encodings;
        if (encodings == null || encodings.isEmpty()) return node;

        DescriptionListImpl encodingList = new DescriptionListImpl(node);
        encodingList.setTitle(labels.getLabel(LABEL_EXAMPLES));

        encodings.forEach((name, encoding) -> {
            DescriptionListEntryImpl encodingEntry = new DescriptionListEntryImpl(encodingList, Collections.singletonList(new ListItemImpl(encodingList, name)));
            ListItemImpl tagDesc = new ListItemImpl(encodingEntry, "");
            ParagraphBlockImpl encodingBlock = new ParagraphBlockImpl(tagDesc);

            StringBuilder sb = new StringBuilder();
            String contentType = encoding.getContentType();
            if(StringUtils.isNotBlank(contentType)){
                sb.append("Content-Type:").append(contentType).append(LINE_SEPARATOR);
            }
            if(encoding.getAllowReserved()){
                sb.append(italicUnconstrained("Allow Reserved").toLowerCase()).append(LINE_SEPARATOR);
            }
            if(encoding.getExplode()){
                sb.append(italicUnconstrained("Explode").toLowerCase()).append(LINE_SEPARATOR);
            }
            Encoding.StyleEnum style = encoding.getStyle();
            if(style != null){
                sb.append("style").append(style).append(LINE_SEPARATOR);
            }
            encodingBlock.setSource(sb.toString());
            tagDesc.append(encodingBlock);
            headersComponent.apply(tagDesc, encoding.getHeaders());

            encodingEntry.setDescription(tagDesc);

            encodingList.addEntry(encodingEntry);
        });
        node.append(encodingList);

        return node;
    }

    public static class Parameters {

        private final Map<String, Encoding> encodings;

        public Parameters(Map<String, Encoding> encodings) {
            this.encodings = encodings;
        }
    }
}
