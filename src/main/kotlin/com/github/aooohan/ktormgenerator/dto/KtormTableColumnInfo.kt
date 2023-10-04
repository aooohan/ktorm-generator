package com.github.aooohan.ktormgenerator.dto

import com.intellij.util.ui.ColumnInfo
import javax.swing.DefaultCellEditor
import javax.swing.JTextField
import javax.swing.event.CellEditorListener
import javax.swing.event.ChangeEvent
import javax.swing.table.TableCellEditor

data class KtormTableColumnInfo(
    val name: String,
    val editable: Boolean
): ColumnInfo<TableUIInfo, String>(name) {
    override fun isCellEditable(item: TableUIInfo?): Boolean {
        return editable
    }

    override fun getEditor(item: TableUIInfo): TableCellEditor {
        val defaultCellEditor = DefaultCellEditor(JTextField(name))
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
        return when(name) {
            "tableName" -> item?.tableName
            "className" -> item?.className
            else -> null
        }
    }
}
