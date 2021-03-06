package com.brightgenerous.cli.delegate;

import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.List;

import com.brightgenerous.cli.CliException;
import com.brightgenerous.cli.CliOption;
import com.brightgenerous.cli.ParseResult;

class CliDelegaterSub implements CliDelegater {

    @Override
    public ParseResult parse(List<CliOption> options, String[] args) throws CliException {
        CommandLineParser parser = new BasicParser();
        Options opts = convert(options);
        CommandLine line;
        try {
            line = parser.parse(opts, args);
        } catch (ParseException e) {
            throw new CliException(e);
        }
        return new ParseResultSub(line);
    }

    private static final int DEFAULT_WIDTH = 74;

    private static final int DEFAULT_LEFT_PAD = 1;

    private static final int DEFAULT_DESC_PAD = 3;

    private static final String DEFAULT_HEADER = null;

    private static final String DEFAULT_FOOTER = null;

    @Override
    public String options(List<CliOption> options) {
        Options opts = convert(options);
        StringWriter sw = new StringWriter();
        try (PrintWriter pw = new PrintWriter(sw)) {
            new HelpFormatter().printOptions(pw, DEFAULT_WIDTH, opts, DEFAULT_LEFT_PAD,
                    DEFAULT_DESC_PAD);
        }
        return sw.toString();
    }

    @Override
    public String help(String cmdLineSyntax, List<CliOption> options) {
        Options opts = convert(options);
        StringWriter sw = new StringWriter();
        try (PrintWriter pw = new PrintWriter(sw)) {
            new HelpFormatter().printHelp(pw, DEFAULT_WIDTH, cmdLineSyntax, DEFAULT_HEADER, opts,
                    DEFAULT_LEFT_PAD, DEFAULT_DESC_PAD, DEFAULT_FOOTER, true);
        }
        return sw.toString();
    }

    @Override
    public String usage(String cmdLineSyntax, List<CliOption> options) {
        Options opts = convert(options);
        StringWriter sw = new StringWriter();
        try (PrintWriter pw = new PrintWriter(sw)) {
            new HelpFormatter().printUsage(pw, DEFAULT_WIDTH, cmdLineSyntax, opts);
        }
        return sw.toString();
    }

    private Options convert(List<CliOption> options) {
        Options opts = new Options();
        if ((options != null) && !options.isEmpty()) {
            for (CliOption option : options) {
                Option opt = new Option(option.opt(), option.description());
                if (option.longOpt() != null) {
                    opt.setLongOpt(option.longOpt());
                }
                if (option.args() != null) {
                    opt.setArgs(option.args().intValue());
                }
                if (option.required() != null) {
                    opt.setRequired(option.required().booleanValue());
                }
                if (option.optionalArg() != null) {
                    opt.setOptionalArg(option.optionalArg().booleanValue());
                }
                if (option.argName() != null) {
                    opt.setArgName(option.argName());
                }
                if (option.valueSeparator() != null) {
                    opt.setValueSeparator(option.valueSeparator().charValue());
                }
                opts.addOption(opt);
            }
        }
        return opts;
    }

    static class ParseResultSub implements ParseResult, Serializable {

        private static final long serialVersionUID = 4178476031707976593L;

        private final CommandLine commandLine;

        public ParseResultSub(CommandLine commandLine) {
            this.commandLine = commandLine;
        }

        @Override
        public boolean has(char opt) {
            return commandLine.hasOption(opt);
        }

        @Override
        public boolean has(String opt) {
            return commandLine.hasOption(opt);
        }

        @Override
        public String value(char opt) {
            return commandLine.getOptionValue(opt);
        }

        @Override
        public String value(String opt) {
            return commandLine.getOptionValue(opt);
        }

        @Override
        public String[] values(char opt) {
            return commandLine.getOptionValues(opt);
        }

        @Override
        public String[] values(String opt) {
            return commandLine.getOptionValues(opt);
        }
    }
}
