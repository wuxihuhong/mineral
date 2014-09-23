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
import com.huhong.mineral.error.MineralExpcetion
import org.apache.lucene.index.IndexWriterConfig
import org.apache.lucene.util.Version
import org.apache.lucene.document.Document
import scala.concurrent.Await
import org.apache.lucene.search.TopDocs
import com.huhong.mineral.messages.SearchResult
import com.huhong.mineral.messages.LoadDocument
import scala.collection.mutable.ListBuffer
import java.util.concurrent.CopyOnWriteArrayList
import akka.actor.ActorSystem
import com.huhong.mineral.messages.TestString
import com.typesafe.config.ConfigFactory
import org.apache.lucene.document.FieldType
import org.apache.lucene.document.Field
import java.util.UUID
import com.huhong.mineral.messages.Documents
import com.huhong.mineral.messages.SearchResult
import com.huhong.mineral.messages.QueryString
import com.huhong.mineral.util.Imports._
import com.huhong.mineral.messages.Querys
import com.huhong.mineral.messages.Deletes

import com.huhong.mineral.messages.SerializableDocuments
import com.huhong.mineral.messages.SerializableDocuments

class MainTest {

  @Before
  def init(): Unit = {
   
    Mineral.start;
  }

  def testMerge(): Unit = {
    val start = System.currentTimeMillis();
    val dir = "/Users/admin/Documents/mineral/mineral/testindex/0";

    val fs = FSDirectory.open(new File(dir));
    val aname = "default";
    val analyzer = SystemContext.analyzers.getOrElse(aname, throw new MineralExpcetion(0, s"没有找到对应的分词器${aname}"));
    val iwc = new IndexWriterConfig(Version.LUCENE_30,
      analyzer);
    iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
    val writer = new IndexWriter(fs, iwc);
    val fses = (1 to 20) map { i ⇒
      {

        val dirname = "/Users/admin/Documents/mineral/mineral/testindex/" + i;
        FSDirectory.open(new File(dirname));

      }
    }

    writer.addIndexes(fses: _*)
    writer.close();
    val end = System.currentTimeMillis();
    println((end - start) + "ms");
  }

  def test2(): Unit = {
    val dir = "/Users/admin/Documents/mineral/mineral/testindex";
    val readers = (0 to 0) map { i ⇒
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

  def parseJsonToDocuments(): Unit = {
    val json = """             
				{
				  "docs":[	{
				  "fields":[
				  	{
				    "name":"title",
				    "content":"$title",
				    "config":{
				    "indexed":true,
				    "stored":true
					 }
				  },{
				    "name":"content",
				    "content":"$content",
				    "config":
				    {
				    "indexed":true,
				    "stored":true
					}
				  }]}
				  ]
				}
             """;
    val td = SerializableDocuments(json);
    val docs = td.toDocuments;
    println(docs.datas(0));
  }

  private def getRemoteActor = {
    val akkaConfig = """
          mineral { 
    		akka {
    		actor { 
    		provider = "akka.remote.RemoteActorRefProvider"}
    		remote { 
        					enabled-transports = ["akka.remote.netty.tcp"]
							netty.tcp { 
								
								maximum-frame-size = 20MiB
						
							} 
      
    						
        				}
    		}
    		} 
    		
    
          """;
    val c = ConfigFactory.parseString(akkaConfig).getConfig("mineral")
    val system = ActorSystem("mineral", c);
    system.actorSelection("akka.tcp://mineral@127.0.0.1:1433/user/test");
  }

  def writeAndReadTest() = {
    val actor = getRemoteActor;

    val title = ChineseUtils.getFixedLengthChinese(3);
    val content = ChineseUtils.getFixedLengthChinese(255);

    val json = s"""             
				{
				  "docs":[	{
				  "fields":[
				  	{
				    "name":"title",
				    "content":"$title",
				    "config":{
				    "indexed":true,
				    "stored":true
					 }
				  },{
				    "name":"content",
				    "content":"胡宏",
				    "config":
				    {
				    "indexed":true,
				    "stored":true
					}
				  }]}
				  ]
				}
             """;

    val docs = SerializableDocuments(json);
    actor ! docs;
    Thread.sleep(2000);
    val q = new TermQuery(new Term("content", "胡"));
    val start = System.currentTimeMillis();
    implicit val timeout = new Timeout(10 seconds)
    val f = actor.ask(Querys(q.toString, begin = 0, end = 100))(timeout);
    val ret = Await.result(f, timeout.duration).asInstanceOf[Array[Byte]];
    val end = System.currentTimeMillis();

    printlnjson(bytesToJson(ret));
  }
  @Test
  def pressureTest() = {
    val akkaConfig = """
          mineral { 
    		akka {
    		actor { 
    		provider = "akka.remote.RemoteActorRefProvider"}
    		remote { 
        					enabled-transports = ["akka.remote.netty.tcp"]
							netty.tcp { 
								
								maximum-frame-size = 20MiB
						
							} 
      
    						
        				}
    		}
    		} 
    		
    
          """;
    val c = ConfigFactory.parseString(akkaConfig).getConfig("mineral")
    val system = ActorSystem("mineral", c);
    val actor = system.actorSelection("akka.tcp://mineral@127.0.0.1:1433/user/test");
    val q = new TermQuery(new Term("content", "胡"));
    for (i ← 0 until 20000) {
      val runtype = if (i % 2 == 0) 2 else { if (i % 3 == 0) 3 else if (i % 100 == 0) 4 else 1 };

      runtype match {

        case 1 ⇒ { //添加
          val title = ChineseUtils.getFixedLengthChinese(3);
          val content = ChineseUtils.getFixedLengthChinese(255);

          val json = s"""             
				{
				  "docs":[	{
				  "fields":[
				  	{
				    "name":"title",
				    "content":"$title",
				    "config":{
				    "indexed":true,
				    "stored":true
					 }
				  },{
				    "name":"content",
				    "content":"$content",
				    "config":
				    {
				    "indexed":true,
				    "stored":true
					}
				  }]}
				  ]
				}
             """;

          val docs = SerializableDocuments(json);
          actor ! docs;
        }
        case 2 ⇒ { //删除
          val q = new TermQuery(new Term("content", "胡"));
          actor ! Deletes(q.toString());

        }
        case 3 ⇒ { //查询

          val q = new TermQuery(new Term("content", "胡"));
          val start = System.currentTimeMillis();
          implicit val timeout = new Timeout(60 seconds)
          val f = actor.ask(Querys(q.toString, begin = 0, end = 100))(timeout);
          val ret = Await.result(f, timeout.duration).asInstanceOf[Array[Byte]];
          val end = System.currentTimeMillis();

          printlnjson(bytesToJson(ret));
        }
        case _ ⇒ {}
      }

    }

  }

  def deleteTest(): Unit = {
    val akkaConfig = """
          mineral { 
    		akka {
    		actor { 
    		provider = "akka.remote.RemoteActorRefProvider"}
    		remote { 
        					enabled-transports = ["akka.remote.netty.tcp"]
							netty.tcp { 
								
								maximum-frame-size = 20MiB
						
							} 
      
    						
        				}
    		}
    		} 
    		
    
          """;
    val c = ConfigFactory.parseString(akkaConfig).getConfig("mineral")
    val system = ActorSystem("mineral", c);
    val actor = system.actorSelection("akka.tcp://mineral@127.0.0.1:1433/user/test");
    val q = new TermQuery(new Term("content", "胡"));

    val f = actor ! Deletes(q.toString);

    System.in.read();
  }

  def remoteTest(): Unit = {

    val akkaConfig = """
          mineral { 
    		akka {
    		actor { 
    		provider = "akka.remote.RemoteActorRefProvider"}
    		remote { 
        					enabled-transports = ["akka.remote.netty.tcp"]
							netty.tcp { 
								
								maximum-frame-size = 20MiB
						
							} 
      
    						
        				}
    		}
    		} 
    		
    
          """;
    val c = ConfigFactory.parseString(akkaConfig).getConfig("mineral")
    val system = ActorSystem("mineral", c);
    val actor = system.actorSelection("akka.tcp://mineral@127.0.0.1:1433/user/test");
    val q = new TermQuery(new Term("content", "胡"));
    implicit val timeout = new Timeout(60 seconds)
    val start = System.currentTimeMillis();

    val f = actor ? Querys(q.toString, begin = 0, end = 10);
    val ret = Await.result(f, timeout.duration).asInstanceOf[Array[Byte]];
    val end = System.currentTimeMillis();
    println("耗时:" + (end - start) + "ms");

    printlnjson(bytesToJson(ret));
    System.in.read();
  }

  def initDatas(): Unit = {
    val index = Mineral.getIndex("test");

    for (i ← 0 until 18000) {

      val doc = new Document;
      val fd = new FieldType;
      fd.setIndexed(true);
      fd.setStored(true);

      val idf = new Field("id", UUID.randomUUID().toString(), fd);
      doc.add(idf);
      val titlef = new Field("title", ChineseUtils.getFixedLengthChinese(5), fd);
      val contentf = new Field("content", ChineseUtils.getFixedLengthChinese(500), fd);
      doc.add(titlef);
      doc.add(contentf);
      val docs = Documents(Array(doc));
      index.actor ! docs;
    }

    System.in.read()
  }

  def queryTest(): Unit = {
    //    implicit val timeout = new Timeout(5 seconds)
    //    val index = Mineral.getIndex("test");
    //    val name = index.indexConfig.name;
    //    implicit def executor: ExecutionContext = index.system.dispatchers.lookup(s"$name-thread-pool-dispatcher");
    //
    //    val q = new TermQuery(new Term("content", "胡"));
    //
    //    for (i ← 0 until 1) {
    //      spawn {
    //        var start = System.currentTimeMillis();
    //        val f = index.actor ? q;
    //
    //        val sr = Await.result(f, timeout.duration).asInstanceOf[SearchResult]
    //
    //        var end = System.currentTimeMillis();
    //        println("耗时1:" + (end - start) + "ms" + "发现数据:" + sr.docs.totalHits);
    //        //        start = System.currentTimeMillis();
    //        //        sr.docs.scoreDocs.map(d ⇒ {
    //        //          sr.searcher.doc(d.doc);
    //        //        });
    //        //        end = System.currentTimeMillis();
    //        //        println("耗时2:" + (end - start) + "ms" + "发现数据:" );
    //        val rets = new CopyOnWriteArrayList[Document];
    //        start = System.currentTimeMillis();
    //
    //        val docids = sr.docs.scoreDocs.map(d ⇒ {
    //          d.doc;
    //        });
    //        //val ld = LoadDocument(sr.searcher, docids);
    //        val df = index.actor ? ld;
    //        df onSuccess {
    //          case docs: Array[_] ⇒ {
    //            end = System.currentTimeMillis();
    //            println("耗时2:" + (end - start) + "ms" + "发现数据:" + docs.length);
    //          }
    //        }
    //      }
    //    }
    //    System.in.read();
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

  @After
  def onExit(): Unit = {
    Mineral.shutdown;
    
  }
}