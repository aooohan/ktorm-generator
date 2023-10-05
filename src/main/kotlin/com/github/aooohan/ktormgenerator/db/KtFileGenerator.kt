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

package com.github.aooohan.ktormgenerator.db

import com.github.aooohan.ktormgenerator.action.GeneratorOptions
import com.github.aooohan.ktormgenerator.services.KtormGeneratorService
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import java.sql.Types

/**
 * @author : lihan
 * @date : 2023/10/5 13:03
 */
class KtFileGenerator(
    private val project: Project
) {
    fun generateTableCode(
        tableInfo: IntellijTableInfo,
        optionInfo: GeneratorOptions,
    ): String {
        val service = project.service<KtormGeneratorService>()
        // 生成类名，将表名称转换为驼峰式
        val className = service.convertNameToCamelStyle(tableInfo.tableName, false)

        val parseImport = parseImport(tableInfo.columnInfos)

        val columMap = tableInfo.columnInfos.map(::parseColum2Field).associateBy { it.originalName }
        tableInfo.primaryKeyColumns.forEach {
            columMap[it.name]?.primaryKey = true
        }

        val interfaceCode = buildString {
            appendLine(
                """
                package ${optionInfo.basePackageText}.${optionInfo.relativePackageText}
                
                import org.ktorm.database.Database
                import org.ktorm.entity.Entity
                import org.ktorm.entity.sequenceOf
                import org.ktorm.schema.*
            """.trimIndent()
            )
            parseImport.forEach { import ->
                appendLine("import $import")
            }
            appendLine(
                """
                
                
                
            """.trimIndent()
            )

            appendLine("interface $className :Entity<$className> {")
            for (columInfo in tableInfo.columnInfos) {
                val nullable = if (columInfo.nullable) "?" else ""
                val fieldInfo = columMap[columInfo.name] ?: continue
                appendLine("    val ${fieldInfo.name}: ${fieldInfo.ktType}$nullable")
            }
            appendLine("}")

            appendLine()
            appendLine("object ${className}s : Table<$className>(\"${tableInfo.tableName}\") {")
            for (columnInfo in tableInfo.columnInfos) {
                val fieldInfo = columMap[columnInfo.name] ?: continue
                if (!fieldInfo.primaryKey) {
                    appendLine("    val ${fieldInfo.name} = ${fieldInfo.ktormType}(\"${fieldInfo.originalName}\").bindTo { it.${fieldInfo.name}}")
                } else {
                    appendLine("    val ${fieldInfo.name} = ${fieldInfo.ktormType}(\"${fieldInfo.originalName}\").primaryKey().bindTo { it.${fieldInfo.name}}")
                }
            }
            appendLine("}")

            appendLine()
            appendLine()
            appendLine()

            appendLine("val Database.${className!!.lowercase()}s get() = this.sequenceOf(${className}s)")
        }

        return interfaceCode
    }

    private fun parseImport(columnInfos: List<IntellijColumnInfo>): List<String> {
        return columnInfos.map {
            when (it.dataType) {
                Types.DECIMAL -> "java.math.BigDecimal"
                Types.TIMESTAMP -> "java.time.LocalDateTime"
                Types.DATE -> "java.time.LocalDate"
                Types.TIME -> "	java.time.Time"
                else -> ""
            }
        }.distinct().filter { it.isNotEmpty() }.toList()
    }

    private fun parseColum2Field(columnInfo: IntellijColumnInfo): FieldTypeInfo {
        val service = project.service<KtormGeneratorService>()
        val convertName = service.convertNameToCamelStyle(columnInfo.name, true)
        val filedInfo = when (columnInfo.dataType) {
            Types.BOOLEAN -> FieldTypeInfo("Boolean", "boolean")
            Types.INTEGER -> FieldTypeInfo("Int", "int")
            Types.SMALLINT -> FieldTypeInfo("Short", "short")
            Types.BIGINT -> FieldTypeInfo("Long", "long")
            Types.DECIMAL -> FieldTypeInfo("BigDecimal", "decimal")
            Types.FLOAT -> FieldTypeInfo("Float", "float")
            Types.DOUBLE -> FieldTypeInfo("Double", "double")
            Types.VARCHAR -> FieldTypeInfo("String", "varchar")
            Types.LONGVARCHAR -> FieldTypeInfo("String", "text")
            Types.BLOB -> FieldTypeInfo("ByteArray", "blob")
            Types.BINARY -> FieldTypeInfo("ByteArray", "bytes")
            Types.TIMESTAMP -> FieldTypeInfo("LocalDateTime", "datetime")
            Types.DATE -> FieldTypeInfo("LocalDate", "date")
            Types.TIME -> FieldTypeInfo("Time", "time")
            else -> FieldTypeInfo("String", "varchar")
        }
        filedInfo.name = convertName!!
        filedInfo.originalName = columnInfo.name
        return filedInfo
    }

}

data class FieldTypeInfo(
    val ktType: String,
    val ktormType: String,
    var originalName: String = "",
    var name: String = "",
    var primaryKey: Boolean = false
)
