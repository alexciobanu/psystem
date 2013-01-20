#!/usr/bin/python

import string
import random
import math

NumberOfRules = 100
NumberOfMembranes = 3
NumberOfObjects = 10
NumberOfObjectInInitialMultiset = int(NumberOfObjects/2)

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
		while i<NumberOfRules:
			i+=1
			LeftObj = random.sample(obj,random.randint(1,3))
			RightObj = random.sample(obj,random.randint(1,5))
			string = "["
			for j in LeftObj:
				string+=j
	 			string+=','
			string=string.strip(',')
			string += " --> "
			for j in LeftObj:
				string+=j
	 			string+=','
			string=string.strip(',')
			string += "]'{0};\n".format(membrane+1)
			f.write(string)

def PrintTail(f):
	f.write ('\n}')

OutputFile = open('Psystem.pli', 'w')
obj=[]
PrintHedder(OutputFile)
PrintMembranes(OutputFile)
GenerateObjects(obj)
PrintInitialMultiset(OutputFile,obj)
PrintRules(OutputFile,obj)
PrintTail(OutputFile)



