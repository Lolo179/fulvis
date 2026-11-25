package com.fulvis.configuration;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "com.fulvis", importOptions = ImportOption.DoNotIncludeTests.class)
class TransactionBoundariesTest {

    @ArchTest
    static final ArchRule domain_and_rest_do_not_manage_transactions =
            noClasses().that().resideInAnyPackage("..domain..", "..adapters.in.rest..")
                    .should().dependOnClassesThat()
                    .resideInAnyPackage("org.springframework.transaction..", "jakarta.transaction..");

    @ArchTest
    static final ArchRule rest_does_not_access_repositories =
            noClasses().that().resideInAPackage("..adapters.in.rest..")
                    .should().dependOnClassesThat()
                    .resideInAnyPackage("..application.order.port..", "..adapters.out.persistence..");
}
