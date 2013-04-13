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
	f.write ('''@model<transition>

def main()
{

/* Membrane Structure */

''')

def PrintMembranes(f):
	i=0;
	string="@mu = " 
	while (i<NumberOfMembranes):
		string+='['
		i+=1
	i=NumberOfMembranes;
	while (i>0):
		string+=']\''
		string+=str(i)
		i-=1
	string+=";\n\n/* Initial Multisets */ \n\n"	
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
		string= "@ms(%d) =" %i
		RandomObjects=random.sample(obj,NumberOfObjectInInitialMultiset)
		for st in RandomObjects:
			string+=st 
			string+=','
		string=string.strip(',')
		string+=';\n'
		f.write(string)
	f.write("\n/* Rules */ \n\n")

def PrintRules(f,obj):
	membrane=1

	while membrane<=NumberOfMembranes:
		membrane+=1
		i=0;
		allRules = set()
		while i<NumberOfRules:
			i+=1
			LeftObj = random.sample(obj,random.randint(2,3))
			RightObj = random.sample(obj,random.randint(1,4))
			while set(LeftObj)==set(RightObj):
				RightObj = random.sample(obj,random.randint(1,4))

			string="["			
			if (i<=NumberOfRules-NumberOfObjects):
				for j in LeftObj:
					string+=j
	 				string+=','
			else:
				string+=obj[NumberOfRules-i]
			string=string.strip(',')
			
			if (membrane!=NumberOfMembranes+1):
				string += "[]\'%d --> " % (membrane)
			else:
				string += "]\'%d --> " % (membrane-1)
			if (membrane==2):
				for j in RightObj:
					string+=j
	 				string+=','
				string=string.strip(',')
			string += "["
			if (membrane!=NumberOfMembranes+1):
				for j in RightObj:
					string+=j
		 			string+=','
				string=string.strip(',')
				string += "]'%d]'%d;\n" % (membrane, membrane-1)
			else:
				string += "]'%d;\n" % (membrane-1)
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
		return
	NumberOfMembranes = int(argv[1])
	NumberOfObjects = int(argv[2])
	NumberOfRules = int(argv[3])

	if NumberOfMembranes == 0 or NumberOfObjects == 0 or NumberOfRules == 0:
		print "Please select numbers greater than 0 for the fields"
		return

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



