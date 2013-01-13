#!/bin/sh

java -jar kv-2.0.23/lib/kvstore-2.0.23.jar runadmin -port 5000 -host `hostname` << EOF

ddl add-schema -file node.avsc
ddl add-schema -file schema.avsc

EOF
