package io.geewit.core.feign;

import feign.RequestTemplate;
import feign.codec.EncodeException;
import feign.codec.Encoder;
import io.geewit.core.feign.request.RequestObject;
import io.geewit.core.utils.reflection.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Type;

/**
 * Provides support for encoding RequestObject via composition.
 * @author geewit
 */
public class RequestObjectEncoder implements Encoder {
    private static Logger logger = LoggerFactory.getLogger(RequestObjectEncoder.class);

    private final Encoder delegate;

    public RequestObjectEncoder(Encoder delegate) {
        this.delegate = delegate;
    }

    @Override
    public void encode(Object object, Type bodyType, RequestTemplate template) throws EncodeException {
        if (supports(object)) {
            Field[] fields = Reflections.getPublicGetters(object.getClass());
            for(Field field : fields) {
                String fieldName = field.getName();
                Object fieldValue = ReflectionUtils.getField(field, object);
                logger.debug("field.name = {}, field.value = {}", fieldName, fieldValue);
                if(fieldValue != null) {
                    template.query(fieldName, String.valueOf(fieldValue));
                }
            }
        } else {
            if (delegate != null) {
                delegate.encode(object, bodyType, template);
            }
            else {
                throw new EncodeException(
                        "RequestObjectEncoder does not support the given object "
                                + object.getClass()
                                + " and no delegate was provided for fallback!");
            }
        }
    }

    protected boolean supports(Object object) {
        return object instanceof RequestObject;
    }
}