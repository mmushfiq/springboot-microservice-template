package com.company.project.template.architecture;

import static com.company.project.template.architecture.ArchConstants.ANNOTATED_EXPLANATION;
import static com.company.project.template.architecture.ArchConstants.SERVICE_PKG;
import static com.company.project.template.architecture.ArchConstants.SERVICE_SUFFIX;
import static com.company.project.template.architecture.CommonRules.beanMethodsAreNotAllowedRule;
import static com.company.project.template.architecture.CommonRules.classesShouldBeNamedProperly;
import static com.company.project.template.architecture.CommonRules.componentAnnotationIsNotAllowedRule;
import static com.company.project.template.architecture.CommonRules.fieldsShouldNotBePublicRule;
import static com.company.project.template.architecture.CommonRules.publicConstructorsRule;
import static com.company.project.template.architecture.CommonRules.staticMethodsAreNotAllowedRule;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.stereotype.Service;

public class ServiceRules {

    @ArchTest
    static final ArchRule no_classes_with_service_annotation_should_reside_outside_service_package = noClasses()
            .that().areAnnotatedWith(Service.class)
            .should().resideOutsideOfPackage(SERVICE_PKG);

    @ArchTest
    static final ArchRule component_annotation_is_not_allowed = componentAnnotationIsNotAllowedRule(SERVICE_PKG);

    @ArchTest
    static final ArchRule classes_should_be_annotated_with_service = classes()
            .that().resideInAPackage(SERVICE_PKG)
            .and().areTopLevelClasses()
            .should().beAnnotatedWith(Service.class)
            .because(String.format(ANNOTATED_EXPLANATION, SERVICE_SUFFIX, "@Service"));

    @ArchTest
    static final ArchRule classes_name_should_be_ending_with_service =
            classesShouldBeNamedProperly(SERVICE_PKG, SERVICE_SUFFIX);

    @ArchTest
    static final ArchRule fields_should_not_be_public = fieldsShouldNotBePublicRule(SERVICE_PKG);

    @ArchTest
    static final ArchRule constructors_should_not_be_private = publicConstructorsRule(SERVICE_PKG);

    @ArchTest
    static final ArchRule bean_methods_are_not_allowed = beanMethodsAreNotAllowedRule(SERVICE_PKG);

    @ArchTest
    static final ArchRule static_methods_are_not_allowed = staticMethodsAreNotAllowedRule(SERVICE_PKG);

}
