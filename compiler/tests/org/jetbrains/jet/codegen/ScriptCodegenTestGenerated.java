/*
 * Copyright 2010-2014 JetBrains s.r.o.
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

package org.jetbrains.jet.codegen;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.jetbrains.jet.JetTestUtils;
import org.jetbrains.jet.test.InnerTestClasses;
import org.jetbrains.jet.test.TestMetadata;

import java.io.File;
import java.util.regex.Pattern;

/** This class is generated by {@link org.jetbrains.jet.generators.tests.TestsPackage}. DO NOT MODIFY MANUALLY */
@SuppressWarnings("all")
@TestMetadata("compiler/testData/codegen/script")
public class ScriptCodegenTestGenerated extends AbstractScriptCodegenTest {
    public void testAllFilesPresentInScript() throws Exception {
        JetTestUtils.assertAllTestsPresentByMetadata(this.getClass(), new File("compiler/testData/codegen/script"), Pattern.compile("^(.+)\\.kts$"), true);
    }
    
    @TestMetadata("empty.kts")
    public void testEmpty() throws Exception {
        doTest("compiler/testData/codegen/script/empty.kts");
    }
    
    @TestMetadata("helloWorld.kts")
    public void testHelloWorld() throws Exception {
        doTest("compiler/testData/codegen/script/helloWorld.kts");
    }
    
    @TestMetadata("parameter.kts")
    public void testParameter() throws Exception {
        doTest("compiler/testData/codegen/script/parameter.kts");
    }
    
    @TestMetadata("parameterArray.kts")
    public void testParameterArray() throws Exception {
        doTest("compiler/testData/codegen/script/parameterArray.kts");
    }
    
    @TestMetadata("parameterClosure.kts")
    public void testParameterClosure() throws Exception {
        doTest("compiler/testData/codegen/script/parameterClosure.kts");
    }
    
    @TestMetadata("parameterLong.kts")
    public void testParameterLong() throws Exception {
        doTest("compiler/testData/codegen/script/parameterLong.kts");
    }
    
    @TestMetadata("secondLevelFunction.kts")
    public void testSecondLevelFunction() throws Exception {
        doTest("compiler/testData/codegen/script/secondLevelFunction.kts");
    }
    
    @TestMetadata("secondLevelFunctionClosure.kts")
    public void testSecondLevelFunctionClosure() throws Exception {
        doTest("compiler/testData/codegen/script/secondLevelFunctionClosure.kts");
    }
    
    @TestMetadata("secondLevelVal.kts")
    public void testSecondLevelVal() throws Exception {
        doTest("compiler/testData/codegen/script/secondLevelVal.kts");
    }
    
    @TestMetadata("simpleClass.kts")
    public void testSimpleClass() throws Exception {
        doTest("compiler/testData/codegen/script/simpleClass.kts");
    }
    
    @TestMetadata("string.kts")
    public void testString() throws Exception {
        doTest("compiler/testData/codegen/script/string.kts");
    }
    
    @TestMetadata("topLevelFunction.kts")
    public void testTopLevelFunction() throws Exception {
        doTest("compiler/testData/codegen/script/topLevelFunction.kts");
    }
    
    @TestMetadata("topLevelFunctionClosure.kts")
    public void testTopLevelFunctionClosure() throws Exception {
        doTest("compiler/testData/codegen/script/topLevelFunctionClosure.kts");
    }
    
    @TestMetadata("topLevelProperty.kts")
    public void testTopLevelProperty() throws Exception {
        doTest("compiler/testData/codegen/script/topLevelProperty.kts");
    }
    
}