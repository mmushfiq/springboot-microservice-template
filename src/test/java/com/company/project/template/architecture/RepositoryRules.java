package com.company.project.template.architecture;

import static com.company.project.template.architecture.ArchConstants.ANNOTATED_EXPLANATION;
import static com.company.project.template.architecture.ArchConstants.REPOSITORY_PKG;
import static com.company.project.template.architecture.ArchConstants.REPOSITORY_SUFFIX;
import static com.company.project.template.architecture.CommonRules.classesShouldBeNamedProperly;
import static com.company.project.template.architecture.CommonRules.interfacesAreOnlyAllowedRule;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.stereotype.Repository;

public class RepositoryRules {

    @ArchTest
    static final ArchRule classes_should_be_annotated_with_repository = classes()
            .that().resideInAPackage(REPOSITORY_PKG)
            .and().areTopLevelClasses()
            .should().beAnnotatedWith(Repository.class)
            .because(String.format(ANNOTATED_EXPLANATION, REPOSITORY_SUFFIX, "@Repository"));

    @ArchTest
    static final ArchRule classes_should_be_interfaces = interfacesAreOnlyAllowedRule(REPOSITORY_PKG, "..cache..");

    @ArchTest
    static final ArchRule no_classes_with_repository_annotation_should_reside_outside_of_repository_package =
            noClasses()
                    .that().areAnnotatedWith(Repository.class)
                    .or().haveSimpleNameEndingWith(REPOSITORY_SUFFIX)
                    .should().resideOutsideOfPackage(REPOSITORY_PKG);

    @ArchTest
    static final ArchRule classes_name_should_be_ending_with_repository =
            classesShouldBeNamedProperly(REPOSITORY_PKG, REPOSITORY_SUFFIX);

}
