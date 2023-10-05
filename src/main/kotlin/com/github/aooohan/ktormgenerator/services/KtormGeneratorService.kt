/*
 *    Copyright 2023 [lihan lihan@apache.org]
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.github.aooohan.ktormgenerator.services

import com.github.aooohan.ktormgenerator.dto.TableUIInfo
import com.intellij.database.psi.DbTable
import com.intellij.openapi.components.Service
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModuleRootManager
import java.util.*

/**
 * @author : lihan
 * @date : 2023/10/5 10:16
 */
@Service(Service.Level.PROJECT)
class KtormGeneratorService(project: Project) {


    fun parseModuleDirPath(module: Module): String {
        var moduleDirPath = ModuleUtil.getModuleDirPath(module)
        val childModuleIndex = moduleDirPath.indexOf(".idea")
        if (childModuleIndex > 0) {
            getPathFromModule(module)?.let {
                moduleDirPath = it
            } ?: run {
                moduleDirPath = moduleDirPath.substring(0, childModuleIndex)
            }
        }
        return moduleDirPath
    }
    private fun getPathFromModule(module: Module): String? = ModuleRootManager.getInstance(module).contentRoots
            .first()?.path
    fun parseTableNames(dbTables: List<DbTable>): List<TableUIInfo> {
        return dbTables.map {
            TableUIInfo(
                tableName = it.name,
                className = dbStringToCamelStyle(it.name) ?: ""
            )
        }
    }
    private fun dbStringToCamelStyle(str: String?): String? {
        str?.let {
            val lowerCaseStr = it.lowercase(Locale.getDefault())
            val sb = StringBuilder()
            sb.append(lowerCaseStr[0].uppercaseChar())
            var i = 1
            while (i < lowerCaseStr.length) {
                val c = lowerCaseStr[i]
                if (c != '_') {
                    sb.append(c)
                } else {
                    if (i + 1 < lowerCaseStr.length) {
                        sb.append(lowerCaseStr[i + 1].toUpperCase())
                        i++
                    }
                }
                i++
            }
            return sb.toString()
        }
        return null
    }
}