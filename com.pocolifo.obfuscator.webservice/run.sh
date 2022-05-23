JAVA_INSTALLATION=$(which java)
CLI_INSTALLATION="/home/youngermax/PocolifoWork/GitLab/obfuscator/com.pocolifo.obfuscator.cli/build/libs/com.pocolifo.obfuscator.cli-1.0-SNAPSHOT.jar"

BEFORE_DIR=$(pwd)

cd run || exit
JAVA_INSTALLATION=$JAVA_INSTALLATION CLI_INSTALLATION=$CLI_INSTALLATION ../webservice.o

cd "$BEFORE_DIR" || exit