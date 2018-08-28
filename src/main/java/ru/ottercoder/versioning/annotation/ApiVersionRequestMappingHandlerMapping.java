package ru.ottercoder.versioning.annotation;

import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.servlet.mvc.condition.ConsumesRequestCondition;
import org.springframework.web.servlet.mvc.condition.HeadersRequestCondition;
import org.springframework.web.servlet.mvc.condition.ParamsRequestCondition;
import org.springframework.web.servlet.mvc.condition.PatternsRequestCondition;
import org.springframework.web.servlet.mvc.condition.ProducesRequestCondition;
import org.springframework.web.servlet.mvc.condition.RequestCondition;
import org.springframework.web.servlet.mvc.condition.RequestMethodsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Method;

public class ApiVersionRequestMappingHandlerMapping extends RequestMappingHandlerMapping {

    private final String prefix;
    private final int latestVersion;

    public ApiVersionRequestMappingHandlerMapping(String prefix, int latestVersion) {
        this.prefix = prefix;
        this.latestVersion = latestVersion;
    }

    @Override
    protected RequestMappingInfo getMappingForMethod(Method method, Class<?> handlerType) {
        RequestMappingInfo info = super.getMappingForMethod(method, handlerType);
        if (info == null) return null;

        ApiVersion methodAnnotation = AnnotationUtils.findAnnotation(method, ApiVersion.class);
        ApiVersion typeAnnotation = AnnotationUtils.findAnnotation(handlerType, ApiVersion.class);
        if (methodAnnotation != null) {
            RequestCondition<?> methodCondition = getCustomMethodCondition(method);
            info = createApiVersionInfo(methodAnnotation, methodCondition).combine(info);
        } else if (typeAnnotation != null) {
            RequestCondition<?> typeCondition = getCustomTypeCondition(handlerType);
            info = createApiVersionInfo(typeAnnotation, typeCondition).combine(info);
        } else {
            info = createBlankApiVersionInfo().combine(info);
        }

        return info;
    }

    private RequestMappingInfo createBlankApiVersionInfo() {
        String[] patterns = new String[latestVersion + 1];
        for (int i = 1; i < latestVersion + 1; i++) {
            patterns[i] = prefix + i;
        }
        return new RequestMappingInfo(
                new PatternsRequestCondition(patterns, getUrlPathHelper(), getPathMatcher(), useSuffixPatternMatch(), useTrailingSlashMatch(), getFileExtensions()),
                new RequestMethodsRequestCondition(),
                new ParamsRequestCondition(),
                new HeadersRequestCondition(),
                new ConsumesRequestCondition(),
                new ProducesRequestCondition(),
                null);
    }

    private RequestMappingInfo createApiVersionInfo(ApiVersion annotation, RequestCondition<?> customCondition) {
        int since = annotation.since();
        int till = annotation.till();
        String[] patterns;
        if (till < latestVersion) {
            patterns = new String[till - since + 1];
            for (int i = 0; i < till - since + 1; i++) {
                patterns[i] = prefix + (since + i);
            }
        } else {
            patterns = new String[latestVersion - since + 1];
            for (int i = 0; i < latestVersion - since + 1; i++) {
                patterns[i] = prefix + (since + i);
            }
        }

        return new RequestMappingInfo(
                new PatternsRequestCondition(patterns, getUrlPathHelper(), getPathMatcher(), useSuffixPatternMatch(), useTrailingSlashMatch(), getFileExtensions()),
                new RequestMethodsRequestCondition(),
                new ParamsRequestCondition(),
                new HeadersRequestCondition(),
                new ConsumesRequestCondition(),
                new ProducesRequestCondition(),
                customCondition);
    }

}