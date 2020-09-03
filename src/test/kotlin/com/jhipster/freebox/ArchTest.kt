package com.jhipster.freebox

import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.core.importer.ImportOption
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses
import org.junit.jupiter.api.Test

class ArchTest {

    @Test
    fun servicesAndRepositoriesShouldNotDependOnWebLayer() {

        val importedClasses = ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.jhipster.freebox")

        noClasses()
            .that()
                .resideInAnyPackage("com.jhipster.freebox.service..")
            .or()
                .resideInAnyPackage("com.jhipster.freebox.repository..")
            .should().dependOnClassesThat()
                .resideInAnyPackage("..com.jhipster.freebox.web..")
        .because("Services and repositories should not depend on web layer")
        .check(importedClasses)
    }
}
