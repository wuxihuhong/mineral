package com.huhong.mineral.index

import akka.actor.Actor
import com.huhong.mineral.messages._

class IndexActor(val indexController: IndexController) extends Actor {

  def receive = {
    case doc: Document ⇒ {

    }

    case str: String ⇒ {
      println(str);

      val writer = indexController.getWriter;

      println(Thread.currentThread().getName() + ":" + writer)
      Thread.sleep(5000);
    }
  }
}

