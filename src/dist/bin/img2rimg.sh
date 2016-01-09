#!/bin/bash

if [ -z "$SCREENTOOL_HOME" ] ; then
  PRG="$0"

  # need this for relative symlinks
  while [ -h "$PRG" ] ; do
    ls=`ls -ld "$PRG"`
    link=`expr "$ls" : '.*-> \(.*\)$'`
    if expr "$link" : '/.*' > /dev/null; then
      PRG="$link"
    else
      PRG="`dirname "$PRG"`/$link"
    fi
  done

  saveddir=`pwd`

  SCREENTOOL_HOME=`dirname "$PRG"`/..

  # make it fully qualified
  SCREENTOOL_HOME=`cd "$SCREENTOOL_HOME" && pwd`

  cd "$saveddir"
fi

# echo "using SCREENTOOL_HOME: $SCREENTOOL_HOME"

CLASSPATH="."
for i in $SCREENTOOL_HOME/lib/*.jar;
do
  CLASSPATH="$CLASSPATH:$i"
done

if [ -e "$SCREENTOOL_HOME/../../target/classes" ] ; then
  CLASSPATH="$SCREENTOOL_HOME/../../target/classes:$CLASSPATH"
fi

# echo "using CLASSPATH: $CLASSPATH"

# echo "# ... java -cp $CLASSPATH fr.an.screencast.batch.ImgToRectImgDescrConverterMain $*"
java -cp "$CLASSPATH" fr.an.screencast.batch.ImgToRectImgDescrConverterMain $* 
