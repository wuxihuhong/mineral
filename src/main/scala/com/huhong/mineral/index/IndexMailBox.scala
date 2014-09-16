package com.huhong.mineral.index
import akka.dispatch.UnboundedPriorityMailbox
import akka.actor.ActorSystem
import com.typesafe.config.Config
import akka.dispatch.PriorityGenerator
import org.apache.lucene.search.Query
import com.huhong.mineral.messages.Documents

class IndexMailBox(settings: ActorSystem.Settings, config: Config) extends UnboundedPriorityMailbox(PriorityGenerator {
  // 高优先级消息应尽可能先处理
  case _: Query ⇒ 0
  case _: Documents ⇒ 1;
  case otherwise ⇒ 2;
});