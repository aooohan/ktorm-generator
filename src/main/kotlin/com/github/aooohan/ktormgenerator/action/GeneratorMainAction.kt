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

import com.github.aooohan.ktormgenerator.db.DbToolsParser
import com.github.aooohan.ktormgenerator.db.KtFileGenerator
import com.intellij.database.psi.DbTable
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VirtualFileManager
import org.slf4j.Logger
import org.slf4j.LoggerFactory


/**
 * @author : lihan
 * @date : 2023/10/4 20:50
 */
class GeneratorMainAction: AnAction() {

    val logger: Logger by lazy {
        LoggerFactory.getLogger(GeneratorMainAction::class.java)
    }

    override fun actionPerformed(event: AnActionEvent) {

        val project = event.project
        val tableElements = event.getData(LangDataKeys.PSI_ELEMENT_ARRAY)
        val dbTables = tableElements?.filterIsInstance<DbTable>()?.toList()
        if (dbTables.isNullOrEmpty()) {
            logger.error("未选择表, 无法生成代码")
            return
        }

        val classGeneratorDialogWrapper = ClassGeneratorDialogWrapper(project!!, dbTables)

        classGeneratorDialogWrapper.show()

        if (classGeneratorDialogWrapper.exitCode == Messages.YES) {
            val parseTables = dbTables.map { dbTable ->
                DbToolsParser.parseIntellijTableInfo(dbTable)
            }.toList()
            KtFileGenerator(project).doGenerate(parseTables, classGeneratorDialogWrapper.generatorOptions)
            VirtualFileManager.getInstance().refreshWithoutFileWatcher(true)
            Messages.showDialog("Code generated successfully", "Tips", arrayOf("Ok"), -1, null)

        }

    }
}