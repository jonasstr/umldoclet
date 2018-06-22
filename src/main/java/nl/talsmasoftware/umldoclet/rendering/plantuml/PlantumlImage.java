/*
 * Copyright 2016-2018 Talsma ICT
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
package nl.talsmasoftware.umldoclet.rendering.plantuml;

import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.SourceStringReader;
import nl.talsmasoftware.umldoclet.configuration.Configuration;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import static java.util.Objects.requireNonNull;

/**
 * @author Sjoerd Talsma
 */
public class PlantumlImage {
    private static final Pattern LINK_PATTERN = Pattern.compile("\\[\\[\\S+]]");

    private final Configuration config;
    private final File file;
    private final FileFormat fileFormat;
    private final Supplier<OutputStream> outputStreamSupplier;

    protected PlantumlImage(Configuration config, File file, FileFormat fileFormat, Supplier<OutputStream> outputStreamSupplier) {
        this.config = requireNonNull(config, "Configuration is <null>.");
        this.file = requireNonNull(file, "Image file is <null>.");
        this.fileFormat = requireNonNull(fileFormat, "File format is <null>.");
        this.outputStreamSupplier = requireNonNull(outputStreamSupplier, "Output stream supplier is <null>.");
    }

    public static Optional<PlantumlImage> fromFile(Configuration config, File file) {
        return fileFormatOf(file).map(format -> new PlantumlImage(config, file, format, () -> createFileOutputStream(file)));
    }

    public String getName() {
        return file.getPath();
    }

    final void renderPlantuml(String uml) throws IOException {
        requireNonNull(uml, "PlantUML diagram is <null>.");
        try (OutputStream imageOutput = new BufferedOutputStream(outputStreamSupplier.get())) {
            new SourceStringReader(filterBrokenLinks(uml)).outputImage(imageOutput, new FileFormatOption(fileFormat));
        }
    }

    private String filterBrokenLinks(String uml) {
        return LINK_PATTERN.matcher(uml).replaceAll(result -> {
            String link = uml.substring(result.start() + 2, result.end() - 2);
            return fixLink(link).map(lnk -> "[[" + lnk + "]]").orElse("");
        });
    }

    private Optional<String> fixLink(String link) {
        // For now only keep links that work relative from where the diagram ends up
        File f = new File(file.getParent(), link);
        if (f.exists()) {
            return Optional.of(link);
        }
        // TODO: Fix images relative to possible umlImagedir
        // TODO: Fix possible JDK documentation links?
        return Optional.empty();
    }

    @Override
    public String toString() {
        return file.getName();
    }

    private static Optional<FileFormat> fileFormatOf(File file) {
        if (file == null) return Optional.empty();
        FileFormat result = null;
        final String name = file.getName().toLowerCase();
        for (FileFormat format : FileFormat.values()) {
            if (name.endsWith(format.getFileSuffix())) result = format;
        }
        return Optional.ofNullable(result);
    }

    private static OutputStream createFileOutputStream(File file) {
        try {
            return new FileOutputStream(file);
        } catch (IOException ioe) {
            throw new IllegalStateException("Could not create writer to PlantUML image: " + file, ioe);
        }
    }
}
