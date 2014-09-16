package com.huhong.mineral.configs

import java.util.Date
import scala.beans.BeanProperty
import org.apache.lucene.util.Version

@serializable
case class IndexConfig(@BeanProperty var name: String, @BeanProperty var targetDir: String, @BeanProperty var analyzer: String = "default",
  @BeanProperty var writeThreadCount: Int,
  @BeanProperty var enabled: Boolean, @BeanProperty var createDate: Date, @BeanProperty var status: Int, @BeanProperty var version: Version = Version.LUCENE_40) {
  def printConfig() = {
    val info = s"索引文件名$name,目标目录:$targetDir,分词器:$analyzer,写线程数:$writeThreadCount";
    println(info);
  }
}