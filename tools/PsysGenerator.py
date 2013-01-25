#!/usr/bin/python

import string
import random
import math
import sys

NumberOfRules = 0
NumberOfMembranes = 0
NumberOfObjects = 0
NumberOfObjectInInitialMultiset = 5 #int(NumberOfObjects/2)

def PrintHedder(f): 
	f.write ('''@model<evolution_communication>

def main()
{

/* Membrane Structure */

''')

def PrintMembranes(f):
	i=0;
	string="@mu = [" 
	while (i<NumberOfMembranes):
		i+=1
		string+='[]\''
		string+=str(i+1)
	string+="]'0;\n\n/* Initial Multisets */ \n\n"	
	f.write(string)

def GenerateObjects(obj):
	i=0
	while (i<NumberOfObjects):
		i+=1
		ObjectSize= int(math.ceil( math.log10(NumberOfObjects)/math.log10(26) ))
		AnObject= ''.join(random.choice(string.ascii_lowercase) for j in xrange(ObjectSize))
		if AnObject in obj:
			i-=1
		else: 	 	
			obj.append(AnObject)

def PrintInitialMultiset(f,obj):
	i=0;
	while i<NumberOfMembranes:
		i+=1
		string= "@ms({0}) =".format(i+1)
		RandomObjects=random.sample(obj,NumberOfObjectInInitialMultiset)
		for st in RandomObjects:
			string+=st 
			string+=','
		string=string.strip(',')
		string+=';\n'
		f.write(string)
	f.write("\n/* Rules */ \n\n")

def PrintRules(f,obj):
	membrane=0
	
	while membrane<NumberOfMembranes:
		membrane+=1
		i=0;
		allRules = set()
		while i<NumberOfRules:
			i+=1
			LeftObj = random.sample(obj,random.randint(1,3))
			RightObj = random.sample(obj,random.randint(1,5))
			while set(LeftObj)==set(RightObj):
				RightObj = random.sample(obj,random.randint(1,5))
			string = "["
			for j in LeftObj:
				string+=j
	 			string+=','
			string=string.strip(',')
			string += " --> "
			for j in RightObj:
				string+=j
	 			string+=','
			string=string.strip(',')
			string += "]'{0};\n".format(membrane)
			if string not in allRules:
				f.write(string)
				allRules.add(string)
			else:
				i-=1;

def PrintTail(f):
	f.write ('\n}')


def main(argv=None):
	global NumberOfMembranes 
	global NumberOfObjects 
	global NumberOfRules 
	if argv is None:
		argv = sys.argv
	if len(argv) != 4:
		print "usage: " + argv[0] + " <NumMembranes> <NumObjects> <NumRulesPerMembrane> "
		exit(1)
	NumberOfMembranes = int(argv[1])
	NumberOfObjects = int(argv[2])
	NumberOfRules = int(argv[3])

	if NumberOfMembranes == 0 or NumberOfObjects == 0 or NumberOfRules == 0:
		print "Please select numbers greater than 0 for the fields"
		exit(1)

	OutputFile = open('Psystem.pli', 'w')
	obj=[]
	PrintHedder(OutputFile)
	PrintMembranes(OutputFile)
	GenerateObjects(obj)
	PrintInitialMultiset(OutputFile,obj)
	PrintRules(OutputFile,obj)
	PrintTail(OutputFile)

if __name__ == "__main__":
	main()



