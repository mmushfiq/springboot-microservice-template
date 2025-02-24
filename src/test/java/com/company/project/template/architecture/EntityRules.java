package com.company.project.template.architecture;

import static com.company.project.template.architecture.ArchConstants.DTO_PKG;
import static com.company.project.template.architecture.ArchConstants.ENTITY_PKG;
import static com.company.project.template.architecture.ArchConstants.ENTITY_SUFFIX;
import static com.company.project.template.architecture.CommonRules.classesShouldBeNamedProperly;
import static com.company.project.template.architecture.CommonRules.methodsShouldBePublicOrProtectedRule;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import com.company.project.template.dao.entity.BaseEntity;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import jakarta.persistence.Entity;

public class EntityRules {

    @ArchTest
    static final ArchRule entity_classes_should_be_public = classes()
            .that().resideInAPackage(ENTITY_PKG)
            .and().areTopLevelClasses()
            .should().bePublic();

    @ArchTest
    static final ArchRule classes_should_be_annotated_with_entity = classes()
            .that().resideInAPackage(ENTITY_PKG)
            .and().areTopLevelClasses()
            .and().areNotAssignableFrom(BaseEntity.class)
            .should().beAnnotatedWith(Entity.class);

    @ArchTest
    static final ArchRule class_name_should_be_ending_with_entity =
            classesShouldBeNamedProperly(ENTITY_PKG, ENTITY_SUFFIX);

    @ArchTest
    static final ArchRule classes_should_override_equals_and_hashCode = classes()
            .that().resideInAnyPackage(ENTITY_PKG)
            .and().areNotMemberClasses()
            .should(CustomConditions.HAVE_EQUALS_AND_HASH_CODE)
            .because("Entity classes should override equals and hashCode methods");

    @ArchTest
    static final ArchRule methods_should_be_public_or_protected = methodsShouldBePublicOrProtectedRule(DTO_PKG);


}

