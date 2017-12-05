#!/bin/bash

# executes the ZFitsTester with a random set of files from the rawfolder

if [ "$#" -ne 3 ]; then
    echo "Illegal number of parameters, expects: jar-file (fact-tools) rawfolder (eg: /fact/raw) and amount (eg: 100)"
    exit
fi

target=$1
rawFolder=$2

if [ ! -d "$rawFolder" ]; then
  echo $rawFolder is not a folder
  echo Stopping
  exit
fi

amount=$3


echo using fact-tools: $target
echo Getting Zfits Files out of: $rawFolder
echo Getting $amount random files

FILES=( $rawFolder/*/*/*/*.fits.fz )
# get the required amount of random files
FILES=$(printf "%s\n" ${FILES[@]} | sort -R | head -n $amount)

# execute the tester
java -cp $target fact.ZFitsTester ${FILES}

