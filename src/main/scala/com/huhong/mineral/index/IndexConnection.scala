package com.huhong.mineral.index

import org.apache.lucene.index.IndexWriter
import org.apache.lucene.index.DirectoryReader
import com.huhong.mineral.configs.IndexConfig
import org.apache.commons.io.DirectoryWalker
import java.util.concurrent.locks.ReentrantReadWriteLock
import com.huhong.mineral.pool.IndexConnectionFactory
import org.apache.lucene.index.IndexReader
import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import com.huhong.mineral.util.SystemContext
import org.apache.lucene.search.Query
import org.slf4j.LoggerFactory

object IndexConnection {
  val log = LoggerFactory.getLogger(classOf[IndexConnection]);
}
class IndexConnection(val writer: IndexWriter, val indexConf: IndexConfig, val realDir: String, val factory: IndexConnectionFactory) {
  private val lock = new ReentrantReadWriteLock();

  @volatile private var writeCount = 0

  @volatile var oldReader: DirectoryReader = openReader(false);

  private def openReader(delete: Boolean) = {

    DirectoryReader.open(writer, delete);

  }

  private def openIfChangedReader(delete: Boolean) = {

    val newReader = DirectoryReader.openIfChanged(oldReader, writer, delete);
    if (newReader != null) {
      oldReader = newReader;
    }
  }

  def delete(q: Query): Unit = {
    factory.allIndexConnections.foreach(c ⇒ {
      c.writer.deleteDocuments(q);

      c.writer.commit();
      IndexConnection.log.debug(c.realDir + "删除索引!");
    })
  }

  def addWrited(count: Int) = {

    writeCount = writeCount + count;

  }

  def resetWrited() = {

    writeCount = 0

  }

  def getWrited() = {

    writeCount;

  }
  protected def getMyReader = {

    if (!oldReader.isCurrent())
      openIfChangedReader(false);
    oldReader;

  }

  @volatile private var readers: Array[IndexReader] = factory.allIndexConnections.map(c ⇒ {
    c.getMyReader;
  }).toArray;

  @volatile private var lastUpdateReaderTime = System.currentTimeMillis();

  def getReaders() = {

    if (readers == null || System.currentTimeMillis() - lastUpdateReaderTime > SystemContext.Config.reloadReaderInterval) {

      readers = factory.allIndexConnections.map(c ⇒ {
        c.getMyReader;
      }).toArray;
      lastUpdateReaderTime = System.currentTimeMillis();

    }
    readers;

  }

}