# Boson

Streaming Data Access for BSON and JSON encoded documents

[![Build Status](https://api.travis-ci.org/ZinkDigital/boson.svg)](https://travis-ci.org/ZinkDigital/boson)

# Table of Contents

- [Scala QuickStart Guide](#id-quickStartGuideScala)
	* [Boson](#id-BosonScala)
		* [Extractor](#id-bosonExtractionScala)
		* [Injector](#id-bosonInjectionScala)
		* [Fuse](#id-bosonFuseScala)
	* [Joson](#id-JosonScala)
		* [Extractor](#id-josonExtractionScala)
		* [Injector](#id-josonInjectionScala)
- [Java QuickStart Guide](#id-quickStartGuideJava)
	* [Boson](#id-BosonJava)
		* [Extractor](#id-extractionJava)
		* [Injector](#id-injectionJava)
		* [Fuse](#id-bosonFuseJava)
	* [Joson](#id-JosonJava)
		 * [Extractor](#id-josonExtractionJava)
		 *	[Injector](#id-josonInjectionJava)
- [Documentation](#documentation)
	* [BsonPath](#bsonpath)
		* [Operators](#operators)
		* [Comparison with JsonPath](#comparison-with-jsonpath)


<div id='id-quickStartGuideScala'/>

## QuickStart Guide

Boson is available through the Central Maven Repository.
For SBT users, please add the following dependency in your build.sbt:
```scala
libraryDependencies += Not Yet
```
For Maven users, please add the following dependency in your pom.xml:
```xml
<dependency>
    <groupId>not.yet</groupId>
    <artifactId>not-yet</artifactId>
    <version>NotYet</version>
</dependency>
```

<div id='id-BosonScala'/>

### Boson
A "Boson" is an object created when constructing an extractor/injector that encapsulates an encoded BSON in a Netty buffer and processes it according to a given expression, traversing the buffer only once.
<div id='id-bosonExtractionScala'/>

#### Extraction
Extraction requires a "BsonPath" expression (see [Operators](#operators) table for examples and syntax), an encoded BSON, an Higher-Order Function and a Synchrononization tool in case multiple extractions are to be performed. The Extractor instance is built only once and can be reused multiple times to extract from different encoded BSON.

```scala
//Encode Bson:
val validatedByteArray: Array[Byte] = bsonEvent.encode().array()

//BsonPath expression:
val expression: String = "..fridgeReadings.[1].fanVelocity"

//Synchronization tool:
val latch: CountDownLatch = new CountDownLatch(1)

//Simple Extractor:
val boson: Boson = Boson.extractor(expression, (in: BsValue) => {
  // Use 'in' value, this is the value extracted.
  latch.countDown()
})

//Trigger extraction with encoded Bson:
boson.go(validatedByteArray)

//Wait to complete extraction:
latch.await()
```
<div id='id-bosonInjectionScala'/>

#### Injection
Injection requires a "BsonPath" expression (see [Operators](#operators) table for examples and syntax), an encoded BSON and an Higher-Order Function. The returned result is a CompletableFuture[Array[Byte]]. The Injector instance is built only once and can be reused to inject different encoded BSON.
```scala
//Encode Bson:
val validBsonArray: Array[Byte] = bsonEvent.encode().array()

//BsonPath expression:
val expression: String = "..Store..name"

//Simple Injector:
val boson: Boson = Boson.injector(expression, (in: String) => "newName")

//Trigger injection with encoded Bson:
val result: Array[Byte] = boson.go(validBsonArray).join()
```
<div id='id-bosonFuseScala'>

### Fuse
Fusion requires  a [Boson Extractor](#id-bosonExtractionScala) and a [Boson Injector](#id-bosonInjectionScala) or two Boson of the same type. The order in which fuse is applied is left to the discretion of the user. This fusion is executed sequentially at the moment.
```scala
//First step is to construct both Boson.injector and Boson.extractor by providing the necessary arguments.
val validatedByteArray: Array[Byte] = bsonEvent.encode().array()

val expression = "name"

val ext: Boson = Boson.extractor(expression, (in: BsValue) => {
  // Use 'in' value, this is the value extracted.
})

val inj: Boson = Boson.injector(expression, (in: String) => "newName")

//Then call fuse() on injector or extractor, it returns a new BosonObject.
val fused: Boson = ext.fuse(inj)

//Finally call go() providing the byte array or a ByteBuffer on the new Boson object.
val finalFuture: Array[Byte] = fused.go(validatedByteArray).join()
```
<div id='id-JosonScala'/>

### Joson
A "Joson" is an object created when constructing an extractor/injector that encapsulates a JSON String, encoding it as a BSON in a Netty buffer, and then processes it according to a given expression, traversing the buffer only once.
<div id='id-josonExtractionScala'/>

#### Extraction
Extraction requires a "BsonPath" expression (see [Operators](#operators) table for examples and syntax), a JSON String, an Higher-Order Function and a Synchrononization tool in case multiple extractions are to be performed. The Extractor instance is built only once and can be reused multiple times to extract from different JSON Strings.
```scala
//BsonPath expression:
val expression: String = "..Book[1]"

//Json String:
val json = “““{
                "Store":{
		  "Book":[
		    {
		      "Price":10
		    },
		    {
		      "Price":20
		    }
		  ],
		  "Hat":[
		    {
		      "Price":30
		    },
		    {
		      "Price":40
		    }
		  ]
		}
              }”””

//Synchronization tool:
val latch: CountDownLatch = new CountDownLatch(1)

//Simple Extractor:
val joson: Joson = Joson.extractor(expression, (in: BsValue) => {
  // Use 'in' value, this is the value extracted.
  latch.countDown()
})

//Trigger extraction with Json:
val result: String = joson.go(json).join()

//Wait to complete extraction:
latch.await()
```
<div id='id-josonInjectionScala'/>

#### Injection
Injection requires a "BsonPath" expression (see [Operators](#operators) table for examples and syntax), a JSON String and an Higher-Order Function. The returned result is a CompletableFuture[Array[Byte]]. The Injector instance is built only once and can be reused to inject different JSON Strings.
```scala
//BsonPath expression:
val expression: String = "..Book[1]"

//Json String:
val json = “““{
                "Store":{
		  "Book":[
		    {
		      "Price":10
		    },
		    {
		      "Price":20
		    }
		  ],
		  "Hat":[
		    {
		      "Price":30
		    },
		    {
		      "Price":40
		    }
		  ]
		}
              }”””

//Simple Injector:
val joson: Joson = Joson.injector(expression,  (in: Map[String, Object]) => {
  in.+(("Title", "Scala"))
})

//Trigger injection with Json:
val result: String = joson.go(json).join()
```

<div id='id-quickStartGuideJava'/>

## QuickStart Guide

For Maven users, please add the following dependency in your pom.xml:
```xml
<dependency>
    <groupId>not.yet</groupId>
    <artifactId>not-yet</artifactId>
    <version>NotYet</version>
</dependency>
```
<div id='id-BosonJava'/>

### Boson
A "Boson" is an object created when constructing an extractor/injector that encapsulates an encoded BSON in a Netty buffer and processes it according to a given expression, traversing the buffer only once.
<div id='id-bosonExtractionJava'/>

#### Extraction
Extraction requires a "BsonPath" expression (see [Operators](#operators) table for examples and syntax), an encoded BSON, an Higher-Order Function and a Synchrononization tool in case multiple extractions are to be performed. The Extractor instance is built only once and can be reused multiple times to extract from different encoded BSON.

```java
//Encode Bson:
byte[] validatedByteArray = bsonEvent.encode().array();

//BsonPath expression:
String expression = "..Store..SpecialEditions[@Extra]";

//Synchronization tool:
CountDownLatch latch = new CountDownLatch(1);

//Simple Extractor:
Boson boson = Boson.extractor(expression, obj-> {
	// Use 'obj' value, this is the value extracted.
	latch.countDown();
});

//Trigger extraction with encoded Bson:
boson.go(validatedByteArray);

//Wait to complete extraction:
latch.await();
```
<div id='id-injectionJava'/>

#### Injection
Injection requires a "BsonPath" expression (see [Operators](#operators) table for examples and syntax), an encoded BSON and an Higher-Order Function. The returned result is a CompletableFuture<byte[]>. The Injector instance is built only once and can be reused to inject different encoded BSON.
```java
//Encode Bson:
byte[] validatedByteArray = bsonEvent.encode().array();

//BsonPath expression:
String expression = "..Store.[2 until 4]";

//Simple Injector:
Boson boson = Boson.injector(expression,  (Map<String, Object> in) -> {
	in.put("WHAT", 10);
	return in;
});

//Trigger injection with encoded Bson:
byte[] result = boson.go(validatedByteArray).join();
```
### Fuse
Fusion requires  a [Boson Extractor](#id-bosonExtractionScala) and a [Boson Injector](#id-bosonInjectionScala) or two Boson of the same type. The order in which fuse is applied is left to the discretion of the user. This fusion is executed sequentially at the moment.
```java
//First step is to construct both Boson.injector and Boson.extractor by providing the necessary arguments.
final byte[] validatedByteArray  = bsonEvent.encode().array();

final String expression = "name";

final Boson ext = Boson.extractor(expression, (in: BsValue) -> {
  // Use 'in' value, this is the value extracted.
});

final Boson inj = Boson.injector(expression, (in: String) -> "newName");

//Then call fuse() on injector or extractor, it returns a new BosonObject.
final Boson fused = ext.fuse(inj);

//Finally call go() providing the byte array or a ByteBuffer on the new Boson object.
final byte[] finalFuture = fused.go(validatedByteArray).join();
```

<div id='id-JosonJava'/>

### Joson
A "Joson" is an object created when constructing an extractor/injector that encapsulates a JSON String, encoding it as a BSON in a Netty buffer, and then processes it according to a given expression, traversing the buffer only once.
<div id='id-josonExtractionJava'/>

#### Extraction
Extraction requires a "BsonPath" expression (see [Operators](#operators) table for examples and syntax), a JSON String, an Higher-Order Function and a Synchrononization tool in case multiple extractions are to be performed. The Extractor instance is built only once and can be reused multiple times to extract from different JSON Strings.
```java
//BsonPath expression:
String expression = "..Book[1]";

//Json String:
String jsonStr = "{\"Store\":{\"Book\":[{\"Price\":10},{\"Price\":20}],\"Hat\":[{\"Price\":30},{\"Price\":40}]}}"

//Synchronization tool:
CountDownLatch latch = new CountDownLatch(1);

//Simple Extractor:
Joson joson = Joson.extractor(expression, obj-> {
	// Use 'obj' value, this is the value extracted.
	latch.countDown();
});

//Trigger extraction with Json:
joson.go(jsonStr);

//Wait to complete extraction:
latch.await()
```
<div id='id-josonInjectionJava'/>

#### Injection
Injection requires a "BsonPath" expression (see [Operators](#operators) table for examples and syntax), a JSON String and a Function. The returned result is a CompletableFuture<byte[]>. The Injector instance is built only once and can be reused to inject different JSON Strings.
```java
//BsonPath expression:
String expression = "..Book[1]";

//Json String:
String jsonStr = "{\"Store\":{\"Book\":[{\"Price\":10},{\"Price\":20}],\"Hat\":[{\"Price\":30},{\"Price\":40}]}}"

//Simple Injector:
Joson joson = Joson.injector(expression,  (Map<String, Object> in) -> {
	in.put("WHAT", 10);
	return in;
});

//Trigger injection with Json:
String result = joson.go(jsonStr).join();
```

# Documentation
## BsonPath

BsonPath expressions targets a BSON structure with the same logic as JsonPath expressions target JSON structure and XPath targeted a XML document. Unlike JsonPath there is no reference of a "root member object", instead if you want to specify a path starting from the root, the expression must begin with a dot (`.key`).

BsonPath expressions use the dot-notation: `key1.key2[0].key3`.

Expressions whose path doesn't necessarily start from the root can be expressed in two ways:
* No dot - ` key`
* Two dots - `..key`

### Operators

Operator | Description
---------|----------
`.` | Child.
`..` | Deep scan. Available anywhere a name is required.
`@` | Current node.
`[<number> ((to,until) <number>)]` | Array index or indexes.
`[@<key>]` | Filter expression.
`*` | Wildcard. Available anywhere a name is required.

### Comparison with JsonPath
Given the json
```json
{
	"Store":{
		"Book":[
	        {
	            "Title":"Java",
	            "Price":15.5,
	            "SpecialEditions":[
	                {
	                    "Title":"JavaMachine",
	                    "Price":39
	                }
	             ]
	        },
	        {
	            "Title":"Scala",
	            "Pri":21.5,
	            "SpecialEditions":[
	                {
	                    "Title":"ScalaMachine",
	                    "Price":40
	                }
	             ]
	        },
	        {
	            "Title":"C++",
	            "Price":12.6,
	            "SpecialEditions":[
	                {
	                    "Title":"C++Machine",
	                    "Price":38
	                }
	             ]
	        }
	        ],
	    "Hat":[
            {
                "Price":48,
                "Color":"Red"
            },
            {
                "Price":35,
                "Color":"White"
            },
            {
                "Price":38,
                "Color":"Blue"
            }
        ]
    }
}
```
BsonPath | JsonPath
---------|---------
`.Store` | `$.Store`
`.Store.Book[@Price]` | `$.Store.Book[?(@.Price)]`
`Book[@Price]..Title` | `$..Book[?(@.Price)]..Title`
`Book[1]` | `$..Book[1]`
`Book[0 to end]..Price` | `$..Book[:]..Price`
`Book[0 to end].*..Title` | `$..Book[:].*..Title`
`.*` | `$.*`
`Book.*.[0 to end]` | `$..Book.*.[:]`
`.Store..Book[1 until end]..SpecialEditions[@Price]` | `$.Store..Book[1:1]..SpecialEditions[?(@.Price)]`
`Bo*k`, `*ok` or `Bo*`  | `Non existent.`
`*ok[@Pri*]..SpecialEd*.Price` | `Non existent.`

**Note: JsonPath doesn't support the *halfkey* (`B*ok`) as well as the range *until end* (`1 until end`).**
