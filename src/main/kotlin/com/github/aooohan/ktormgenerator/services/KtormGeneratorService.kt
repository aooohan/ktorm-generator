/*
 *        KtormGenerator - a plugin for generating Ktorm entity class from database table.
 *        Copyright (C) <2023>  <Han Li>
 *
 *        This program is free software: you can redistribute it and/or modify
 *        it under the terms of the GNU General Public License as published by
 *        the Free Software Foundation, either version 3 of the License, or
 *        (at your option) any later version.
 *
 *        This program is distributed in the hope that it will be useful,
 *        but WITHOUT ANY WARRANTY; without even the implied warranty of
 *        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *        GNU General Public License for more details.
 *
 *        You should have received a copy of the GNU General Public License
 *        along with this program.  If not, see <https://www.gnu.org/licenses/>.
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
                className = convertNameToCamelStyle(it.name)
            )
        }
    }
    fun convertNameToCamelStyle(str: String, firstLetterLower: Boolean = false): String {
        // TEST_NAME
        // TestName
        // test_name
        // testName
        val result = if (str.contains("_")) {
            val sb = StringBuilder()
            var i = 0
            var toUp = !firstLetterLower
            while (i < str.length) {
                val c = str[i]
                toUp = if (c == '_') {
                    true
                } else {
                    sb.append(if (toUp) c.uppercaseChar() else c.lowercaseChar())
                    false
                }
                i++
            }
            sb.toString()
        } else {
            str
        }
        return if (firstLetterLower) {
            result.replaceFirstChar { it.lowercaseChar() }
        } else {
            result.replaceFirstChar { it.uppercaseChar() }
        }
    }
}