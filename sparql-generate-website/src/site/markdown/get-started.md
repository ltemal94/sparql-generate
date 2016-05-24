# Get Started

`sparql-generate-jena` is an implementation of SPARQL-Generate over Apache Jena. Its binaries, sources and documentation are available for download at [Maven Central](http://search.maven.org/#search%7Cga%7C1%7Csparql-generate-jena%22). To use it in your Maven project, add the following dependency declaration to your Maven project file ( `*.pom` file):
 
```xml
<dependency>
    <groupId>com.github.thesmartenergy</groupId>
    <artifactId>sparql-generate-jena</artifactId>
    <version>1.0</version>
</dependency>
```

Latest version implements iterator and custom SPARQL functions to generate RDF from JSON, XML, HTML, CSV, CBOR, plain text. See the implemented [SPARQL binding functions and SPARQL-Generate iterator functions](functions.html). 

The [javadoc](http://w3id.org/sparql-generate/apidocs/index.html) contains comprehensive documentations and examples, and the [sources](http://search.maven.org/#search%7Cga%7C1%7Csparql-generate-jena) contains a set of unit tests to get more examples. 

### Parsing a Query

```java
 String queryString = "PREFIX ... GENERATE {...} FROM ... SOURCE ... ITERATOR ... WHERE {...}";
 Syntax syntax = SPARQLGenerate.SYNTAX;
 SPARQLGenerateQuery query = (SPARQLGenerateQuery) QueryFactory.create(queryString, syntax);
```

### Classes `PlanFactory` and `RootPlan`

First use an instance of `PlanFactory` to instantiate a `RootPlan` for a SPARQL-Generate query.

```java
SPARQLGenerateQuery query;
PlanFactory factory = new PlanFactory();
RootPlan plan = factory.create(query);
```

Then the `RootPlan` can be executed several times on different SPARQL datasets, and with different initial bindings (i.e., on multiple _messages_). Call one of the `exec` methods to trigger the RDF generation. Here is the signature of three of these methods:

```java
Model exec();
void exec(Model inputModel, QuerySolution initialBindings, Model initialModel);
void exec(Dataset inputDataset, QuerySolution initialBindings, Model initialModel);
```

### Retrieving the generated RDF

RDF generated by the execution plan is added to a `Model`, which is the Jena class for a RDF Graph.

- some `exec` methods create a new model from scratch at execution time, fill it with the generated triples, and return it to the caller;
- other method use parameter `initialModel`, and add triples. 

To instantiate a `Model`, which is the Jena model for a RDF Graph, one may use:

```java
ModelFactory.createDefaultModel();
```


### Evaluating a query over a SPARQL Dataset

Part of the SPARQL GENERATE query execution consists in evaluating a SPARQL 1.1 `SELECT *` query over a RDF Graph, or a SPARQL Dataset. Exactly like in SPARQL 1.1. The corresponding parameters are `inputModel` or `inputDataset`. To instantiate a `Dataset`, which is the Jena class for a SPARQL Dataset, one may use:

```java
DatasetFactory.create(Model model);
```

Note:

- If `initialModel == null`, then an `IllegalArgumentException` exception will be thrown;
- The behaviour when the same instance is passed as `inputModel` and `initialModel` is not specified. You should avoid this situation.


### Generating RDF for streams of messages

Finally, one may execute the query with initial bindings. This is useful when one wants to apply the same plan to generate RDF from multiple messages regularly received from a lightweight sensor for instance. 

Suppose one needs to execute a plan with a variable `?msg` bound to a message with textual representation `"mymessage"`and internet media-type _application/json_. Then the RDF literal to bind to `?msg`:

- has lexical form `"mymessage"`,
- has datatype IRI `<urn:iana:mime:application/json>` (this is optional).

In SPARQL Generate over Apache Jena, one calls an `exec` method with parameter `initialBindings`. The following code shows how this can be done with the plan instantiated above:

```java
String variable = "msg";
String message = "mymessage";
String uri = "urn:iana:mime:application/json";

QuerySolutionMap initialBinding = new QuerySolutionMap();
Model initialModel = ModelFactory.createDefaultModel();

TypeMapper typeMapper = TypeMapper.getInstance();
RDFDatatype dt = typeMapper.getSafeTypeByName(uri);
Node arqLiteral = NodeFactory.createLiteral(message, dt);
RDFNode jenaLiteral = initialModel.asRDFNode(arqLiteral);
initialBinding.add(variable, jenaLiteral);

plan.exec(initialBinding, initialModel);
```


### Using a File Manager

The default behaviour of the implementation is to attempt to operate a HTTP GET request to the URI of a source, and to use the retrieved document as the literal for this name. One may choose to use local files instead.

The Jena FileManager is designed just for that. It uses a configuration file to load files from disk instead of attempting HTTP GET calls. See
[The Jena FileManager and LocationMapper](https://jena.apache.org/documentation/notes/file-manager.html). The following code illustrates how a `FileManager` can be passed to the constructor of a `PlanFactory`, in order to be used during the execution of the plans this `PlanFactory` will create.

```java
Model conf = RDFDataMgr.loadModel("file:/path/to/configuration.ttl");
LocationMapper mapper = new LocationMapper(conf);
FileManager fm = new FileManager();

fm.setLocationMapper(mapper);

Locator loc;

loc = new LocatorFile("file:/path/to/directory1");
fm.addLocator(loc);

loc = new LocatorFile("file:/path/to/directory2");
fm.addLocator(loc);

PlanFactory factory = new PlanFactory(fm);
...

```
