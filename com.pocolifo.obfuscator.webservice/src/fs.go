package main

import "os"

func DoesFileExist(path string) bool {
    _, err := os.Stat(path)
    return err == nil || os.IsExist(err)
}