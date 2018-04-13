package com.thoughtworks.gauge.execution.parameters.parsers.base;

import com.thoughtworks.gauge.execution.parameters.ParsingException;
import com.thoughtworks.gauge.execution.parameters.parsers.converters.TableConverter;
import com.thoughtworks.gauge.execution.parameters.parsers.types.EnumParameterParser;
import com.thoughtworks.gauge.execution.parameters.parsers.types.PrimitiveParameterParser;
import com.thoughtworks.gauge.execution.parameters.parsers.types.PrimitivesConverter;
import com.thoughtworks.gauge.execution.parameters.parsers.types.TableParameterParser;
import gauge.messages.Spec.Parameter;
import org.reflections.Reflections;

import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class ParameterParsingChain implements ParameterParser {
    private List<ParameterParser> chain = new LinkedList<>();

    public ParameterParsingChain() {
        new Reflections().getSubTypesOf(CustomParameterParser.class).stream()
                .map(this::asCustomParameterParser)
                .filter(Objects::nonNull)
                .forEach(chain::add);
        System.out.println("LOADING " + chain);
        chain.add(new TableParameterParser(new TableConverter()));
        chain.add(new EnumParameterParser());
        chain.add(new PrimitiveParameterParser(new PrimitivesConverter()));
    }

    private @Nullable
    ParameterParser asCustomParameterParser(Class<? extends ParameterParser> clazz) {
        try {
            return clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
            // currently there seems to be no logging system used, so we cannot warn the user about an error
            return null;
        }
    }


    @Override
    public boolean canParse(Class<?> parameterType, Parameter parameter) {
        return true;
    }

    public Object parse(Class<?> parameterType, Parameter parameter) throws ParsingException {
        for (ParameterParser parser : chain) {
            if (parser.canParse(parameterType, parameter)) {
                return parser.parse(parameterType, parameter);
            }
        }
        return parameter.getValue();
    }
}
