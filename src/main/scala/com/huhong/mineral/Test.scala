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

object Test extends App {
  SystemContext.configDB = Db4oEmbedded.openFile(Db4oEmbedded.newConfiguration(), "config.yap");
  Mineral.start;

//  import com.huhong.mineral.commands.Imports._
//  createIndex("test", "/Users/admin/Documents/mineral/mineral/testindex", "default", 10, Version.LUCENE_30);
//
//  exit;
  val index = Mineral.getIndex("test");

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

  val docs = Documents(Array(doc))
  for (i ← 0 until 5)
    index ! docs;

  //  val fs = FSDirectory.open(new File("/Users/admin/Documents/mineral/mineral/testindex/1"));
  //  println(fs.getLockID());
  //
  //  println(IndexWriter.isLocked(fs));
  //

  //  val dirs = FileUtils.listFilesAndDirs(new File("/Users/admin/Documents/mineral/mineral/testindex"), new NotFileFilter(TrueFileFilter.INSTANCE), DirectoryFileFilter.DIRECTORY);
  //  dirs.filter(f ⇒ { !f.getPath().equals("/Users/admin/Documents/mineral/mineral/testindex") }).foreach(d ⇒ { println(d.getPath()) })
}