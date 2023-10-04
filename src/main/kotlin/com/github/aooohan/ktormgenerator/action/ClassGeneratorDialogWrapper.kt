package com.github.aooohan.ktormgenerator.action

import com.github.aooohan.ktormgenerator.ui.KtormGeneratorGUI
import com.intellij.database.psi.DbTable
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import javax.swing.JPanel

/**
 * @author : lihan
 * @date : 2023/10/4 21:25
 */
class ClassGeneratorDialogWrapper(
    project: Project
) : DialogWrapper(project) {
    private val rootPanel: JPanel = JPanel()
    val generatorOptions: GeneratorOptions
    private val ktormGeneratorGUI: KtormGeneratorGUI

    init {
        title = "Generate Options"
        this.generatorOptions = GeneratorOptions()
        this.ktormGeneratorGUI = KtormGeneratorGUI(project)
        this.ktormGeneratorGUI.setOptions(generatorOptions)
        rootPanel.add(ktormGeneratorGUI.rootPanel)
        rootPanel.repaint()
        rootPanel.validate()
        super.init()
    }

    override fun createCenterPanel() = rootPanel
}

data class GeneratorOptions(
    var moduleChooseText: String = "",
    var basePackageText: String = "",
    var basePathText: String = "",
    var relativePackageText: String = "",
    var tableName: String = "",
    var className: String = ""
)