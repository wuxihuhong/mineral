package com.huhong.mineral.util

import com.huhong.mineral.util.SystemContext._
import com.db4o.query.Predicate
import com.huhong.mineral.configs.IndexConfig
import java.util.Date
import com.huhong.mineral.error.MineralExpcetion
import com.huhong.mineral.util.Imports._
import org.apache.lucene.util.Version

object ConfigHelper {

  @throws(classOf[MineralExpcetion])
  def createIndex(conf: IndexConfig): IndexConfig = {
    val rets = configDB.query(new Predicate[IndexConfig]() {

      def `match`(c: IndexConfig): Boolean = {
        c.name.equals(conf.name);
      }
    });
    if (rets.size() > 0) {
      throws(0, "索引已经存在!");
    }

    configDB.store(conf);
    configDB.commit();
    conf;
  }

  @throws(classOf[MineralExpcetion])
  def createIndex(name: String, targetDir: String, analyzer: String, writeThreadCount: Int = 20, version: Version = Version.LUCENE_40): IndexConfig = {

    val c = IndexConfig(name, targetDir, analyzer, writeThreadCount, writeThreadCount * 10, true, new Date, 0, version);
    createIndex(c);
  }

  def getConfig(name: String) = {
    val rets = configDB.query(new Predicate[IndexConfig]() {

      def `match`(c: IndexConfig): Boolean = {
        c.name.equals(name);
      }
    });
    if (rets != null && rets.size() > 0) {
      rets.get(0);
    } else {
      null;
    }
  }

  def getConfigs() = {
    configDB.query(new Predicate[IndexConfig]() {

      def `match`(c: IndexConfig): Boolean = {
        c.enabled == true
      }
    });

  }

  def listConfigName() = {
    val rets = configDB.query(new Predicate[IndexConfig]() {

      def `match`(c: IndexConfig): Boolean = {
        println(c.name);

        false;
      }
    });
  }

  def deleteConfig(name: String) = {
    val rets = configDB.query(new Predicate[IndexConfig]() {

      def `match`(c: IndexConfig): Boolean = {
        c.name.equals(name);
      }
    });
    if (rets != null && rets.size() > 0) {
      configDB.delete(rets.get(0))
      configDB.commit();
    }
  }
}