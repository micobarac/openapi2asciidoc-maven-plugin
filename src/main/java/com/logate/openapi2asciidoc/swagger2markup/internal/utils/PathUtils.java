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
package com.logate.openapi2asciidoc.swagger2markup.internal.utils;

import com.logate.openapi2asciidoc.swagger2markup.core.model.PathOperation;
import com.logate.openapi2asciidoc.swagger2markup.model.SwaggerPathOperation;
import io.swagger.models.HttpMethod;
import io.swagger.models.Operation;
import io.swagger.models.Path;

import java.util.*;

import static org.apache.commons.lang3.StringUtils.isBlank;

public class PathUtils {

    /**
     * Returns the operations of a path as a map which preserves the insertion order.
     *
     * @param path the path
     * @return the operations of a path as a map
     */
    private static Map<HttpMethod, Operation> getOperationMap(Path path) {
        Map<HttpMethod, Operation> result = new LinkedHashMap<>();

        if (path.getGet() != null) {
            result.put(HttpMethod.GET, path.getGet());
        }
        if (path.getPut() != null) {
            result.put(HttpMethod.PUT, path.getPut());
        }
        if (path.getPost() != null) {
            result.put(HttpMethod.POST, path.getPost());
        }
        if (path.getDelete() != null) {
            result.put(HttpMethod.DELETE, path.getDelete());
        }
        if (path.getPatch() != null) {
            result.put(HttpMethod.PATCH, path.getPatch());
        }
        if (path.getHead() != null) {
            result.put(HttpMethod.HEAD, path.getHead());
        }
        if (path.getOptions() != null) {
            result.put(HttpMethod.OPTIONS, path.getOptions());
        }

        return result;
    }

    /**
     * Converts the Swagger paths into a list of PathOperations.
     *
     * @param paths      the Swagger paths
     * @param host       the host of all paths
     * @param basePath   the basePath of all paths
     * @param comparator the comparator to use.
     * @return the path operations
     */
    public static List<SwaggerPathOperation> toPathOperationsList(Map<String, Path> paths,
                                                                  String host,
                                                                  String basePath,
                                                                  Comparator<PathOperation> comparator) {
        List<SwaggerPathOperation> pathOperations = new ArrayList<>();

        paths.forEach((relativePath, path) ->
                pathOperations.addAll(toPathOperationsList(host + basePath + relativePath, path)));
        if (comparator != null) {
            pathOperations.sort(comparator);
        }
        return pathOperations;
    }

    /**
     * Converts a Swagger path into a PathOperation.
     *
     * @param path      the  path
     * @param pathModel the Swagger Path model
     * @return the path operations
     */
    public static List<SwaggerPathOperation> toPathOperationsList(String path, Path pathModel) {
        List<SwaggerPathOperation> pathOperations = new ArrayList<>();
        getOperationMap(pathModel).forEach((httpMethod, operation) -> {
            String id = operation.getOperationId();
            if (id == null)
                id = path + " " + httpMethod.toString().toLowerCase();

            String operationName = operation.getSummary();
            if (isBlank(operationName)) {
                operationName = httpMethod.toString() + " " + path;
            }
            pathOperations.add(new SwaggerPathOperation(httpMethod.name(), path, id, operationName, operation));
        });
        return pathOperations;
    }
}
