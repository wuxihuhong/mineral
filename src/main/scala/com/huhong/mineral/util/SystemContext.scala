package com.huhong.mineral.util

import scala.tools.nsc.interpreter.ILoop
import com.db4o.ObjectContainer
import akka.actor.{ ActorRef, ActorSystem, Props, Actor, Inbox }
import org.apache.lucene.analysis.core.SimpleAnalyzer
import org.apache.lucene.util.Version
import org.apache.lucene.analysis.Analyzer
import scala.collection.mutable.Map
import org.apache.lucene.analysis.cn.ChineseAnalyzer
import com.typesafe.config.ConfigFactory

object SystemContext {

  val analyzers = Map[String, Analyzer]()
  analyzers += ("english" -> new SimpleAnalyzer(Version.LUCENE_4_9));
  analyzers += ("chinese" -> new ChineseAnalyzer());
  analyzers += ("default" -> new ChineseAnalyzer());
  var sysInterpreter: ILoop = _;

  var configDB: ObjectContainer = _;

  //lazy val actors = ActorSystem("root", ConfigFactory.load(customConf));

  object Config {
    //尝试写索引次数(如果出错)
    var tryWriteDocmuentTimes: Int = 3;
    //搜索的默认返回数据行数
    var defaultSearchMaxDocs: Int = 3000;
    //cpu核心数，要保障高性能请合理配置
    var cpus: Int = 4;
    //提交数 0立即提交
    var commits: Long = 1000;

    //重置reader的时间，0为根据变更重置
    var reloadReaderInterval: Long = 0;

    //加载doc详细内容的超时
    var loadDocuemntTimeOut: Long = 16000;

    var remoteAkka = true;

  }
}

