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
import com.logate.openapi2asciidoc.swagger2markup.internal.resolver.DocumentResolver;
import com.logate.openapi2asciidoc.swagger2markup.internal.type.ObjectType;
import com.logate.openapi2asciidoc.swagger2markup.internal.type.ObjectTypePolymorphism;
import com.logate.openapi2asciidoc.swagger2markup.internal.type.Type;
import com.logate.openapi2asciidoc.swagger2markup.internal.utils.ModelUtils;
import com.logate.openapi2asciidoc.swagger2markup.markup.builder.MarkupDocBuilder;
import com.logate.openapi2asciidoc.swagger2markup.markup.builder.MarkupLanguage;
import com.logate.openapi2asciidoc.swagger2markup.spi.DefinitionsDocumentExtension;
import com.logate.openapi2asciidoc.swagger2markup.spi.MarkupComponent;
import io.swagger.models.Model;
import io.swagger.models.properties.Property;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.Validate;

import java.util.*;

import static com.logate.openapi2asciidoc.swagger2markup.internal.utils.InlineSchemaUtils.createInlineType;
import static com.logate.openapi2asciidoc.swagger2markup.internal.utils.MarkupDocBuilderUtils.copyMarkupDocBuilder;
import static com.logate.openapi2asciidoc.swagger2markup.internal.utils.MarkupDocBuilderUtils.markupDescription;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class DefinitionComponent extends MarkupComponent<DefinitionComponent.Parameters> {

    /* Discriminator is only displayed for inheriting definitions */
    private static final boolean ALWAYS_DISPLAY_DISCRIMINATOR = false;

    private final Map<String, Model> definitions;
    private final Map<ObjectTypePolymorphism.Nature, String> POLYMORPHISM_NATURE;
    private final DocumentResolver definitionsDocumentResolver;
    private PropertiesTableComponent propertiesTableComponent;

    public DefinitionComponent(Swagger2MarkupConverter.SwaggerContext context,
                               DocumentResolver definitionsDocumentResolver) {
        super(context);
        this.definitions = context.getSchema().getDefinitions();
        this.definitionsDocumentResolver = definitionsDocumentResolver;
        POLYMORPHISM_NATURE = new HashMap<ObjectTypePolymorphism.Nature, String>() {{
            put(ObjectTypePolymorphism.Nature.COMPOSITION, labels.getLabel(SwaggerLabels.POLYMORPHISM_NATURE_COMPOSITION));
            put(ObjectTypePolymorphism.Nature.INHERITANCE, labels.getLabel(SwaggerLabels.POLYMORPHISM_NATURE_INHERITANCE));
        }};
        propertiesTableComponent = new PropertiesTableComponent(context, definitionsDocumentResolver);
    }

    public static Parameters parameters(String definitionName,
                                                            Model model,
                                                            int titleLevel) {
        return new Parameters(definitionName, model, titleLevel);
    }

    @Override
    public MarkupDocBuilder apply(MarkupDocBuilder markupDocBuilder, Parameters params) {
        String definitionName = params.definitionName;
        String definitionTitle = determineDefinitionTitle(params);

        Model model = params.model;
        applyDefinitionsDocumentExtension(new DefinitionsDocumentExtension.Context(DefinitionsDocumentExtension.Position.DEFINITION_BEFORE, markupDocBuilder, definitionName, model));
        markupDocBuilder.sectionTitleWithAnchorLevel(params.titleLevel, definitionTitle, definitionName);
        applyDefinitionsDocumentExtension(new DefinitionsDocumentExtension.Context(DefinitionsDocumentExtension.Position.DEFINITION_BEGIN, markupDocBuilder, definitionName, model));
        String description = model.getDescription();
        if (isNotBlank(description)) {
            markupDocBuilder.paragraph(markupDescription(MarkupLanguage.valueOf(config.getSchemaMarkupLanguage().name()),
                    markupDocBuilder, description));
        }
        inlineDefinitions(markupDocBuilder, typeSection(markupDocBuilder, definitionName, model), definitionName);
        applyDefinitionsDocumentExtension(new DefinitionsDocumentExtension.Context(DefinitionsDocumentExtension.Position.DEFINITION_END, markupDocBuilder, definitionName, model));
        applyDefinitionsDocumentExtension(new DefinitionsDocumentExtension.Context(DefinitionsDocumentExtension.Position.DEFINITION_AFTER, markupDocBuilder, definitionName, model));

        return markupDocBuilder;
    }

    /**
     * Determines title for definition. If title is present, it is used, definitionName is returned otherwise.
     *
     * @param params      params object for this definition
     * @return Definition title - value from title tag if present, definitionName otherwise
     */
    private String determineDefinitionTitle(Parameters params) {
       if (params.model.getTitle() != null) {
            return params.model.getTitle();
        } else {
            return params.definitionName;
        }
    }

    /**
     * Builds the title of an inline schema.
     * Inline definitions should never been referenced in TOC because they have no real existence, so they are just text.
     *
     * @param title      inline schema title
     * @param anchor     inline schema anchor
     * @param docBuilder the docbuilder do use for output
     */
    private void addInlineDefinitionTitle(String title, String anchor, MarkupDocBuilder docBuilder) {
        docBuilder.anchor(anchor, null);
        docBuilder.newLine();
        docBuilder.boldTextLine(title);
    }

    /**
     * Builds inline schema definitions
     *
     * @param markupDocBuilder the docbuilder do use for output
     * @param definitions      all inline definitions to display
     * @param uniquePrefix     unique prefix to prepend to inline object names to enforce unicity
     */
    private void inlineDefinitions(MarkupDocBuilder markupDocBuilder, List<ObjectType> definitions, String uniquePrefix) {
        if (CollectionUtils.isNotEmpty(definitions)) {
            for (ObjectType definition : definitions) {
                addInlineDefinitionTitle(definition.getName(), definition.getUniqueName(), markupDocBuilder);

                List<ObjectType> localDefinitions = new ArrayList<>();
                propertiesTableComponent.apply(markupDocBuilder, new PropertiesTableComponent.Parameters(definition.getProperties(), uniquePrefix, localDefinitions));
                for (ObjectType localDefinition : localDefinitions)
                    inlineDefinitions(markupDocBuilder, Collections.singletonList(localDefinition), localDefinition.getUniqueName());
            }
        }
    }

    /**
     * Builds the type informations of a definition
     *
     * @param markupDocBuilder the docbuilder do use for output
     * @param definitionName   name of the definition to display
     * @param model            model of the definition to display
     * @return a list of inlined types.
     */
    private List<ObjectType> typeSection(MarkupDocBuilder markupDocBuilder, String definitionName, Model model) {
        List<ObjectType> inlineDefinitions = new ArrayList<>();
        Type modelType = ModelUtils.resolveRefType(ModelUtils.getType(model, definitions, definitionsDocumentResolver));

        if (!(modelType instanceof ObjectType) && config.isInlineSchemaEnabled()) {
            modelType = createInlineType(modelType, definitionName, definitionName + " " + "inline", inlineDefinitions);
        }

        if (modelType instanceof ObjectType) {
            ObjectType objectType = (ObjectType) modelType;
            MarkupDocBuilder typeInfos = copyMarkupDocBuilder(markupDocBuilder);
            switch (objectType.getPolymorphism().getNature()) {
                case COMPOSITION:
                    typeInfos.italicText(labels.getLabel(SwaggerLabels.POLYMORPHISM_COLUMN)).textLine(COLON + POLYMORPHISM_NATURE.get(objectType.getPolymorphism().getNature()));
                    break;
                case INHERITANCE:
                    typeInfos.italicText(labels.getLabel(SwaggerLabels.POLYMORPHISM_COLUMN)).textLine(COLON + POLYMORPHISM_NATURE.get(objectType.getPolymorphism().getNature()));
                    typeInfos.italicText(labels.getLabel(SwaggerLabels.POLYMORPHISM_DISCRIMINATOR_COLUMN)).textLine(COLON + objectType.getPolymorphism().getDiscriminator());
                    break;
                case NONE:
                    if (ALWAYS_DISPLAY_DISCRIMINATOR && isNotBlank(objectType.getPolymorphism().getDiscriminator()))
                        typeInfos.italicText(labels.getLabel(SwaggerLabels.POLYMORPHISM_DISCRIMINATOR_COLUMN)).textLine(COLON + objectType.getPolymorphism().getDiscriminator());
                    break;
                default:
                    break;
            }

            String typeInfosString = typeInfos.toString();
            if (isNotBlank(typeInfosString))
                markupDocBuilder.paragraph(typeInfosString, true);

            Map<String, Property> properties = ((ObjectType) modelType).getProperties();
            if (!properties.isEmpty()) {
                propertiesTableComponent.apply(markupDocBuilder,
                        PropertiesTableComponent.parameters(
                                properties,
                                definitionName,
                                inlineDefinitions));
            }
        } else if (modelType != null) {
            MarkupDocBuilder typeInfos = copyMarkupDocBuilder(markupDocBuilder);
            typeInfos.italicText(labels.getLabel(SwaggerLabels.TYPE_COLUMN)).textLine(COLON + modelType.displaySchema(markupDocBuilder));

            markupDocBuilder.paragraph(typeInfos.toString());
        }

        return inlineDefinitions;
    }

    /**
     * Apply extension context to all DefinitionsContentExtension
     *
     * @param context context
     */
    private void applyDefinitionsDocumentExtension(DefinitionsDocumentExtension.Context context) {
        extensionRegistry.getDefinitionsDocumentExtensions().forEach(extension -> extension.apply(context));
    }

    public static class Parameters {
        private final String definitionName;
        private final Model model;
        private final int titleLevel;

        public Parameters(String definitionName,
                          Model model,
                          int titleLevel) {
            this.definitionName = Validate.notBlank(definitionName, "DefinitionName must not be empty");
            this.model = Validate.notNull(model, "Model must not be null");
            this.titleLevel = titleLevel;
        }
    }


}
