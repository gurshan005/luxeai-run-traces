# LuxeAI run traces

A Java code sample from LuxeAI, an agent management platform. It turns one run and its spans into a graph: root spans attach to the run, nested spans attach to their parent, and a summary carries the signals raised anywhere in the trace.

The signal classifier and graph construction come from the platform. Here, small Java records replace the Spring service and PostgreSQL repository so the same operations can be inspected with synthetic data. It takes Java 17 or later and has no external dependencies.

### Run the example

```sh
mkdir -p out
javac --release 17 -d out src/showcase/traces/*.java
java -cp out showcase.traces.Example
```

A simulated document read times out and a child span records the retry. The example checks the two parent links, the combined signals and the empty-run case before printing the result.

```text
run:demo -> span:read
span:read -> span:retry
signal: toolFailed
signal: timeout
signal: retry
```

[RunGraphBuilder](src/showcase/traces/RunGraphBuilder.java) constructs the nodes, edges and summary in one pass. [SignalFlags](src/showcase/traces/SignalFlags.java) packs nine event indicators into an integer; bitwise OR combines flags without counting events twice. The classifier uses metadata hints and is intended for observability, not access decisions.
