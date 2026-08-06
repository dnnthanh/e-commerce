package com.dnnthanh.marketplace.be.checkout.api.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/** Executable package-boundary rules for the Checkout reference bounded context. */
@AnalyzeClasses(packages = "com.dnnthanh.marketplace.be.checkout.api")
class CheckoutArchitectureTest {

    @ArchTest
    static final ArchRule application_must_not_depend_on_adapter_out =
            noClasses()
                    .that()
                    .resideInAPackage("..application..")
                    .should()
                    .dependOnClassesThat()
                    .resideInAnyPackage("..adapter.out..");

    @ArchTest
    static final ArchRule controllers_must_not_depend_on_repositories =
            noClasses()
                    .that()
                    .resideInAPackage("..adapter.in.web..")
                    .should()
                    .dependOnClassesThat()
                    .haveSimpleNameEndingWith("Repository");

    @ArchTest
    static final ArchRule domain_must_not_depend_on_spring =
            noClasses()
                    .that()
                    .resideInAPackage("..domain..")
                    .should()
                    .dependOnClassesThat()
                    .resideInAnyPackage("org.springframework..", "jakarta.persistence..");
}
