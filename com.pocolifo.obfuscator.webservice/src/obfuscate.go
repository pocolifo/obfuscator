package main

import "os"
// import "os/exec"
import "path/filepath"
import "math/rand"
import "strconv"
import "time"
// import "bufio"
// import "fmt"

const (
    STATUS_INIT = 0x0,
    STATUS_RUNNING = 0x1,
    STATUS_FINISHED = 0x2
)

type ObfTask struct {
    id string

    input string
    libraries []string
    config string

    status byte

    logs []string
}

type ObfQueue struct {
    directory string
    tasks []ObfTask
}


func getObfQueuePath() string {
    return filepath.Abs("obfuscation_queue")
}

func checkDirectoryAccess() {
    _, err := getObfQueuePath()

    if err != nil {
        panic(err)
    }
}

func NewObfQueue() *ObfQueue {
    // random
    rand.Seed(time.Now().UnixNano())

    // directory
    checkDirectoryAccess()
    path, _ := getObfQueuePath()
    os.MkdirAll(path, os.ModePerm)

    return &ObfQueue{directory: path, tasks: []ObfTask{}}
}

func CreateNewObfTask(queue *ObfQueue) *ObfTask {
    var id string
    for id = strconv.Itoa(rand.Int()); DoesFileExist(filepath.Join(queue.directory, id)); id = strconv.Itoa(rand.Int()) {}

    path, _ := getObfQueuePath()
    os.MkdirAll(filepath.Join(path, id), os.ModePerm)

    return &ObfTask{id: id, status: STATUS_INIT}
}

func Get

/*func RunObfuscator(id string) {
     java := os.Getenv("JAVA_INSTALLATION")
     cli := os.Getenv("CLI_INSTALLATION")

    queue, _ := GetObfQueuePath()
    jar := filepath.Join(queue, id)
    cfg := filepath.Join(queue, id + ".config.json")

    args := []string{"-jar", cli, jar, cfg}

    cmd := exec.Command(java, args...)

    stdout, _ := cmd.StdoutPipe()
    stderr, _ := cmd.StderrPipe()

    cmd.Start()

    stdout_scanner := bufio.NewScanner(stdout)
    stderr_scanner := bufio.NewScanner(stderr)

    for {
        stdout_ok := stdout_scanner.Scan()
        stderr_ok := stderr_scanner.Scan()

        if !(stdout_ok || stderr_ok) {
            break
        }

        if stdout_ok {
            fmt.Println("!!! [OUT] " + stdout_scanner.Text())
        }

        if stderr_ok {
            fmt.Println("!!! [ERR] " + stderr_scanner.Text())
        }
    }

    cmd.Wait()
}*/