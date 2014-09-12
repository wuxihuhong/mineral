package com.huhong.mineral.index

import akka.actor.Actor
import com.huhong.mineral.messages._
import com.huhong.mineral.util.SystemContext
import org.slf4j.LoggerFactory
import org.apache.lucene.search.Query
import org.apache.lucene.search.SearcherManager
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.index.MultiReader

object IndexWriterActor {
  val logger = LoggerFactory.getLogger(classOf[IndexWriterActor]);

}

class IndexReaderActor(val inxController: IndexController) extends Actor {
  def receive = {
    case q: Query ⇒ {
      val readers = inxController.getReader;
      readers.refreshReaders;
      val searcher = new IndexSearcher(new MultiReader(readers.indexReaders: _*))
      val docs = searcher.search(q, SystemContext.Config.defaultSearchMaxDocs);
      docs.scoreDocs.map(d ⇒ {
        val doc=searcher.doc(d.doc);
        
      });
    }
  }

}

class IndexWriterActor(val indexController: IndexController) extends Actor {

  def receive = {
    case docs: Documents ⇒ {
      val writer = indexController.getWriter;
      try {
        docs.datas.foreach(d ⇒ {
          writer.addDocument(d);

        });
        writer.commit();
      } catch {
        case e: Throwable ⇒ {
          writer.rollback();
          docs.error = e;
          docs.errorCount = docs.errorCount + 1;

          if (docs.errorCount > SystemContext.Config.tryWriteDocmuentTimes) {
            IndexWriterActor.logger.error("写入索引文档错误！", e);
          } else {
            self ! docs;
          }
        }
      }
    }

    case str: String ⇒ {

      val writer = indexController.getWriter;

      println(Thread.currentThread().getName() + ":" + writer)
      Thread.sleep(5000);
    }
  }
}

