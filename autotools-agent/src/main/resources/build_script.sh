#!/bin/sh
export LC_ALL=C
set +e

cd "$SOURCE_PATH"
if [ "$NEED_AUTORECONF" -eq 1 ]
then
 autoreconf -ifs
 code=$?
 if [ "$code" -eq 127 ]
 then
  echo "##teamcity[buildProblem description='autoreconf not available.' identity='autoreconf']"
  exit
 fi
 if [ "$code" -ne 0 ]
 then
  echo "##teamcity[buildProblem description='autoreconf step failed.' identity='autoreconf']"
  exit
 fi
fi

rm -f $TMPDIR/artifacts/$ARTIFACT_NAME.tar.gz
rm -f $TMPDIR/config.log
rm -f $TMPDIR/makefiles.tar.gz

if [ "$SOURCE_PATH" = "" ]
then
 $SOURCE_PATH/configure
else
 $SOURCE_PATH/configure $CONF_PARAMS
fi
code=$?
if [ "$code" -eq 127 ]
then
 echo "##teamcity[buildProblem description='configure not available.' identity='configure']"
 exit
fi
if [ "$code" -ne 0 ]
then
 echo "##teamcity[buildProblem description='configure step failed.' identity='configure']"
 cp config.log $TMPDIR/config.log
 echo "##teamcity[publishArtifacts '$TMPDIR/config.log']"
 exit
fi

echo "##teamcity[compilationStarted compiler='CC']"
if [ "$MK_PARAMS" = "" ]
then
 make
else
 make $MK_PARAMS
fi
code=$?
if [ "$code" -eq 127 ]
then
 echo "##teamcity[buildProblem description='make not available.' identity='make']"
 exit
fi

if [ "$code" -ne 0 ]
then
 echo "##teamcity[buildProblem description='make step failed.' identity='make']"
 find . -type f \( -name "Makefile.*" -o -name "configure.ac" -o -name "configure.in" \) | xargs  tar -cvf $TMPDIR/makefiles.tar
 cd $TMPDIR
 gzip -9 makefiles.tar
 echo "##teamcity[publishArtifacts '$TMPDIR/makefiles.tar.gz']"
 exit
fi
echo "##teamcity[compilationFinished compiler='CC']"


if [ "$HAS_RUNTEST" = "" ]
then
 make $MAKE_CHECK
 code=$?
else
 make $MAKE_CHECK "$MY_RUNTESTFLAGS"
 code=$?
 if [ "$MY_RUNTESTFLAGS" = "RUNTESTFLAGS=--all" ]
 then
  find . -type f \( -path  "*/testsuite/*.log" -o -path  "*/testsuite/*.sum" \) | xargs  tar -cvf $TMPDIR/dejagnulogs.tar
  gzip -9 $TMPDIR/dejagnulogs.tar
  echo "##teamcity[publishArtifacts '$TMPDIR/dejagnulogs.tar.gz']"
 fi
fi

if [ "$code" -eq 127 ]
then
 echo "##teamcity[buildProblem description='make not available.' identity='make']"
 exit
fi
if [ "$code" -ne 0 ]
then
 echo "##teamcity[buildProblem description='make check step failed.' identity='make check']"
 exit
fi

make DESTDIR=$TMPDIR/artifacts install
code=$?
if [ "$code" -eq 127 ]
then
 echo "##teamcity[buildProblem description='make install not available.' identity='make install']"
 exit
fi
if [ "$code" -ne 0 ]
then
 echo "##teamcity[buildProblem description='make install step failed.' identity='make install']"
 exit
fi

cd $TMPDIR/artifacts
find * -type f -print > ../files.lst
tar cvf  $ARTIFACT_NAME.tar `cat ../files.lst`
code=$?
if [ "$code" -eq 127 ]
then
 echo "##teamcity[buildProblem description='tar not available.' identity='tar']"
 exit
fi
if [ "$code" -ne 0 ]
then
 echo "##teamcity[buildProblem description='tar step failed.' identity='tar']"
 exit
fi


gzip -9 $ARTIFACT_NAME.tar
code=$?
if [ "$code" -eq 127 ]
then
 echo "##teamcity[buildProblem description='gzip not available.' identity='gzip']"
 exit
fi
if [ "$code" -ne 0 ]
then
 echo "##teamcity[buildProblem description='gzip step failed.' identity='gzip']"
 exit
fi

echo "##teamcity[publishArtifacts '$TMPDIR/artifacts/$ARTIFACT_NAME.tar.gz']"