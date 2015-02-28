#!/bin/bash          
outputDir="texdoclet"
outputFileName="docs"    


#curdir=${PWD}
#cd `dirname $0` 


pushd `dirname $0` > /dev/null
SCRIPTPATH=`pwd`
popd > /dev/null

echo "-----script dir: $SCRIPTPATH"

outputDir="$SCRIPTPATH/$outputDir"



mkdir $outputDir
echo "----starting script-----"
echo "removing stuff in outputfolder: $outputDir/$outputFileName.aux "
rm "$outputDir/$outputFileName.aux"
rm "$outputDir/$outputFileName.tex"

docletpath=$1
if [ -z "$VAR" ]
	then
	echo "No argument to texdoclet given. "
	docletpath="$SCRIPTPATH/TeXDoclet.jar"
fi

echo "----looking for TeXDoclet.jar in path: $SCRIPTPATH "
sourcedir="$SCRIPTPATH/../../../src/main/java"
echo "---- sourcepath is $sourcedir" 

if [ -f "$docletpath" ] 
	then
	javadoc -docletpath "$SCRIPTPATH/TeXDoclet.jar" \
	-doclet org.stfm.texdoclet.TeXDoclet \
	-hyperref \
	-docclass article \
	-output "$outputDir/$outputFileName.tex" \
	-title "Fact-Tools Documentation" \
	-subtitle "Automatisch generierte Fact-Tools Dokumentation" \
	-author "Christian Bockermann \and Kai Bruegge" \
	-sourcepath "$sourcedir" \
	-subpackages  fact.io fact.utils fact fact.statistics fact.filter fact.features \
	-public \
	-noinherited \
	-version \
	-nosummaries \
	-tree \
	-quiet \
	-texsetup "$SCRIPTPATH/abstract.tex" \
	-texintro "$SCRIPTPATH/intro.tex" \

else 
	echo "doclet not found. aborting"
	exit 1
fi


		if [ -f "$outputDir/$outputFileName.tex" ]
		  then
		    echo ".tex file exists. creating pdf"
		    cd $outputDir

			pdflatex -shell-escape  -interaction=nonstopmode $outputFileName.tex 
			pdflatex -shell-escape  -interaction=nonstopmode $outputFileName.tex 

			echo " "
			echo " -------------------------------------------  "
			echo "Created $outputFileName.tex and $outputFileName.pdf in the $outputDir Folder."
			echo "Used pdflatex for compilation."
		else
			echo "xxxxx no .tex file written javadoc command probably failed to produce output"
		fi



