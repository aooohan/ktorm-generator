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

        // Using the event, create and show a dialog
        // Using the event, create and show a dialog
        val project = event.project
        val tableElements = event.getData(LangDataKeys.PSI_ELEMENT_ARRAY)
        val dbTables = tableElements?.filterIsInstance<DbTable>()?.toList()
        if (dbTables.isNullOrEmpty()) {
            logger.error("未选择表, 无法生成代码")
            return
        }

        val classGeneratorDialogWrapper = ClassGeneratorDialogWrapper(project!!)

        classGeneratorDialogWrapper.show()

        if (classGeneratorDialogWrapper.exitCode == Messages.YES) {
            println(classGeneratorDialogWrapper.generatorOptions)
            logger.info("最终结果:{}",classGeneratorDialogWrapper.generatorOptions)
            TODO("生成代码")
        }

    }
}