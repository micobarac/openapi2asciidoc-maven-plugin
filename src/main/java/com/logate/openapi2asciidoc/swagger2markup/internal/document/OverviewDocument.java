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
package com.logate.openapi2asciidoc.swagger2markup.internal.document;

import com.logate.openapi2asciidoc.swagger2markup.Swagger2MarkupConverter;
import com.logate.openapi2asciidoc.swagger2markup.SwaggerLabels;
import com.logate.openapi2asciidoc.swagger2markup.internal.component.*;
import com.logate.openapi2asciidoc.swagger2markup.markup.builder.MarkupDocBuilder;
import com.logate.openapi2asciidoc.swagger2markup.markup.builder.MarkupLanguage;
import com.logate.openapi2asciidoc.swagger2markup.spi.MarkupComponent;
import com.logate.openapi2asciidoc.swagger2markup.spi.OverviewDocumentExtension;
import io.swagger.models.*;
import org.apache.commons.lang3.Validate;

import java.util.List;

import static com.logate.openapi2asciidoc.swagger2markup.internal.utils.MarkupDocBuilderUtils.markupDescription;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class OverviewDocument extends MarkupComponent<OverviewDocument.Parameters> {

    public static final int SECTION_TITLE_LEVEL = 2;
    private static final String OVERVIEW_ANCHOR = "overview";
    private final VersionInfoComponent versionInfoComponent;
    private final ContactInfoComponent contactInfoComponent;
    private final LicenseInfoComponent licenseInfoComponent;
    private final UriSchemeComponent uriSchemeComponent;
    private final TagsComponent tagsComponent;
    private final ProducesComponent producesComponent;
    private final ConsumesComponent consumesComponent;
    private final ExternalDocsComponent externalDocsComponent;

    public OverviewDocument(Swagger2MarkupConverter.SwaggerContext context) {
        super(context);
        versionInfoComponent = new VersionInfoComponent(context);
        contactInfoComponent = new ContactInfoComponent(context);
        licenseInfoComponent = new LicenseInfoComponent(context);
        uriSchemeComponent = new UriSchemeComponent(context);
        tagsComponent = new TagsComponent(context);
        producesComponent = new ProducesComponent(context);
        consumesComponent = new ConsumesComponent(context);
	    externalDocsComponent = new ExternalDocsComponent((context));
    }

    public static Parameters parameters(Swagger swagger) {
        return new Parameters(swagger);
    }

    /**
     * Builds the overview MarkupDocument.
     *
     * @return the overview MarkupDocument
     */
    @Override
    public MarkupDocBuilder apply(MarkupDocBuilder markupDocBuilder, Parameters params) {
        Swagger swagger = params.swagger;
        Info info = swagger.getInfo();
        buildDocumentTitle(markupDocBuilder, info.getTitle());
        applyOverviewDocumentExtension(new OverviewDocumentExtension.Context(OverviewDocumentExtension.Position.DOCUMENT_BEFORE, markupDocBuilder));
        buildOverviewTitle(markupDocBuilder, labels.getLabel(SwaggerLabels.OVERVIEW));
        applyOverviewDocumentExtension(new OverviewDocumentExtension.Context(OverviewDocumentExtension.Position.DOCUMENT_BEGIN, markupDocBuilder));
        buildDescriptionParagraph(markupDocBuilder, info.getDescription());
        buildVersionInfoSection(markupDocBuilder, info);
        buildContactInfoSection(markupDocBuilder, info.getContact());
        buildLicenseInfoSection(markupDocBuilder, info);
        buildUriSchemeSection(markupDocBuilder, swagger);
        buildTagsSection(markupDocBuilder, swagger.getTags());
        buildConsumesSection(markupDocBuilder, swagger.getConsumes());
        buildProducesSection(markupDocBuilder, swagger.getProduces());
        buildExternalDocsSection(markupDocBuilder, swagger.getExternalDocs());
        applyOverviewDocumentExtension(new OverviewDocumentExtension.Context(OverviewDocumentExtension.Position.DOCUMENT_END, markupDocBuilder));
        applyOverviewDocumentExtension(new OverviewDocumentExtension.Context(OverviewDocumentExtension.Position.DOCUMENT_AFTER, markupDocBuilder));
        return markupDocBuilder;
    }

    private void buildDocumentTitle(MarkupDocBuilder markupDocBuilder, String title) {
        markupDocBuilder.documentTitle(title);
    }

    private void buildOverviewTitle(MarkupDocBuilder markupDocBuilder, String title) {
        markupDocBuilder.sectionTitleWithAnchorLevel1(title, OVERVIEW_ANCHOR);
    }

    void buildDescriptionParagraph(MarkupDocBuilder markupDocBuilder, String description) {
        if (isNotBlank(description)) {
            markupDocBuilder.paragraph(markupDescription(MarkupLanguage.valueOf(config.getSchemaMarkupLanguage().name()),
                    markupDocBuilder, description));
        }
    }

    private void buildVersionInfoSection(MarkupDocBuilder markupDocBuilder, Info info) {
        if (info != null) {
            versionInfoComponent.apply(markupDocBuilder, VersionInfoComponent.parameters(info, SECTION_TITLE_LEVEL));
        }
    }

    private void buildContactInfoSection(MarkupDocBuilder markupDocBuilder, Contact contact) {
        if (contact != null) {
            contactInfoComponent.apply(markupDocBuilder, ContactInfoComponent.parameters(contact, SECTION_TITLE_LEVEL));
        }
    }

    private void buildLicenseInfoSection(MarkupDocBuilder markupDocBuilder, Info info) {
        if (info != null) {
            licenseInfoComponent.apply(markupDocBuilder, LicenseInfoComponent.parameters(info, SECTION_TITLE_LEVEL));
        }
    }

    private void buildUriSchemeSection(MarkupDocBuilder markupDocBuilder, Swagger swagger) {
        uriSchemeComponent.apply(markupDocBuilder, UriSchemeComponent.parameters(swagger, SECTION_TITLE_LEVEL));
    }

    private void buildTagsSection(MarkupDocBuilder markupDocBuilder, List<Tag> tags) {
        if (isNotEmpty(tags)) {
            tagsComponent.apply(markupDocBuilder, TagsComponent.parameters(tags, SECTION_TITLE_LEVEL));
        }
    }

    private void buildConsumesSection(MarkupDocBuilder markupDocBuilder, List<String> consumes) {
        if (isNotEmpty(consumes)) {
            consumesComponent.apply(markupDocBuilder, ConsumesComponent.parameters(consumes, SECTION_TITLE_LEVEL));
        }
    }

    private void buildProducesSection(MarkupDocBuilder markupDocBuilder, List<String> produces) {
        if (isNotEmpty(produces)) {
            producesComponent.apply(markupDocBuilder, ProducesComponent.parameters(produces, SECTION_TITLE_LEVEL));
        }
    }

    private void buildExternalDocsSection(MarkupDocBuilder markupDocBuilder, ExternalDocs externalDocs) {
	    if (externalDocs != null) {
	    	externalDocsComponent.apply(markupDocBuilder, ExternalDocsComponent.parameters(externalDocs, SECTION_TITLE_LEVEL));
	    }
    }

    /**
     * Apply extension context to all OverviewContentExtension
     *
     * @param context context
     */
    private void applyOverviewDocumentExtension(OverviewDocumentExtension.Context context) {
        extensionRegistry.getOverviewDocumentExtensions().forEach(extension -> extension.apply(context));
    }

    public static class Parameters {
        private final Swagger swagger;

        public Parameters(Swagger swagger) {
            this.swagger = Validate.notNull(swagger, "Swagger must not be null");
        }
    }

}
