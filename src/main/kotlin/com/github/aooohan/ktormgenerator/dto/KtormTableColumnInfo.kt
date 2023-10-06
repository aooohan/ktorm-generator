/*
 *        Ktorm Generator - a plugin for generating Ktorm entity class from database table.
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
