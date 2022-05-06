package com.logate.openapi2asciidoc;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.logate.openapi2asciidoc.openapi2markup.OpenAPI2MarkupConverter;
import com.logate.openapi2asciidoc.openapi2markup.OpenSchema2MarkupConfig;
import com.logate.openapi2asciidoc.openapi2markup.config.builder.OpenAPI2MarkupConfigBuilder;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.URIBuilder;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Goal which touches a timestamp file.
 *
 * @goal touch
 *
 * @phase process-sources
 */
@Mojo(name = "convertOpenapi2Asciidoc")
public class OpenAPI2AsciidocMojo extends AbstractMojo
{
    @Parameter(property = "swaggerInput", required = true)
    protected String swaggerInput;

    @Parameter(property = "outputDir")
    protected File outputDir;

    @Parameter(property = "outputFile")
    protected File outputFile;

    @Parameter
    protected Map<String, String> config = new HashMap<>();

    @Parameter(property = "skip")
    protected boolean skip;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        if (skip) {
            getLog().info("convertOpenapi2Asciidoc is skipped.");
            return;
        }

        if (getLog().isDebugEnabled()) {
            getLog().debug("convertOpenapi2Asciidoc goal started");
            getLog().debug("swaggerInput: " + swaggerInput);
            getLog().debug("outputDir: " + outputDir);
            getLog().debug("outputFile: " + outputFile);
            for (Map.Entry<String, String> entry : this.config.entrySet()) {
                getLog().debug(entry.getKey() + ": " + entry.getValue());
            }
        }

        try {
            OpenSchema2MarkupConfig openSchema2MarkupConfig = new OpenAPI2MarkupConfigBuilder(config).build();
            if (isLocalFolder(swaggerInput)) {
                getSwaggerFiles(new File(swaggerInput), true).forEach(f -> {
                    OpenAPI2MarkupConverter converter = OpenAPI2MarkupConverter.from(f.toURI())
                            .withConfig(openSchema2MarkupConfig)
                            .build();
                    openapiToMarkup(converter, true);
                });
            } else {
                OpenAPI2MarkupConverter converter = OpenAPI2MarkupConverter.from(new URIBuilder(swaggerInput).build())
                        .withConfig(openSchema2MarkupConfig).build();
                openapiToMarkup(converter, false);
            }
        } catch (Exception e) {
            throw new MojoFailureException("Failed to execute goal 'convertOpenapi2Asciidoc'", e);
        }
        getLog().debug("convertOpenapi2Asciidoc goal finished");
    }

    private boolean isLocalFolder(String swaggerInput) {
        return !swaggerInput.toLowerCase().startsWith("http") && new File(swaggerInput).isDirectory();
    }

    private void openapiToMarkup(OpenAPI2MarkupConverter converter, boolean inputIsLocalFolder) {
        if (outputFile != null) {
            Path useFile = outputFile.toPath();
            /*
             * If user has specified input folder with multiple files to convert,
             * and has specified a single output file, then route all conversions
             * into one file under each 'new' sub-directory, which corresponds to
             * each input file.
             * Otherwise, specifying the output file with an input DIRECTORY means
             * last file converted wins.
             */
            if (inputIsLocalFolder) {
                if ( outputDir != null ) {
                    File effectiveOutputDir = outputDir;
                    effectiveOutputDir = getEffectiveOutputDirWhenInputIsAFolder(converter);
                    converter.getContext().setOutputPath(effectiveOutputDir.toPath());
                    useFile =  Paths.get(effectiveOutputDir.getPath(), useFile.getFileName().toString());
                }
            }
            if ( getLog().isInfoEnabled() ) {
                getLog().info("Converting input to one file: " + useFile);
            }
            converter.toFile(useFile);
        } else if (outputDir != null) {
            File effectiveOutputDir = outputDir;
            if (inputIsLocalFolder) {
                effectiveOutputDir = getEffectiveOutputDirWhenInputIsAFolder(converter);
            }
            if (getLog().isInfoEnabled()) {
                getLog().info("Converting input to multiple files in folder: '" + effectiveOutputDir + "'");
            }
            converter.toFolder(effectiveOutputDir.toPath());
        } else {
            throw new IllegalArgumentException("Either outputFile or outputDir parameter must be used");
        }
    }

    private File getEffectiveOutputDirWhenInputIsAFolder(OpenAPI2MarkupConverter converter) {
        String outputDirAddendum = getInputDirStructurePath(converter);
        if (multipleSwaggerFilesInSwaggerLocationFolder(converter)) {
            /*
             * If the folder the current Swagger file resides in contains at least one other Swagger file then the
             * output dir must have an extra subdir per file to avoid markdown files getting overwritten.
             */
            outputDirAddendum += File.separator + extractSwaggerFileNameWithoutExtension(converter);
        }
        return new File(outputDir, outputDirAddendum);
    }

    private String getInputDirStructurePath(OpenAPI2MarkupConverter converter) {
        /*
         * When the Swagger input is a local folder (e.g. /Users/foo/) you'll want to group the generated output in the
         * configured output directory. The most obvious approach is to replicate the folder structure from the input
         * folder to the output folder. Example:
         * - swaggerInput is set to /Users/foo
         * - there's a single Swagger file at /Users/foo/bar-service/v1/bar.yaml
         * - outputDir is set to /tmp/asciidoc
         * -> markdown files from bar.yaml are generated to /tmp/asciidoc/bar-service/v1
         */
        String swaggerFilePath = new File(converter.getContext().getSwaggerLocation()).getAbsolutePath(); // /Users/foo/bar-service/v1/bar.yaml
        String swaggerFileFolder = StringUtils.substringBeforeLast(swaggerFilePath, File.separator); // /Users/foo/bar-service/v1
        return StringUtils.remove(swaggerFileFolder, getSwaggerInputAbsolutePath()); // /bar-service/v1
    }

    private boolean multipleSwaggerFilesInSwaggerLocationFolder(OpenAPI2MarkupConverter converter) {
        Collection<File> swaggerFiles = getSwaggerFiles(new File(converter.getContext().getSwaggerLocation())
                .getParentFile(), false);
        return swaggerFiles != null && swaggerFiles.size() > 1;
    }

    private String extractSwaggerFileNameWithoutExtension(OpenAPI2MarkupConverter converter) {
        return FilenameUtils.removeExtension(new File(converter.getContext().getSwaggerLocation()).getName());
    }

    private Collection<File> getSwaggerFiles(File directory, boolean recursive) {
        return FileUtils.listFiles(directory, new String[]{"yaml", "yml", "json"}, recursive);
    }

    /*
     * The 'swaggerInput' provided by the user can be anything; it's just a string. Hence, it could by Unix-style,
     * Windows-style or even a mix thereof. This methods turns the input into a File and returns its absolute path. It
     * will be platform dependent as far as file separators go but at least the separators will be consistent.
     */
    private String getSwaggerInputAbsolutePath(){
        return new File(swaggerInput).getAbsolutePath();
    }
}
