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

package com.github.aooohan.ktormgenerator.action

import com.github.aooohan.ktormgenerator.dto.TableUIInfo
import com.github.aooohan.ktormgenerator.services.KtormGeneratorService
import com.github.aooohan.ktormgenerator.ui.KtormGeneratorGUI
import com.intellij.database.psi.DbTable
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.modules
import com.intellij.openapi.ui.DialogWrapper
import javax.swing.JPanel

/**
 * @author : lihan
 * @date : 2023/10/4 21:25
 */
class ClassGeneratorDialogWrapper(
    project: Project,
    dbTables: List<DbTable>
) : DialogWrapper(project) {
    val generatorOptions: GeneratorOptions
    private val ktormGeneratorGUI: KtormGeneratorGUI
    private val service = project.service<KtormGeneratorService>()

    init {
        title = "Ktorm Generator"

        setSize(600, 300)
        val parseModuleDirPath = service.parseModuleDirPath(project.modules.first())
        this.generatorOptions = GeneratorOptions(parseModuleDirPath)
        val tableUIInfos = service.parseTableNames(dbTables)
        this.ktormGeneratorGUI = with(KtormGeneratorGUI(project)) {
            setOptions(tableUIInfos, generatorOptions)
            this
        }
        super.init()
    }

    override fun createCenterPanel(): JPanel = ktormGeneratorGUI.rootPanel
}

data class GeneratorOptions(
    var moduleChooseText: String,
    var basePackageText: String = "generator",
    var basePathText: String = "src/main/kotlin",
    var relativePackageText: String = "domain",
    var tableInfoList: List<TableUIInfo> = ArrayList()
)