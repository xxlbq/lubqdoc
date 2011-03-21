#!/bin/sh
#
# run process script for GateWay(DB)
#   created by guoys
#---------------------------------------------------------------------------
if [ "$OS" = "Windows_NT" ]; then
	ARGS=""
else
	MIN_MEMORY=128m
	MAX_MEMORY=128m
	MIN_NEW_SIZE=8m
	MAX_NEW_SIZE=8m
	ARGS="-Xms${MIN_MEMORY} -Xmx${MAX_MEMORY} -XX:NewSize=${MIN_NEW_SIZE} -XX:MaxNewSize=${MAX_NEW_SIZE}"
fi
tcsh -c "java -DPROCESS_NAME=GWGKGOH_Fetch -DconfigPath=/jhfapp/app/conf ${ARGS} cn.bestwiz.jhf.gws.gkgoh.main.GKGOHFetchStartup &"
