package com.huhong.mineral.pool

import org.apache.commons.pool2.BasePooledObjectFactory
import com.huhong.mineral.index.IndexConnection
import org.apache.commons.pool2.PooledObject
import org.apache.commons.pool2.impl.DefaultPooledObject
import java.io.File
import com.huhong.mineral.configs.IndexConfig
import org.apache.lucene.store.FSDirectory
import com.huhong.mineral.util.SystemContext
import com.huhong.mineral.error.MineralExpcetion
import org.apache.lucene.index.IndexWriterConfig
import org.apache.lucene.index.IndexWriter
import org.slf4j.LoggerFactory
import java.util.concurrent.CopyOnWriteArrayList

object IndexConnectionFactory {
  private val logger = LoggerFactory.getLogger(classOf[IndexConnectionFactory]);
}
class IndexConnectionFactory(conf: IndexConfig) extends BasePooledObjectFactory[IndexConnection] {
  @volatile var index = 0;

  val allIndexConnections = new CopyOnWriteArrayList[IndexConnection];
  
  def create(): IndexConnection = {
    index += 1;
    val dir = conf.targetDir + File.separatorChar + index;
    val fs = FSDirectory.open(new File(dir));
    val aname = conf.analyzer;
    val analyzer = SystemContext.analyzers.getOrElse(aname, throw new MineralExpcetion(0, s"没有找到对应的分词器${aname}"));
    val iwc = new IndexWriterConfig(conf.version,
      analyzer);
    iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
    val writer = new IndexWriter(fs, iwc);

    IndexConnectionFactory.logger.info("索引连接:" + dir);
    val conn = new IndexConnection(writer, conf, dir, this);
    allIndexConnections.add(conn);
    conn;
  }

  def wrap(conn: IndexConnection): PooledObject[IndexConnection] = {
    new DefaultPooledObject(conn);
  }

  override def destroyObject(pooledConn: PooledObject[IndexConnection]): Unit = {
    val conn = pooledConn.getObject();
    conn.writer.commit();
    conn.writer.close();

    IndexConnectionFactory.logger.info("关闭索引连接:" + conn.realDir);
    allIndexConnections.remove(conn);
  }

  override def passivateObject(pooledConn: PooledObject[IndexConnection]): Unit = {
    val conn = pooledConn.getObject();

    if (conn.getWrited >= SystemContext.Config.commits) {
      conn.writer.commit();
      IndexConnectionFactory.logger.info("数据被提交：" + conn.realDir);
      conn.resetWrited;
    }
  }

}