#!/bin/sh
javac -cp kv-2.0.26/lib/kvclient.jar:examples kv-2.0.26/examples/hello/*.java
java -cp kv-2.0.26/lib/kvclient.jar:kv-2.0.26/examples hello.HelloBigDataWorld -host hadoop1 -port 5000 -store PsystemStore
