BEFORE_DIR=$(pwd)

cd src || exit
go build -o ../webservice.o

cd "$BEFORE_DIR" || exit