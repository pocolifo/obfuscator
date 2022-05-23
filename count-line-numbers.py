if __name__ == '__main__':
    import os

    rootdir = [
        'com.pocolifo.obfuscator.annotations/src/',
        'com.pocolifo.obfuscator.cli/src/',
        'com.pocolifo.obfuscator.engine/src/',
        'com.pocolifo.obfuscator.gradleplugin/src/',
        'com.pocolifo.obfuscator.testproject/src/'
    ]

    lines = 0
    file_count = 0

    for dir in rootdir:
        for subdir, _, files in os.walk(dir):
            for file in files:
                f = os.path.join(subdir, file)
                if f.endswith('.java'):
                    file_count += 1
                    with open(f, 'rt') as file_open:
                        lines += len(file_open.readlines())


    print(f'{lines} lines total across {file_count} files')
