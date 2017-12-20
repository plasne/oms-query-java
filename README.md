# oms-query-java
An implementation of Log Analytics post/query using Java.

# Build
```bash
mvn clean dependency:copy-dependencies package
```

# Run

To post:

```bash
java -cp target/oms-query-1.0-SNAPSHOT.jar:target/dependency/* com.plasne.App post
```

To query:

```bash
java -cp target/oms-query-1.0-SNAPSHOT.jar:target/dependency/* com.plasne.App query
```

