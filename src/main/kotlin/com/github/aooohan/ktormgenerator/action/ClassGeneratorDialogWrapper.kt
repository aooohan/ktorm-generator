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

    init {
        title = "Generate Options"

        setSize(600, 300)
        val service = project.service<KtormGeneratorService>()
        val tableUIInfos = service.parseTableNames(dbTables)
        val parseModuleDirPath = service.parseModuleDirPath(project.modules.first())

        this.generatorOptions = GeneratorOptions(parseModuleDirPath)

        this.ktormGeneratorGUI = KtormGeneratorGUI(project, tableUIInfos, generatorOptions)
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