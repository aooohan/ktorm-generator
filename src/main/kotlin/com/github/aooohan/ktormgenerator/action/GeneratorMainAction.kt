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

package com.github.aooohan.ktormgenerator.action

import com.intellij.database.psi.DbTable
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.ui.Messages
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
    companion object {
        const val TITLE = "Ktorm Generator"
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
            println(classGeneratorDialogWrapper.generatorOptions)
            TODO("生成代码")
        }

    }
}