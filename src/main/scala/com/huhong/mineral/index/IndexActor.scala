package com.huhong.mineral.index

import akka.actor.Actor
import com.huhong.mineral.messages._
import com.huhong.mineral.util.SystemContext
import org.slf4j.LoggerFactory
import org.apache.lucene.search.Query
import org.apache.lucene.search.SearcherManager
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.index.MultiReader
import com.huhong.mineral.results.Result
import akka.actor.ActorRef
import akka.actor.ActorSystem
import com.huhong.mineral.configs.IndexConfig
import org.apache.lucene.index.IndexReader

object IndexActor {
  val logger = LoggerFactory.getLogger(classOf[IndexActor]);

}

class Index(val indexConfig: IndexConfig, val system: ActorSystem, val actor: ActorRef, val indexController: IndexController);

//class IndexReaderActor(val inxController: IndexController) extends Actor {
//  def receive = {
//    case q: Query ⇒ {
//
//      val readers = inxController.getReader;
//
//      val searcher = new IndexSearcher(new MultiReader(readers: _*))
//      val docs = searcher.search(q, SystemContext.Config.defaultSearchMaxDocs);
//
//      val ret = docs.scoreDocs.map(d ⇒ {
//        searcher.doc(d.doc);
//
//      });
//
//      sender ! ret;
//    }
//    case t: TestString ⇒ {
//      IndexReaderActor.logger.info(t.string);
//    }
//    case _ ⇒ {
//      println("未知的命令!");
//    }
//  }
//
//}

class IndexActor(val indexController: IndexController) extends Actor {


  @volatile private var shutdowning = false;
  def receive = {
    case docs: Documents ⇒ {

      val conn = indexController.getConnection;
      try {
        val writer = conn.writer;

        docs.datas.foreach(d ⇒ {
          writer.addDocument(d);
          IndexActor.logger.debug(d.toString() + "--->" + conn.realDir);
        });
        conn.addWrited(docs.datas.length);

      } catch {
        case e: Throwable ⇒ {

          docs.error = e;
          docs.errorCount = docs.errorCount + 1;

          if (docs.errorCount > SystemContext.Config.tryWriteDocmuentTimes) {
            IndexActor.logger.error("写入索引文档错误！", e);
          } else {
            self ! docs;
          }
        }
      } finally {
        indexController.returnConnection(conn);
      }

    }
    case q: Query ⇒ {

      val readers = indexController.getReader;

      val searcher = new IndexSearcher(new MultiReader(readers: _*))
      val docs = searcher.search(q, SystemContext.Config.defaultSearchMaxDocs);

      println("命中:"+docs.totalHits);
      val ret = docs.scoreDocs.map(d ⇒ {
        searcher.doc(d.doc);

      });

      sender ! ret;
    }

    case t: TestString ⇒ {
      IndexActor.logger.info(t.string);
    }
    case _ ⇒ {
      println("未知的命令!");
    }
  }

}



