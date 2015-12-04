# Smiles2Monomers
A tool to link chemical structures to biological structures

Resear article (in press) :  
"Smiles2Monomers : a link between chemical and biological structures for polymers"  
Yoann Dufresne, Laurent Noé, Valérie Leclère and Maude Pupin  
Journal of Cheminformatics

## How to install (Command line procedure)

### Download
git clone https://github.com/yoann-dufresne/Smiles2Monomers  
cd Smiles2Monomers

### Compile
ant compile  
ant create_jar

Now you must find a binary file named s2m.jar into the build directory. The program is ready.

## How to use s2m

### Using the ant file

#### Pre-computation
First of all you have to pre-compute all the monomers that you wish to use.  
For the pre-computation you need one json file containing all the monomers that you want to use and one json file containing the polymers that you want to use for the indexation process and the file containing the biding rules (see the research article for more details). The json files structures are detailed in the wiki (work in progress). These json files are respectively named "data/monomers.json" and "data/learning.json". You can name your files in the same way or modify the properties in the beginning of the build.xml file.  
Then you just have to call:  
ant preCompute

To verify if everything is fine, go into the data directory and look for residues.json chains.json and serials/ directory. If they all exists, you can now run the program.

#### Running s2m


Work in progress...
ant s2m

### Java command line
