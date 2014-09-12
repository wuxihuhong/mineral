package com.huhong.mineral.configs

import java.util.Date
import scala.beans.BeanProperty
import org.apache.lucene.util.Version

@serializable
case class IndexConfig(@BeanProperty var name: String, @BeanProperty var targetDir: String, @BeanProperty var analyzer: String = "default",
  @BeanProperty var writeThreadCount: Int, @BeanProperty var readThreaCount:Int,
  @BeanProperty var enabled: Boolean, @BeanProperty var createDate: Date, @BeanProperty var status: Int, @BeanProperty var version: Version = Version.LUCENE_40);