package com.netease.yunyuedu.sbt.model

import sbt.File

case class GeneratorContext(connect: String => String, connectUserName: String => String, connectPassword: String => String,
                            connectMode: String => String,
                            tables: Seq[TableMeta],
                            connectionsMapper: String => Int,
                            tableJobSettingMapper: String => String,
                            jobOutputBaseDirectory: String,
                            jobOutputDirectoryMapper: String => String,
                            jobSplitKeyMapper: String => String,
                            hiveSchema: String,
                            hiveTablePartitioned: Boolean,
                            hiveTableMapper: String => String,
                            tableZorderColumnsMapper: String => String,
                            binlogTableMapper: String => String
                           )
