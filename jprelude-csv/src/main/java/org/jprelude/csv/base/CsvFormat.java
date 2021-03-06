package org.jprelude.csv.base;

import org.jprelude.core.util.LineSeparator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.QuoteMode;
import org.jprelude.core.util.Seq;


/*

// Returns a lazy sequence of CSV formatted person records (type Seq<Person>).
// Source is a lazy sequence of person objects.
Seq<String> csvLines = CsvFormat.builder()
  .columns(
      "FIRST_NAME",
      "LAST_NAME",
      "COUNTRY",
  .delimiter(";")
  .recordSeparator("\r\n")
  .build()
  .apply(persons.map(person ->
      Arrays.asList(
          person.getFirstName(),
          person.getLastName(),
          person.Country()
      )
   ));


// Generates output file "output.csv" with CSV formatted person records
// and returns the number of written records.
long n = CsvFormat.builder()
  .columns(
      "FIRST_NAME",
      "LAST_NAME",
      "COUNTRY")
  .delimiter(";")
  .recordSeparator("\r\n")
  .build()
  .forOutputTo(TextWriter.from(new File("~/output.csv")) // return type: IOFunction<Seq<List<?>, Long>  
  .apply(persons.map(person ->
      Arrays.asList(
          person.getFirstName(),
          person.getLastName(),
          person.Country()
      )
   ));


// Reads records from CSV formatted input file "input.csv" with CSV formatted person
// and converts them to a lazy sequence of Person objects.
// In this example the CSV parsing will work even if the order of the CSV column 
// is different than expected.
CsvFormat.builder()
    .columns( 
        new CsvColumn("firstName", "FIRST_NAME") 
        new CsvColumn("lastName", "LAST_NAME")
        new CsvColumn("country", "COUNTRY"))
    .delimiter(";")
    .recordSeparator("\r\n")
    .build()
    .forInputFrom(TextReader.from(new File("~/input.csv")) // return type: Function<<Function<CsvRow, T>, Seq<T>> 
    .apply(row ->
        Person person ret = new Person();
        ret.setFirstName(row.get("firstName"));
        ret.setLastName(row.get("lastName"));
        ret.setCountry(row.get("country"));
        return ret;
    );

// Reads records from CSV formatted input file "input.csv" and prints out
// human readable information about the involved persons.
CsvFormat.builder()
  .columns(         // In this particular example it is not really necessary to define the
      "FIRST_NAME", // concrete column names.
      "LAST_NAME",
      "COUNTRY")
  .delimiter(";")
  .build()
  .forInputFrom(TextReader.from(new File("~/input.csv"))
  .apply(Function.identity())
  .forEach((row, idx) ->
       System.out.println(String.format("Person #%s: %d %s - %s", idx, row.get(0), row.get(1), row.get(2)); 
  );



*/


public final class CsvFormat {
    private final List<CsvColumn> columns;
    private final char delimiter;
    private final LineSeparator recordSeparator;
    private final boolean autoTrim;
    private final Character escapeCharacter;
    private final Character quoteCharacter;
    private final CsvQuoteMode quoteMode;
    private final CSVFormat apacheCommonsCsvFormatForExport;
    private final CSVFormat apacheCommonsCsvFormatForImport;
            
    
    private CsvFormat(final Builder builder) {
        this.columns = new ArrayList<>(builder.columns);
        this.delimiter = builder.delimiter;
        this.recordSeparator = builder.recordSeparator;
        this.autoTrim = builder.autoTrim;
        this.escapeCharacter = builder.escapeCharacter;
        this.quoteCharacter = builder.quoteCharacter;
        this.quoteMode = builder.quoteMode;
        
        final QuoteMode apacheCommonsCsvQuoteMode;
        
        switch (this.quoteMode) {
            case ALL:
                apacheCommonsCsvQuoteMode = QuoteMode.ALL;
                break;

            case NONE:
                apacheCommonsCsvQuoteMode = QuoteMode.NONE;
                break;
            
            case NON_NUMERIC:
                apacheCommonsCsvQuoteMode = QuoteMode.NON_NUMERIC;
                break;

            default:
              apacheCommonsCsvQuoteMode = QuoteMode.MINIMAL;
        }
        
        CSVFormat formatExport = CSVFormat.DEFAULT
                .withDelimiter(this.delimiter)
                .withRecordSeparator(this.recordSeparator.value())
                .withIgnoreSurroundingSpaces(this.autoTrim)
                .withEscape(this.escapeCharacter)
                .withQuote(this.quoteCharacter)
                .withQuoteMode(apacheCommonsCsvQuoteMode);
        
        CSVFormat formatImport = formatExport;
        
        if (!this.columns.isEmpty()) {
            final String[] columnNames = new String[this.columns.size()];
            Seq.from(this.columns).forEach((col, idx) -> columnNames[idx.intValue()] = col.getName());
            formatImport = formatImport.withHeader(columnNames).withSkipHeaderRecord();
        }
        
        this.apacheCommonsCsvFormatForImport = formatImport;
        this.apacheCommonsCsvFormatForExport = formatExport;
    }

    public char getDelimiter() {
        return this.delimiter;
    }
    
    public LineSeparator getRecordSeparator() {
        return this.recordSeparator;
    }
    
    public boolean isAutoTrimmed() {
        return this.autoTrim;
    }
   
    public List<CsvColumn> getColumns() {
        return new ArrayList<>(this.columns);
    }
    
    public Character getEscapeCharacter() {
        return this.escapeCharacter;
    }
    
    public CsvQuoteMode getQuoteMode() {
        return this.quoteMode;
    }
    
    public Character getQuoteCharacter() {
        return this.quoteCharacter;
    }
    
    
    public Function<List<?>, String> asMapper() {
        return this.asMapper(false);
    }

    public Function<List<?>, String> asMapper(final boolean appendRecordSeparator) {
        return row -> {
            Seq seq = Seq.from(row);
            
            if (this.autoTrim) {
                seq = seq.map(field -> field == null ? null : field.toString().trim());
            }

            String ret = this.apacheCommonsCsvFormatForExport.format(seq.toArray());
            
            if (appendRecordSeparator) {
               ret = ret + this.getRecordSeparator();
            }
            
            return ret;
        }; 
    }

    public Seq<String> map(final Seq<List<?>> rows) {
        return this.map(rows, false);
    }
 
    public Seq<String> map(final Seq<List<?>> rows, final boolean appendRecordSeparator) {
        Seq<String> ret = Seq.sequential(rows).map(this.asMapper(appendRecordSeparator));
        
        if (!this.columns.isEmpty()) {
            final String headline = this.asMapper(appendRecordSeparator)
                    .apply(columns.stream().map(CsvColumn::getName)
                    .collect(Collectors.toList()));
            
            ret = ret.prepend(headline + this.getRecordSeparator());
        }
        
        return ret;
    }

    public static Builder builder() {
        return new Builder();
    }
    
    public static Builder builder(final CsvFormat prototype) {
        return new Builder(prototype);
    }
        
    public static final class Builder {
        private List<CsvColumn> columns;
        private char delimiter;
        private LineSeparator recordSeparator;
        private boolean autoTrim;
        private Character escapeCharacter;
        private Character quoteCharacter;
        private CsvQuoteMode quoteMode;
        
        private Builder() {
            this.columns = new ArrayList<>();
            this.delimiter = ',';
            this.recordSeparator = LineSeparator.LF;
            this.autoTrim = false;
            this.escapeCharacter = null;
            this.quoteCharacter = '"';
            this.quoteMode = CsvQuoteMode.MINIMAL;
        }
        
        private Builder(final CsvFormat prototype) {
            if (prototype != null) {
                this.columns = prototype.columns;
                this.delimiter = prototype.delimiter;
                this.recordSeparator = prototype.recordSeparator;
                this.autoTrim = prototype.autoTrim;
                this.escapeCharacter = prototype.escapeCharacter;
                this.quoteCharacter = prototype.quoteCharacter;
                this.quoteMode = prototype.quoteMode;
            }
        }
        
        public Builder delimiter(final char delimiter) {
            this.delimiter = delimiter;
            return this;
        }
        
        public Builder recordSeparator(final LineSeparator recordSeparator) {
            this.recordSeparator = recordSeparator;
            return this;
        }
       
        public Builder escape(final Character escapeCharacter) {
            this.escapeCharacter = escapeCharacter;
            return this;
        }
        
        public Builder quote(final Character quoteCharacter) {
            this.quoteCharacter = quoteCharacter;
            return this;
        }
        
        public Builder quoteMode(final CsvQuoteMode quoteMode) {
            Objects.requireNonNull(quoteMode);
            
            this.quoteMode = quoteMode;
            return this;
        }
        
        public CsvFormat.Builder columns(String... columnNames) {
            this.columns.clear();

            if (columnNames != null) {
                for (final String columnName : columnNames) {
                    this.columns.add(new CsvColumn(columnName));
                }
            }

            return this;
        }

        public CsvFormat.Builder columns(final CsvColumn... columns) {
            return this.columns(Arrays.asList(columns));
        }

        public CsvFormat.Builder columns(final List<CsvColumn> columns) {
            this.columns.clear();

            if (columns != null) {
                for (final CsvColumn column : columns) {
                    if (column != null) {
                        this.columns.add(column);
                    }
                }
            }

            return this;        
        }
        
        public Builder autoTrim(final boolean autoTrim) {
            this.autoTrim = autoTrim;
            return this;
        }
        
        public CsvFormat build() {
            return new CsvFormat(this);
        }
    }         
}
