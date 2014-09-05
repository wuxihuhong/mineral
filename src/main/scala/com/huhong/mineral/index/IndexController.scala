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

//控制索引的所有witer
class IndexController(private val indexconfig: IndexConfig) {
  val indexwriters: ThreadLocal[IndexWriter] = new ThreadLocal[IndexWriter];

  def getWriter() = {

    val writer = indexwriters.get();

    if (writer == null) {
      //println(Thread.currentThread().getName());

      val fs = getIndexDirByNoUsed(indexconfig.getTargetDir);

      val aname = indexconfig.analyzer;
      val analyzer = SystemContext.analyzers.getOrElse(aname, throw new MineralExpcetion(0, s"没有找到对应的分词器${aname}"))
      val iwc = new IndexWriterConfig(Version.LUCENE_4_9,
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
      val uuid = UUID.randomUUID().toString().replace("-", "");
      FSDirectory.open(new File(root + "/" + uuid));

    }
  }
}

