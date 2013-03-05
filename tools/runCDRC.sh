#!/bin/sh

hadoop fs -rmr /user/cristi/output
hadoop jar Psystem.jar CDRCTesting.Hadoop PsystemStore machine1:5000 /user/cristi/output level1
