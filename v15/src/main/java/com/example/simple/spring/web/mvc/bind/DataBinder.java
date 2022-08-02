package com.example.simple.spring.web.mvc.bind;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.ConfigurablePropertyAccessor;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyAccessException;
import org.springframework.beans.PropertyAccessorUtils;
import org.springframework.beans.PropertyBatchUpdateException;
import org.springframework.beans.PropertyEditorRegistry;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.SimpleTypeConverter;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.TypeMismatchException;
import org.springframework.core.MethodParameter;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.util.PatternMatchUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.AbstractPropertyBindingResult;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingErrorProcessor;
import org.springframework.validation.BindingResult;
import org.springframework.validation.DefaultBindingErrorProcessor;
import org.springframework.validation.DirectFieldBindingResult;
import org.springframework.validation.MessageCodesResolver;
import org.springframework.validation.SmartValidator;
import org.springframework.validation.Validator;

import java.beans.PropertyEditor;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class DataBinder implements PropertyEditorRegistry, TypeConverter {

    public static final String DEFAULT_OBJECT_NAME = "target";

    public static final int DEFAULT_AUTO_GROW_COLLECTION_LIMIT = 256;

    protected static final Log logger = LogFactory.getLog(DataBinder.class);

    private final Object target;

    private final String objectName;

    private AbstractPropertyBindingResult bindingResult;

    private SimpleTypeConverter typeConverter;

    private BindException bindException;

    private boolean ignoreUnknownFields = true;

    private boolean ignoreInvalidFields = false;

    private boolean autoGrowNestedPaths = true;

    private int autoGrowCollectionLimit = DEFAULT_AUTO_GROW_COLLECTION_LIMIT;

    private String[] allowedFields;

    private String[] disallowedFields;

    private String[] requiredFields;

    private BindingErrorProcessor bindingErrorProcessor = new DefaultBindingErrorProcessor();

    private Validator validator;

    private ConversionService conversionService;

    public DataBinder(Object target) {
        this(target, DEFAULT_OBJECT_NAME);
    }

    public DataBinder(Object target, String objectName) {
        this.target = target;
        this.objectName = objectName;
    }

    public Object getTarget() {
        return this.target;
    }

    public String getObjectName() {
        return this.objectName;
    }

    public boolean isAutoGrowNestedPaths() {
        return this.autoGrowNestedPaths;
    }

    public void setAutoGrowNestedPaths(boolean autoGrowNestedPaths) {
        Assert.state(this.bindingResult == null,
            "DataBinder is already initialized - call setAutoGrowNestedPaths before other configuration methods");
        this.autoGrowNestedPaths = autoGrowNestedPaths;
    }

    public int getAutoGrowCollectionLimit() {
        return this.autoGrowCollectionLimit;
    }

    public void setAutoGrowCollectionLimit(int autoGrowCollectionLimit) {
        this.autoGrowCollectionLimit = autoGrowCollectionLimit;
    }

    public void initBeanPropertyAccess() {
        Assert.state(this.bindingResult == null,
            "DataBinder is already initialized - call initBeanPropertyAccess before other configuration methods");
        this.bindingResult = new BeanPropertyBindingResult(
            getTarget(), getObjectName(), isAutoGrowNestedPaths(), getAutoGrowCollectionLimit());
        if (this.conversionService != null) {
            this.bindingResult.initConversion(this.conversionService);
        }
    }

    public void initDirectFieldAccess() {
        Assert.state(this.bindingResult == null,
            "DataBinder is already initialized - call initDirectFieldAccess before other configuration methods");
        this.bindingResult = new DirectFieldBindingResult(getTarget(), getObjectName());
        if (this.conversionService != null) {
            this.bindingResult.initConversion(this.conversionService);
        }
    }

    protected AbstractPropertyBindingResult getInternalBindingResult() {
        if (this.bindingResult == null) {
            initBeanPropertyAccess();
        }
        return this.bindingResult;
    }

    protected ConfigurablePropertyAccessor getPropertyAccessor() {
        return getInternalBindingResult().getPropertyAccessor();
    }

    protected SimpleTypeConverter getSimpleTypeConverter() {
        if (this.typeConverter == null) {
            this.typeConverter = new SimpleTypeConverter();
            if (this.conversionService != null) {
                this.typeConverter.setConversionService(this.conversionService);
            }
        }
        return this.typeConverter;
    }

    protected PropertyEditorRegistry getPropertyEditorRegistry() {
        if (getTarget() != null) {
            return getInternalBindingResult().getPropertyAccessor();
        } else {
            return getSimpleTypeConverter();
        }
    }

    protected TypeConverter getTypeConverter() {
        if (getTarget() != null) {
            return getInternalBindingResult().getPropertyAccessor();
        } else {
            return getSimpleTypeConverter();
        }
    }

    public BindingResult getBindingResult() {
        return getInternalBindingResult();
    }

    @Deprecated
    public BindException getErrors() {
        if (this.bindException == null) {
            this.bindException = new BindException(getBindingResult());
        }
        return this.bindException;
    }

    public boolean isIgnoreUnknownFields() {
        return this.ignoreUnknownFields;
    }

    public void setIgnoreUnknownFields(boolean ignoreUnknownFields) {
        this.ignoreUnknownFields = ignoreUnknownFields;
    }

    public boolean isIgnoreInvalidFields() {
        return this.ignoreInvalidFields;
    }

    public void setIgnoreInvalidFields(boolean ignoreInvalidFields) {
        this.ignoreInvalidFields = ignoreInvalidFields;
    }

    public String[] getAllowedFields() {
        return this.allowedFields;
    }

    public void setAllowedFields(String... allowedFields) {
        this.allowedFields = PropertyAccessorUtils.canonicalPropertyNames(allowedFields);
    }

    public String[] getDisallowedFields() {
        return this.disallowedFields;
    }

    public void setDisallowedFields(String... disallowedFields) {
        this.disallowedFields = PropertyAccessorUtils.canonicalPropertyNames(disallowedFields);
    }

    public String[] getRequiredFields() {
        return this.requiredFields;
    }

    public void setRequiredFields(String... requiredFields) {
        this.requiredFields = PropertyAccessorUtils.canonicalPropertyNames(requiredFields);
        if (logger.isDebugEnabled()) {
            logger.debug("DataBinder requires binding of required fields [" +
                StringUtils.arrayToCommaDelimitedString(requiredFields) + "]");
        }
    }

    public void setExtractOldValueForEditor(boolean extractOldValueForEditor) {
        getPropertyAccessor().setExtractOldValueForEditor(extractOldValueForEditor);
    }

    public void setMessageCodesResolver(MessageCodesResolver messageCodesResolver) {
        getInternalBindingResult().setMessageCodesResolver(messageCodesResolver);
    }

    public BindingErrorProcessor getBindingErrorProcessor() {
        return this.bindingErrorProcessor;
    }

    public void setBindingErrorProcessor(BindingErrorProcessor bindingErrorProcessor) {
        Assert.notNull(bindingErrorProcessor, "BindingErrorProcessor must not be null");
        this.bindingErrorProcessor = bindingErrorProcessor;
    }

    public Validator getValidator() {
        return this.validator;
    }

    public void setValidator(Validator validator) {
        if (validator != null && (getTarget() != null && !validator.supports(getTarget().getClass()))) {
            throw new IllegalStateException("Invalid target for Validator [" + validator + "]: " + getTarget());
        }
        this.validator = validator;
    }

    //---------------------------------------------------------------------
    // Implementation of PropertyEditorRegistry/TypeConverter interface
    //---------------------------------------------------------------------

    public ConversionService getConversionService() {
        return this.conversionService;
    }

    public void setConversionService(ConversionService conversionService) {
        Assert.state(this.conversionService == null, "DataBinder is already initialized with ConversionService");
        this.conversionService = conversionService;
        if (this.bindingResult != null && conversionService != null) {
            this.bindingResult.initConversion(conversionService);
        }
    }

    public void registerCustomEditor(Class<?> requiredType, PropertyEditor propertyEditor) {
        getPropertyEditorRegistry().registerCustomEditor(requiredType, propertyEditor);
    }

    public void registerCustomEditor(Class<?> requiredType, String field, PropertyEditor propertyEditor) {
        getPropertyEditorRegistry().registerCustomEditor(requiredType, field, propertyEditor);
    }

    public PropertyEditor findCustomEditor(Class<?> requiredType, String propertyPath) {
        return getPropertyEditorRegistry().findCustomEditor(requiredType, propertyPath);
    }

    @Override
    public <T> T convertIfNecessary(Object value, Class<T> requiredType) throws TypeMismatchException {
        return getTypeConverter().convertIfNecessary(value, requiredType);
    }

    @Override
    public <T> T convertIfNecessary(Object value, Class<T> requiredType, Field field) throws TypeMismatchException {
        return null;
    }

    @Override
    public <T> T convertIfNecessary(Object value, Class<T> requiredType, TypeDescriptor typeDescriptor) throws TypeMismatchException {
        return TypeConverter.super.convertIfNecessary(value, requiredType, typeDescriptor);
    }

    @Override
    public <T> T convertIfNecessary(Object value, Class<T> requiredType, MethodParameter methodParam) throws TypeMismatchException {
        return getTypeConverter().convertIfNecessary(value, requiredType, methodParam);
    }

    public void bind(PropertyValues pvs) {
        MutablePropertyValues mpvs = (pvs instanceof MutablePropertyValues) ?
            (MutablePropertyValues) pvs : new MutablePropertyValues(pvs);
        doBind(mpvs);
    }

    protected void doBind(MutablePropertyValues mpvs) {
        checkAllowedFields(mpvs);
        checkRequiredFields(mpvs);
        applyPropertyValues(mpvs);
    }

    protected void checkAllowedFields(MutablePropertyValues mpvs) {
        PropertyValue[] pvs = mpvs.getPropertyValues();
        for (PropertyValue pv : pvs) {
            String field = PropertyAccessorUtils.canonicalPropertyName(pv.getName());
            if (!isAllowed(field)) {
                mpvs.removePropertyValue(pv);
                getBindingResult().recordSuppressedField(field);
                if (logger.isDebugEnabled()) {
                    logger.debug("Field [" + field + "] has been removed from PropertyValues " +
                        "and will not be bound, because it has not been found in the list of allowed fields");
                }
            }
        }
    }

    protected boolean isAllowed(String field) {
        String[] allowed = getAllowedFields();
        String[] disallowed = getDisallowedFields();
        return ((ObjectUtils.isEmpty(allowed) || PatternMatchUtils.simpleMatch(allowed, field)) &&
            (ObjectUtils.isEmpty(disallowed) || !PatternMatchUtils.simpleMatch(disallowed, field)));
    }

    protected void checkRequiredFields(MutablePropertyValues mpvs) {
        String[] requiredFields = getRequiredFields();
        if (!ObjectUtils.isEmpty(requiredFields)) {
            Map<String, PropertyValue> propertyValues = new HashMap<>();
            PropertyValue[] pvs = mpvs.getPropertyValues();
            for (PropertyValue pv : pvs) {
                String canonicalName = PropertyAccessorUtils.canonicalPropertyName(pv.getName());
                propertyValues.put(canonicalName, pv);
            }
            for (String field : requiredFields) {
                PropertyValue pv = propertyValues.get(field);
                boolean empty = (pv == null || pv.getValue() == null);
                if (!empty) {
                    if (pv.getValue() instanceof String) {
                        empty = !StringUtils.hasText((String) pv.getValue());
                    } else if (pv.getValue() instanceof String[]) {
                        String[] values = (String[]) pv.getValue();
                        empty = (values.length == 0 || !StringUtils.hasText(values[0]));
                    }
                }
                if (empty) {
                    // Use bind error processor to create FieldError.
                    getBindingErrorProcessor().processMissingFieldError(field, getInternalBindingResult());
                    // Remove property from property values to bind:
                    // It has already caused a field error with a rejected value.
                    if (pv != null) {
                        mpvs.removePropertyValue(pv);
                        propertyValues.remove(field);
                    }
                }
            }
        }
    }

    protected void applyPropertyValues(MutablePropertyValues mpvs) {
        try {
            // Bind request parameters onto target object.
            getPropertyAccessor().setPropertyValues(mpvs, isIgnoreUnknownFields(), isIgnoreInvalidFields());
        } catch (PropertyBatchUpdateException ex) {
            // Use bind error processor to create FieldErrors.
            for (PropertyAccessException pae : ex.getPropertyAccessExceptions()) {
                getBindingErrorProcessor().processPropertyAccessException(pae, getInternalBindingResult());
            }
        }
    }

    public void validate() {
        this.validator.validate(getTarget(), getBindingResult());
    }

    public void validate(Object... validationHints) {
        Validator validator = getValidator();
        if (!ObjectUtils.isEmpty(validationHints) && validator instanceof SmartValidator) {
            ((SmartValidator) validator).validate(getTarget(), getBindingResult(), validationHints);
        } else if (validator != null) {
            validator.validate(getTarget(), getBindingResult());
        }
    }

    public Map<?, ?> close() throws BindException {
        if (getBindingResult().hasErrors()) {
            throw new BindException(getBindingResult());
        }
        return getBindingResult().getModel();
    }

}
