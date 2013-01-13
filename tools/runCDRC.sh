#!/bin/sh

hadoop fs -rmr /user/a/output
hadoop jar Psystem.jar CDRCTesting.Hadoop PsystemStore machine1:5000 /user/a/output level1
