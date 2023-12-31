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

package com.github.aooohan.ktormgenerator.db

import com.intellij.database.model.DasColumn
import com.intellij.database.model.DasTableKey
import com.intellij.database.model.DasTypedObject
import com.intellij.database.model.MultiRef
import com.intellij.database.psi.DbTable
import com.intellij.database.util.DasUtil
import com.intellij.util.containers.JBIterable
import java.sql.Types
import java.util.*

/**
 * The type Db tools utils.
 */
object DbToolsParser {

    /**
     * Build intellij table info intellij table info.
     *
     * @param currentTable the current table
     * @return the intellij table info
     */
    fun parseIntellijTableInfo(currentTable: DbTable): IntellijTableInfo {
        val tableInfo = IntellijTableInfo(
            tableName = currentTable.name,
            tableRemark = currentTable.comment,
            tableType = currentTable.typeName,
            databaseType = extractDatabaseTypeFromUrl(currentTable)
        )
        val intellijColumnInfos: MutableList<IntellijColumnInfo> = ArrayList<IntellijColumnInfo>()
        val columns: JBIterable<out DasColumn?> = DasUtil.getColumns(currentTable)
        for (column in columns) {
            val columnInfo: IntellijColumnInfo = convertColumnToIntellijColumnInfo(column!!, tableInfo.databaseType)
            intellijColumnInfos.add(columnInfo)
        }
        tableInfo.columnInfos = intellijColumnInfos
        val primaryColumnInfos: MutableList<IntellijColumnInfo> = ArrayList<IntellijColumnInfo>()
        val primaryKey: DasTableKey? = DasUtil.getPrimaryKey(currentTable)
        if (primaryKey != null) {
            val columnsRef: MultiRef<out DasTypedObject?> = primaryKey.columnsRef
            val iterate: MultiRef.It<out DasTypedObject?> = columnsRef.iterate()
            var s: Short = 0
            while (iterate.hasNext()) {
                val columnName: String = iterate.next()
                for (intellijColumnInfo in intellijColumnInfos) {
                    if (columnName == intellijColumnInfo.name) {
                        val info: IntellijColumnInfo = intellijColumnInfo.copy()
                        info.keySeq = s
                        primaryColumnInfos.add(info)
                        s++
                        break
                    }
                }
            }
        }
        tableInfo.primaryKeyColumns = primaryColumnInfos
        return tableInfo
    }

    private fun extractDatabaseTypeFromUrl(currentTable: DbTable): String {
        val url: String = currentTable.dataSource.connectionConfig!!.url
        return extractDatabaseTypeFromUrl(url)
    }

    /**
     * Extract database type from url string.
     *
     * @param url the url
     * @return the string
     */
    private fun extractDatabaseTypeFromUrl(u: String?): String {
        return if (u.isNullOrBlank()) {
            ""
        } else {
            val url = u.lowercase(Locale.getDefault())
            if (url.contains(":mysql")) {
                "MySql"
            } else if (url.contains(":oracle")) {
                "Oracle"
            } else if (url.contains(":postgresql")) {
                "PostgreSQL"
            } else if (url.contains(":sqlserver")) {
                "SqlServer"
            } else {
                if (url.contains(":sqlite")) "Sqlite" else ""
            }
        }
    }

    /**
     * Convert column to intellij column info intellij column info.
     *
     * @param column       the column
     * @param databaseType the database type
     * @return the intellij column info
     */
    private fun convertColumnToIntellijColumnInfo(column: DasColumn, databaseType: String): IntellijColumnInfo {
        val toDataType = column.dasType.toDataType()
        return IntellijColumnInfo(
            name = column.name,
            dataType = convertTypeNameToJdbcType(
                toDataType.typeName,
                databaseType
            ),
            size = toDataType.length,
            decimalDigits = toDataType.scale,
            remarks = column.comment,
            columnDefaultValue = column.default,
            nullable = !column.isNotNull,
            autoIncrement = DasUtil.isAutoGenerated(column),
            generatedColumn = DasUtil.isAutoGenerated(column),
            keySeq = column.position
        )
    }

    /**
     * Convert type name to jdbc type int.
     *
     * @param jdbcTypeName the jdbc type name
     * @param size         the size
     * @param databaseType the database type
     * @return the int
     */
    private fun convertTypeNameToJdbcType(jdbcTypeName: String?,  databaseType: String): Int {
        if (jdbcTypeName.isNullOrBlank()) {
            return Types.OTHER
        }
        val fixed: String = jdbcTypeName.uppercase(Locale.getDefault())
        return if (fixed.contains("BIGINT")) {
            Types.BIGINT
        } else if (fixed.contains("TINYINT")) {
            Types.TINYINT
        } else if (fixed.contains("LONGVARBINARY")) {
            Types.LONGVARBINARY
        } else if (fixed.contains("VARBINARY")) {
            Types.VARBINARY
        } else if (fixed.contains("LONGVARCHAR")) {
            Types.LONGVARCHAR
        } else if (fixed.contains("SMALLINT")) {
            Types.SMALLINT
        } else if (fixed.contains("DATETIME")) {
            Types.TIMESTAMP
        } else if (fixed == "DATE" && "Oracle" == databaseType) {
            Types.TIMESTAMP
        } else if (fixed.contains("NUMBER")) {
            Types.DECIMAL
        } else if (fixed.contains("BOOLEAN")) {
            Types.BOOLEAN
        } else if (fixed.contains("BINARY")) {
            Types.VARBINARY
        } else if (fixed.contains("BIT")) {
            Types.BIT
        } else if (fixed.contains("BOOL")) {
            Types.BOOLEAN
        } else if (fixed.contains("DATE")) {
            Types.DATE
        } else if (fixed.contains("TIMESTAMP")) {
            Types.TIMESTAMP
        } else if (fixed.contains("TIME")) {
            Types.TIME
        } else if (!fixed.contains("REAL") && !fixed.contains("NUMBER")) {
            if (fixed.contains("FLOAT")) {
                Types.FLOAT
            } else if (fixed.contains("DOUBLE")) {
                Types.DOUBLE
            } else if ("CHAR" == fixed) {
                Types.CHAR
            } else if (fixed.contains("INT")) {
                Types.INTEGER
            } else if (fixed.contains("DECIMAL")) {
                Types.DECIMAL
            } else if (fixed.contains("NUMERIC")) {
                Types.NUMERIC
            } else if (!fixed.contains("CHAR") && !fixed.contains("TEXT")) {
                if (fixed.contains("BLOB")) {
                    Types.BLOB
                } else if (fixed.contains("CLOB")) {
                    Types.CLOB
                } else {
                    if (fixed.contains("REFERENCE")) Types.REF else Types.OTHER
                }
            } else {
                Types.VARCHAR
            }
        } else {
            Types.REAL
        }
    }

}

data class IntellijTableInfo(
    var tableName: String = "",
    var tableRemark: String? = "",
    var tableType: String = "",
    var databaseType: String = "",
    var columnInfos: List<IntellijColumnInfo> = ArrayList(),
    var primaryKeyColumns: List<IntellijColumnInfo> = ArrayList()

)

data class IntellijColumnInfo(
    var name: String,
    var dataType: Int,
    var size: Int,
    var decimalDigits: Int,
    var remarks: String?,
    var columnDefaultValue: String?,
    var nullable: Boolean,
    var autoIncrement: Boolean,
    var generatedColumn: Boolean,
    var keySeq: Short
)
