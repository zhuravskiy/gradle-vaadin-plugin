/*
* Copyright 2014 John Ahlroos
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package fi.jasoft.plugin.tasks;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction
import org.gradle.api.plugins.WarPluginConvention;
import org.gradle.api.file.FileTree

class CompileThemeTask extends DefaultTask {

    public static final String NAME = 'vaadinThemes'

    public CompileThemeTask() {
        description = "Compiles a Vaadin SASS theme into CSS"

        File webAppDir = project.convention.getPlugin(WarPluginConvention).webAppDir

        getInputs().files(project.fileTree(dir: webAppDir.canonicalPath + '/VAADIN/themes', include: '**/*.scss'))

        FileTree themes = project.fileTree(dir: webAppDir.canonicalPath + '/VAADIN/themes', include: '**/styles.scss')
        themes.each { File theme ->
            File dir = new File(theme.parent)
            File css = new File(dir.canonicalPath + '/styles.css')
            getOutputs().files(css)
        }
    }

    @TaskAction
    public void exec() {
        if (project.vaadin.version.startsWith('6')) {
            project.logger.error("SASS themes are not compatible with Vaadin 6.")
            return;
        }

        File webAppDir = project.convention.getPlugin(WarPluginConvention).webAppDir
        FileTree themes = project.fileTree(dir: webAppDir.canonicalPath + '/VAADIN/themes', include: '**/styles.scss')
        themes.each { File theme ->
            File dir = new File(theme.parent)
            project.logger.lifecycle("Compiling " + theme.canonicalPath + "...")
            project.javaexec {
                setMain('com.vaadin.sass.SassCompiler')
                setClasspath(project.sourceSets.main.runtimeClasspath + project.sourceSets.main.compileClasspath)
                setArgs([theme.canonicalPath, dir.canonicalPath + '/styles.css'])
            }
        }
    }
}