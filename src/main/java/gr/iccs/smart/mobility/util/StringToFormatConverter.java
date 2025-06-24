package gr.iccs.smart.mobility.util;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToFormatConverter implements Converter<String, FormatSelection> {
    @Override
    public FormatSelection convert(String source) {
        return FormatSelection.valueOf(source.toUpperCase());
    }
}