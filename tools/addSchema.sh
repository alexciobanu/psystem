#!/bin/sh

java -jar kv-2.0.26/lib/kvstore.jar runadmin -port 5000 -host `hostname` << EOF

ddl add-schema -file node.avsc
ddl add-schema -file schema.avsc

EOF
