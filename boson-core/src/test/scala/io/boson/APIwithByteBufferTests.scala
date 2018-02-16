package io.boson

import java.nio.ByteBuffer
import java.util.concurrent.CompletableFuture

import bsonLib.{BsonArray, BsonObject}
import io.boson.bson.Boson
import io.boson.bson.bsonValue.{BsException, BsSeq, BsValue}
import io.netty.util.ResourceLeakDetector
import org.junit.Assert.{assertEquals,assertTrue}
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class APIwithByteBufferTests extends FunSuite{
  ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.ADVANCED)
  val br4: BsonArray = new BsonArray().add("Insecticida")
  val br1: BsonArray = new BsonArray().add("Tarantula").add("Aracnídius").add(br4)
  val obj1: BsonObject = new BsonObject().put("Jose", br1)
  val br2: BsonArray = new BsonArray().add("Spider")
  val obj2: BsonObject = new BsonObject().put("Jose", br2)
  val br3: BsonArray = new BsonArray().add("Fly")
  val obj3: BsonObject = new BsonObject().put("Jose", br3)
  val arr: BsonArray = new BsonArray().add(2.2f).add(obj1).add(obj2).add(obj3).add(br4)
  val bsonEvent: BsonObject = new BsonObject().put("StartUp", arr)

  val validatedByteArray: Array[Byte] = arr.encodeToBarray()
  val validatedByteArrayObj: Array[Byte] = bsonEvent.encodeToBarray()

  val validatedByteBuffer: ByteBuffer = ByteBuffer.allocate(validatedByteArray.length)
  validatedByteBuffer.put(validatedByteArray)

  val validatedByteBufferObj: ByteBuffer = ByteBuffer.allocate(validatedByteArrayObj.length)
  validatedByteBufferObj.put(validatedByteArrayObj)


  test("extract PosV1 w/ key") {
    val expression: String = "[2 to 3]"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson.go(validatedByteBuffer)

    val expected: Vector[Array[Byte]] = Vector(arr.getBsonObject(2).encodeToBarray(),arr.getBsonObject(3).encodeToBarray())
    val result = future.join().getValue.asInstanceOf[Vector[Array[Any]]]
    assert(expected.size === result.size)
    assertTrue(expected.zip(result).forall(b => b._1.sameElements(b._2)))
  } //TODO: This test should return 1 more objects

  test("extract PosV2 w/ key") {
    val expression: String = "[2 until 3]"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson.go(validatedByteBuffer)

    val expected: Vector[Array[Byte]] = Vector(arr.getBsonObject(2).encodeToBarray())
    val result = future.join().getValue.asInstanceOf[Vector[Array[Any]]]
    assert(expected.size === result.size)
    assertTrue(expected.zip(result).forall(b => b._1.sameElements(b._2)))
  } //TODO: This test should return 1 more objects

  test("extract PosV3 w/ key") {
    val expression: String = "[2 until end]"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson.go(validatedByteBuffer)

    val expected: Vector[Array[Byte]] = Vector(arr.getBsonObject(2).encodeToBarray(),arr.getBsonObject(3).encodeToBarray())
    val result = future.join().getValue.asInstanceOf[Vector[Array[Any]]]
    assert(expected.size === result.size)
    assertTrue(expected.zip(result).forall(b => b._1.sameElements(b._2)))
  } //TODO: This test should return 1 more objects

  test("extract PosV4 w/ key") {
    val expression: String = "[2 to end]"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson.go(validatedByteBuffer)

    val expected: Vector[Any] = Vector(arr.getBsonObject(2).encodeToBarray(),arr.getBsonObject(3).encodeToBarray(),br4.encodeToBarray())
    val result = future.join().getValue.asInstanceOf[Vector[Any]]
    assert(expected.size === result.size)
    assertTrue(expected.zip(result).forall{
      case (e: Array[Byte],r: Array[Byte]) => e.sameElements(r)
      case (e,r) => e.equals(r)
    })
  } //TODO: This test should return 1 more objects

  test("extract PosV5 w/ key") {
    val expression: String = "[3]"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson.go(validatedByteBuffer)

    val expected: Vector[Array[Byte]] = Vector(arr.getBsonObject(3).encodeToBarray())
    val result = future.join().getValue.asInstanceOf[Vector[Array[Any]]]
    assert(expected.size === result.size)
    assertTrue(expected.zip(result).forall(b => b._1.sameElements(b._2)))
  }

  test("extract with 2nd Key PosV1 w/ key") {
    val expression: String = "[2 to 3].Jose"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson.go(validatedByteBuffer)

    val expected: Vector[Array[Byte]] = Vector(br2.encodeToBarray(), br3.encodeToBarray())
    val result = future.join().getValue.asInstanceOf[Vector[Array[Any]]]
    assert(expected.size === result.size)
    assertTrue(expected.zip(result).forall(b => b._1.sameElements(b._2)))
  }

  test("extract with 2nd Key PosV2 w/ key") {
    val expression: String = "[2 until 3].Jose"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson.go(validatedByteBuffer)

    val expected: Vector[Array[Byte]] = Vector(br2.encodeToBarray())
    val result = future.join().getValue.asInstanceOf[Vector[Array[Any]]]
    assert(expected.size === result.size)
    assertTrue(expected.zip(result).forall(b => b._1.sameElements(b._2)))
  }

  test("extract with 2nd Key PosV3 w/ key") {
    val expression: String = "[2 until end].Jose"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson.go(validatedByteBuffer)

    val expected: Vector[Array[Byte]] = Vector(br2.encodeToBarray(), br3.encodeToBarray())
    val result = future.join().getValue.asInstanceOf[Vector[Array[Any]]]
    assert(expected.size === result.size)
    assertTrue(expected.zip(result).forall(b => b._1.sameElements(b._2)))
  }

  test("extract with 2nd Key PosV4 w/ key") {
    val expression: String = "[2 to end].Jose"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson.go(validatedByteBuffer)

    val expected: Vector[Array[Byte]] = Vector(br2.encodeToBarray(), br3.encodeToBarray())
    val result = future.join().getValue.asInstanceOf[Vector[Array[Any]]]
    assert(expected.size === result.size)
    assertTrue(expected.zip(result).forall(b => b._1.sameElements(b._2)))
  }

  test("extract with 2nd Key PosV5 w/ key") {
    val expression: String = "[3].Jose"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson.go(validatedByteBuffer)

    val expected: Vector[Array[Byte]] = Vector(br3.encodeToBarray())
    val result = future.join().getValue.asInstanceOf[Vector[Array[Any]]]
    assert(expected.size === result.size)
    assertTrue(expected.zip(result).forall(b => b._1.sameElements(b._2)))
  }

  test("extract PosV1") {
    val expression: String = "Jose[0 until end]"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson.go(validatedByteArrayObj)
    assertEquals(BsSeq(Vector(
      "Tarantula", "Aracnídius"
    )), future.join())
  }

  test("extract PosV2") {
    val expression: String = "Jose[0 to end]"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson.go(validatedByteBuffer)

    val expected: Vector[Any] = Vector( "Tarantula", "Aracnídius",br4.encodeToBarray(),"Spider","Fly")
    val result = future.join().getValue.asInstanceOf[Vector[Any]]
    assert(expected.size === result.size)
    assertTrue(expected.zip(result).forall{
      case (e: Array[Byte],r: Array[Byte]) => e.sameElements(r)
      case (e,r) => e.equals(r)
    })
  }

  test("extract PosV3") {
    val expression: String = "Jose[1 to 2]"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson.go(validatedByteBufferObj)
    val expected: Vector[Any] = Vector("Aracnídius",br4.encodeToBarray())
    val result = future.join().getValue.asInstanceOf[Vector[Any]]
    assert(expected.size === result.size)
    assertTrue(expected.zip(result).forall{
      case (e: Array[Byte],r: Array[Byte]) => e.sameElements(r)
      case (e,r) => e.equals(r)
    })
  }

  test("extract PosV4") {
    val expression: String = "StartUp[1 to 2]"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson.go(validatedByteBufferObj)

    val expected: Vector[Array[Byte]] = Vector(arr.getBsonObject(1).encodeToBarray(),arr.getBsonObject(2).encodeToBarray())
    val result = future.join().getValue.asInstanceOf[Vector[Array[Any]]]
    assert(expected.size === result.size)
    assertTrue(expected.zip(result).forall(b => b._1.sameElements(b._2)))
  }

  test("extract PosV5") {
    val expression: String = "StartUp[3]"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson.go(validatedByteBufferObj)

    val expected: Vector[Array[Byte]] = Vector(arr.getBsonObject(3).encodeToBarray())
    val result = future.join().getValue.asInstanceOf[Vector[Array[Any]]]
    assert(expected.size === result.size)
    assertTrue(expected.zip(result).forall(b => b._1.sameElements(b._2)))
  }

  test("extract with 2nd Key PosV1") {
    val expression: String = "StartUp[0 until end].Jose"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson.go(validatedByteArrayObj)

    val expected: Vector[Any] = Vector( br1.encodeToBarray(),br2.encodeToBarray(),br3.encodeToBarray())
    val result = future.join().getValue.asInstanceOf[Vector[Any]]
    assert(expected.size === result.size)
    assertTrue(expected.zip(result).forall{
      case (e: Array[Byte],r: Array[Byte]) => e.sameElements(r)
      case (e,r) => e.equals(r)
    })
  }

  test("extract with 2nd Key PosV2") {
    val expression: String = "StartUp[2 to end].Jose"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson.go(validatedByteArrayObj)
    val expected: Vector[Any] = Vector(br2.encodeToBarray(),br3.encodeToBarray())
    val result = future.join().getValue.asInstanceOf[Vector[Any]]
    assert(expected.size === result.size)
    assertTrue(expected.zip(result).forall{
      case (e: Array[Byte],r: Array[Byte]) => e.sameElements(r)
      case (e,r) => e.equals(r)
    })
  }

  test("extract with 2nd Key PosV3") {
    val expression: String = "StartUp[2 to 3].Jose"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson.go(validatedByteArrayObj)

    val expected: Vector[Any] = Vector(br2.encodeToBarray(),br3.encodeToBarray())
    val result = future.join().getValue.asInstanceOf[Vector[Any]]
    assert(expected.size === result.size)
    assertTrue(expected.zip(result).forall{
      case (e: Array[Byte],r: Array[Byte]) => e.sameElements(r)
      case (e,r) => e.equals(r)
    })
  }

  test("extract with 2nd Key PosV4") {
    val expression: String = "StartUp[2 until 3].Jose"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson.go(validatedByteArrayObj)

    val expected: Vector[Any] = Vector(br2.encodeToBarray())
    val result = future.join().getValue.asInstanceOf[Vector[Any]]
    assert(expected.size === result.size)
    assertTrue(expected.zip(result).forall{
      case (e: Array[Byte],r: Array[Byte]) => e.sameElements(r)
      case (e,r) => e.equals(r)
    })
  }

  test("extract with 2nd Key PosV5") {
    val expression: String = "StartUp[4].Jose"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson.go(validatedByteArrayObj)
    assertEquals(BsSeq(Vector()), future.join())
  }

  test("extract all") {
    val expression: String = "Jose"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson.go(validatedByteBufferObj)

    val expected: Vector[Any] = Vector( br1.encodeToBarray(),br2.encodeToBarray(),br3.encodeToBarray())
    val result = future.join().getValue.asInstanceOf[Vector[Any]]
    assert(expected.size === result.size)
    assertTrue(expected.zip(result).forall{
      case (e: Array[Byte],r: Array[Byte]) => e.sameElements(r)
      case (e,r) => e.equals(r)
    })
  }

//  test("extract all elements containing partial key") {
//    val expression: String = "*os"
//    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
//    val boson: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
//    boson.go(validatedByteBufferObj)
//
//    assertEquals(BsSeq(Vector(
//      "Tarantula", "Aracnídius", Seq("Insecticida"),
//      "Spider",
//      "Fly"
//    )), future.join())
//  }
  // TODO:Bug with halfKey matching a key(see trello)

  test("extract all elements of root") {
    val expression: String = ".*"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson.go(validatedByteBuffer)
    val res = future.join().getValue.asInstanceOf[Vector[Any]]
    val expected: Vector[Any] =
      Vector(2.2f,obj1.encodeToBarray(),obj2.encodeToBarray(),obj3.encodeToBarray(),br4.encodeToBarray())
    assert(expected.size === res.size)
    assertTrue(expected.zip(res).forall{
      case (e: Array[Byte],r: Array[Byte]) => e.sameElements(r)
      case (e,r: Double) => e == r
      case (e,r) => e.equals(r)
    })
  }

  test("extract objects with a certain element") {
    val br4: BsonArray = new BsonArray().add("Tarantula").add("Aracnídius")
    val obj4: BsonObject = new BsonObject().put("Joseeee", br4)
    val br5: BsonArray = new BsonArray().add("Spider")
    val obj5: BsonObject = new BsonObject().put("Jose", br5)
    val arr1: BsonArray = new BsonArray().add(2.2f).add(obj4).add(true).add(obj5)
    val bsonEvent1: BsonObject = new BsonObject().put("StartUp", arr1)
    val validatedByteArrayObj1: Array[Byte] = bsonEvent1.encodeToBarray()
    val validatedByteBufferObj1: ByteBuffer = ByteBuffer.allocate(validatedByteArrayObj1.length)
    validatedByteBufferObj1.put(validatedByteArrayObj1)
    validatedByteBufferObj1.flip()

    val expression: String = "StartUp[@Jose]"
    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
    val boson: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
    boson.go(validatedByteBufferObj1)

    val expected: Vector[Array[Byte]] = Vector(obj5.encodeToBarray())
    val result = future.join().getValue.asInstanceOf[Vector[Array[Any]]]
    assert(expected.size === result.size)
    assertTrue(expected.zip(result).forall(b => b._1.sameElements(b._2)))
  }

}
