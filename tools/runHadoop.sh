#!/bin/sh

if [ "$1" = "" ]
then
    echo "Please enter the level number"
else
    hadoop jar Psystem.jar Simulator.DerivationTreeGenerator PsystemStore hadoop1:5000 $1
fi
