package com.huhong.mineral.util

import com.huhong.mineral.util.SystemContext._
import com.db4o.query.Predicate
import com.huhong.mineral.configs.IndexConfig
import java.util.Date
import com.huhong.mineral.error.MineralExpcetion
import com.huhong.mineral.util.Imports._

object IndexHelper {
  @throws(classOf[MineralExpcetion])
  def createIndex(name: String, targetDir: String, writeThreadCount: Int = 20) = {
    val rets = configDB.query(new Predicate[IndexConfig]() {

      def `match`(c: IndexConfig): Boolean = {
        c.name.equals(name);
      }
    });
    if (rets.size() > 0) {
      throws(0, "索引已经存在!");
    }
    val c = IndexConfig(name, targetDir, writeThreadCount, true, new Date, 0);

    configDB.store(c);
    configDB.commit();
    c;
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