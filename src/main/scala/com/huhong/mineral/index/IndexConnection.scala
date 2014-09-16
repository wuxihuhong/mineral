package com.huhong.mineral.index

import org.apache.lucene.index.IndexWriter
import org.apache.lucene.index.DirectoryReader
import com.huhong.mineral.configs.IndexConfig
import org.apache.commons.io.DirectoryWalker
import java.util.concurrent.locks.ReentrantReadWriteLock

class IndexConnection(val writer: IndexWriter, val indexConf: IndexConfig, val realDir: String) {
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
  def getReader = {

    this.synchronized {

      if (!oldReader.isCurrent()) {
        openIfChangedReader(true)

      }

      oldReader;
    }

  }
}