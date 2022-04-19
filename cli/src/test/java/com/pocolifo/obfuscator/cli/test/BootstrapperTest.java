package com.pocolifo.obfuscator.cli.test;

import com.pocolifo.obfuscator.cli.Bootstrapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BootstrapperTest {
    @Test
    void main() {
        Bootstrapper.main(new String[]{
                "--input",
                "/home/youngermax/PocolifoWork/GitLab/obfuscator/pocolifoclient-reobfuscated.jar"
        });
    }
}