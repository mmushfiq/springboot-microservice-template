package com.company.project.template.architecture;

import static com.tngtech.archunit.core.domain.JavaModifier.SYNTHETIC;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.constructors;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.fields;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;

import com.google.common.collect.Maps;
import com.tngtech.archunit.lang.ArchRule;
import java.util.Arrays;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

class CommonRules {

    //-----  Class Rules -----//

    static ArchRule classesShouldBeNamedProperly(String packageName, String suffix) {
        return classes()
                .that().resideInAPackage(packageName)
                .and().areTopLevelClasses()
                .should().haveSimpleNameEndingWith(suffix)
                .because(String.format("Classes in %s package should be named with %s suffix", packageName, suffix));
    }

    static ArchRule interfacesAreOnlyAllowedRule(String packageName, String... excludedPackages) {
        return classes().that()
                .resideInAPackage(packageName)
                .and().resideOutsideOfPackages(excludedPackages)
                .should().beInterfaces()
                .because(String.format("Resources should be interfaces in %s", packageName));
    }

    static ArchRule componentAnnotationIsNotAllowedRule(String packageName) {
        return classes().that()
                .resideInAPackage(packageName)
                .should().notBeAnnotatedWith(Component.class)
                .because(String.format("Component annotation is not allowed in %s", packageName));
    }

    static ArchRule springAnnotationsClassesAreNotAllowedRule(String... packageNames) {
        return classes().that()
                .resideInAnyPackage(packageNames)
                .should().notBeAnnotatedWith(Service.class)
                .andShould().notBeAnnotatedWith(Component.class)
                .andShould().notBeAnnotatedWith(Configuration.class)
                .andShould().notBeAnnotatedWith(ConfigurationProperties.class)
                .andShould().notBeAnnotatedWith(Bean.class)
                .andShould().notBeAnnotatedWith(Controller.class)
                .andShould().notBeAnnotatedWith(RestController.class)
                .because(String.format("Classes in %s should not be annotated with Spring annotations",
                        Arrays.toString(packageNames)));
    }


    //---  Constructor Rules ---//

    static ArchRule publicConstructorsRule(String packageName) {
        return constructors().that()
                .areDeclaredInClassesThat().resideInAPackage(packageName).and()
                .areDeclaredInClassesThat().areNotAnonymousClasses()
                .should().bePublic()
                .because(String.format("Public constructors are only allowed in %s", packageName));
    }


    //-----  Field Rules -----//

    static ArchRule fieldsShouldHaveGetterRule(Map<String, String> exclusions, String... packageNames) {
        return fields().that()
                .areDeclaredInClassesThat().resideInAnyPackage(packageNames)
                .and().areDeclaredInClassesThat().areNotMemberClasses()
                .and().arePrivate().and().areNotFinal().and().areNotStatic()
                .should(CustomConditions.haveGetter(exclusions))
                .because("Private fields should have getters in %s" + Arrays.toString(packageNames));
    }

    static ArchRule fieldsShouldHaveGetterRule(String... packageNames) {
        return fieldsShouldHaveGetterRule(Maps.newHashMap(), packageNames);
    }

    static ArchRule fieldsShouldNotBePublicRule(String packageName) {
        return fields().that()
                .areDeclaredInClassesThat().resideInAPackage(packageName)
                .should().notBePublic()
                .because(String.format("Public fields are not allowed in %s", packageName));
    }

    static ArchRule publicAndFinalFieldsAreNotAllowedRule(String... packageNames) {
        return fields().that()
                .areDeclaredInClassesThat().resideInAnyPackage(packageNames)
                .and().doNotHaveName("serialVersionUID")
                .should().notBeFinal()
                .andShould().notBePublic()
                .because(String.format("Fields with public and final modifiers are not allowed in %s",
                        Arrays.toString(packageNames)));
    }

    static ArchRule finalFieldsRule(String packageName, String... excludedPackages) {
        return fields().that()
                .areDeclaredInClassesThat().resideInAPackage(packageName)
                .and().areDeclaredInClassesThat().resideOutsideOfPackages(excludedPackages)
                .and().arePrivate()
                .and().doNotHaveModifier(SYNTHETIC)
                .should().beFinal()
                .because(String.format(
                        "Private attributes should be instanced by constructor classes, or it should be static in %s",
                        packageName));
    }


    //-----  Method Rules -----//

    static ArchRule beanMethodsAreNotAllowedRule(String packageName) {
        return methods().that()
                .areDeclaredInClassesThat().resideInAPackage(packageName)
                .should().notBeAnnotatedWith(Bean.class)
                .because(String.format("Bean methods are not allowed in %s", packageName));
    }

    static ArchRule privateMethodsAreNotAllowedRule(String packageName) {
        return methods().that()
                .areDeclaredInClassesThat().resideInAPackage(packageName)
                .should().notBePrivate()
                .because(String.format("Private methods are not allowed in %s", packageName));
    }

    static ArchRule staticMethodsAreNotAllowedRule(String packageName) {
        return methods()
                .that().areDeclaredInClassesThat().resideInAPackage(packageName)
                .should().notBeStatic()
                .orShould().haveModifier(SYNTHETIC)
                // alternatively exclude methods by name:
                .orShould().haveNameMatching("builder()")
                .because(String.format("Static methods are not allowed in %s", packageName));
    }

    static ArchRule methodsShouldBePublicRule(String... packageNames) {
        return methods().that()
                .areDeclaredInClassesThat().resideInAnyPackage(packageNames)
                .should().bePublic()
                .because("Public methods are only allowed in " + Arrays.toString(packageNames));
    }


    static ArchRule methodsShouldBePublicOrProtectedRule(String... packageNames) {
        return methods().that()
                .areDeclaredInClassesThat().resideInAnyPackage(packageNames)
                .should().bePublic().orShould().beProtected()
                .because("Public or protected methods are only allowed in " + Arrays.toString(packageNames));
    }

    static ArchRule staticMethodsAreOnlyAllowedRule(String packageName) {
        return methods().that()
                .areDeclaredInClassesThat().resideInAPackage(packageName)
                .should().beStatic()
                .because(String.format("Static methods are only allowed in %s", packageName));
    }

}
