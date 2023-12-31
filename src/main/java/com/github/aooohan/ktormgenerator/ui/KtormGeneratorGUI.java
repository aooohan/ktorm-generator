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

package com.github.aooohan.ktormgenerator.ui;

import com.github.aooohan.ktormgenerator.action.GeneratorOptions;
import com.github.aooohan.ktormgenerator.dto.KtormTableColumnInfo;
import com.github.aooohan.ktormgenerator.dto.TableUIInfo;
import com.github.aooohan.ktormgenerator.services.KtormGeneratorService;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ui.configuration.ChooseModulesDialog;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.table.TableView;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.util.ui.ListTableModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.List;

/**
 * @author : lihan
 * @date : 2023/10/4 21:56
 */
public class KtormGeneratorGUI {

    ListTableModel<TableUIInfo> model = new ListTableModel<>(
            new KtormTableColumnInfo("tableName", false),
            new KtormTableColumnInfo("className", true)
    );
    private final Project project;
    private final KtormGeneratorService ktormGeneratorService;
    private JTextField basePackageTextField;
    private JTextField basePathTextField;
    private JTextField relativePackageTextField;
    private JPanel tableListPanel;
    private JTextField moduleChooseTextField;
    private JPanel rootPanel;

    public KtormGeneratorGUI(Project project) {
        this.project = project;
        this.ktormGeneratorService = project.getService(KtormGeneratorService.class);
    }

    public JPanel getRootPanel() {
        return rootPanel;
    }



    public void setOptions(List<TableUIInfo> tableUIInfos, GeneratorOptions options) {
        model.addRows(tableUIInfos);
        options.setTableInfoList(model.getItems());
        basePackageTextField.setText(options.getBasePackageText());
        basePathTextField.setText(options.getBasePathText());
        relativePackageTextField.setText(options.getRelativePackageText());
        moduleChooseTextField.setText(options.getModuleChooseText());

        TableView<TableUIInfo> tableView = new TableView<>(model);
        GridConstraints gridConstraints = new GridConstraints();
        gridConstraints.setFill(GridConstraints.FILL_HORIZONTAL);

        tableListPanel.add(ToolbarDecorator.createDecorator(tableView)
                        .setPreferredSize(new Dimension(860, 200))
                        .disableAddAction()
                        .disableRemoveAction()
                        .disableUpDownActions()
                        .createPanel(),
                gridConstraints);

        tableView.addPropertyChangeListener(evt -> {
            options.setTableInfoList(model.getItems());
        });

        moduleChooseTextField.addPropertyChangeListener(evt -> {
            options.setModuleChooseText(moduleChooseTextField.getText());
        });
        basePackageTextField.addPropertyChangeListener(evt -> {
            options.setBasePackageText(basePackageTextField.getText());
        });
        basePathTextField.addPropertyChangeListener(evt -> {
            options.setBasePathText(basePathTextField.getText());
        });
        relativePackageTextField.addPropertyChangeListener(evt -> {
            options.setRelativePackageText(relativePackageTextField.getText());
        });

        moduleChooseTextField.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                chooseModule(project);
            }
        });
    }

    private void chooseModule(Project project) {
        Module[] modules = ModuleManager.getInstance(project).getModules();
        ChooseModulesDialog dialog = new ChooseModulesDialog(project, Arrays.asList(modules), "Choose Module", "Choose single module");
        dialog.setSize(150, 300);
        dialog.setSingleSelectionMode();
        dialog.show();

        List<Module> chosenElements = dialog.getChosenElements();
        if (!chosenElements.isEmpty()) {
            Module module = chosenElements.get(0);
            moduleChooseTextField.setText(ktormGeneratorService.parseModuleDirPath(module));
        }
    }
}
