package com.huhong.mineral.index

import org.apache.lucene.index.IndexWriter
import java.util.concurrent.ConcurrentHashMap
import java.util.{ Map ⇒ JMap }
import com.huhong.mineral.configs.IndexConfig
import scala.collection.JavaConversions._
import scala.collection.mutable.ConcurrentMap
import com.huhong.mineral.util.ConfigHelper
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

//控制索引的所有witer
class IndexController(private val indexconfig: IndexConfig) {
  val indexwriters: ThreadLocal[IndexWriter] = new ThreadLocal[IndexWriter];
  val indexreaders: ThreadLocal[IndexReaderHolder] = new ThreadLocal[IndexReaderHolder];

  @volatile var childrenIndexDirs: List[File] = listChildrenDirs(indexconfig.getTargetDir);

  def getReader() = {
    var readerHolder = indexreaders.get();
    if (readerHolder == null) {
      val rh = new IndexReaderHolder(childrenIndexDirs);
      indexreaders.set(rh);
      readerHolder = rh;
    }
    readerHolder;

  }
  def getWriter() = {

    val writer = indexwriters.get();

    if (writer == null) {
      //println(Thread.currentThread().getName());

      val fs = getIndexDirByNoUsed(indexconfig.getTargetDir);

      val aname = indexconfig.analyzer;
      val analyzer = SystemContext.analyzers.getOrElse(aname, throw new MineralExpcetion(0, s"没有找到对应的分词器${aname}"))
      val iwc = new IndexWriterConfig(indexconfig.version,
        analyzer);
      iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
      val w = new IndexWriter(fs, iwc);

      indexwriters.set(w);
      w;
    } else {
      writer;
    }

  }

  private def listChildrenDirs(root: String) = {
    val rets = FileUtils.listFilesAndDirs(new File(root), new NotFileFilter(TrueFileFilter.INSTANCE), DirectoryFileFilter.DIRECTORY)
    rets.filter(f ⇒ { !f.getPath.equals(root) }).toList;

  }

  private def getIndexDirByNoUsed(root: String) = {
    val currents = listChildrenDirs(root);
    val found = currents.find(dir ⇒ {

      val fs = FSDirectory.open(dir);
      val r = (IndexWriter.isLocked(fs));

      fs.close();
      !r;
    });

    if (found.isDefined) {
      FSDirectory.open(found.get);
    } else {
      //创建新的子索引文件夹
      val uuid = UUID.randomUUID().toString().replace("-", "");

      val ret = FSDirectory.open(new File(root + "/" + uuid));
      childrenIndexDirs = listChildrenDirs(root);
      ret;
    }
  }
}

