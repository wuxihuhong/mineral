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
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext
import akka.dispatch._
import scala.collection.mutable.ListBuffer
import akka.actor.{ ActorRef, ActorSystem, Props, Actor, Inbox }
import scala.concurrent.Await
import org.apache.lucene.document.Document
import java.util.concurrent.CopyOnWriteArrayList
import akka.pattern.AskTimeoutException
import com.huhong.mineral.Mineral
import java.util.Arrays
import org.apache.lucene.queryparser.classic.QueryParser
import com.huhong.mineral.error.MineralExpcetion
import com.fasterxml.jackson.databind.ObjectMapper
import java.io.StringWriter
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream
import java.io.ByteArrayOutputStream
import com.huhong.mineral.results.DynamicResult
import com.huhong.mineral.util.Imports._
import org.apache.lucene.search.TopFieldCollector
import org.apache.lucene.search.TopDocsCollector
import org.apache.lucene.search.TopScoreDocCollector

class Index(val indexConfig: IndexConfig, val system: ActorSystem, val actor: ActorRef, val indexController: IndexController);

class IndexActor(val indexController: IndexController) extends Actor with akka.actor.ActorLogging {

  val name = indexController.indexconfig.name;
  val docloader = context.actorOf(Props[DocumentLoader].withDispatcher(s"$name-docloader-thread-pool-dispatcher"), s"$name-docloader")

  val defaultQueryParser = new QueryParser(indexController.indexconfig.version, "",
    SystemContext.analyzers.getOrElse(indexController.indexconfig.analyzer, throw new MineralExpcetion(0, "未找到指定分词器！")));

  val objectMapper = new ObjectMapper;

  private def loadDocuments(ld: LoadDocument) = {
    val timeout = new Timeout(SystemContext.Config.loadDocuemntTimeOut millis);
    val dispatcher = context.system.dispatchers.lookup(s"$name-docloader-thread-pool-dispatcher");

    val f = docloader.ask(ld)(timeout);
    val r = Await.result(f, timeout.duration);
    val docrets = r.asInstanceOf[Array[(Document, Float)]];

    val bos = new ByteArrayOutputStream();

    val gos = new GzipCompressorOutputStream(bos);

    docsToJson(docrets, gos, ld.docs.totalHits, System.currentTimeMillis() - ld.beginTime);

    val buff = bos.toByteArray();

    gos.close;
    bos.close;

    buff;
  }

  private def addDocuments(docs: Documents) = {
    val conn = indexController.getConnection;
    try {
      val writer = conn.writer;

      docs.datas.foreach(d ⇒ {
        writer.addDocument(d);
        log.info(d.toString() + "--->" + conn.realDir);
      });
      conn.addWrited(docs.datas.length);

    } catch {
      case e: Throwable ⇒ {

        docs.error = e;
        docs.errorCount = docs.errorCount + 1;

        if (docs.errorCount > SystemContext.Config.tryWriteDocmuentTimes) {
          log.error("写入索引文档错误！", e);
        } else {
          self ! docs;
        }
      }
    } finally {
      indexController.releaseConnection(conn);
    }
  }

  def receive = {
    case docs: SerializableDocuments ⇒ {
      addDocuments(docs.toDocuments);
    }
    case docs: Documents ⇒ {

      addDocuments(docs);

    }
    case q: Querys ⇒ {

      val conn = indexController.getConnection;
      try {
        val readers = conn.getReaders;
        val searcher = new IndexSearcher(new MultiReader(readers: _*));
        val query = q.getQuery(defaultQueryParser);
        val sort = q.getSort;

        val ret = if (sort != null) TopFieldCollector.create(sort, SystemContext.Config.defaultSearchMaxDocs, true,
          true, true, false);
        else TopScoreDocCollector.create(SystemContext.Config.defaultSearchMaxDocs, false);
        val begin = System.currentTimeMillis();
        searcher.search(query, ret);
        val docs = if (q.begin == -1 && q.end == -1) ret.topDocs() else if (q.end == -1) ret.topDocs(q.begin) else ret.topDocs(q.begin, q.end - q.begin);
        val buff = loadDocuments(LoadDocument(searcher, docs, begin));
        sender ! buff;
      } finally {
        indexController.releaseConnection(conn);
      }
    }
    case d: Deletes ⇒ {
      val conn = indexController.getConnection;
      try {
        val q = defaultQueryParser.parse(d.queryStr)
        conn.delete(q);
      } finally {
        indexController.releaseConnection(conn); ;
      }

    }
    //    case q: QueryString ⇒ {
    //      val conn = indexController.getConnection;
    //      try {
    //        val readers = conn.getReaders;
    //
    //        val searcher = new IndexSearcher(new MultiReader(readers: _*))
    //        val begin = System.currentTimeMillis();
    //        val docs = searcher.search(defaultQueryParser.parse(q.queryStr), SystemContext.Config.defaultSearchMaxDocs);
    //        val buff = loadDocuments(LoadDocument(searcher, docs, begin));
    //        sender ! buff;
    //
    //      } finally {
    //        indexController.releaseConnection(conn);
    //      }
    //    }
    //
    //    case ld: LoadDocument ⇒ {
    //      val timeout = new Timeout(SystemContext.Config.loadDocuemntTimeOut millis);
    //      val dispatcher = context.system.dispatchers.lookup(s"$name-docloader-thread-pool-dispatcher");
    //
    //      val f = docloader.ask(ld)(timeout);
    //      val r = Await.result(f, timeout.duration);
    //      sender ! r;
    //
    //    }
    //
    //    case t: TestString ⇒ {
    //
    //      System.err.println(t.string);
    //      Thread.sleep(5000);
    //    }
    case _ ⇒ {
      println("未知的命令!");
    }
  }

}

class DocumentLoader extends Actor {
  def receive = {
    case ld: LoadDocument ⇒ {
      val ret = ld.docs.scoreDocs.map(d ⇒ {
        val doc = ld.searcher.doc(d.doc)

        (doc, d.score);

      })
      sender ! ret;

    }
    case _ ⇒ {
      println("未知的命令!");
    }
  }
}


