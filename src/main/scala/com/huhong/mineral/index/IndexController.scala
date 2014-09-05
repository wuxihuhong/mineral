package com.huhong.mineral.index

import org.apache.lucene.index.IndexWriter
import java.util.concurrent.ConcurrentHashMap
import java.util.{ Map ⇒ JMap }
import com.huhong.mineral.configs.IndexConfig
import scala.collection.JavaConversions._
import scala.collection.mutable.ConcurrentMap

class IndexController private (private val indexconfig: IndexConfig) {
  val indexwriters: ThreadLocal[IndexWriter] = new ThreadLocal[IndexWriter];
  
}

object IndexController {
  private val controllers: ConcurrentMap[String, IndexController] = new ConcurrentHashMap[String, IndexController]();

  def getIndexController(indexConfig: IndexConfig) = {
    val found = controllers.find(c ⇒ { c._1.equals(indexConfig.name) });
    if (found.isDefined) {
      found.get._2
    } else {
      val c = new IndexController(indexConfig);
      controllers.put(indexConfig.name, c);
      c;
    }
  }
  
  
}