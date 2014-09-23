package com.huhong.mineral.test

import org.scalatest._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import com.huhong.mineral.util.SystemContext
import com.db4o.Db4oEmbedded
import com.huhong.mineral.Mineral
import com.typesafe.config.ConfigFactory
import akka.actor.ActorSystem
import scala.actors.threadpool.Executors
import mineral.ChineseUtils
import com.huhong.mineral.messages.SerializableDocuments
import org.apache.lucene.search.TermQuery
import org.apache.lucene.index.Term
import akka.util.Timeout
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext
import akka.dispatch._
import akka.pattern.ask
import com.huhong.mineral.messages.Querys
import scala.concurrent.Await
import com.huhong.mineral.util.Imports._

@RunWith(classOf[JUnitRunner])
class BaseTest extends FunSuite with BeforeAndAfter {

  private val threadPool = Executors.newFixedThreadPool(50);

  before {

    Mineral.start;
  }

  private def getRemoteActor(indexName: String) = {
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
    system.actorSelection(s"akka.tcp://mineral@127.0.0.1:1433/user/$indexName");
  }

  private def getLocalActor(indexName: String) = {
    Mineral.getIndex(indexName).actor;
  }

  test("read and write test") {
    val actor = getRemoteActor("test");
    val writeHandler = new Runnable {
      def run = {
        val title = ChineseUtils.getFixedLengthChinese(5);
        val content = ChineseUtils.getFixedLengthChinese(500);
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
    }

    val readHandler = new Runnable {
      def run = {
        val q = new TermQuery(new Term("content", "胡"));
        val start = System.currentTimeMillis();
        implicit val timeout = new Timeout(10 seconds)
        val f = actor.ask(Querys(q.toString, begin = 0, end = 100))(timeout);
        val ret = Await.result(f, timeout.duration).asInstanceOf[Array[Byte]];
        val end = System.currentTimeMillis();

        printlnjson(bytesToJson(ret));
      }
    }

    for (i ← 0 to 20000) {
      if (i % 2 == 0)
        threadPool.execute(writeHandler);
      else {
        threadPool.execute(readHandler);
      }
    }

    pause;
  }

  after {
    Mineral.shutdown;
  }
}