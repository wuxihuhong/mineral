package mineral

import org.junit.Test
import org.apache.commons.pool2.PooledObjectFactory
import org.apache.lucene.index.IndexWriter
import org.apache.commons.pool2.BasePooledObjectFactory
import org.apache.commons.pool2.PooledObject
import org.apache.commons.pool2.impl.DefaultPooledObject
import org.apache.commons.pool2.impl.GenericObjectPoolConfig
import scala.concurrent.ops._
import com.huhong.mineral.util.SystemContext
import com.db4o.Db4oEmbedded
import com.huhong.mineral.pool.IndexConnectionFactory
import com.huhong.mineral.pool.IndexConnectionFactory
import com.huhong.mineral.pool.IndexConnectionPool
import com.huhong.mineral.configs.ConfigHelper

class MainTest {

  SystemContext.configDB = Db4oEmbedded.openFile(Db4oEmbedded.newConfiguration(), "config.yap");
  val indexConf = ConfigHelper.getConfig("test");

  val conf = new GenericObjectPoolConfig();
  conf.setMaxTotal(indexConf.getWriteThreadCount);
  conf.setMaxIdle(indexConf.getWriteThreadCount);
  val indexConnFactory = new IndexConnectionFactory(indexConf);
  val indexConnPool = new IndexConnectionPool(indexConnFactory, conf);

  @Test
  def poolTest(): Unit = {
    val conn = indexConnPool.borrowObject();
    println("取出:" + conn.realDir);
    indexConnPool.returnObject(conn);
    indexConnPool.close;
  }
}