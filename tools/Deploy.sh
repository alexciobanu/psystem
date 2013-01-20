#!/bin/bash
#NO SPACES ALLOWED THIS THESE VARIABLES PLEASE SO targetDir can NOT be '/home/mke a dude/bla' THANK YOU
declare -a manchines=('machine1' 'machine2' 'machine3')
user='a'
targetDir='/home/a'
fails=0

function checkMachines 
{
	
	echo "Checking if I can ping all of the machines"
	for aMachine in ${manchines[*]}
	do
		ping -c 1 $aMachine &>/dev/null
		if [ $? -ne 0 ]; 
		then 	
			echo -e "\e[1;31m" $aMachine "is can not be pinged" "\e[0m"
			fails=$((fails+1))
		fi
	done
	echo "Done checking ping"
	echo "Checking if all of the machines have passwordless SSH"
	for aMachine in ${manchines[*]}
	do
		declare -a args=($user@$aMachine 'ls >/dev/null')
		ssh "${args[@]}"
		if [ $? -ne 0 ]; 
		then 	
			echo -e "\e[1;31m" $aMachine "can not be SSHed" "\e[0m"
			fails=$((fails+1))
		fi
	done
	echo "Done checing SSH"
	echo "Checking if all of the machines have java installed"
	for aMachine in ${manchines[*]}
	do
		declare -a args=($user@$aMachine 'java -version >&/dev/null')
		ssh "${args[@]}"
		if [ $? -ne 0 ]; 
		then 	
			echo -e "\e[1;31m" $aMachine "does not have java" "\e[0m"
			fails=$((fails+1))
		fi
	done
	echo "Done checing for JAVA"
}

function checkForFile
{
	echo "Checking if I have the tar for the NoSQL database"
	if [ -f kv-ce-2.0.23.tar.gz ]; 
	then
		echo "Found File"
	else
		echo "File not found downloading it"
		wget http://download.oracle.com/otn-pub/otn_software/nosql-database/kv-ce-2.0.23.tar.gz
		if [ -f kv-ce-2.0.23.tar.gz ];
		then
			echo "File successfully downloaded"
		else
			echo "File was not downloaded"
			fails=$((fails+1))	
		fi
	fi	
}

function checkForFail
{
	if [ $fails -ne 0 ]; 
	then
		echo -e "\e[1;31m" "The stage of $1 failed. Please correct and retry"  "\e[0m"
		exit 1
	fi
}

function deleteDeployment
{
	for aMachine in ${manchines[*]}
	do
		declare -a args=($user@$aMachine 'rm -rf KVROOT')
		ssh "${args[@]}"
		if [ $? -ne 0 ]; 
		then 	
			echo -e "\e[1;31m" $aMachine "'s KVROOT could not be deleted" "\e[0m"
			fails=$((fails+1))
		fi
	done	
}

function copyAndUntar
{
	for aMachine in ${manchines[*]}
	do
		echo "Copying file to machine"
		declare -a args=("kv-ce-2.0.23.tar.gz" $user@$aMachine":"$targetDir)
		scp "${args[@]}"
		if [ $? -ne 0 ]; 
		then 	
			echo -e "\e[1;31m" "kv-ce-2.0.23.tar.gz could not be copied to" $aMachine " " "\e[0m"
			fails=$((fails+1))
		fi
		echo "Files successfully copied"
		echo "Untaring files on the machines"
		declare -a args=($user@$aMachine "tar -zxvf $targetDir/kv-ce-2.0.23.tar.gz >/dev/null" )
		ssh "${args[@]}"
		if [ $? -ne 0 ]; 
		then 	
			echo -e "\e[1;31m" "kv-ce-2.0.23.tar.gz could not be copied UnTarged on machine" $aMachine " " "\e[0m"
			fails=$((fails+1))
		fi
		echo "Done untaring files on the machines"
	done
}

function createRootDirAndConfig 
{
	echo "Creating KVROOT dir"
	for aMachine in ${manchines[*]}	
	do
		declare -a args=($user@$aMachine 'mkdir KVROOT')
		ssh "${args[@]}"
		if [ $? -ne 0 ]; 
		then 	
			echo -e "\e[1;31m" "KVROOT could not be created on nachine: " $aMachine "\e[0m"
			fails=$((fails+1))
		fi
	done
	echo "Done creating KVROOT dir"

	echo "Creating initial configuration"

	#this will be the for the admin node which is the fisrt machine in the list
	declare -a args=($user@${manchines[0]} "java -jar kv-2.0.23/lib/kvstore-2.0.23.jar makebootconfig -root KVROOT -port 5000 -admin 5001 -host ${manchines[0]} -harange 5010,5020 -capacity 1 -num_cpus 0 -memory_mb 0")
	ssh "${args[@]}"
	if [ $? -ne 0 ]; 
		then 	
			echo -e "\e[1;31m" "KVROOT config could not be created on nachine: " ${manchines[0]} "\e[0m"
			fails=$((fails+1))
		fi
	
	#for the rest of the machines
	for aMachine in ${manchines[*]:1}
	do
		declare -a args=($user@$aMachine "java -jar kv-2.0.23/lib/kvstore-2.0.23.jar makebootconfig -root KVROOT -port 5000 -host $aMachine -harange 5010,5020 -capacity 1 -num_cpus 0 -memory_mb 0")
		ssh "${args[@]}"
		if [ $? -ne 0 ]; 
		then 	
			echo -e "\e[1;31m" "KVROOT config could not be created on nachine: " $aMachine "\e[0m"
			fails=$((fails+1))
		fi
	done
	echo "Done creating initial configuration"	
}

function startNoSQLInstances 
{
	echo "Stating NoSQL instance on each machine"
	for aMachine in ${manchines[*]}	
	do
		declare -a args=($user@$aMachine 'nohup java -jar kv-2.0.23/lib/kvstore-2.0.23.jar start -root KVROOT > foo.out 2> foo.err < /dev/null &')
		ssh "${args[@]}"
		if [ $? -ne 0 ]; 
		then 	
			echo -e "\e[1;31m" "No SQL instance could not be created on nachine: " $aMachine "\e[0m"
			fails=$((fails+1))
		fi
	done
	echo "Done stating NoSQL instance on each machine"
}

function createAndRunConfigurationFile 
{
	filename="KVConfigFile.txt"
	echo "Stating to create cluster config file"

	echo "" > $filename
	echo "configure -name PsystemStore" >> $filename
	echo "plan deploy-datacenter -name \"VirtualDatacentre\" -rf 3 -wait" >> $filename
	echo "plan deploy-sn -dc dc1 -host ${manchines[0]} -port 5000 -wait" >> $filename
	echo "plan deploy-admin -sn sn1 -port 5001 -wait" >> $filename
	echo "pool create -name PsystemPool" >> $filename
	echo "pool join -name PsystemPool -sn sn1" >> $filename
	i=1;
	for aMachine in ${manchines[*]:1}	
	do
		i=$((i+1))
		echo "plan deploy-sn -dc dc1 -host $aMachine -port 5000 -wait" >> $filename
		echo "pool join -name PsystemPool -sn sn$i" >> $filename
	done
	echo "topology create -name topo -pool PsystemPool -partitions 9" >> $filename
	echo "plan deploy-topology -name topo -wait" >> $filename

	echo "Copying Config File to first host"
	declare -a args=($filename $user@$aMachine":"$targetDir)
	scp "${args[@]}"

	echo "Executing Config File in admin console"
	declare -a args=($user@$aMachine "java -jar kv-2.0.23/lib/kvstore-2.0.23.jar runadmin -port 5000 -host ${manchines[0]} load -file "$targetDir/$filename)
	ssh "${args[@]}"
	if [ $? -ne 0 ]; 
	then 	
		echo -e "\e[1;31m" "No SQL instance could not be configured: " $aMachine "\e[0m"
			fails=$((fails+1))
		fi
}

function addSchemas
{
	echo "Copying needed files to the first machine and running addSchema"
	declare -a args=("node.avsc" "schema.avsc" "addSchema.sh" $user@${manchines[0]}":"$targetDir)
	scp "${args[@]}"
	declare -a args=($user@${manchines[0]} "chmod +x $targetDir/addSchema.sh")
	ssh "${args[@]}"
	declare -a args=($user@${manchines[0]} "cd $targetDir; ./addSchema.sh")
	ssh "${args[@]}"
	if [ $? -ne 0 ]; 
	then 	
		echo -e "\e[1;31m" "Could not add the needed schemas to the database" "\e[0m"
			fails=$((fails+1))
	fi
	echo "Done adding schemas"
}

function stoptNoSQLInstances 
{
	echo "Stopping NoSQL instance on each machine"
	for aMachine in ${manchines[*]}	
	do
		declare -a args=($user@$aMachine 'java -jar kv-2.0.23/lib/kvstore-2.0.23.jar stop -root KVROOT')
		ssh "${args[@]}"
		if [ $? -ne 0 ]; 
		then 	
			echo -e "\e[1;31m" "No SQL instance could not be stopped on nachine: " $aMachine "\e[0m"
			fails=$((fails+1))
		fi
	done
	echo "Done stopping NoSQL instance on each machine"
}

function powerCycleHosts 
{
	echo "Rebooting all of the hosts"
	for aMachine in ${manchines[*]}	
	do
		declare -a args=($user@$aMachine 'sudo shutdown -r now')
		ssh "${args[@]}"
		if [ $? -ne 0 ]; 
		then 	
			echo -e "\e[1;31m" "Host :" $aMachine "did not reboot" "\e[0m"
			fails=$((fails+1))
		fi
	done
	echo "Done sending the reboot command to all of the hosts"
}

function shutdownHosts 
{
	echo "Shutting down all of the hosts"
	for aMachine in ${manchines[*]}	
	do
		declare -a args=($user@$aMachine 'sudo shutdown -h now')
		ssh "${args[@]}"
		if [ $? -ne 0 ]; 
		then 	
			echo -e "\e[1;31m" "Host :" $aMachine "did not reboot" "\e[0m"
			fails=$((fails+1))
		fi
	done
	echo "Done sending the shutdown command to all of the hosts"
}

function doDeployment
{
	checkMachines
	checkForFail "Check the machines for conectivity"
	checkForFile
	checkForFail "Getting the NoSQL database file or having it in the curret directory"
	copyAndUntar
	checkForFail "Copying the NoSQL database tar and untaring it"
	createRootDirAndConfig 
	checkForFail "Creating the Root Directory and or configuring the node confifguration"
	startNoSQLInstances 
	checkForFail "Starting the NoSQL database on each node"
	createAndRunConfigurationFile 
	checkForFail "Confuguring the NoSQL cluser using config file $filename"
	doSmokeTest
	checkForFail "Smoke test"
	addSchemas
	checkForFail "Adding schemas to the database"
}

function doSmokeTest
{
	checkForFail "Doing snmoke test"
	testScript="test.sh"
	echo "#!/bin/sh" >$testScript
	echo "javac -cp kv-2.0.23/lib/kvclient-2.0.23.jar:examples kv-2.0.23/examples/hello/*.java" >$testScript
	echo "java -cp kv-2.0.23/lib/kvclient-2.0.23.jar:kv-2.0.23/examples hello.HelloBigDataWorld -host ${manchines[0]} -port 5000 -store PsystemStore" >$testScript
	declare -a args=($testScript $user@${manchines[0]}":"$targetDir)
	scp "${args[@]}"
	declare -a args=($user@${manchines[0]} "chmod +x "$targetDir/$testScript)
	ssh "${args[@]}"
	declare -a args=($user@${manchines[0]} $targetDir/$testScript)
	ssh "${args[@]}"
	if [ $? -ne 0 ]; 
		then 	
			echo -e "\e[1;31m" "Smoke test failed" "\e[0m"
			fails=$((fails+1))
		fi
	checkForFail "Smoke test finished"	
}

if [[ $# -ne 1 ]];
then
	echo -e "Use cases are:"
	echo -e "\t $0 deploy: this will deploy the NoSQL database from scratch"
	echo -e "\t $0 start: this will start an already configured NoSQL database"
	echo -e "\t $0 delete: this will completely delete a NoSQL deployment"
	echo -e "\t $0 reboot: this will reboot all of the host machines"
	echo -e "\t $0 shutdown: this will halt all of the host machines"
	echo -e "Don't forget to edit the first 3 lines of this script with the appropriate data"	
else
	case $1 in
	"deploy" )
		doDeployment
		echo "Deployment complete"
	;;
	"start" )
		checkMachines
		checkForFail "Check the machines for conectivity"
		startNoSQLInstances 
		checkForFail "Starting the NoSQL database on each node"
		addSchemas
		echo "NoSQL started"
	;;
	"delete" )
		stoptNoSQLInstances
		#checkForFail "Stopping the NoSQL instances"
		deleteDeployment
		checkForFail "Deleting the previous deployment"
		echo "Done Deleting Instances"
	;;
	"reboot" )
		powerCycleHosts
		echo "Done starting the reboot"
	;;
	"shutdown" )
		shutdownHosts
		echo "Hosts are now shutting down"
	;;
	*)
		echo "Option not recognized type $0 to see options"
	;;
	esac
fi




