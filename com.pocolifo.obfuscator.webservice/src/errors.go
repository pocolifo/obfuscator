package main

import "github.com/gin-gonic/gin"
import "net/http"

type ObfError struct {
    status int
    message string
}

var (
    ERR_1 = &ObfError{status: http.StatusBadRequest, message: "Could not receive the 'binary' file in the form data. Did you add your JAR as part of the form data? [0x1]"}
    ERR_2 = &ObfError{status: http.StatusBadRequest, message: "Could not receive the 'config' file in the form data. Are you sure you added your config as part of the form data? [0x2]"}
)

func HandleError(ctx *gin.Context, oerr *ObfError) {
    ctx.JSON(oerr.status, gin.H{
        "error": gin.H{
            "status": oerr.status,
            "message": oerr.message,
        },
    })

    return
}

