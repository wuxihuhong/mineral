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

object Test extends App {
  SystemContext.configDB = Db4oEmbedded.openFile(Db4oEmbedded.newConfiguration(), "config.yap");
  Mineral.start;
  val index = Mineral.getIndex("test");

  for (i ← 0 until 20)
    index ! "";

  //  val fs = FSDirectory.open(new File("/Users/admin/Documents/mineral/mineral/testindex/1"));
  //  println(fs.getLockID());
  //
  //  println(IndexWriter.isLocked(fs));
  //

  //  val dirs = FileUtils.listFilesAndDirs(new File("/Users/admin/Documents/mineral/mineral/testindex"), new NotFileFilter(TrueFileFilter.INSTANCE), DirectoryFileFilter.DIRECTORY);
  //  dirs.filter(f ⇒ { !f.getPath().equals("/Users/admin/Documents/mineral/mineral/testindex") }).foreach(d ⇒ { println(d.getPath()) })
}