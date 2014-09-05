package com.huhong.mineral

import com.db4o.Db4o
import com.db4o.Db4oEmbedded
import com.db4o.query.Predicate
import akka.actor.{ ActorRef, ActorSystem, Props, Actor, Inbox }
import scala.concurrent.duration._
import scala.concurrent.ops._
import com.typesafe.config.ConfigFactory
import akka.routing.RoundRobinRouter
import akka.routing.SmallestMailboxRouter
import java.util.concurrent.ConcurrentHashMap
import scala.collection.JavaConversions._

object Test extends App {

  private val controllers: collection.mutable.ConcurrentMap[String, String] = new ConcurrentHashMap[String, String]();
  controllers.put("1", "1");
  
 
  exit;
  class Tester extends Actor {
    var greeting = ""

    def receive = {
      case _ ⇒ {
        println(Thread.currentThread().getName());
        Thread.sleep(2000);
      }

    }
  }

  val customConf = ConfigFactory.parseString("""
      akka.actor.deployment {
        /test-akka1{
          router = "round-robin"
          nr-of-instances = 20
        }
      }
      """)

  val conf = ConfigFactory.load(customConf);
  val system = ActorSystem("helloakka", conf.getConfig("akka.actor.deployment"));

  // Create the 'tester' actor
  val props = Props[Tester];

  val tester =
    system.actorOf(
      props.withRouter(SmallestMailboxRouter(10)), "test-akka1")
  for (i ← 0 to 21)
    //spawn {
    tester ! "hi";
  //}

}