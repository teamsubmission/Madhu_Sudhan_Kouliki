package com.chubb.policyoverview.config;

import com.chubb.policyoverview.domain.models.LineOfBusiness;
import com.chubb.policyoverview.domain.models.Region;
import com.chubb.policyoverview.domain.models.Status;
import java.util.Arrays;
import java.util.function.Function;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConversionConfig implements WebMvcConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(String.class, Status.class,
                source -> byDisplayName(Status.values(), Status::getDisplayName, source));
        registry.addConverter(String.class, LineOfBusiness.class,
                source -> byDisplayName(LineOfBusiness.values(), LineOfBusiness::getDisplayName, source));
        registry.addConverter(String.class, Region.class,
                source -> byDisplayName(Region.values(), Region::getDisplayName, source));
    }

    private static <E extends Enum<E>> E byDisplayName(E[] values, Function<E, String> displayName, String source) {
        return Arrays.stream(values)
                .filter(value -> displayName.apply(value).equalsIgnoreCase(source))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown value: " + source));
    }
}
