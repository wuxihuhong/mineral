package com.huhong.mineral.commands

import com.huhong.mineral.util.SystemContext
import com.huhong.mineral.configs.IndexConfig
import java.util.Date
import com.db4o.query.Predicate

import com.huhong.mineral.util.Imports._
import com.huhong.mineral.util.ConfigHelper

object Imports {
  def quit() = {

    if (SystemContext.sysInterpreter != null)
      SystemContext.sysInterpreter.close;

    sys.exit(0);
  }

  def configdb() = {
    SystemContext.configDB;
  }

  def createIndex(name: String, targetDir: String, writeThreadCount: Int = 20) = {
    try {
      ConfigHelper.createIndex(name, targetDir, writeThreadCount);
    } catch {
      case e: Exception ⇒ {
        error(e);
      }
    }
  }

  def getConfig(name: String) = {
    ConfigHelper.getConfig(name);
  }

  def deleteConfig(name: String) = {
    ConfigHelper.deleteConfig(name);
  }

  def showidx() = ConfigHelper.listConfigName;
}