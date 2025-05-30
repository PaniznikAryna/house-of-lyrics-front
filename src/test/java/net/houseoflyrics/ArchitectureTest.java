package net.houseoflyrics;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

@AnalyzeClasses(packages = ArchitectureTest.BASE_PACKAGE)
class ArchitectureTest {

    static final String BASE_PACKAGE = "net.houseoflyrics";

    // TODO Add your own rules and remove those that don't apply to your project

    @ArchTest
    public static final ArchRule domain_model_should_not_depend_on_application_services = noClasses().that()
            .resideInAPackage(BASE_PACKAGE + "..domain..").should().dependOnClassesThat()
            .resideInAPackage(BASE_PACKAGE + "..service..");

    @ArchTest
    public static final ArchRule domain_model_should_not_depend_on_the_user_interface = noClasses().that()
            .resideInAPackage(BASE_PACKAGE + "..domain..").should().dependOnClassesThat()
            .resideInAnyPackage(BASE_PACKAGE + "..ui..");

    @ArchTest
    public static final ArchRule application_services_should_not_depend_on_the_user_interface = noClasses().that()
            .resideInAPackage(BASE_PACKAGE + "..service..").should().dependOnClassesThat()
            .resideInAnyPackage(BASE_PACKAGE + "..ui..");

    @ArchTest
    public static final ArchRule there_should_not_be_circular_dependencies_between_feature_packages = slices()
            .matching(BASE_PACKAGE + ".(*)..").should().beFreeOfCycles();
}
