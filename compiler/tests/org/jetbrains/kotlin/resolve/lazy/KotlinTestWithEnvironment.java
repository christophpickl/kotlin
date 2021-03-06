/*
 * Copyright 2010-2015 JetBrains s.r.o.
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

package org.jetbrains.kotlin.resolve.lazy;

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.cli.jvm.compiler.JetCoreEnvironment;
import org.jetbrains.kotlin.test.KotlinTestWithEnvironmentManagement;

public abstract class KotlinTestWithEnvironment extends KotlinTestWithEnvironmentManagement {
    private JetCoreEnvironment environment;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        environment = createEnvironment();
    }

    @Override
    protected void tearDown() throws Exception {
        environment = null;
        super.tearDown();
    }

    protected abstract JetCoreEnvironment createEnvironment();

    @NotNull
    public JetCoreEnvironment getEnvironment() {
        return environment;
    }

    @NotNull
    public Project getProject() {
        return getEnvironment().getProject();
    }
}
