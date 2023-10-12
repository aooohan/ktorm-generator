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

import com.github.aooohan.ktormgenerator.action.GeneratorOptions
import com.github.aooohan.ktormgenerator.dto.TableUIInfo
import com.github.aooohan.ktormgenerator.services.KtormGeneratorService
import com.intellij.database.util.common.isNotNullOrEmpty
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import java.io.File
import java.sql.Types
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * @author : lihan
 * @date : 2023/10/5 13:03
 */
class KtFileGenerator(
    private val project: Project
) {
    var ktormGeneratorService: KtormGeneratorService

    init {
        project.service<KtormGeneratorService>().let {
            ktormGeneratorService = it
        }
    }

    private fun generateTableCode(
        tableInfo: CodeTableInfo
    ): String {
        val className = tableInfo.className
        val optionInfo = tableInfo.optionInfo
        val parseImport = parseImport(tableInfo.columnInfos)

        val columMap = tableInfo.columnInfos.associateBy { it.originalName }

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
                
                
            
            /**
            * generated by KtormGenerator
            * @date ${LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))}
            */
            """.trimIndent()
            )

            appendLine("interface $className : Entity<$className> {")
            appendLine("    companion object : Entity.Factory<$className>()")
            for (columInfo in tableInfo.columnInfos) {
                val nullable = if (columInfo.nullable) "?" else ""
                val fieldInfo = columMap[columInfo.originalName] ?: continue
                if (fieldInfo.comment.isNotNullOrEmpty) {
                    appendLine("    /**")
                    appendLine("     * ${fieldInfo.comment}")
                    appendLine("     */")
                }
                appendLine("    var ${fieldInfo.name}: ${fieldInfo.ktType}$nullable")
            }
            appendLine("}")

            appendLine()
            appendLine("object ${className}s : Table<$className>(\"${tableInfo.tableName}\") {")
            for (columnInfo in tableInfo.columnInfos) {
                val fieldInfo = columMap[columnInfo.originalName] ?: continue
                if (!fieldInfo.primaryKey) {
                    appendLine("    val ${fieldInfo.name} = ${fieldInfo.ktormType}(\"${fieldInfo.originalName}\").bindTo { it.${fieldInfo.name} }")
                } else {
                    appendLine("    val ${fieldInfo.name} = ${fieldInfo.ktormType}(\"${fieldInfo.originalName}\").primaryKey().bindTo { it.${fieldInfo.name} }")
                }
            }
            appendLine("}")

            appendLine()
            appendLine()
            appendLine()

            appendLine("val Database._${className.replaceFirstChar { it.lowercaseChar() }}s get() = this.sequenceOf(${className}s)")
        }

        return interfaceCode
    }

    private fun parseImport(columnInfos: List<FieldTypeInfo>): List<String> {
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
        filedInfo.dataType = columnInfo.dataType
        filedInfo.nullable = columnInfo.nullable
        filedInfo.comment = columnInfo.remarks
        return filedInfo
    }


    private fun writeFile(tableInfo: CodeTableInfo, content: String) {
        val optionInfo = tableInfo.optionInfo
        val className = tableInfo.className
        val basePath = "${optionInfo.moduleChooseText}/${optionInfo.basePathText}/${
            optionInfo.basePackageText.replace(
                ".",
                "/"
            )
        }/${
            optionInfo.relativePackageText.replace(
                ".",
                "/"
            )
        }/$className.kt"
        val outputFile = File(basePath)
        if (!outputFile.exists()) {
            val parentFile = outputFile.parentFile
            if (!parentFile.exists()) {
                parentFile.mkdirs()
            }
            outputFile.createNewFile()
        }
        outputFile.writeText(content)
        // Refresh the file system to see the changes in the IDE
        LocalFileSystem.getInstance().findFileByIoFile(outputFile)?.refresh(false, true)
    }

    fun doGenerate(tableInfos: List<IntellijTableInfo>, optionInfo: GeneratorOptions) {
        val tableInfo = combineTableInfo(tableInfos, optionInfo)
        tableInfo.forEach {
            val content = generateTableCode(it)
            writeFile(it, content)
        }

    }

    private fun combineTableInfo(
        tableInfos: List<IntellijTableInfo>,
        optionInfo: GeneratorOptions
    ): List<CodeTableInfo> {
        val tableUIMap = optionInfo.tableInfoList.associateBy { it.tableName }
        return tableInfos.mapNotNull {
            val tableUIInfo: TableUIInfo? = tableUIMap[it.tableName]
            if (tableUIInfo == null) {
                null
            } else {
                val codeTableInfo = CodeTableInfo(
                    tableName = tableUIInfo.tableName,
                    className = tableUIInfo.className,
                    optionInfo = optionInfo
                )
                val tableKeyFieldMap = it.primaryKeyColumns.associateBy { it.name }

                val filedTypeInfos = it.columnInfos.map { columnInfo ->
                    parseColum2Field(columnInfo).let { fti ->
                        if (tableKeyFieldMap.containsKey(fti.originalName)) {
                            fti.primaryKey = true
                        }
                        fti
                    }
                }.toList()
                codeTableInfo.columnInfos = filedTypeInfos
                codeTableInfo
            }
        }.toList()
    }

}

data class CodeTableInfo(
    val tableName: String,
    val className: String,
    val optionInfo: GeneratorOptions,
    var columnInfos: List<FieldTypeInfo> = ArrayList(),
)

data class FieldTypeInfo(
    val ktType: String,
    val ktormType: String,
    var dataType: Int = Types.VARCHAR,
    var originalName: String = "",
    var name: String = "",
    var primaryKey: Boolean = false,
    var nullable: Boolean = true,
    var comment: String? = null,
)
