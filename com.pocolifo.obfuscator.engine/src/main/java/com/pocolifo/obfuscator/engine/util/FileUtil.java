package com.pocolifo.obfuscator.engine.util;

import java.io.File;
import java.io.FileFilter;
import java.util.List;

public class FileUtil {
    public static List<File> recursivelyFindFiles(File directory, FileFilter fileFilter, List<File> files) {
        File[] listedFiles = directory.listFiles(fileFilter);

        if (listedFiles != null) {
            for (File file : listedFiles) {
                files.add(file);

                if (file.isDirectory()) recursivelyFindFiles(file, fileFilter, files);
            }
        }

        return files;
    }
}
