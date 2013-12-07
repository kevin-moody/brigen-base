package com.brightgenerous.cli.delegate;

import java.io.Serializable;
import java.util.List;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;

import com.brightgenerous.cli.Option;
import com.brightgenerous.cli.ParseException;
import com.brightgenerous.cli.ParseResult;

class CliDelegaterCommons implements CliDelegater {

    @Override
    public ParseResult parse(List<Option> options, String[] args) throws ParseException {
        CommandLineParser parser = new BasicParser();
        Options opts = new Options();
        if ((options != null) && !options.isEmpty()) {
            for (Option option : options) {
                org.apache.commons.cli.Option opt = new org.apache.commons.cli.Option(option.opt(),
                        option.description());
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
        CommandLine line;
        try {
            line = parser.parse(opts, args);
        } catch (org.apache.commons.cli.ParseException e) {
            throw new ParseException(e);
        }
        return new ParseResultCommons(line);
    }

    static class ParseResultCommons implements ParseResult, Serializable {

        private static final long serialVersionUID = 4178476031707976593L;

        private final CommandLine commandLine;

        public ParseResultCommons(CommandLine commandLine) {
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