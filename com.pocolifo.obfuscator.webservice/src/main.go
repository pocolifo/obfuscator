package main

import "github.com/gin-gonic/gin"
// import "net/http"
import "fmt"


var queue *ObfQueue

func main() {
    // init :: misc
    queue = GetNewObfQueue()

    // init :: webserver
    router := gin.Default()

    // trust no proxies and resolves warning:
    // [GIN-debug] [WARNING] You trusted all proxies, this is NOT safe. We recommend you to set a value.
    //   Please check https://pkg.go.dev/github.com/gin-gonic/gin#readme-don-t-trust-all-proxies for details.
    router.SetTrustedProxies([]string{})

    // routes
    router.Static("/public", "./public")

    // actual routes
    router.POST("/new", NewObfuscateRequest)

    identifier := router.Group("/:id")
    {
        add := identifier.Group("/add")
        {
            add.POST("/input", ObfuscateInputRequest)
            add.POST("/library", ObfuscateLibraryRequest)
            add.POST("/config", ObfuscateConfigRequest)
        }

        identifier.POST("/obfuscate", ObfuscateRequest)
        identifier.GET("/status", ObfuscateStatusRequest)
        identifier.GET("/realtime", RealtimeOutputRequest) // go get github.com/gorilla/websocket
    }

    router.Run()
}

/*func ObfuscateRequest(ctx *gin.Context) {
    // binary file
    binary_file, err := ctx.FormFile("binary")

    if err != nil {
        HandleError(ctx, ERR_1)
        return
    }

    // config file
    config_file, err := ctx.FormFile("config")

    if err != nil {
        HandleError(ctx, ERR_2)
        return
    }

    // save to file
    id, path := GetNewPathInQueue()

    ctx.SaveUploadedFile(binary_file, path)
    ctx.SaveUploadedFile(config_file, path + ".config.json")

    RunObfuscator(id)

    ctx.JSON(http.StatusOK, gin.H{
        "status": "started",
        "id": id,
    })
}*/

func NewObfuscateRequest(ctx *gin.Context) {
    path, _ := GetNewPathInQueue(queue)
    fmt.Println(path)
}

func ObfuscateStatusRequest(ctx *gin.Context) {
}

func RealtimeOutputRequest(ctx *gin.Context) {
}

func ObfuscateInputRequest(ctx *gin.Context) {
}

func ObfuscateLibraryRequest(ctx *gin.Context) {
}

func ObfuscateConfigRequest(ctx *gin.Context) {
}

func ObfuscateRequest(ctx *gin.Context) {
}