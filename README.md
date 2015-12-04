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
First of all you have to pre-compute all the monomers that you wish to use.
For the pre-computation you need one json file containing all the monomers that you want to use and one json file containing the polymers that you want to use for the indexation process (see the article for more details). The json files structures are detailed in the wiki (work in progress).

Work in progress...

ant preCompute

ant s2m

### Java command line
