package com.example.payment.architecture;

/*
 * - Testes de Arquitetura Hexagonal (ArchUnit)
 */

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

@AnalyzeClasses(packages = "com.example.payment")
public class HexagonalArchitectureTest {

    @ArchTest
    static final ArchRule domain_should_not_depend_on_infrastructure =
            noClasses()
                    .that().resideInAnyPackage("..domain..")
                    .should().dependOnClassesThat()
                    .resideInAnyPackage("..infrastructure..");

    @ArchTest
    static final ArchRule getDomain_should_not_depend_on_spring_framework =
            noClasses()
                    .that().resideInAnyPackage("..domain..")
                    .should().dependOnClassesThat()
                    .resideInAnyPackage("org.springframework");

    @ArchTest
    static final ArchRule application_should_only_depend_on_domain =
            classes()
                    .that().resideInAPackage("..application..")
                    .should().onlyDependOnClassesThat()
                    .resideInAnyPackage("..application..", "..domain..", "java..", "org.slf4j..");

}
