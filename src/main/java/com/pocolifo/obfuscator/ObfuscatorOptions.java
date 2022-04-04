package com.pocolifo.obfuscator;

import lombok.Builder;
import lombok.Data;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import java.lang.AutoCloseable;


public class ObfuscatorOptions implements AutoCloseable {
    private List<InputStream> libraryJars = new ArrayList<>();
    private InputStream inJar;
    private OutputStream outJar;

    public ObfuscatorOptions setInJar(File file) throws IOException {
        this.inJar = new FileInputStream(file);
        return this;
    }

    public ObfuscatorOptions setInJar(InputStream stream) {
        this.inJar = stream;
        return this;
    }

    public ObfuscatorOptions addLibraryJar(File file) throws IOException {
        this.libraryJars.add(new FileInputStream(file));
        return this;
    }

    public ObfuscatorOptions addLibraryJar(InputStream stream) {
        this.libraryJars.add(stream);
    }

    public ObfuscatorOptions setOutJar(File file) throws IOException {
        this.outJar = new FileOutputStream(file);
        return this;
    }

    public ObfuscatorOptions setOutJar(OutputStream stream) {
        this.outJar = stream;
        return this;
    }

    public void prepare() throws RuntimeException {
        if (this.inJar == null) throw new RuntimeException("input jar is not set");

        this.outJar = new File(this.inJar.getPath() + "-out.jar");
    }

    @Override
    public void close() throws IOException {
        this.inJar.close();
        this.outJar.close();

        this.libraryJars.forEach(InputStream::close);
    }
}
