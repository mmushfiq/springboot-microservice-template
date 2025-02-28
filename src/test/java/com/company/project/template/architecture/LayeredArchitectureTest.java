package com.company.project.template.architecture;

import static com.company.project.template.architecture.ArchConstants.BASE_PACKAGE;
import static com.company.project.template.architecture.ArchConstants.CONTROLLER_LAYER;
import static com.company.project.template.architecture.ArchConstants.CONTROLLER_PKG;
import static com.company.project.template.architecture.ArchConstants.DAO_LAYER;
import static com.company.project.template.architecture.ArchConstants.DAO_PKG;
import static com.company.project.template.architecture.ArchConstants.MAPPER_LAYER;
import static com.company.project.template.architecture.ArchConstants.MAPPER_PKG;
import static com.company.project.template.architecture.ArchConstants.MESSAGING_LAYER;
import static com.company.project.template.architecture.ArchConstants.MESSAGING_PKG;
import static com.company.project.template.architecture.ArchConstants.SCHEDULER_LAYER;
import static com.company.project.template.architecture.ArchConstants.SCHEDULER_PKG;
import static com.company.project.template.architecture.ArchConstants.SERVICE_LAYER;
import static com.company.project.template.architecture.ArchConstants.SERVICE_PKG;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

import com.tngtech.archunit.core.importer.ImportOption.DoNotIncludeJars;
import com.tngtech.archunit.core.importer.ImportOption.DoNotIncludeTests;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(packages = BASE_PACKAGE, importOptions = {DoNotIncludeTests.class, DoNotIncludeJars.class})
class LayeredArchitectureTest {

    private static final boolean ALLOW_EMPTY_LAYERS = true;

    @ArchTest
    static final ArchRule layered_architecture_rule = layeredArchitecture()
            .consideringAllDependencies()
            .layer(CONTROLLER_LAYER).definedBy(CONTROLLER_PKG)
            .layer(SERVICE_LAYER).definedBy(SERVICE_PKG)
            .layer(DAO_LAYER).definedBy(DAO_PKG)
            .layer(MAPPER_LAYER).definedBy(MAPPER_PKG)
            .layer(MESSAGING_LAYER).definedBy(MESSAGING_PKG)
            .layer(SCHEDULER_LAYER).definedBy(SCHEDULER_PKG)
            .withOptionalLayers(ALLOW_EMPTY_LAYERS)

            .whereLayer(CONTROLLER_LAYER).mayNotBeAccessedByAnyLayer()
            .whereLayer(DAO_LAYER).mayOnlyBeAccessedByLayers(DAO_LAYER, SERVICE_LAYER, MAPPER_LAYER)
            .whereLayer(SERVICE_LAYER).mayOnlyBeAccessedByLayers(CONTROLLER_LAYER, SERVICE_LAYER, MESSAGING_LAYER,
                    SCHEDULER_LAYER);

}


