# servertest
A load test tool for the ShortReadServer

The xompiled executable jar file is in the '/store' directory.

You can build it from source using `ant package-for-store` and this will overwrite the jat file in /store.

To run it use `java -jar servertest.jar` {num sequences} {num threads} {path to index Fasta file} [{server url} - default is localhost:9221}]

The test will randomly select {num sequences} sequences from the reference fasta file, and use {num threads} to make REST calls to {server url}.
