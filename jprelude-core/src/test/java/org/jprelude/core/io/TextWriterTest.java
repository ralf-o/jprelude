package org.jprelude.core.io;


import java.io.IOException;
import java.nio.file.Files;
import org.jprelude.core.util.Seq;
import org.junit.Test;

public class TextWriterTest {
    @Test
    public void testWritingToStdOut() throws IOException {
        final Seq<?> seq = Seq.range(1, 11).map(n -> "Line " + n);
        final TextWriter textWriter = TextWriter.from(System.out);
        textWriter.writeLines(seq);
    }
}
