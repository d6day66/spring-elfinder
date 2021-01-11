###新增功能
- 整合mybatisplus
- 整合springSecurity
- 增加防止重复提交注解
- 整合hotool工具
###计划新增功能
- 全局异常处理
- 自定义类加载器构建热加载功能
####功能详情
- 全局异常处理
```
springboot注解@ControllerAdvice
1.增加模板引擎依赖用于返回页面
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-freemarker</artifactId>
</dependency>
2.增加配置
spring:
  # 出现错误时, 直接抛出异常(便于异常统一处理，否则捕获不到404)
  mvc:
    throw-exception-if-no-handler-found: true
  # 不要为工程中的资源文件建立映射
  resources:
    add-mappings: false
3.错误信息实体
ExceptionEntity
4.自定义异常

```