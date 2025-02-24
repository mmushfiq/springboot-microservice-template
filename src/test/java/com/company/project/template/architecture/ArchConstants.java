package com.company.project.template.architecture;

public final class ArchConstants {

    private ArchConstants() {
    }

    //---  Packages ---//
    static final String BASE_PACKAGE = "com.company.project.template";
    static final String CONFIGURATION_PKG = "..config..";
    static final String CONTROLLER_PKG = "..controller..";
    static final String SERVICE_PKG = "..service..";
    static final String DAO_PKG = "..dao..";
    static final String REPOSITORY_PKG = "..repository..";
    static final String ENTITY_PKG = "..entity..";
    static final String MAPPER_PKG = "..mapper..";
    static final String DTO_PKG = "..dto..";
    static final String MESSAGING_PKG = "..messaging..";

    //---  Layers ---//
    static final String CONTROLLER_LAYER = "Controller";
    static final String SERVICE_LAYER = "Service";
    static final String DAO_LAYER = "Dao";
    static final String MAPPER_LAYER = "Mapper";
    static final String MESSAGING_LAYER = "Messaging";

    //---  Suffixes ---//
    static final String CONFIGURATION_SUFFIX = "Config";
    static final String CONTROLLER_SUFFIX = "Controller";
    static final String SERVICE_SUFFIX = "Service";
    static final String REPOSITORY_SUFFIX = "Repository";
    static final String ENTITY_SUFFIX = "Entity";

    //---  Explanations ---//
    static final String ANNOTATED_EXPLANATION = "Classes in %s package should be annotated with %s";

}
