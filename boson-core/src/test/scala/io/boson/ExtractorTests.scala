package io.boson

import java.time.Instant

import bsonLib.{BsonArray, BsonObject}
import io.boson.bson.bsonImpl.BosonImpl
import io.netty.buffer.{ByteBuf, Unpooled}
import io.netty.util.ResourceLeakDetector
import org.junit.Assert.{assertEquals,assertTrue}
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

/**
  * Created by Tiago Filipe on 04/10/2017.
  */
@RunWith(classOf[JUnitRunner])
class ExtractorTests extends FunSuite {
  ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.ADVANCED)
  val obj1: BsonObject = new BsonObject().put("Andre", 975).put("António", 975L)
  val obj2: BsonObject = new BsonObject().put("Pedro", 1250L).put("José", false)
  val obj3: BsonObject = new BsonObject().put("Americo", 1500).putNull("Amadeu")

  val arr: BsonArray = new BsonArray().add(obj1).add(obj2).add(obj3)

  val now: Instant = Instant.now

  val globalObj: BsonObject = new BsonObject().put("Salary", 1000).put("AnualSalary", 12000L).put("Unemployed", false)
    .put("Residence", "Lisboa").putNull("Experience").put("BestFriend", obj1).put("Colleagues", arr)
    .put("UpdatedOn", now).put("FavoriteSentence", "Be the best".getBytes)

  test("Extract Int") {
    val bsonEvent: BsonObject = new BsonObject().put("StartUp", arr)
    val bosonBson: BosonImpl = new BosonImpl(byteArray = Option(bsonEvent.encode().getBytes))
    assert(1500 === bosonBson.extract(bosonBson.getByteBuf, List(("Americo","first")), List((None,None,""))).get.asInstanceOf[Vector[Any]].head)
  }

  test("Extract Long") {
    val bsonEvent: BsonObject = new BsonObject().put("StartUp", arr)
    val bosonBson: BosonImpl = new BosonImpl(byteArray = Option(bsonEvent.encode().getBytes))
    assert(1250L === bosonBson.extract(bosonBson.getByteBuf, List(("Pedro", "first")), List((None,None,""))).get.asInstanceOf[Vector[Any]].head)
  }

  test("Extract Boolean") {
    val bsonEvent: BsonObject = new BsonObject().put("StartUp", arr)
    val bosonBson: BosonImpl = new BosonImpl(byteArray = Option(bsonEvent.encode().getBytes()))
    assert(false === bosonBson.extract(bosonBson.getByteBuf, List(("José","first")), List((None,None,""))).get.asInstanceOf[Vector[Any]].head)
  }

  test("Extract Null") {
    val bsonEvent: BsonObject = new BsonObject().put("StartUp", arr)
    val bosonBson: BosonImpl = new BosonImpl(byteArray = Option(bsonEvent.encode().getBytes()))
    assert("Null" === bosonBson.extract(bosonBson.getByteBuf, List(("Amadeu","first")), List((None,None,""))).get.asInstanceOf[Vector[Any]].head)
  }

  test("Extract Instant") {
    val bosonBson: BosonImpl = new BosonImpl(byteArray = Option(globalObj.encode().getBytes()))
    assertEquals(now.toString, bosonBson.extract(bosonBson.getByteBuf, List(("UpdatedOn","first")), List((None,None,""))).get.asInstanceOf[Vector[Any]].head)
  }

  test("Extract String") {
    val bosonBson: BosonImpl = new BosonImpl(byteArray = Option(globalObj.encode().getBytes))
    assertEquals("Lisboa", bosonBson.extract(bosonBson.getByteBuf, List(("Residence","first")), List((None,None,"")))
      .get.asInstanceOf[Vector[Any]].head)
  }

  test("Extract Array[Byte] w/ Netty") {
    val help: ByteBuf = Unpooled.buffer()
    val finalBuf: ByteBuf = Unpooled.buffer()
    val bosonBson: BosonImpl = new BosonImpl(byteArray = Option(globalObj.encode().getBytes()))
    help.writeBytes(bosonBson.extract(bosonBson.getByteBuf, List(("FavoriteSentence","first")), List((None,None,""))).get.asInstanceOf[Vector[Any]].head.asInstanceOf[String].getBytes)
    finalBuf.writeBytes(io.netty.handler.codec.base64.Base64.decode(help))
    assert("Be the best".getBytes === new String(finalBuf.array()).replaceAll("\\p{C}", "").getBytes) //TODO:eliminate replaceAll from here
  }

  test("Extract Array[Byte] w/ String") {
    val bosonBson: BosonImpl = new BosonImpl(byteArray = Option(globalObj.encode().getBytes))
    assert("Be the best".getBytes === java.util.Base64.getMimeDecoder
      .decode(bosonBson.extract(bosonBson.getByteBuf, List(("FavoriteSentence","first")), List((None,None,"")))
        .get.asInstanceOf[Vector[Any]].head.asInstanceOf[String]))
  }

  test("Extract BsonObject") {
    val bsonEvent: BsonObject = new BsonObject().put("First", obj1).put("Second", obj2).put("Third", obj3)
    val bosonBson: BosonImpl = new BosonImpl(byteArray = Option(bsonEvent.encode().getBytes()))
    val result = bosonBson.extract(bosonBson.getByteBuf, List(("Second","first")), List((None,None,""))).get.asInstanceOf[Vector[Array[Any]]]
    val expected: Vector[Array[Byte]] = Vector(obj2.encodeToBarray())
    assert(expected.size === result.size)
    assertTrue(expected.zip(result).forall(b => b._1.sameElements(b._2)))
  }

  test("Extract BsonArray") {
    val bosonBson: BosonImpl = new BosonImpl(byteArray = Option(globalObj.encode().getBytes()))
    val result = bosonBson.extract(bosonBson.getByteBuf, List(("Colleagues","all")), List((None,None,""))).get.asInstanceOf[Vector[Array[Any]]]

    val expected: Vector[Array[Byte]] = Vector(arr.encodeToBarray())
    assert(expected.size === result.size)
    assertTrue(expected.zip(result).forall(b => b._1.sameElements(b._2)))
  }

  test("Extract deep layer") {
    val bsonEvent: BsonObject = new BsonObject().put("StartUp", arr)
    val arr2: BsonArray = new BsonArray().add("Day3").add("Day20").add("Day31")
    obj2.put("JoséMonthLeave", arr2)
    val bosonBson: BosonImpl = new BosonImpl(byteArray = Option(bsonEvent.encode().getBytes()))
    val result = bosonBson.extract(bosonBson.getByteBuf, List(("JoséMonthLeave","all")), List((None,None,""))).get.asInstanceOf[Vector[Array[Any]]]
    val expected: Vector[Array[Byte]] = Vector(arr2.encodeToBarray())
    assert(expected.size === result.size)
    assertTrue(expected.zip(result).forall(b => b._1.sameElements(b._2)))
  }

  test("Extract nonexistent key") {
    val bosonBson: BosonImpl = new BosonImpl(byteArray = Option(globalObj.encode().getBytes()))
    assert(None === bosonBson.extract(bosonBson.getByteBuf, List(("Random","first")), List((None,None,""))))
  }
}
