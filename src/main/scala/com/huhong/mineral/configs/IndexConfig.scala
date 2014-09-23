package com.huhong.mineral.configs

import java.util.Date
import scala.beans.BeanProperty
import org.apache.lucene.util.Version

@serializable
case class IndexConfig(var name: String,
  @BeanProperty var targetDir: String,
  @BeanProperty var analyzer: String = "default",
  @BeanProperty var writerCount: Int,
  @BeanProperty var coreThreadCount: Int,
  @BeanProperty var enabled: Boolean = true,
  @BeanProperty var createDate: Date = new Date,
  @BeanProperty var status: Int = 0,
  @BeanProperty var version: Version = Version.LUCENE_4_9,
  @BeanProperty var remote: Boolean = false,
  @BeanProperty var hostname: String = "N/A",
  @BeanProperty var port: Int = 0,
  @BeanProperty var readerCount: Int = 0) {

  def printConfig() = {
    println(this);
  }

  override def toString = {
    s"索引文件名$name,目标目录:$targetDir,分词器:$analyzer,核心线程数:$coreThreadCount,写入器数:$writerCount,读取器数:$readerCount,远程:$remote,hostname:$hostname,port:$port";
  }
  
  
}