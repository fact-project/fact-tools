#!/bin/bash

# executes the ZFitsTester with a random set of files from the rawfolder

if [ "$#" -ne 2 ]; then
    echo "Illegal number of parameters, expects: rawfolder (eg: /fact/raw) and amount (eg: 100)"
    exit
fi

rawFolder=$1

if [ ! -d "$rawFolder" ]; then
  echo $rawFolder is not a folder
  echo Stopping
  exit
fi

amount=$2

echo Getting Zfits Files out of: $rawFolder
echo Getting $amount random files

FILES=( $rawFolder/*/*/*/*.fits.fz )
# get the required amount of random files
FILES=$(printf "%s\n" ${FILES[@]} | sort -R | head -n $amount)

# execute the tester
java -cp target/fact-tools-0.18.1-SNAPSHOT.jar fact.ZFitsTester ${FILES}

