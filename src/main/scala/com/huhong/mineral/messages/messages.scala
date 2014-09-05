package com.huhong.mineral.messages

import org.apache.lucene.document.{ Document ⇒ LDoucment }

case class Document(val indexname: String, val doc: LDoucment)