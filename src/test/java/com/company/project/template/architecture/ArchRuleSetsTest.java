package com.company.project.template.architecture;

import static com.company.project.template.architecture.ArchConstants.BASE_PACKAGE;

import com.tngtech.archunit.core.importer.ImportOption.DoNotIncludeJars;
import com.tngtech.archunit.core.importer.ImportOption.DoNotIncludeTests;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.junit.ArchTests;

@AnalyzeClasses(packages = BASE_PACKAGE, importOptions = {DoNotIncludeTests.class, DoNotIncludeJars.class})
public class ArchRuleSetsTest {

    // -----  Layer Rules ----- //

    @ArchTest
    static final ArchTests CONFIGURATION_RULES = ArchTests.in(ConfigurationRules.class);

    @ArchTest
    static final ArchTests CONTROLLER_RULES = ArchTests.in(ControllerRules.class);

    @ArchTest
    static final ArchTests SERVICE_RULES = ArchTests.in(ServiceRules.class);

    @ArchTest
    static final ArchTests REPOSITORY_RULES = ArchTests.in(RepositoryRules.class);

    @ArchTest
    static final ArchTests ENTITY_RULES = ArchTests.in(EntityRules.class);

    @ArchTest
    static final ArchTests DTO_RULES = ArchTests.in(DtoRules.class);

}

