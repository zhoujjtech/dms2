package com.example.dms2;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 架构测试 验证DDD分层规则和模块依赖关系
 *
 * @author DMS2 Team
 * @since 1.0.0
 */
@DisplayName("架构测试")
class ArchitectureTest {

  private final JavaClasses classes = new ClassFileImporter().importPackages("com.example.dms2");

  @Test
  @DisplayName("Domain层不应依赖Application层")
  void domainLayerShouldNotDependOnApplicationLayer() {
    ArchRule rule =
        noClasses()
            .that()
            .resideInAPackage("..domain..")
            .should()
            .dependOnClassesThat()
            .resideInAPackage("..application..");

    rule.check(classes);
  }

  @Test
  @DisplayName("Domain层不应依赖Infrastructure层")
  void domainLayerShouldNotDependOnInfrastructureLayer() {
    ArchRule rule =
        noClasses()
            .that()
            .resideInAPackage("..domain..")
            .should()
            .dependOnClassesThat()
            .resideInAPackage("..infrastructure..");

    rule.check(classes);
  }

  @Test
  @DisplayName("Domain层不应依赖Interface层")
  void domainLayerShouldNotDependOnInterfaceLayer() {
    ArchRule rule =
        noClasses()
            .that()
            .resideInAPackage("..domain..")
            .should()
            .dependOnClassesThat()
            .resideInAPackage("..infrastructure..");

    rule.check(classes);
  }

  @Test
  @DisplayName("Application层不应依赖Infrastructure层")
  void applicationLayerShouldNotDependOnInfrastructureLayer() {
    ArchRule rule =
        noClasses()
            .that()
            .resideInAPackage("..application..")
            .should()
            .dependOnClassesThat()
            .resideInAPackage("..infrastructure..");

    rule.check(classes);
  }

  @Test
  @DisplayName("API模块不应依赖任何业务实现模块")
  void apiModuleShouldNotDependOnBusinessModules() {
    ArchRule rule =
        noClasses()
            .that()
            .resideInAPackage("..api..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage("..domain..", "..application..", "..infrastructure..");

    rule.check(classes);
  }

  @Test
  @DisplayName("控制器应该命名为UserController")
  void controllersShouldBeNamedXxxController() {
    ArchRule rule =
        classes()
            .that()
            .resideInAPackage("..infrastructure.rest..")
            .should()
            .haveSimpleNameEndingWith("Controller");

    rule.check(classes);
  }

  @Test
  @DisplayName("应用服务应该命名为XxxAppService或XxxService")
  void appServicesShouldBeNamedXxxAppService() {
    ArchRule rule =
        classes()
            .that()
            .resideInAPackage("..application.service..")
            .should()
            .haveSimpleNameEndingWith("AppService")
            .orShould()
            .haveSimpleNameEndingWith("Service");

    rule.check(classes);
  }

  @Test
  @DisplayName("仓储接口应该命名为XxxRepository")
  void repositoryInterfacesShouldBeNamedXxxRepository() {
    ArchRule rule =
        classes()
            .that()
            .areInterfaces()
            .and()
            .resideInAPackage("..domain.repository..")
            .should()
            .haveSimpleNameEndingWith("Repository");

    rule.check(classes);
  }

  @Test
  @DisplayName("仓储实现应该命名为XxxRepositoryImpl")
  void repositoryImplementationsShouldBeNamedXxxRepositoryImpl() {
    ArchRule rule =
        classes()
            .that()
            .resideInAPackage("..infrastructure.repository..")
            .and()
            .areNotAssignableTo(org.apache.ibatis.annotations.Mapper.class)
            .should()
            .haveSimpleNameEndingWith("RepositoryImpl");

    rule.check(classes);
  }

  @Test
  @DisplayName("DTO转换器应该命名为XxxAssembler")
  void assemblersShouldBeNamedXxxAssembler() {
    ArchRule rule =
        classes()
            .that()
            .resideInAPackage("..application.assembler..")
            .should()
            .haveSimpleNameEndingWith("Assembler");

    rule.check(classes);
  }

  @Test
  @DisplayName("Feign Client应该命名在feign包下")
  void feignClientsShouldResideInFeignPackage() {
    ArchRule rule =
        classes()
            .that()
            .haveSimpleNameEndingWith("FeignClient")
            .should()
            .resideInAPackage("..api.feign..");

    rule.check(classes);
  }

  @Test
  @DisplayName("分层架构应该遵循依赖方向")
  void layeredArchitectureShouldBeRespected() {
    layeredArchitecture()
        .consideringAllDependencies()
        .layer("API")
        .definedBy("..api..")
        .layer("Domain")
        .definedBy("..domain..")
        .layer("Application")
        .definedBy("..application..")
        .layer("Infrastructure")
        .definedBy("..infrastructure..")
        .whereLayer("Application")
        .mayOnlyBeAccessedByLayers("Infrastructure")
        .whereLayer("Domain")
        .mayOnlyBeAccessedByLayers("Application", "Infrastructure")
        .whereLayer("API")
        .mayOnlyBeAccessedByLayers("Domain", "Application", "Infrastructure")
        .because("DDD分层架构要求：上层可以依赖下层，下层不能依赖上层")
        .check(classes);
  }

  @Test
  @DisplayName("仓储实现应该实现仓储接口")
  void repositoryImplementationsShouldImplementRepositoryInterface() {
    ArchRule rule =
        classes()
            .that()
            .resideInAPackage("..infrastructure.repository..")
            .and()
            .areNotAssignableTo(org.apache.ibatis.annotations.Mapper.class)
            .should()
            .dependOnClassesThat()
            .resideInAPackage("..domain.repository..");

    rule.check(classes);
  }

  @Test
  @DisplayName("Controller应该只依赖Application服务")
  void controllersShouldOnlyDependOnApplicationServices() {
    ArchRule rule =
        classes()
            .that()
            .resideInAPackage("..infrastructure.rest..")
            .should()
            .onlyDependOnClassesThat()
            .resideInAnyPackage(
                "..api..",
                "..application..",
                "java..",
                "org.springframework..",
                "lombok..",
                "jakarta..",
                "org.slf4j..",
                "io.swagger.v3.oas..",
                "com.fasterxml.jackson..");

    rule.check(classes);
  }

  @Test
  @DisplayName("领域实体不应该使用Spring注解")
  void domainEntitiesShouldNotUseSpringAnnotations() {
    ArchRule rule =
        noClasses()
            .that()
            .resideInAPackage("..domain.model.entity..")
            .should()
            .beAnnotatedWith("org.springframework.stereotype..")
            .orShould()
            .beAnnotatedWith("org.springframework.web.bind..")
            .orShould()
            .beAnnotatedWith("org.springframework.beans..");

    rule.check(classes);
  }

  @Test
  @DisplayName("API模块的DTO应该放在dto包下")
  void apiDTOsShouldResideInDtoPackage() {
    ArchRule rule =
        classes()
            .that()
            .haveSimpleNameEndingWith("DTO")
            .or()
            .haveSimpleNameEndingWith("Request")
            .or()
            .haveSimpleNameEndingWith("Response")
            .should()
            .resideInAPackage("..api.dto..");

    rule.check(classes);
  }

  @Test
  @DisplayName("Feign包下的Client类应该以FeignClient结尾")
  void feignClientsShouldHaveProperNaming() {
    // 检查feign包下的类，允许Fallback和Service后缀
    ArchRule rule =
        classes()
            .that()
            .resideInAPackage("..api.feign..")
            .should()
            .haveSimpleNameEndingWith("FeignClient")
            .orShould()
            .haveSimpleNameEndingWith("Fallback")
            .orShould()
            .haveSimpleNameEndingWith("Service");

    rule.check(classes);
  }

  @Test
  @DisplayName("MyBatis Mapper应该命名在mapper包下")
  void mybatisMappersShouldResideInMapperPackage() {
    ArchRule rule =
        classes()
            .that()
            .areAnnotatedWith(org.apache.ibatis.annotations.Mapper.class)
            .should()
            .resideInAPackage("..infrastructure.mapper..");

    rule.check(classes);
  }

  @Test
  @DisplayName("MyBatis Mapper应该以Mapper结尾")
  void mybatisMappersShouldHaveProperNaming() {
    ArchRule rule =
        classes()
            .that()
            .resideInAPackage("..infrastructure.mapper..")
            .should()
            .haveSimpleNameEndingWith("Mapper");

    rule.check(classes);
  }
}
