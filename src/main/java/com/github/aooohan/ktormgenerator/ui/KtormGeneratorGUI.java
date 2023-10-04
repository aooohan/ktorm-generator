package com.github.aooohan.ktormgenerator.ui;

import com.github.aooohan.ktormgenerator.action.GeneratorOptions;
import com.github.aooohan.ktormgenerator.dto.KtormTableColumnInfo;
import com.github.aooohan.ktormgenerator.dto.TableUIInfo;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ui.configuration.ChooseModulesDialog;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ui.ListTableModel;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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
    private JPanel leftPanel;
    private JTextField basePackageTextField;
    private JTextField basePathTextField;
    private JTextField relativePackageTextField;
    private JPanel listPanel;
    private JTextField moduleChooseTextField;
    private JPanel rootPanel;

    private String moduleName;

    public KtormGeneratorGUI(Project project) {
        this.project = project;
    }

    public JPanel getRootPanel() {
        return rootPanel;
    }



    public void setOptions(GeneratorOptions options) {
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

        String moduleDirPath = ModuleUtil.getModuleDirPath(module);
        int childModuleIndex = indexFromChildModule(moduleDirPath);
        if (hasChildModule(childModuleIndex)) {
            Optional<String> pathFromModule = getPathFromModule(module);
            if (pathFromModule.isPresent()) {
                moduleDirPath = pathFromModule.get();
            } else {
                moduleDirPath = moduleDirPath.substring(0, childModuleIndex);
            }
        }

        moduleChooseTextField.setText(moduleDirPath);
    }
    private int indexFromChildModule(String moduleDirPath) {
        return moduleDirPath.indexOf(".idea");
    }
    private boolean hasChildModule(int childModuleIndex) {
        return childModuleIndex > -1;
    }
    private Optional<String> getPathFromModule(Module module) {
        // 兼容gradle获取子模块
        VirtualFile[] contentRoots = ModuleRootManager.getInstance(module).getContentRoots();
        if (contentRoots.length == 1) {
            return Optional.ofNullable(contentRoots[0].getPath());
        }
        return Optional.empty();
    }
}
