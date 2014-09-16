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

class IndexConnection(val writer: IndexWriter, val indexConf: IndexConfig, val realDir: String, val factory: IndexConnectionFactory) {
  //private val lock = new ReentrantReadWriteLock();

  @volatile private var writeCount = 0

  var oldReader: DirectoryReader = _;
  openReader(false);

  private def openReader(delete: Boolean) = {

    oldReader = DirectoryReader.open(writer, delete);

  }

  private def openIfChangedReader(delete: Boolean) = {

    oldReader = DirectoryReader.openIfChanged(oldReader, writer, delete);

  }

  def addWrited(count: Int) = {

    //    try {
    //      lock.writeLock().lock();
    writeCount = writeCount + count;
    //    } finally {
    //      lock.writeLock().unlock();
    //    }
  }

  def resetWrited() = {
    //    try {
    //      lock.writeLock().lock();
    writeCount = 0
    //    } finally {
    //      lock.writeLock().unlock();
    //    }
  }

  def getWrited() = {
    //    try {
    //      lock.readLock().lock();
    writeCount;
    //    } finally {
    //      lock.readLock().unlock();
    //    }
  }
  protected def getMyReader = {
    this.synchronized {
      if (!oldReader.isCurrent()) {
        openIfChangedReader(true)

      }

      oldReader;
    }

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