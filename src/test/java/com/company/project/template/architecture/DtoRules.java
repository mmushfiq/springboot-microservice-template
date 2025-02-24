package com.company.project.template.architecture;

import static com.company.project.template.architecture.ArchConstants.DTO_PKG;
import static com.company.project.template.architecture.CommonRules.fieldsShouldHaveGetterRule;
import static com.company.project.template.architecture.CommonRules.methodsShouldBePublicOrProtectedRule;
import static com.company.project.template.architecture.CommonRules.publicAndFinalFieldsAreNotAllowedRule;
import static com.company.project.template.architecture.CommonRules.staticMethodsAreNotAllowedRule;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.thirdparty.com.google.common.collect.Maps;

public class DtoRules {

    @ArchTest
    static final ArchRule fields_should_have_getter = fieldsShouldHaveGetterRule(Maps.newHashMap(), DTO_PKG);

    @ArchTest
    static final ArchRule public_and_final_fields_are_not_allowed = publicAndFinalFieldsAreNotAllowedRule(DTO_PKG);

    @ArchTest
    static final ArchRule methods_should_be_public_or_protected = methodsShouldBePublicOrProtectedRule(DTO_PKG);

    @ArchTest
    static final ArchRule static_methods_are_not_allowed = staticMethodsAreNotAllowedRule(DTO_PKG);

    @ArchTest
    static final ArchRule classes_should_override_equals_and_hashCode = classes()
            .that().resideInAnyPackage(DTO_PKG)
            .and().areNotMemberClasses()
            .should(CustomConditions.HAVE_EQUALS_AND_HASH_CODE)
            .because("DTO classes should override equals and hashCode methods");

}
