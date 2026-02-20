# 使用 Java 21 作为基础镜像 (与 pom.xml 一致)
FROM eclipse-temurin:21-jdk-alpine

# 设置工作目录
WORKDIR /app

# 将当前目录的所有文件复制到容器中
COPY . .

# 赋予 mvnw 执行权限
RUN chmod +x mvnw

# 使用 mvnw 进行打包 (跳过测试以加快速度)
RUN ./mvnw clean package -DskipTests

# 暴露 Spring Boot 默认端口 (根据你的配置是 4000)
EXPOSE 4000

# 运行生成的 jar 包
CMD ["java", "-jar", "target/spring-demo-0.0.1-SNAPSHOT.jar"]