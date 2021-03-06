package com.huhong.mineral

import java.util.concurrent.ConcurrentHashMap
import java.util.{ Map ⇒ JMap }
import com.huhong.mineral.configs.IndexConfig
import scala.collection.JavaConversions._
import scala.collection.mutable.ConcurrentMap
import com.huhong.mineral.configs.ConfigHelper
import com.huhong.mineral.index.IndexController
import org.slf4j.LoggerFactory
import com.huhong.mineral.util.SystemContext
import akka.actor.{ ActorRef, ActorSystem, Props, Actor, Inbox }
import akka.routing.RoundRobinRouter
import com.huhong.mineral.error.MineralExpcetion
import com.huhong.mineral.util.Imports._
import com.huhong.mineral.index.IndexActor
import com.huhong.mineral.index.IndexActor
import com.typesafe.config.ConfigFactory
import scala.concurrent.ExecutionContext
import com.huhong.mineral.index.Index
import scala.concurrent.Future
import akka.actor.PoisonPill
import akka.actor.Actor._
import akka.actor.Address
import akka.remote.RemoteScope
import akka.actor.Deploy
import akka.remote.RemoteScope
import com.db4o.Db4oEmbedded

object Mineral {
  val logger = LoggerFactory.getLogger("ROOT");
  private val actors: ConcurrentHashMap[String, Index] = new ConcurrentHashMap[String, Index]();

  @throws(classOf[MineralExpcetion])
  def getIndex(name: String): Index = {
    val found = actors.find(c ⇒ { c._1.equals(name) });
    if (found.isDefined) {
      found.get._2
    } else {
      val config = ConfigHelper.getConfig(name);
      if (config != null) {
        getIndex(config);
        //        val ic = new IndexController(config);
        //        val name = config.name;
        //        val maxReadCount = config.readThreadCount;
        //        val maxWriteCount = config.writeThreadCount;
        //        val confstr = s"""
        //		     $name-write-thread-pool-dispatcher {
        //		        		type = PinnedDispatcher
        //		        		executor = "fork-join-executor"
        //		        		fork-join-executor {
        //		        			parallelism-max = $maxWriteCount
        //		        		}
        //		        		throughput = 1
        //		     }
        //		     $name-read-thread-pool-dispatcher {
        //		        		type = PinnedDispatcher
        //		        		executor = "fork-join-executor"
        //		        		fork-join-executor {
        //		        			parallelism-max = $maxReadCount
        //		        		}
        //		        		throughput = 1
        //		     }
        //        """;
        //        val akkaConfig = ConfigFactory.parseString(confstr)
        //        val system = ActorSystem(name, ConfigFactory.load(akkaConfig));
        //        val actorPropsWriter = Props(classOf[IndexWriterActor], ic);
        //        val writer = system.actorOf(actorPropsWriter.withRouter(RoundRobinRouter(config.writeThreadCount)).withDispatcher(s"$name-write-thread-pool-dispatcher"), config.name + "-writer");
        //        val actorPropsReader = Props(classOf[IndexReaderActor], ic);
        //        val reader = system.actorOf(actorPropsReader.withRouter(RoundRobinRouter(config.readThreadCount)).withDispatcher(s"$name-read-thread-pool-dispatcher"), config.name + "-reader");
        //        val actor = new IndexActor(config, system, reader, writer);
        //        actors.put(name, actor);
        //
        //        actor;
      } else {
        throws(0, "未找到配置");
      }
    }
  }

  @throws(classOf[MineralExpcetion])
  private def getIndex(config: IndexConfig): Index = {
    val found = actors.find(c ⇒ { c._1.equals(config.name) });
    if (found.isDefined) {
      found.get._2
    } else {

      if (config != null) {
        val ic = new IndexController(config);
        val name = config.name;

        val maxThreadCount = config.coreThreadCount;
        val maxReaderCount = config.readerCount;
        val hostname = config.hostname;
        val port = config.port;
        val remoteConfig = if (config.remote) {
          s"""actor {
        					provider = "akka.remote.RemoteActorRefProvider"
          				}
        				remote { 
        					enabled-transports = ["akka.remote.netty.tcp"]
							netty.tcp { 
								hostname = $hostname
								port = $port
								
								maximum-frame-size = 20MiB
							} 
							
    						
        				} """;
        } else {
          "";
        }
        val confstr = s"""
        	
        		 mineral{
        			akka{
        				loglevel = "DEBUG"
        				$remoteConfig
        			}
				     $name-thread-pool-dispatcher {
				     			mailbox-type = "com.huhong.mineral.index.IndexMailBox"
				        		type = Dispatcher
				        		executor = "thread-pool-executor"
				        		thread-pool-executor {
				        			core-pool-size-max = $maxThreadCount
				        		}
				        		throughput = 1
				     }
				    
				     $name-docloader-thread-pool-dispatcher {
				     			mailbox-type = "com.huhong.mineral.index.IndexMailBox"
				        		type = Dispatcher
				        		executor = "thread-pool-executor"
				        		thread-pool-executor {
				        			core-pool-size-max = $maxReaderCount
				        		}
				        		throughput = 1
				     }
      
    
				 }	
        		
       
        """;
        val akkaConfig = ConfigFactory.parseString(confstr)

        val system = ActorSystem("mineral", akkaConfig.getConfig("mineral"));

        val actorPropsWriter =

          Props(classOf[IndexActor], ic); //.withDeploy(Deploy(scope=RemoteScope(Address("akka.tcp","mineral","0.0.0.0",3000))));

        val actor = system.actorOf(actorPropsWriter.withRouter(RoundRobinRouter(maxThreadCount)).withDispatcher(s"$name-thread-pool-dispatcher"), config.name);
        Mineral.logger.info("启动actor:" + actor.path)
        val index = new Index(config, system, actor, ic);

        actors.put(config.name, index);
        index;
      } else {
        throws(0, "未找到配置");
      }
    }
  }
  //启动索引核心
  @throws(classOf[MineralExpcetion])
  def start() = {
    SystemContext.configDB = Db4oEmbedded.openFile(Db4oEmbedded.newConfiguration(), "config.yap");
    logger.info("启动索引核心");
    val cnfs = ConfigHelper.getConfigs();

    //初始化所有的IndexController和IndexHandler actor
    cnfs.foreach(c ⇒ {

      getIndex(c);
    })

  }

  def shutdown() = {
    import akka.pattern.gracefulStop

    import scala.concurrent.duration._
    import scala.concurrent.Await
    actors.foreach(i ⇒ {

      try {
        val stopped: Future[Boolean] = gracefulStop(i._2.actor, 5 seconds, PoisonPill);
        Await.result(stopped, 6 seconds);

      } catch {
        case e: akka.pattern.AskTimeoutException ⇒ {
          logger.warn("关闭上下文超时！");
        }
      }

      i._2.indexController.shutdown;
    })

    Mineral.logger.info("已经关闭索引上下文!");

    SystemContext.configDB.close();
  }

}