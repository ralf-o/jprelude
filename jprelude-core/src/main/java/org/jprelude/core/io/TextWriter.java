package org.jprelude.core.io;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;
import org.jprelude.core.util.LineSeparator;
import org.jprelude.core.util.Mutable;
import org.jprelude.core.util.Seq;
import org.jprelude.core.util.function.CheckedBiConsumer;
import org.jprelude.core.util.function.CheckedConsumer;
import org.jprelude.core.util.function.CheckedSupplier;

public interface TextWriter {

    Charset getCharset();

    URI getUri();

    OutputStream newOutputStream() throws IOException;

    default long writeLines(final Seq<?> lines) throws IOException {
        Objects.requireNonNull(lines);

        return this.writeLines(lines, LineSeparator.LF);
    }

    default long writeLines(final Seq<?> lines, LineSeparator lineSeparator)
            throws IOException {

        Objects.requireNonNull(lines);

        final Mutable<Long> counter = Mutable.of(0L);

        final String lineSeparatorValue
                = (lineSeparator == null || lineSeparator == LineSeparator.NONE)
                        ? ""
                        : lineSeparator.value();

        this.write(printStream -> Seq.sequential(lines).forEach(line -> {
            printStream.print(Objects.toString(line, ""));
            printStream.print(lineSeparatorValue);

            if (printStream.checkError()) {
                throw new UncheckedIOException(
                        new IOException(
                                "Could not write to PrintStream - checkError() returned true"));
            }

            counter.update(n -> n + 1);
        }));

        return counter.get();
    }

    default void writeFullText(final Object text) throws IOException {
        this.writeLines(Seq.of(Objects.toString(text, "")), LineSeparator.NONE);
    }

    default void write(final CheckedConsumer<PrintStream, IOException> delegate)
            throws IOException {

        this.write((printStream, charset) -> delegate.accept(printStream));
    }

    default void write(final CheckedBiConsumer<PrintStream, Charset, IOException> delegate)
            throws IOException {

        Objects.requireNonNull(delegate);

        final Charset charset = this.getCharset();

        final Charset nonNullCharset = charset != null
                ? charset
                : StandardCharsets.UTF_8;

        try (
                final OutputStream outputStream = this.newOutputStream();
                final PrintStream printStream = new PrintStream(
                        new BufferedOutputStream(outputStream),
                        true,
                        nonNullCharset.name())) {

            delegate.accept(printStream, charset);

            if (printStream.checkError()) {
                throw new UncheckedIOException(
                        new IOException("Could not write to PrintStream - "
                        + "checkError() returned true"));
            }
        }
    }

    static TextWriter create(
            final CheckedSupplier<OutputStream, IOException> outputStreamSupplier) {

        Objects.requireNonNull(outputStreamSupplier);

        return TextWriter.create(outputStreamSupplier, StandardCharsets.UTF_8, null);
    }

    static TextWriter create(
            final CheckedSupplier<OutputStream, IOException> outputStreamSupplier,
            final Charset charset,
            final URI uri) {

        Objects.requireNonNull(outputStreamSupplier);

        final Charset nonNullCharset = charset != null
                ? charset
                : StandardCharsets.UTF_8;

        return new TextWriter() {
            @Override
            public OutputStream newOutputStream() throws IOException {
                return outputStreamSupplier.get();
            }

            @Override
            public URI getUri() {
                return uri;
            }

            @Override
            public Charset getCharset() {
                return nonNullCharset;
            }
        };
    }

    static TextWriter forFile(
            final Path path,
            final OpenOption... openOptions) {

        Objects.requireNonNull(path);

        return TextWriter.forFile(path, null, openOptions);
    }

    static TextWriter forFile(
            final Path path,
            final Charset charset,
            final OpenOption... openOptions) {

        Objects.requireNonNull(path);

        final OpenOption[] options = Seq.of(openOptions)
                .filter(option -> option != null)
                .prependMany(StandardOpenOption.WRITE, StandardOpenOption.CREATE)
                .toArray(OpenOption[]::new);

        return TextWriter.create(
                () -> Files.newOutputStream(path, options),
                charset != null ? charset : StandardCharsets.UTF_8,
                path.toUri());
    }

    static TextWriter forOutputStream(final OutputStream outputStream) {
        Objects.requireNonNull(outputStream);

        return TextWriter.forOutputStream(outputStream, null);
    }

    static TextWriter forOutputStream(
            final OutputStream outputStream,
            final Charset charset) {

        Objects.requireNonNull(outputStream);

        final CheckedSupplier<OutputStream, IOException> supplier = () -> new OutputStream() {
            private boolean isClosed = false;

            @Override
            public void write(int b) throws IOException {
                if (!isClosed) {
                    outputStream.write(b);
                } else {
                    throw new UncheckedIOException(
                            new IOException("Output stream is already closed"));
                }
            }

            @Override
            public void flush() throws IOException {
                if (!isClosed) {
                    outputStream.flush();
                }
            }

            @Override
            public void close() throws IOException {
                isClosed = true;
            }
        };

        return TextWriter.create(supplier, charset, null);
    }
}
