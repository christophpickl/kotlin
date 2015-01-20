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

package org.jetbrains.kotlin.idea.completion.smart

import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.types.JetType
import java.util.HashMap
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.DescriptorUtils
import org.jetbrains.kotlin.types.SubstitutionUtils
import org.jetbrains.kotlin.types.Variance

public object HeuristicSignatures {
    private val signatures = HashMap<Pair<FqName, Name>, List<String>>()

    private fun registerHeuristicSignature(
            classFqName: String,
            name: String,
            vararg parameterTypes: String) {
        signatures[FqName(classFqName) to Name.identifier(name)] = parameterTypes.toList()
    }

    {
        registerHeuristicSignature("kotlin.Collection", "contains", "E")
    }

    public fun correctedParameterType(function: FunctionDescriptor, parameterIndex: Int): JetType? {
        val ownerType = function.getDispatchReceiverParameter()?.getType() ?: return null

        val superFunctions = function.getOverriddenDescriptors()
        if (superFunctions.isNotEmpty()) {
            for (superFunction in superFunctions) {
                val correctedType = correctedParameterType(superFunction, parameterIndex) ?: continue
                val typeSubstitutor = SubstitutionUtils.buildDeepSubstitutor(ownerType)
                return typeSubstitutor.safeSubstitute(correctedType, Variance.INVARIANT)
            }
            return null
        }
        else {
            val ownerClass = ownerType.getConstructor().getDeclarationDescriptor() ?: return null
            val classFqName = DescriptorUtils.getFqNameSafe(ownerClass)
            val parameterTypes = signatures[classFqName to function.getName()] ?: return null
            val typeStr = parameterTypes[parameterIndex]
            val classTypeParams = ownerClass.getTypeConstructor().getParameters()
            val matchedParameterIndex = classTypeParams.indices.firstOrNull { classTypeParams[it].getName().asString() == typeStr } ?: return null //TODO
            return ownerType.getArguments()[matchedParameterIndex].getType()
        }
    }
}