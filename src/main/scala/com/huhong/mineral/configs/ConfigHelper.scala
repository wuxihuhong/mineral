package com.huhong.mineral.configs

import com.huhong.mineral.util.SystemContext._
import com.db4o.query.Predicate
import java.util.Date
import com.huhong.mineral.error.MineralExpcetion
import com.huhong.mineral.util.Imports._
import org.apache.lucene.util.Version
import com.huhong.mineral.Mineral

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
     Mineral.logger.info("创建索引:"+conf.toString);
    conf;
  }

  @throws(classOf[MineralExpcetion])
  def createIndex(name: String, targetDir: String, analyzer: String, writerCount: Int = 20, version: Version = Version.LUCENE_40): IndexConfig = {

    val c = IndexConfig(name, targetDir, analyzer, writerCount, writerCount * 2, true, new Date, 0, version, readerCount = writerCount * 3);
    
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