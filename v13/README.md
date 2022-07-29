# ModelAttribute support and add example IndexController

RequestMappingHandlerAdapter
ModelAttributeMethodProcessor
ServletModelAttributeMethodProcessor
ModelFactory

# add config for RequestParamMethodArgumentResolver support simple type such as String

    // Catch-all
    resolvers.add(new RequestParamMethodArgumentResolver(getBeanFactory(), true));
