package com.huhong.mineral.messages

import org.apache.lucene.search.IndexSearcher
import akka.actor.ActorRef
import org.apache.lucene.search.TopDocs
import java.lang.Iterable
import org.apache.lucene.index.IndexableField
import java.util.ArrayList
import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import scala.util.control.Breaks._
import org.apache.lucene.util.BytesRef
import scala.collection.mutable.ListBuffer
import org.apache.lucene.search.Sort
import org.apache.lucene.search.Query
import org.apache.lucene.queryparser.classic.QueryParser
import org.apache.lucene.search.SortField
import org.apache.lucene.document.FieldType
import org.apache.lucene.document.Document
import org.apache.lucene.document.Field
import org.apache.lucene.document.LongField
import org.apache.lucene.document.IntField
import java.lang.Float
import org.apache.lucene.document.FloatField
import org.apache.lucene.document.DoubleField
import java.lang.Double
import com.huhong.mineral.util.Imports._
import org.apache.lucene.queryparser.flexible.core.config.FieldConfig
import org.apache.lucene.index.FieldInfo

//{
//  "docs":[	{
//  "fields":[
//  	{
//    "name":"",
//    "type":"string",
//    "content":"",
//    "config":{
//    "indexed":true,
//    "stored":true,
//	  "tokenized":true,
//	  "omitNorms":true,
//	  "storeTermVectors":true,
//	  "storeTermVectorOffsets":true,
//	  "storeTermVectorPositions":true,
//	  "storeTermVectorPayloads":true,
//	  "indexOptions":"",
//	  "docValueType":""
//	  }
//  },{
//    "name":"",
//    "content":"",
//    "config":
//    {
//    "indexed":true,
//    "stored":true,
//	  "tokenized":true,
//	  "omitNorms":true,
//	  "storeTermVectors":true,
//	  "storeTermVectorOffsets":true,
//	  "storeTermVectorPositions":true,
//	  "storeTermVectorPayloads":true,
//	  "indexOptions":"",
//	  "docValueType":""
//	  }
//  }]},{
//  "fields":[
//  	{
//    "name":"",
//    "content":"",
//    "config":{
//    "indexed":true,
//    "stored":true,
//	  "tokenized":true,
//	  "omitNorms":true,
//	  "storeTermVectors":true,
//	  "storeTermVectorOffsets":true,
//	  "storeTermVectorPositions":true,
//	  "storeTermVectorPayloads":true,
//	  "indexOptions":"",
//	  "docValueType":""
//	  }
//  },{
//    "name":"",
//    "content":"",
//    "config":
//    {
//    "indexed":true,
//    "stored":true,
//	  "tokenized":true,
//	  "omitNorms":true,
//	  "storeTermVectors":true,
//	  "storeTermVectorOffsets":true,
//	  "storeTermVectorPositions":true,
//	  "storeTermVectorPayloads":true,
//	  "indexOptions":"",
//	  "docValueType":""
//	  }
//  }]}
//  ]
//}

@serializable
case class SerializableDocuments(val json: String) {
  @transient def toDocuments(): Documents = {
    val jsonnode = rootObjectMapper.readTree(json);
    val docsNode = jsonnode.get("docs");
    val docs=docsNode.map(d ⇒ {
      val doc = new Document;
      val fs = d.get("fields");
      fs.foreach(f ⇒ {
        val name = f.get("name").asText();
        val t = if (f.has("type")) f.get("type").asText().toLowerCase() else "string";
        val fc = new FieldType();
        if (f.has("config")) {
          val conf = f.get("config");
          if (conf.has("indexed")) {
            fc.setIndexed(conf.get("indexed").asBoolean());
          }
          if (conf.has("stored")) {
            fc.setStored(conf.get("stored").asBoolean());
          }
          if (conf.has("tokenized")) {
            fc.setStored(conf.get("tokenized").asBoolean());
          }
          if (conf.has("omitNorms")) {
            fc.setOmitNorms(conf.get("omitNorms").asBoolean());
          }
          if (conf.has("storeTermVectors")) {
            fc.setStoreTermVectors(conf.get("storeTermVectors").asBoolean);
          }
          if (conf.has("storeTermVectorOffsets")) {
            fc.setStoreTermVectorOffsets(conf.get("storeTermVectorOffsets").asBoolean());
          }
          if (conf.has("storeTermVectorPositions")) {
            fc.setStoreTermVectorPositions(conf.get("storeTermVectorPositions").asBoolean());
          }
          if (conf.has("storeTermVectorPayloads")) {
            fc.setStoreTermVectorPayloads(conf.get("storeTermVectorPayloads").asBoolean());
          }
          if (conf.has("indexOptions")) {
            fc.setIndexOptions(FieldInfo.IndexOptions.valueOf(conf.get("indexOptions").asText().toUpperCase()));
          }
          if (conf.has("docValueType")) {
            fc.setDocValueType(FieldInfo.DocValuesType.valueOf(conf.get("docValueType").asText().toUpperCase()));
          }
        }
        doc.add(
          t match {
            case "string" ⇒ {
              new Field(name, f.get("content").asText(), fc);
            }
            case "int" ⇒ {
              new IntField(name, f.get("content").asInt(), fc);
            }
            case "long" ⇒ {
              new LongField(name, f.get("content").asLong(), fc);
            }
            case "float" ⇒ {
              new FloatField(name, f.get("content").asDouble().toFloat, fc);
            }
            case "double" ⇒ {
              new DoubleField(name, f.get("content").asDouble(), fc);
            }
            case "bytes" ⇒ {
              val buffs = f.get("content").asText().split(" ").map(bs ⇒ { bs.toByte });
              new Field(name, buffs, fc);
            }
          });
      })
      doc;
    })
   
    Documents(docs.toArray);
  }

}
case class Documents(val datas: Array[org.apache.lucene.document.Document], var error: Throwable = null, var errorCount: Int = 0) {

}

@serializable
case class TestString(val string: String);

case class LoadDocument(val searcher: IndexSearcher, docs: TopDocs, beginTime: Long);

case class SearchResult(val searcher: IndexSearcher, docs: org.apache.lucene.search.TopDocs);

@serializable
case class QueryString(val queryStr: String);

@serializable
case class Deletes(val queryStr: String);
//sort:fieldName1 desc(asc) type,fieldName2 desc(asc) type
@serializable
case class Querys(val queryStr: String, val sortStr: String = null, val begin: Int = (-1), val end: Int = (-1)) {
  def getQuery(queryParser: QueryParser): Query = {
    queryParser.parse(queryStr);
  }

  def getSort(): Sort = {
    if (sortStr != null) {
      val sfs = sortStr.split(",").map(ss ⇒ {

        val sis = ss split "";
        val name = sis(0);
        name match {
          case "~" ⇒ new SortField(null, SortField.Type.SCORE, false);
          case "`" ⇒ new SortField(null, SortField.Type.SCORE, true);
          case _ ⇒ {
            val reverse = if (sis.length <= 1) false else if (sis(1).equals("asc")) false else true;

            val sorttype = if (sis.length <= 2) SortField.Type.STRING else SortField.Type.valueOf(sis(2).toUpperCase());
            new SortField(name, sorttype, reverse);
          }

        }

      });
      new Sort(sfs: _*);
    } else
      null;
  }
}