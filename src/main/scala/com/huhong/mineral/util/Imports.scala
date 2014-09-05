package com.huhong.mineral.util

import com.huhong.mineral.error.MineralExpcetion

object Imports {
  def error(e: Exception) = System.err.println(e.getMessage());

  @throws(classOf[MineralExpcetion])
  def throws(errorCode: Int, msg: String = null, cause: Throwable = null,
    enableSuppression: Boolean = true, writableStackTrace: Boolean = true) = {
    throw new MineralExpcetion(errorCode, msg, cause, enableSuppression, writableStackTrace);
  }
}