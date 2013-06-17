#!/bin/bash          
outputDir="texdoclet"
outputFileName="docs"    



cd `dirname $0` 

outputDir="$outputDir"

mkdir $outputDir

rm "$outputDir/$outputFileName.aux"


javadoc -docletpath ./TeXDoclet.jar \
-doclet org.stfm.texdoclet.TeXDoclet \
-hyperref \
-imagespath "images" \
-output "$outputDir/$outputFileName.tex" \
-title "Fact-Tools Documentation" \
-subtitle "Fact-Tools Dokumentation mit JavaDocs" \
-author "Christian Bockermann \and Kai Bruegge" \
-sourcepath ../../../src/main/java \
-subpackages fact.processors fact.image fact.io \
-noinherited \
-nosummaries \
-public \
-tree \
-quiet


cd $outputDir

pdflatex -shell-escape  -interaction=nonstopmode $outputFileName.tex 
pdflatex -shell-escape  -interaction=nonstopmode $outputFileName.tex 

echo " "
echo " -------------------------------------------  "
echo "Created $outputFileName.tex and $outputFileName.pdf in the $outputDir Folder."
echo "Used pdflatex for compilation."
