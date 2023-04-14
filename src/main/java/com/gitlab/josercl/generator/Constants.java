package com.gitlab.josercl.generator;

public class Constants {

    public static final String MAPPER_SUFFIX = "Mapper";

    public static class Infrastructure {
        public static final String ENTITY_PACKAGE = "persistence.entity";
        public static final String MAPPER_PACKAGE = "persistence.entity.mapper";
        public static final String REPOSITORY_PACKAGE = "persistence.repository";
        public static final String ADAPTER_PACKAGE = "persistence.adapter";
        public static final String MODEL_SUFFIX = "Entity";
        public static final String REPOSITORY_SUFFIX = "Repository";
        public static final String ADAPTER_SUFFIX = "Adapter";
    }

    public static class Common {
        public static final String EXCEPTION_PACKAGE = "common.exception";
    }

    public static class Domain {
        public static final String MODEL_PACKAGE = "domain.model";
        public static final String SPI_PACKAGE = "domain.spi";
        public static final String API_PACKAGE = "domain.api";
        public static final String EXCEPTION_PACKAGE = "domain.exception";
        public static final String API_IMPL_PACKAGE = API_PACKAGE + ".impl";
        public static final String SERVICE_SUFFIX = "Service";
        public static final String PORT_SUFFIX = "Port";
    }

    public static class Application {
        public static final String CONTROLLER_SUFFIX = "Controller";

        public static final String CONTROLLER_PACKAGE = "application.rest.controller";
        public static final String MAPPER_PACKAGE = "application.rest.model.mapper";
    }
}
