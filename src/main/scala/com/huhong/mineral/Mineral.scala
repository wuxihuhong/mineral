package com.huhong.mineral

import java.util.concurrent.ConcurrentHashMap
import java.util.{ Map ⇒ JMap }
import com.huhong.mineral.configs.IndexConfig
import scala.collection.JavaConversions._
import scala.collection.mutable.ConcurrentMap
import com.huhong.mineral.util.ConfigHelper
import com.huhong.mineral.index.IndexController
import org.slf4j.LoggerFactory
import com.huhong.mineral.util.SystemContext
import akka.actor.{ ActorRef, ActorSystem, Props, Actor, Inbox }
import akka.routing.RoundRobinRouter
import com.huhong.mineral.error.MineralExpcetion
import com.huhong.mineral.util.Imports._
import com.huhong.mineral.index.IndexWriterActor
object Mineral {
  val logger = LoggerFactory.getLogger("ROOT");
  private val actors: ConcurrentHashMap[String, ActorRef] = new ConcurrentHashMap[String, ActorRef]();

  @throws(classOf[MineralExpcetion])
  def getIndex(name: String) = {
    val found = actors.find(c ⇒ { c._1.equals(name) });
    if (found.isDefined) {
      found.get._2
    } else {
      val config = ConfigHelper.getConfig(name);
      if (config != null) {
        val actorProps = Props(classOf[IndexWriterActor], new IndexController(config));
        val actor = SystemContext.actors.actorOf(actorProps.withRouter(RoundRobinRouter(config.writeThreadCount)), config.name);
        actors.put(name, actor);
       
        actor;
      } else {
        throws(0, "未找到配置");
      }
    }
  }

  @throws(classOf[MineralExpcetion])
  private def getIndex(config: IndexConfig) = {
    val found = actors.find(c ⇒ { c._1.equals(config.name) });
    if (found.isDefined) {
      found.get._2
    } else {

      if (config != null) {
        val actorProps = Props(classOf[IndexWriterActor], new IndexController(config));
        val actor = SystemContext.actors.actorOf(actorProps.withRouter(RoundRobinRouter(config.writeThreadCount)), config.name);
        actors.put(config.name, actor);
        actor;
      } else {
        throws(0, "未找到配置");
      }
    }
  }
  //启动索引核心
  @throws(classOf[MineralExpcetion])
  def start() = {

    logger.info("启动索引核心");
    val cnfs = ConfigHelper.getConfigs();

    //初始化所有的IndexController和IndexHandler actor
    cnfs.foreach(c ⇒ {

      getIndex(c);
    })
  }
}