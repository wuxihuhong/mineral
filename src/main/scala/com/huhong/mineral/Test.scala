package com.huhong.mineral

import com.db4o.Db4o
import com.db4o.Db4oEmbedded
import com.db4o.query.Predicate
import akka.actor.{ ActorRef, ActorSystem, Props, Actor, Inbox }
import scala.concurrent.duration._
import scala.concurrent.ops._
import com.typesafe.config.ConfigFactory
import akka.routing.RoundRobinRouter
import akka.routing.SmallestMailboxRouter
import java.util.concurrent.ConcurrentHashMap
import scala.collection.JavaConversions._
import com.huhong.mineral.util.SystemContext
import org.apache.lucene.store.FSDirectory
import java.io.File
import org.apache.lucene.index.IndexReader
import org.apache.lucene.index.IndexWriter
import org.apache.commons.io.FileUtils
import org.apache.commons.io.filefilter.NotFileFilter
import org.apache.commons.io.filefilter.TrueFileFilter
import org.apache.commons.io.filefilter.DirectoryFileFilter
import org.apache.lucene.document.Document
import org.apache.lucene.document.Field
import java.util.UUID
import org.apache.lucene.document.FieldType
import com.huhong.mineral.messages.Documents
import org.apache.lucene.util.Version
import org.apache.lucene.search.TermQuery
import org.apache.lucene.index.Term
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._

import scala.concurrent.ExecutionContext
import akka.dispatch._
import com.huhong.mineral.util.{ SystemContext ⇒ system }
import com.huhong.mineral.messages.TestString
import java.text.SimpleDateFormat
import org.apache.lucene.util.Version
import org.apache.lucene.index.IndexWriterConfig
import java.util.Date
import com.huhong.mineral.configs.IndexConfig

object Test extends App {

  val doc = new Document;
  val fd = new FieldType;
  fd.setIndexed(true);
  fd.setStored(true);

  val idf = new Field("id", UUID.randomUUID().toString(), fd);
  doc.add(idf);
  val titlef = new Field("title", "标题", fd);
  val contentf = new Field("content", "胡宏伟大啊！", fd);
  doc.add(titlef);
  doc.add(contentf);
  //  val fs = FSDirectory.open(new java.io.File("/Users/admin/Documents/mineral/mineral/testindex2"));
  //  val iwc = new IndexWriterConfig(Version.LUCENE_30, SystemContext.analyzers.get("default").get);
  //  iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
  //  val writer = new IndexWriter(fs, iwc);
  //
  //  val start1 = System.currentTimeMillis();
  //  for (i ← 0 until 5000) {
  //    writer.addDocument(doc);
  //    writer.commit();
  //  }
  //
  //  println((System.currentTimeMillis() - start1)/1000 + "s");
  //  System.in.read();
  //  writer.close();
  //
  //  exit;
  SystemContext.configDB = Db4oEmbedded.openFile(Db4oEmbedded.newConfiguration(), "config.yap");
  Mineral.start;
  val config = IndexConfig("test",
    "/Users/admin/Documents/mineral/mineral/testindex",
    "default",
    20,
    40,
    true,
    remote = true,
    hostname = "127.0.0.1",
    port = 1433, readerCount = 40)

  import com.huhong.mineral.commands.Imports._

  createIndex(config);
  exit;

  val index = Mineral.getIndex("test");
  //val index2 = Mineral.getIndex("test2");

  implicit val timeout = new Timeout(30 seconds)
  val name = index.indexConfig.name;
  implicit def executor: ExecutionContext = index.system.dispatchers.lookup(s"$name-thread-pool-dispatcher");

  val q = new TermQuery(new Term("content", "大"));
  val docs = Documents(Array(doc))

  val start = System.currentTimeMillis();

  for (i ← 0 until 25000) {

    index.actor ! docs;

    //Thread.sleep(500);
    //   
    //Thread.sleep(500);
    //    val f = index.actor ? q;
    //
    //    f.onSuccess {
    //      case result: Array[Document] ⇒
    //        {
    //
    //          println("结果:" + result.length)
    //
    //        }
    //    }
    //
    //    f.onFailure {
    //      case e: Exception ⇒ {
    //        e.printStackTrace();
    //      }
    //    }

  }
  //

  System.in.read();
  println("开始时间:" + new Date(start));
  Mineral.shutdown;
  //exit;
  //
  //  for (i ← 0 until 500) {
  //
  //    index.writer ! docs;
  //
  //    val f = index.reader ? q;
  //
  //    f.onSuccess {
  //      case result: Array[Result] ⇒
  //        {
  //          println("结果:" + result.length);
  //        }
  //    }
  //
  //    f.onFailure {
  //      case e: Exception ⇒ {
  //        e.printStackTrace();
  //      }
  //    }

  //    val f2 = index2.reader ? q;
  //
  //    f2.onSuccess {
  //      case result: Array[Result] ⇒
  //        {
  //
  //        }
  //    }
  //  }
  //  System.in.read();
  //
  //  Mineral.shutdown;
  //
  //  System.in.read();
  //  Thread.sleep(5000);
  //  val f = index.reader ? q;
  //  f.onSuccess {
  //    case result: Array[Result] ⇒
  //      {
  //        for (i ← 0 until 10) {
  //          index.writer ! docs;
  //
  //        }
  //      }
  //  }
  //
  //  Thread.sleep(5000);
  //  val f2 = index.reader ? q;
  //  f2.onSuccess {
  //    case result: Array[Result] ⇒
  //      {
  //        //println("结果:" + result.length);
  //      }
  //  }
  //  val fs = FSDirectory.open(new File("/Users/admin/Documents/mineral/mineral/testindex/1"));
  //  println(fs.getLockID());
  //
  //  println(IndexWriter.isLocked(fs));
  //

  //  val dirs = FileUtils.listFilesAndDirs(new File("/Users/admin/Documents/mineral/mineral/testindex"), new NotFileFilter(TrueFileFilter.INSTANCE), DirectoryFileFilter.DIRECTORY);
  //  dirs.filter(f ⇒ { !f.getPath().equals("/Users/admin/Documents/mineral/mineral/testindex") }).foreach(d ⇒ { println(d.getPath()) })
}