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

package org.jetbrains.kotlin.generators.injectors

import com.intellij.openapi.project.Project
import org.jetbrains.kotlin.context.GlobalContext
import org.jetbrains.kotlin.context.GlobalContextImpl
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.descriptors.impl.ModuleDescriptorImpl
import org.jetbrains.kotlin.resolve.*
import org.jetbrains.kotlin.load.java.JavaClassFinderImpl
import org.jetbrains.kotlin.resolve.jvm.JavaDescriptorResolver
import org.jetbrains.kotlin.load.java.components.*
import org.jetbrains.kotlin.load.java.sam.SamConversionResolverImpl
import org.jetbrains.kotlin.load.kotlin.VirtualFileFinder
import org.jetbrains.kotlin.resolve.lazy.ResolveSession
import org.jetbrains.kotlin.resolve.lazy.declarations.DeclarationProviderFactory
import org.jetbrains.kotlin.types.expressions.ExpressionTypingServices
import org.jetbrains.kotlin.generators.di.*
import org.jetbrains.kotlin.types.expressions.ExpressionTypingComponents
import org.jetbrains.kotlin.types.expressions.ExpressionTypingUtils
import org.jetbrains.kotlin.resolve.calls.CallResolver
import org.jetbrains.kotlin.load.java.structure.impl.JavaPropertyInitializerEvaluatorImpl
import com.intellij.psi.search.GlobalSearchScope
import org.jetbrains.kotlin.load.java.lazy.ModuleClassResolver
import org.jetbrains.kotlin.load.kotlin.DeserializationComponentsForJava
import org.jetbrains.kotlin.load.java.lazy.SingleModuleClassResolver
import org.jetbrains.kotlin.load.kotlin.VirtualFileFinderFactory
import org.jetbrains.kotlin.resolve.jvm.TopDownAnalyzerFacadeForJVM
import org.jetbrains.kotlin.load.kotlin.KotlinJvmCheckerProvider
import org.jetbrains.kotlin.resolve.lazy.KotlinCodeAnalyzer
import org.jetbrains.kotlin.load.java.JavaFlexibleTypeCapabilitiesProvider
import org.jetbrains.kotlin.context.LazyResolveToken
import org.jetbrains.kotlin.resolve.jvm.JavaLazyAnalyzerPostConstruct
import org.jetbrains.kotlin.resolve.jvm.JavaDescriptorResolverPostConstruct
import org.jetbrains.kotlin.resolve.lazy.ScopeProvider
import org.jetbrains.kotlin.js.resolve.KotlinJsCheckerProvider
import org.jetbrains.kotlin.types.DynamicTypesAllowed
import org.jetbrains.kotlin.types.DynamicTypesSettings

// NOTE: After making changes, you need to re-generate the injectors.
//       To do that, you can run main in this file.
public fun main(args: Array<String>) {
    for (generator in createInjectorGenerators()) {
        try {
            generator.generate()
        }
        catch (e: Throwable) {
            System.err.println(generator.getOutputFile())
            throw e
        }
    }
}

private val DI_DEFAULT_PACKAGE = "org.jetbrains.kotlin.di"

public fun createInjectorGenerators(): List<DependencyInjectorGenerator> =
        listOf(
                generatorForTopDownAnalyzerBasic(),
                generatorForLazyTopDownAnalyzerBasic(),
                generatorForTopDownAnalyzerForJvm(),
                generatorForJavaDescriptorResolver(),
                generatorForLazyResolveWithJava(),
                generatorForTopDownAnalyzerForJs(),
                generatorForMacro(),
                generatorForTests(),
                generatorForLazyResolve(),
                generatorForBodyResolve(),
                generatorForLazyBodyResolve(),
                generatorForReplWithJava()
        )

private fun generatorForTopDownAnalyzerBasic() =
        generator("compiler/frontend/src", DI_DEFAULT_PACKAGE, "InjectorForTopDownAnalyzerBasic") {
            parameter<Project>()
            parameter<GlobalContext>(useAsContext = true)
            parameter<BindingTrace>()
            publicParameter<ModuleDescriptor>(useAsContext = true)

            publicField<TopDownAnalyzer>()

            field<MutablePackageFragmentProvider>()

            parameter<AdditionalCheckerProvider>()
            parameter<DynamicTypesSettings>()
        }

private fun generatorForLazyTopDownAnalyzerBasic() =
        generator("compiler/frontend/src", DI_DEFAULT_PACKAGE, "InjectorForLazyTopDownAnalyzerBasic") {
            commonForResolveSessionBased()

            publicField<LazyTopDownAnalyzer>()

            field<AdditionalCheckerProvider.DefaultProvider>()
        }

private fun generatorForLazyBodyResolve() =
        generator("compiler/frontend/src", DI_DEFAULT_PACKAGE, "InjectorForLazyBodyResolve") {
            parameter<Project>()
            parameter<GlobalContext>(useAsContext = true)
            parameter<KotlinCodeAnalyzer>(name = "analyzer")
            parameter<BindingTrace>()
            parameter<AdditionalCheckerProvider>()
            parameter<DynamicTypesSettings>()

            field<ModuleDescriptor>(init = GivenExpression("analyzer.getModuleDescriptor()"), useAsContext = true)

            publicField<LazyTopDownAnalyzer>()
        }

private fun generatorForTopDownAnalyzerForJs() =
        generator("js/js.frontend/src", DI_DEFAULT_PACKAGE, "InjectorForTopDownAnalyzerForJs") {
            commonForResolveSessionBased()

            publicField<LazyTopDownAnalyzer>()

            field<MutablePackageFragmentProvider>()
            field<KotlinJsCheckerProvider>()
            field<DynamicTypesAllowed>()
        }

private fun generatorForTopDownAnalyzerForJvm() =
        generator("compiler/frontend.java/src", DI_DEFAULT_PACKAGE, "InjectorForTopDownAnalyzerForJvm") {
            commonForJavaTopDownAnalyzer()
        }

private fun generatorForJavaDescriptorResolver() =
        generator("compiler/frontend.java/src", DI_DEFAULT_PACKAGE, "InjectorForJavaDescriptorResolver") {
            parameter<Project>()
            parameter<BindingTrace>()

            publicField<GlobalContextImpl>(useAsContext = true,
                        init = GivenExpression("org.jetbrains.kotlin.context.ContextPackage.GlobalContext()"))
            publicField<ModuleDescriptorImpl>(name = "module",
                        init = GivenExpression(javaClass<TopDownAnalyzerFacadeForJVM>().getName() + ".createJavaModule(\"<fake-jdr-module>\")"))
            publicField<JavaDescriptorResolver>()
            publicField<JavaClassFinderImpl>()

            field<GlobalSearchScope>(
                  init = GivenExpression(javaClass<GlobalSearchScope>().getName() + ".allScope(project)"))

            field<TraceBasedExternalSignatureResolver>()
            field<TraceBasedJavaResolverCache>()
            field<TraceBasedErrorReporter>()
            field<PsiBasedMethodSignatureChecker>()
            field<PsiBasedExternalAnnotationResolver>()
            field<JavaPropertyInitializerEvaluatorImpl>()
            field<SamConversionResolverImpl>()
            field<JavaSourceElementFactoryImpl>()
            field<SingleModuleClassResolver>()
            field<JavaDescriptorResolverPostConstruct>()

            field<VirtualFileFinder>(
                  init = GivenExpression(javaClass<VirtualFileFinder>().getName() + ".SERVICE.getInstance(project)"))
        }

private fun generatorForLazyResolveWithJava() =
        generator("compiler/frontend.java/src", DI_DEFAULT_PACKAGE, "InjectorForLazyResolveWithJava") {
            commonForResolveSessionBased()

            parameter<GlobalSearchScope>(name = "moduleContentScope")

            parameter<ModuleClassResolver>()

            publicField<JavaDescriptorResolver>()

            field<VirtualFileFinder>(
                  init = GivenExpression(javaClass<VirtualFileFinderFactory>().getName()
                                         + ".SERVICE.getInstance(project).create(moduleContentScope)")
            )

            field<JavaClassFinderImpl>()
            field<TraceBasedExternalSignatureResolver>()
            field<LazyResolveBasedCache>()
            field<TraceBasedErrorReporter>()
            field<PsiBasedMethodSignatureChecker>()
            field<PsiBasedExternalAnnotationResolver>()
            field<JavaPropertyInitializerEvaluatorImpl>()
            field<SamConversionResolverImpl>()
            field<JavaSourceElementFactoryImpl>()
            field<JavaFlexibleTypeCapabilitiesProvider>()
            field<LazyResolveToken>()
            field<JavaLazyAnalyzerPostConstruct>()

            field<KotlinJvmCheckerProvider>()
        }

private fun generatorForReplWithJava() =
        generator("compiler/frontend.java/src", DI_DEFAULT_PACKAGE, "InjectorForReplWithJava") {
            commonForJavaTopDownAnalyzer()
            parameter<ScopeProvider.AdditionalFileScopeProvider>()
        }

private fun generatorForMacro() =
        generator("compiler/frontend/src", DI_DEFAULT_PACKAGE, "InjectorForMacros") {
            parameter<Project>()
            parameter<ModuleDescriptor>(useAsContext = true)

            publicField<ExpressionTypingServices>()
            publicField<ExpressionTypingComponents>()
            publicField<CallResolver>()

            field<GlobalContext>(useAsContext = true,
                  init = GivenExpression("org.jetbrains.kotlin.context.ContextPackage.GlobalContext()"))

            field<AdditionalCheckerProvider.DefaultProvider>()
        }

private fun generatorForTests() =
        generator("compiler/tests", DI_DEFAULT_PACKAGE, "InjectorForTests") {
            parameter<Project>()
            parameter<ModuleDescriptor>(useAsContext = true)

            publicField<DescriptorResolver>()
            publicField<ExpressionTypingServices>()
            publicField<ExpressionTypingUtils>()
            publicField<TypeResolver>()

            field<GlobalContext>(init = GivenExpression("org.jetbrains.kotlin.context.ContextPackage.GlobalContext()"),
                  useAsContext = true)

            field<KotlinJvmCheckerProvider>()
        }

private fun generatorForBodyResolve() =
        generator("compiler/frontend/src", DI_DEFAULT_PACKAGE, "InjectorForBodyResolve") {
            parameter<Project>()
            parameter<GlobalContext>(useAsContext = true)
            parameter<BindingTrace>()
            parameter<ModuleDescriptor>(useAsContext = true)
            parameter<AdditionalCheckerProvider>()
            parameter<PartialBodyResolveProvider>()

            publicField<BodyResolver>()
        }

private fun generatorForLazyResolve() =
        generator("compiler/frontend/src", DI_DEFAULT_PACKAGE, "InjectorForLazyResolve") {
            parameter<Project>()
            parameter<GlobalContext>(useAsContext = true)
            parameter<ModuleDescriptorImpl>(useAsContext = true)
            parameter<DeclarationProviderFactory>()
            parameter<BindingTrace>()
            parameter<AdditionalCheckerProvider>()
            parameter<DynamicTypesSettings>()

            publicField<ResolveSession>()

            field<LazyResolveToken>()
        }

private fun DependencyInjectorGenerator.commonForResolveSessionBased() {
    parameter<Project>()
    parameter<GlobalContext>(useAsContext = true)
    parameter<BindingTrace>()
    parameter<ModuleDescriptorImpl>(name = "module", useAsContext = true)
    parameter<DeclarationProviderFactory>()

    publicField<ResolveSession>()
}

private fun DependencyInjectorGenerator.commonForJavaTopDownAnalyzer() {
    commonForResolveSessionBased()

    parameter<GlobalSearchScope>(name = "moduleContentScope")

    publicField<LazyTopDownAnalyzer>()
    publicField<JavaDescriptorResolver>()
    publicField<DeserializationComponentsForJava>()

    field<VirtualFileFinder>(
          init = GivenExpression(javaClass<VirtualFileFinderFactory>().getName()
                                 + ".SERVICE.getInstance(project).create(moduleContentScope)")
    )

    field<JavaClassFinderImpl>()
    field<TraceBasedExternalSignatureResolver>()
    field<LazyResolveBasedCache>()
    field<TraceBasedErrorReporter>()
    field<PsiBasedMethodSignatureChecker>()
    field<PsiBasedExternalAnnotationResolver>()
    field<JavaPropertyInitializerEvaluatorImpl>()
    field<SamConversionResolverImpl>()
    field<JavaSourceElementFactoryImpl>()
    field<MutablePackageFragmentProvider>()
    field<SingleModuleClassResolver>()
    field<JavaLazyAnalyzerPostConstruct>()
    field<JavaFlexibleTypeCapabilitiesProvider>()

    field<KotlinJvmCheckerProvider>()

    field<VirtualFileFinder>(init = GivenExpression(javaClass<VirtualFileFinder>().getName() + ".SERVICE.getInstance(project)"))
}


private fun generator(
        targetSourceRoot: String,
        injectorPackageName: String,
        injectorClassName: String,
        body: DependencyInjectorGenerator.() -> Unit
) = generator(targetSourceRoot, injectorPackageName, injectorClassName, "org.jetbrains.kotlin.generators.injectors.InjectorsPackage", body)
