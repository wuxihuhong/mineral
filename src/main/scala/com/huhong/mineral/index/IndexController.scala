package com.huhong.mineral.index

import org.apache.lucene.index.IndexWriter
import java.util.concurrent.ConcurrentHashMap
import java.util.{ Map ⇒ JMap }
import com.huhong.mineral.configs.IndexConfig
import scala.collection.JavaConversions._
import scala.collection.mutable.ConcurrentMap
import com.huhong.mineral.configs.ConfigHelper
import org.apache.commons.io.FileUtils
import org.apache.lucene.store.FSDirectory
import java.io.File
import org.apache.lucene.index.IndexWriterConfig
import org.apache.lucene.util.Version
import com.huhong.mineral.util.SystemContext
import com.huhong.mineral.error.MineralExpcetion
import org.apache.commons.io.filefilter.IOFileFilter
import org.apache.commons.io.filefilter.NotFileFilter
import org.apache.commons.io.filefilter.TrueFileFilter
import org.apache.commons.io.filefilter.DirectoryFileFilter
import java.util.UUID
import org.apache.lucene.index.IndexReader
import java.util.concurrent.CopyOnWriteArrayList
import com.huhong.mineral.Mineral
import org.apache.lucene.index.DirectoryReader
import org.apache.commons.pool2.impl.GenericObjectPoolConfig
import com.huhong.mineral.pool.IndexConnectionFactory
import com.huhong.mineral.pool.IndexConnectionPool

//控制索引的所有witer
class IndexController(val indexconfig: IndexConfig) {
  val poolconf = new GenericObjectPoolConfig();
  poolconf.setMaxTotal(indexconfig.writerCount);
  poolconf.setMaxIdle(indexconfig.writerCount);
  val indexConnFactory = new IndexConnectionFactory(indexconfig);
  val indexConnPool = new IndexConnectionPool(indexConnFactory, poolconf);

  def getConnection() = {
    indexConnPool.borrowObject();
  }

  
  
  
  
  
  def releaseConnection(conn: IndexConnection) = {
    indexConnPool.returnObject(conn);
  }

  //  private def listChildrenDirs(root: String) = {
  //    val rets = FileUtils.listFilesAndDirs(new File(root), new NotFileFilter(TrueFileFilter.INSTANCE), DirectoryFileFilter.DIRECTORY)
  //    rets.filter(f ⇒ { !f.getPath.equals(root) }).toList;
  //
  //  }
  //
  //  private def getIndexDirByNoUsed(root: String): (FSDirectory, Boolean) = {
  //    val currents = listChildrenDirs(root);
  //    val found = currents.find(dir ⇒ {
  //
  //      val fs = FSDirectory.open(dir);
  //      val r = (IndexWriter.isLocked(fs));
  //
  //      fs.close();
  //      !r;
  //    });
  //
  //    if (found.isDefined) {
  //      (FSDirectory.open(found.get), false);
  //    } else {
  //      //创建新的子索引文件夹
  //      val uuid = UUID.randomUUID().toString().replace("-", "");
  //
  //      val ret = FSDirectory.open(new File(root + "/" + uuid));
  //
  //      (ret, true);
  //    }
  //  }

  def shutdown() = {
    if (!indexConnPool.isClosed())
      indexConnPool.close;
  }
}

