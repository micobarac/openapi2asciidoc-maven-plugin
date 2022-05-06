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
package com.logate.openapi2asciidoc.swagger2markup.core.config;

import com.logate.openapi2asciidoc.swagger2markup.core.*;
import com.logate.openapi2asciidoc.swagger2markup.core.model.Parameter;
import com.logate.openapi2asciidoc.swagger2markup.core.model.PathOperation;

import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Swagger2Markup configuration interface.
 */
public interface Schema2MarkupConfig {

    /**
     * Specifies the markup language which should be used to generate the files.
     *
     * @return Specifies the markup language which should be used to generate the files.
     */
    MarkupLanguage getMarkupLanguage();

    /**
     * Specifies the markup language used in Swagger descriptions.<br>
     * By default, {@link MarkupLanguage#MARKDOWN} is assumed.
     *
     * @return Specifies the markup language used in Swagger descriptions.
     */
    MarkupLanguage getSchemaMarkupLanguage();

    /**
     * Include generated examples into the documents.
     *
     * @return Include generated examples into the documents.
     */
    boolean isGeneratedExamplesEnabled();

    /**
     * Prepend the hostname to all paths.
     *
     * @return Prepend the hostname to all paths.
     */
    boolean isHostnameEnabled();

    /**
     * Prepend the base path to all paths.
     *
     * @return Prepend the base path to all paths.
     */
    boolean isBasePathPrefixEnabled();

    /**
     * In addition to the Definitions file, also create separate definition files for each model definition.
     *
     * @return In addition to the Definitions file, also create separate definition files for each model definition.
     */
    boolean isSeparatedDefinitionsEnabled();

    /**
     * In addition to the Paths file, also create separate operation files for each operation.
     *
     * @return In addition to the Paths file, also create separate operation files for each operation.
     */
    boolean isSeparatedOperationsEnabled();

    /**
     * Specifies if the operations should be grouped by tags or stay as-is.
     *
     * @return Specifies if the operations should be grouped by tags or stay as-is.
     */
    GroupBy getPathsGroupedBy();

    /**
     * Specifies labels language of output files.
     *
     * @return Specifies labels language of output files.
     */
    Language getLanguage();

    /**
     * Specifies if inline schemas are detailed
     *
     * @return Specifies if inline schemas are detailed
     */
    boolean isInlineSchemaEnabled();

    /**
     * Specifies tag ordering.
     *
     * @return Specifies tag ordering.
     */
    OrderBy getTagOrderBy();

    /**
     * Specifies the regex pattern used for header matching
     *
     * @return Specifies the regex pattern used for header matching
     */
    Pattern getHeaderPattern();

    /**
     * Specifies a custom comparator function to order tags.
     *
     * @return Specifies a custom comparator function to order tags.
     */
    Comparator<String> getTagOrdering();

    /**
     * Specifies operation ordering.
     *
     * @return Specifies operation ordering.
     */
    OrderBy getOperationOrderBy();

    /**
     * Specifies a custom comparator function to order operations.
     *
     * @return Specifies a custom comparator function to order operations.
     */
    Comparator<PathOperation> getOperationOrdering();

    /**
     * Specifies definition ordering.
     *
     * @return Specifies definition ordering.
     */
    OrderBy getDefinitionOrderBy();

    /**
     * Specifies a custom comparator function to order definitions.
     *
     * @return Specifies a custom comparator function to order definitions.
     */
    Comparator<String> getDefinitionOrdering();

    /**
     * Specifies parameter ordering.
     *
     * @return Specifies parameter ordering.
     */
    OrderBy getParameterOrderBy();

    /**
     * Specifies a custom comparator function to order parameters.
     *
     * @return Specifies a custom comparator function to order parameters.
     */
    Comparator<Parameter> getParameterOrdering();

    /**
     * Specifies property ordering.
     *
     * @return Specifies property ordering.
     */
    OrderBy getPropertyOrderBy();

    /**
     * Specifies a custom comparator function to order properties.
     *
     * @return Specifies a custom comparator function to order properties.
     */
    Comparator<String> getPropertyOrdering();

    /**
     * Specifies response ordering.
     *
     * @return Specifies response ordering.
     */
    OrderBy getResponseOrderBy();

    /**
     * Specifies a custom comparator function to order responses.
     *
     * @return Specifies a custom comparator function to order responses.
     */
    Comparator<String> getResponseOrdering();

    /**
     * Enable use of inter-document cross-references when needed.
     *
     * @return Enable use of inter-document cross-references when needed.
     */
    boolean isInterDocumentCrossReferencesEnabled();

    /**
     * Inter-document cross-references optional prefix.
     *
     * @return Inter-document cross-references optional prefix.
     */
    String getInterDocumentCrossReferencesPrefix();

    /**
     * Optionally isolate the body parameter, if any, from other parameters.
     *
     * @return Optionally isolate the body parameter, if any, from other parameters.
     */
    boolean isFlatBodyEnabled();

    /**
     * Optionally disable the security section for path sections
     *
     * @return Optionally disable the security section for path sections
     */
    boolean isPathSecuritySectionEnabled();

    /**
     * Optionally prefix all anchors for uniqueness.
     *
     * @return Optionally prefix all anchors for uniqueness.
     */
    String getAnchorPrefix();

    /**
     * Overview document name (without extension).
     *
     * @return the overview document name (without extension)
     */
    String getOverviewDocument();

    /**
     * Paths document name (without extension).
     *
     * @return the paths document name (without extension)
     */
    String getPathsDocument();

    /**
     * Definitions document name (without extension).
     *
     * @return the definitions document name (without extension)
     */
    String getDefinitionsDocument();

    /**
     * Security document name (without extension).
     *
     * @return the security document name (without extension)
     */
    String getSecurityDocument();

    /**
     * Separated operations sub-folder name.
     *
     * @return the operations sub-folder name
     */
    String getSeparatedOperationsFolder();

    /**
     * Separated definitions sub-folder name.
     *
     * @return the definitions sub-folder name
     */
    String getSeparatedDefinitionsFolder();

    /**
     * Specifies the line separator which should be used.
     *
     * @return the line separator
     */
    LineSeparator getLineSeparator();

    /**
     * Specifies the array element delimiter to use for multi-valued properties.
     *
     * @return the element delimiter if any
     */
    Character getListDelimiter();

    /**
     * Optionally allow lists in property values. Uses the {{@link #getListDelimiter()} to
     * delimit list values.
     *
     * @return whether lists are converted to arrays
     */
    boolean isListDelimiterEnabled();

    /**
     * Returns properties for extensions.
     *
     * @return the extension properties
     */
    Schema2MarkupProperties getExtensionsProperties();

    /**
     * Returns the list of page break locations
     *
     * @return List of PageBreakLocations
     */
    List<PageBreakLocations> getPageBreakLocations();

    /**
     * Returns custom timeout value.
     *
     * @return custom timeout value
     */
    int getAsciidocPegdownTimeoutMillis();

    /**
     * Returns format name which should be used to format request example string.
     *
     * @return `basic`, `curl` or `invoke-webrequest`
     */
    String getRequestExamplesFormat();

    /**
     * Returns format name which should be used to highlight source block with request example string
     *
     * @return any string or `default`
     */
    String getRequestExamplesSourceFormat();

    /**
     * Should we output optional query params in source block with request example string
     *
     * @return false if example request should contain only required params
     */
    boolean getRequestExamplesIncludeAllQueryParams();

    /**
     * How we should output array query params:
     *
     * @return `single` —  single time (similar to basic types), `commaSeparated` — single time with multiple comma
     * separated values, `multiple` times with same param name and different values, `multiple[]` times with array
     * brackets as param name suffix.
     */
    String getRequestExamplesQueryArrayStyle();

    /**
     * Should we hide, inherit or override hostname (e.g. with google.com) from  yml file
     *
     * @return `hide`, `inherit` or string with hostname to be used in request example
     */
    String getRequestExamplesHost();

    /**
     * Should we hide, inherit or override schema (http, https name it) from yml file
     *
     * @return `hide`, `inherit` or string with schema name to be used in request example
     */
    String getRequestExamplesSchema();

    /**
     * Should we hide or show base path in example request endpoint address
     *
     * @return true or false
     */
    boolean getRequestExamplesHideBasePath();
}
