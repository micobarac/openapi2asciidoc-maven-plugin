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

import ch.netzwerg.paleo.DataFrame;
import ch.netzwerg.paleo.StringColumn;
import com.logate.openapi2asciidoc.swagger2markup.Swagger2MarkupConverter;
import com.logate.openapi2asciidoc.swagger2markup.markup.builder.MarkupDocBuilder;
import com.logate.openapi2asciidoc.swagger2markup.markup.builder.MarkupLanguage;
import com.logate.openapi2asciidoc.swagger2markup.markup.builder.MarkupTableColumn;
import com.logate.openapi2asciidoc.swagger2markup.spi.MarkupComponent;
import io.vavr.collection.Array;
import io.vavr.collection.IndexedSeq;
import io.vavr.collection.List;
import org.apache.commons.lang3.StringUtils;


public class TableComponent extends MarkupComponent<TableComponent.Parameters> {

    public static final String WIDTH_RATIO = "widthRatio";
    public static final String HEADER_COLUMN = "headerColumn";

    public TableComponent(Swagger2MarkupConverter.SwaggerContext context) {
        super(context);
    }

    public static Parameters parameters(StringColumn... columns) {
        return new Parameters(columns);
    }

    public static boolean isNotBlank(StringColumn column) {
        return !column.getValues().filter(StringUtils::isNotBlank).isEmpty();
    }

    @Override
    public MarkupDocBuilder apply(MarkupDocBuilder markupDocBuilder, Parameters params) {
        DataFrame dataFrame = params.dataFrame;
        java.util.List<MarkupTableColumn> columnSpecs = dataFrame.getColumns().map(column -> {
                    Integer widthRatio = Integer.valueOf(column.getMetaData().get(WIDTH_RATIO).getOrElse("0"));
                    return new MarkupTableColumn(column.getId().getName())
                            .withWidthRatio(widthRatio)
                            .withHeaderColumn(Boolean.parseBoolean(column.getMetaData().get(HEADER_COLUMN).getOrElse("false")))
                            .withMarkupSpecifiers(MarkupLanguage.ASCIIDOC, ".^" + widthRatio + "a");
                }
        ).toJavaList();

        IndexedSeq<IndexedSeq<String>> columnValues = dataFrame.getColumns()
                .map(column -> ((StringColumn) column).getValues());

        java.util.List<java.util.List<String>> cells = Array.range(0, dataFrame.getRowCount())
                .map(rowNumber -> columnValues.map(values -> values.get(rowNumber)).toJavaList()).toJavaList();

        return markupDocBuilder.tableWithColumnSpecs(columnSpecs, cells);
    }

    public static class Parameters {
        private final DataFrame dataFrame;

        public Parameters(StringColumn... columns) {
            this.dataFrame = DataFrame.ofAll(List.of(columns).filter(TableComponent::isNotBlank));
        }
    }
}
