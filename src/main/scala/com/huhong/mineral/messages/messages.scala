package com.huhong.mineral.messages

import org.apache.lucene.document.{ Document }

case class Documents(val datas: Array[Document], var error: Throwable = null, var errorCount: Int = 0)