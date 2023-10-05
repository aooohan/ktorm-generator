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
    private JPanel leftPanel;
    private JTextField basePackageTextField;
    private JTextField basePathTextField;
    private JTextField relativePackageTextField;
    private JPanel listPanel;
    private JTextField moduleChooseTextField;
    private JPanel rootPanel;

    private String moduleName;

    public KtormGeneratorGUI(Project project, List<TableUIInfo> tableUIInfos, GeneratorOptions options) {
        this.project = project;
        this.ktormGeneratorService = project.getService(KtormGeneratorService.class);

        setOptions(options);
        model.addRows(tableUIInfos);

        TableView<TableUIInfo> tableView = new TableView<>(model);
        GridConstraints gridConstraints = new GridConstraints();
        gridConstraints.setFill(GridConstraints.FILL_HORIZONTAL);

        listPanel.add(ToolbarDecorator.createDecorator(tableView)
                        .setPreferredSize(new Dimension(860, 200))
                        .disableAddAction()
                        .disableRemoveAction()
                        .disableUpDownActions()
                        .createPanel(),
                gridConstraints);

        tableView.addPropertyChangeListener(evt -> {
            if (evt.getPropertyName().equals("tableCellEditor")) {
                options.setTableInfoList(model.getItems());
            }
        });
    }

    public JPanel getRootPanel() {
        return rootPanel;
    }



    private void setOptions(GeneratorOptions options) {
        options.setTableInfoList(model.getItems());
        basePackageTextField.setText(options.getBasePackageText());
        basePathTextField.setText(options.getBasePathText());
        relativePackageTextField.setText(options.getRelativePackageText());
        moduleChooseTextField.setText(options.getModuleChooseText());
        if (moduleName != null && !moduleName.isBlank()) {
            Module[] modules = ModuleManager.getInstance(project).getModules();
            for (Module module : modules) {
                if (module.getName().equals(moduleName)) {
                    chooseModulePath(module);
                }
            }
        }

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
        ChooseModulesDialog dialog = new ChooseModulesDialog(project, Arrays.asList(modules), "Choose Module", "Choose Single Module");
        dialog.setSingleSelectionMode();
        dialog.show();

        List<Module> chosenElements = dialog.getChosenElements();
        if (!chosenElements.isEmpty()) {
            Module module = chosenElements.get(0);
            chooseModulePath(module);
            moduleName = module.getName();
        }
    }
    private void chooseModulePath(Module module) {
        moduleChooseTextField.setText(ktormGeneratorService.parseModuleDirPath(module));
    }
}
