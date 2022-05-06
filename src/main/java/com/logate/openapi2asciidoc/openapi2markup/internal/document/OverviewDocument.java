package com.logate.openapi2asciidoc.openapi2markup.internal.document;

import com.logate.openapi2asciidoc.openapi2markup.OpenAPI2MarkupConverter;
import com.logate.openapi2asciidoc.openapi2markup.extension.MarkupComponent;
import com.logate.openapi2asciidoc.openapi2markup.extension.OverviewDocumentExtension;
import com.logate.openapi2asciidoc.openapi2markup.internal.component.ExternalDocumentationComponent;
import com.logate.openapi2asciidoc.openapi2markup.internal.component.TagsComponent;
import com.logate.openapi2asciidoc.openapi2markup.internal.helper.OpenApiHelpers;
import com.logate.openapi2asciidoc.swagger2markup.adoc.ast.impl.BlockImpl;
import com.logate.openapi2asciidoc.swagger2markup.adoc.ast.impl.DocumentImpl;
import com.logate.openapi2asciidoc.swagger2markup.adoc.ast.impl.ParagraphBlockImpl;
import com.logate.openapi2asciidoc.swagger2markup.adoc.ast.impl.SectionImpl;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.asciidoctor.ast.Block;
import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.Section;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

import static com.logate.openapi2asciidoc.openapi2markup.config.OpenAPILabels.LABEL_TERMS_OF_SERVICE;
import static com.logate.openapi2asciidoc.openapi2markup.config.OpenAPILabels.SECTION_TITLE_OVERVIEW;

public class OverviewDocument extends MarkupComponent<Document, OverviewDocument.Parameters, Document> {
    private final TagsComponent tagsComponent;
    private final ExternalDocumentationComponent externalDocumentationComponent;

    public OverviewDocument(OpenAPI2MarkupConverter.OpenAPIContext context) {
        super(context);
        tagsComponent = new TagsComponent(context);
        this.externalDocumentationComponent = new ExternalDocumentationComponent(context);
    }

    public static Parameters parameters(OpenAPI schema) {
        return new Parameters(schema);
    }

    @Override
    public Document apply(Document document, Parameters parameters) {
        Info apiInfo = parameters.openAPI.getInfo();
        document.setAttribute("openapi", parameters.openAPI.getOpenapi(), true);
        addDocumentTitle(document, apiInfo);
        addAuthorInfo(document, apiInfo);
        addVersionInfo(document, apiInfo);

        applyOverviewDocumentExtension(new OverviewDocumentExtension.Context(OverviewDocumentExtension.Position.DOCUMENT_BEFORE, document));
        Document subDocument = new DocumentImpl(document);
        Section overviewDoc = new SectionImpl(subDocument, "section", new HashMap<>(), new ArrayList<>(),
                null, new ArrayList<>(), 1, "", new ArrayList<>(),
                null, null, "", "", false, false);
        applyOverviewDocumentExtension(new OverviewDocumentExtension.Context(OverviewDocumentExtension.Position.DOCUMENT_BEGIN, subDocument));
        overviewDoc.setTitle(labels.getLabel(SECTION_TITLE_OVERVIEW));

        OpenApiHelpers.appendDescription(overviewDoc, apiInfo.getDescription());
        appendTermsOfServiceInfo(overviewDoc, apiInfo);
        appendLicenseInfo(overviewDoc, apiInfo);
        subDocument.append(overviewDoc);
        applyOverviewDocumentExtension(new OverviewDocumentExtension.Context(OverviewDocumentExtension.Position.DOCUMENT_END, subDocument));
        document.append(subDocument);

        externalDocumentationComponent.apply(document, parameters.openAPI.getExternalDocs());
        tagsComponent.apply(document, parameters.openAPI.getTags());
        applyOverviewDocumentExtension(new OverviewDocumentExtension.Context(OverviewDocumentExtension.Position.DOCUMENT_AFTER, document));
        return document;
    }

    private void applyOverviewDocumentExtension(OverviewDocumentExtension.Context context) {
        extensionRegistry.getOverviewDocumentExtensions().forEach(extension -> extension.apply(context));
    }

    private void addDocumentTitle(Document rootDocument, Info apiInfo) {
        String title = apiInfo.getTitle();
        if (StringUtils.isNotBlank(title)) {
            rootDocument.setTitle(title);
        }
    }

    private void addVersionInfo(Document rootDocument, Info info) {
        String version = info.getVersion();
        if (StringUtils.isNotBlank(version)) {
            rootDocument.setAttribute("revnumber", version, true);
        }
    }

    private void addAuthorInfo(Document rootDocument, Info info) {
        Contact contact = info.getContact();
        if (null != contact) {
            String author = Optional.ofNullable(contact.getName()).orElse("");
            String email = contact.getEmail();
            if (StringUtils.isNotBlank(email)) {
                rootDocument.setAttribute("email", email, true);
            }
            rootDocument.setAttribute("author", author, true);
            rootDocument.setAttribute("authorcount", 1L, true);
        }
    }

    private void appendLicenseInfo(Section overviewDoc, Info info) {
        License license = info.getLicense();
        if (null != license) {
            StringBuilder sb = new StringBuilder();
            if (StringUtils.isNotBlank(license.getUrl())) {
                sb.append(license.getUrl()).append("[");
            }
            sb.append(license.getName());
            if (StringUtils.isNotBlank(license.getUrl())) {
                sb.append("]");
            }
            BlockImpl paragraph = new ParagraphBlockImpl(overviewDoc);
            paragraph.setSource(sb.toString());
            overviewDoc.append(paragraph);
        }
    }

    private void appendTermsOfServiceInfo(Section overviewDoc, Info info) {
        String termsOfService = info.getTermsOfService();
        if (StringUtils.isNotBlank(termsOfService)) {
            Block paragraph = new ParagraphBlockImpl(overviewDoc);
            paragraph.setSource(termsOfService + "[" + labels.getLabel(LABEL_TERMS_OF_SERVICE) + "]");
            overviewDoc.append(paragraph);
        }
    }

    public static class Parameters {
        private final OpenAPI openAPI;

        public Parameters(OpenAPI openAPI) {
            this.openAPI = Validate.notNull(openAPI, "Schema must not be null");
        }
    }
}
