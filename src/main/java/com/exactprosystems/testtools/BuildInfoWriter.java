/******************************************************************************
 * Copyright 2009-2016 Exactpro (Exactpro Systems Limited)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.exactprosystems.testtools;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.apache.commons.lang3.StringUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskAction;

import com.google.common.collect.Sets;

public class BuildInfoWriter extends DefaultTask {
    private static final String CONFIGURATION_NAME = "compile";

    private static final String BUILD_NAME_ATTRIBUTE = "Build_Name";
    private static final String BUILD_NUMBER_ATTRIBUTE = "Build_Number";
    private static final String REVISION_ATTRIBUTE = "Implementation-Version";
    private static final String GIT_HASH_ATTRIBUTE = "Git_Hash";

    private static final String BUILD_NUMBER_PROPERTY = "build_number";
    private static final String DICTIONARY_VERSION_PROPERTY = "dictVer";
    private static final String RELEASE_PROPERTY = "release";

    private static final String BUILD_INFO_FILE_NAME = "buildinfo.htm";

    private static final int HEADER_SIZE = 3;

    public static void main(String[] args) throws IOException {
        File f = new File("/home/ivan.druzhinin/buildinfo.htm");
        List<String> lines = Files.readAllLines(f.toPath(), Charset.defaultCharset());
        System.out.println(lines.size());
    }

    @TaskAction
    public void writeBuildInfo() throws IOException {
        Project project = getProject();
        Iterable<File> files = project.getConfigurations().getByName(CONFIGURATION_NAME);
        Map<String, String> buildInfoMap = new TreeMap<String, String>();

        System.out.println("Component builds number:");

        for(File file : files) {
            try (JarFile jarFile = new JarFile(file)) {
                Manifest manifest = jarFile.getManifest();

                if(manifest != null) {
                    Attributes attributes = manifest.getMainAttributes();

                    String buildName = attributes.getValue(BUILD_NAME_ATTRIBUTE);
                    String buildNumber = attributes.getValue(BUILD_NUMBER_ATTRIBUTE);
                    String revision = attributes.getValue(REVISION_ATTRIBUTE);
                    String gitHash = attributes.getValue(GIT_HASH_ATTRIBUTE);

                    if(buildName != null && buildNumber != null && revision != null && gitHash != null) {
                        System.out.println(String.format("%-45s|%5s|%4s|%11s", file.getName(), revision, buildNumber, gitHash));
                        buildInfoMap.put(buildName, revision + "/" + gitHash);
                    }
                }
            }
        }

        File buildInfoFile = new File(project.getProjectDir(), BUILD_INFO_FILE_NAME);

        List<String> header = new ArrayList<String>();
        List<String> body = new ArrayList<String>();

        if(buildInfoFile.exists()) {
            List<String> lines = Files.readAllLines(buildInfoFile.toPath(), Charset.defaultCharset());

            header = lines.subList(0, HEADER_SIZE);
            body = lines.subList(HEADER_SIZE, lines.size());

            Set<String> components = Sets.newHashSet(StringUtils.split(header.get(2).replaceAll("<.*?>",  ","), ","));
            Set<String> newComponents = buildInfoMap.keySet();

            if(!components.equals(newComponents)) {
                List<String> newBody = new ArrayList<String>();

                newBody.add("</table>");
                newBody.add("<br>");
                newBody.addAll(header);
                newBody.addAll(body);

                body = newBody;
                header = getHeader(newComponents);
            }
        } else {
            header = getHeader(buildInfoMap.keySet());
        }

        Map<String,?> projectProperties = project.getProperties();
        StringBuilder line = new StringBuilder();

        String buildNumber = projectProperties.get(BUILD_NUMBER_PROPERTY).toString();
        String dictVer = projectProperties.get(DICTIONARY_VERSION_PROPERTY).toString();
        String release = projectProperties.get(RELEASE_PROPERTY).toString();

        line.append("<tr><td>");
        line.append(buildNumber);
        line.append("<td>");
        line.append(dictVer);
        line.append("<td>");
        line.append(release);
        line.append("<td>");
        line.append(StringUtils.join(buildInfoMap.values(), "<td>"));

        List<String> lines = new ArrayList<String>();

        lines.addAll(header);
        lines.add(line.toString());
        lines.addAll(body);

        Files.write(buildInfoFile.toPath(), lines);
    }

    private List<String> getHeader(Set<String> components) {
        List<String> header = new ArrayList<String>();

        header.add("<table border=\"1\" cellspacing=\"1\" cellpadding=\"2\" style=\"font-family:monospace\">");
        header.add(String.format("<tr><th rowspan=2>Build Number<th rowspan=2>Dictionary Version<th rowspan=2>Release<th colspan=%s>Components", components.size()));
        header.add("<tr><th>" + StringUtils.join(components, "<th>"));

        return header;
    }
}
