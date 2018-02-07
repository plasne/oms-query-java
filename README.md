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

# RFC-1123

There was an interesting problem and I just wanted to document it somewhere. While the timestamp format for the POST method is documented as RFC-1123, it turns out the Log Analytics service doesn't actually adhere to that specification, which allows for the "day" (among other things) to be 1 or 2 digits. The service will throw an error if it isn't 2 digits. Rather than formatting to RFC-1123, I had to format to:

```bash
EEE, dd MMM yyyy HH:mm:ss O
```
