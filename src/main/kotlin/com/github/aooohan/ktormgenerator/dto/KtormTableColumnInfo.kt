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

package com.github.aooohan.ktormgenerator.dto

import com.intellij.util.ui.ColumnInfo
import javax.swing.DefaultCellEditor
import javax.swing.JTextField
import javax.swing.event.CellEditorListener
import javax.swing.event.ChangeEvent
import javax.swing.table.TableCellEditor

data class KtormTableColumnInfo(
    val columnName: String,
    val editable: Boolean,
): ColumnInfo<TableUIInfo, String>(columnName) {
    override fun isCellEditable(item: TableUIInfo?): Boolean {
        return editable
    }

    override fun getEditor(item: TableUIInfo): TableCellEditor {
        val defaultCellEditor = DefaultCellEditor(JTextField(columnName))
        defaultCellEditor.addCellEditorListener(object : CellEditorListener {
            override fun editingStopped(e: ChangeEvent?) {
                val cellEditorValue = defaultCellEditor.cellEditorValue
                item.className = cellEditorValue.toString()
            }

            override fun editingCanceled(e: ChangeEvent) {
            }
        })
        return defaultCellEditor
    }

    override fun valueOf(item: TableUIInfo?): String? {
        return when(columnName) {
            "tableName" -> item?.tableName
            "className" -> item?.className
            else -> null
        }
    }
}
