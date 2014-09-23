package com.huhong.mineral

import scala.tools.nsc._
import scala.tools.nsc.interpreter._
import scala.tools.nsc.interpreter.Results._
import org.apache.lucene.analysis.Analyzer
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.store.FSDirectory
import java.io.File
import java.util.ResourceBundle
import scala.collection.mutable.Buffer
import java.util.jar.Manifest
import org.apache.commons.io.FileUtils
import java.lang.Runnable

import com.huhong.mineral.util.SystemContext
import com.db4o.Db4oEmbedded
import org.slf4j.LoggerFactory
import scala.collection.JavaConversions._
import scala.collection.JavaConverters._

object Main extends App {

  Runtime.getRuntime().addShutdownHook(new Thread() {

    override def run() = {
      //      if (SystemContext.configDB != null) {
      //
      //        println("正在关闭配置数据库");
      //        SystemContext.configDB.close();
      //      }
    }
  });

  val settings = new Settings;

  settings.usejavacp.value = true;
  settings.deprecation.value = true

  if (ResourceBundle.getBundle("app").containsKey("app.lib.path")) {
    val jarpath = ResourceBundle.getBundle("app").getString("app.lib.path");
    val files = FileUtils.listFiles(new File(jarpath), Array("jar"), true).asScala;

    val bootclasspath = files.map { f => { f.getAbsolutePath() } } mkString (java.io.File.pathSeparator);

    settings.bootclasspath.value = bootclasspath;
  }

  settings.embeddedDefaults(this.getClass().getClassLoader());

  settings.Yreplsync.value = true;

  val interpreter = new ILoop() {

    override def loop(): Unit = {

      intp.interpret("import com.huhong.mineral.commands.Imports._ ")

      super.loop()
    }

  };

  SystemContext.configDB = Db4oEmbedded.openFile(Db4oEmbedded.newConfiguration(), "config.yap"); //打开配置数据库

  SystemContext.sysInterpreter = interpreter;

  interpreter.process(settings);
}