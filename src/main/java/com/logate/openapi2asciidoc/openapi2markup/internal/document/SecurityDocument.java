package com.logate.openapi2asciidoc.openapi2markup.internal.document;

import com.logate.openapi2asciidoc.openapi2markup.OpenAPI2MarkupConverter;
import com.logate.openapi2asciidoc.openapi2markup.extension.MarkupComponent;
import com.logate.openapi2asciidoc.openapi2markup.internal.component.SecurityRequirementTableComponent;
import com.logate.openapi2asciidoc.swagger2markup.adoc.ast.impl.SectionImpl;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.apache.commons.lang3.Validate;
import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.Section;

import java.util.List;

import static com.logate.openapi2asciidoc.openapi2markup.config.OpenAPILabels.SECTION_TITLE_SECURITY;

public class SecurityDocument extends MarkupComponent<Document, SecurityDocument.Parameters, Document> {
    private final SecurityRequirementTableComponent securityRequirementTableComponent;

    public SecurityDocument(OpenAPI2MarkupConverter.OpenAPIContext context) {
        super(context);
        this.securityRequirementTableComponent = new SecurityRequirementTableComponent(context);
    }

    public static Parameters parameters(OpenAPI schema) {
        return new Parameters(schema);
    }

    @Override
    public Document apply(Document document, Parameters parameters) {
        List<SecurityRequirement> securityRequirements = parameters.schema.getSecurity();
        if (null == securityRequirements || securityRequirements.isEmpty()) return document;

        Section securityRequirementsSection = new SectionImpl(document);
        securityRequirementsSection.setTitle(labels.getLabel(SECTION_TITLE_SECURITY));
        securityRequirementTableComponent.apply(securityRequirementsSection, securityRequirements, false);
        document.append(securityRequirementsSection);

        return document;
    }

    public static class Parameters {
        private final OpenAPI schema;

        public Parameters(OpenAPI schema) {
            this.schema = Validate.notNull(schema, "Schema must not be null");
        }
    }
}
