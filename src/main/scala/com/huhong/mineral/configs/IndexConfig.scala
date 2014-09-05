package com.huhong.mineral.configs

import java.util.Date
import scala.beans.BeanProperty

@serializable
case class IndexConfig(@BeanProperty var name: String, @BeanProperty var targetDir: String, @BeanProperty var writeThreadCount: Int,
  @BeanProperty var enabled: Boolean, @BeanProperty var createDate: Date, @BeanProperty var status: Int);