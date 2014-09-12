package com.huhong.mineral.index

import org.apache.lucene.index.IndexReader
import java.io.File
import scala.util.control.Breaks._
import org.apache.lucene.store.FSDirectory
import java.util.concurrent.locks.{ ReadWriteLock, ReentrantReadWriteLock }
import org.apache.lucene.index.DirectoryReader

class IndexReaderHolder(cids: List[File]) {

  if (cids != null) {
    reOpenIndexReader(cids);
  }

  var indexReaders: Array[IndexReader] = _;
  var childrenIndexDirs: List[File] = _;
  private val lock = new ReentrantReadWriteLock();

  def getIndexReaders(cids: List[File]) = {

    if (isMustReOpen(cids)) {
      reOpenIndexReader(cids);
    }
    indexReaders;

  }

  private def reOpenIndexReader(cids: List[File]): Unit = {
    lock.writeLock().lock();
    indexReaders = cids.map(f ⇒ {
      val fs = FSDirectory.open(f);
      DirectoryReader.open(fs);

    }).toArray;

    childrenIndexDirs = cids;
    lock.writeLock().unlock();
  }

  private def isMustReOpen(cids: List[File]): Boolean = {
    lock.readLock().lock()
    val r = if (indexReaders == null) {
      true;
    } else {
      if (cids.length != indexReaders.length) {
        true;
      } else {
        var ret = false;
        breakable {
          cids.foreach(f ⇒ {
            val found = childrenIndexDirs.exists(ff ⇒ { ff.getPath().equals(f.getParent()) });
            if (!found) {
              ret = true;
              break;
            }
          })
        }
        ret;

      }
    }
    lock.readLock().unlock();
    r;
  }

  def refreshReaders() = {

    if (indexReaders != null) {
      lock.writeLock().lock();
      indexReaders = indexReaders.map { reader ⇒
        {
          if (reader.isInstanceOf[DirectoryReader]) {
            val dirreader = reader.asInstanceOf[DirectoryReader];
            if (!dirreader.isCurrent()) {
              DirectoryReader.openIfChanged(dirreader);
            } else {
              reader;
            }
          } else {
            reader;
          }
        }
      }
      lock.writeLock().unlock();
    }
  }
}