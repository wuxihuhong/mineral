package com.huhong.mineral.util

import scala.tools.nsc.interpreter.ILoop
import com.db4o.ObjectContainer
import akka.actor.{ ActorRef, ActorSystem, Props, Actor, Inbox }
import org.apache.lucene.analysis.core.SimpleAnalyzer
import org.apache.lucene.util.Version
import org.apache.lucene.analysis.Analyzer
import scala.collection.mutable.Map
import org.apache.lucene.analysis.cn.ChineseAnalyzer

object SystemContext {

  val analyzers = Map[String, Analyzer]()
  analyzers += ("english" -> new SimpleAnalyzer(Version.LUCENE_4_9));
  analyzers += ("chinese" -> new ChineseAnalyzer());
  analyzers += ("default" -> new ChineseAnalyzer());
  var sysInterpreter: ILoop = _;

  var configDB: ObjectContainer = _;

  lazy val actors = ActorSystem("root");

  object Config {
    var tryWriteDocmuentTimes: Int = 3;
    var defaultSearchMaxDocs: Int = 5000;
  }

}