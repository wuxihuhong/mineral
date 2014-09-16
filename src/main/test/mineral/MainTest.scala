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
import org.junit.Before
import com.huhong.mineral.Mineral
import org.junit.After
import org.apache.lucene.search.TermQuery
import org.apache.lucene.index.Term
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext
import akka.dispatch._
import com.huhong.mineral.index.IndexController
import org.apache.lucene.store.FSDirectory
import java.io.File
import org.apache.lucene.index.DirectoryReader
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.index.MultiReader

class MainTest {

  //@Before
  def init(): Unit = {
    SystemContext.configDB = Db4oEmbedded.openFile(Db4oEmbedded.newConfiguration(), "config.yap");
    Mineral.start;
  }

  @Test
  def test2(): Unit = {
    val dir = "/Users/admin/Documents/mineral/mineral/testindex";
    val readers = (1 to 20) map { i ⇒
      {
        val fs = FSDirectory.open(new File(dir + "/" + i));
        DirectoryReader.open(fs);
      }
    }
    val q = new TermQuery(new Term("content", "大"));

    val searcher = new IndexSearcher(new MultiReader(readers: _*))
    val start = System.currentTimeMillis();
    val docs = searcher.search(q, SystemContext.Config.defaultSearchMaxDocs);

    println("命中:" + docs.totalHits);
    val ret = docs.scoreDocs.map(d ⇒ {
      searcher.doc(d.doc);

    });
    val end = System.currentTimeMillis();
    println((end - start) + "ms");
  }

  def test(): Unit = {
    implicit val timeout = new Timeout(30 seconds)
    val index = Mineral.getIndex("test");
    val name = index.indexConfig.name;
    implicit def executor: ExecutionContext = index.system.dispatchers.lookup(s"$name-thread-pool-dispatcher");

    val q = new TermQuery(new Term("content", "大"));

    for (i ← 0 until 2) {
      spawn {
        val start = System.currentTimeMillis();
        val f = index.actor ? q;
        f onSuccess {
          case _ ⇒ {
            val end = System.currentTimeMillis();
            println("耗时:" + (end - start) + "ms");
          }
        }
      }
    }
    System.in.read();
  }

  //  @Test
  //  def test2(): Unit = {
  //    val config = ConfigHelper.getConfig("test");
  //    val ic = new IndexController(config);
  //    for (i ← 0 until 21) {
  //      spawn {
  //        val o = ic.indexConnPool.borrowObject();
  //        println(o.realDir);
  //        Thread.sleep(1000);
  //        ic.indexConnPool.returnObject(o);
  //      }
  //    }
  //
  //    System.in.read();
  //    ic.indexConnPool.close;
  //  }

  //@After
  def onExit(): Unit = {
    Mineral.shutdown;
    SystemContext.configDB.close();
  }
}