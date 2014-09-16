package com.huhong.mineral.pool

import org.apache.commons.pool2.impl.DefaultPooledObject
import org.apache.lucene.index.IndexWriter
import org.apache.commons.pool2.impl.GenericObjectPool
import org.apache.commons.pool2.PooledObjectFactory
import org.apache.commons.pool2.impl.GenericObjectPoolConfig
import org.apache.commons.pool2.impl.AbandonedConfig
import com.huhong.mineral.index.IndexConnection
import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import org.slf4j.LoggerFactory
import org.apache.lucene.store.AlreadyClosedException
import org.apache.lucene.index.IndexReader
import com.huhong.mineral.util.SystemContext

object IndexConnectionPool {
  private val logger = LoggerFactory.getLogger(classOf[IndexConnectionPool]);
}
class IndexConnectionPool(factory: IndexConnectionFactory,
  config: GenericObjectPoolConfig) extends GenericObjectPool[IndexConnection](factory,
  config) {

  init();
  def init() = {
    for (i ← 0 until config.getMaxTotal()) {
      this.addObject();

    }

  }

  



  //  override def close(): Unit = {
  //
  //    closeLock.synchronized {
  //
  //      allObjects.foreach(kv ⇒ {
  //        val conn = kv._1;
  //        conn.writer.commit();
  //
  //        conn.writer.close();
  //
  //        IndexConnectionPool.logger.info(conn.realDir + "已经提交");
  //        conn.writeCount = 0;
  //      })
  //      //      var obj = this.idleObjects.poll();
  //      //      while (obj != null) {
  //      //        val conn = obj.getObject();
  //      //        conn.writer.commit();
  //      //
  //      //        conn.writer.close();
  //      //
  //      //        IndexConnectionPool.logger.info(conn.realDir + "已经提交");
  //      //        conn.writeCount = 0;
  //      //
  //      //        obj = this.idleObjects.poll();
  //      //      }
  //
  //    }
  //    super.close;
  //  }
}