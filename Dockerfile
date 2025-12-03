# 使用官方OpenJDK运行时作为基础镜像
FROM openjdk:8-jdk-alpine

# 设置维护者信息
LABEL maintainer="fund-monitor-team"

# 设置工作目录
WORKDIR /app

# 复制Maven生成的JAR文件到容器中
# 注意：这里假设你已经通过mvn package命令生成了可执行的JAR文件
COPY target/monitor-1.0-SNAPSHOT.jar app.jar

# 复制项目的资源文件
COPY src/main/resources/ src/main/resources/

# 暴露应用端口
EXPOSE 8081

# 设置环境变量
ENV JAVA_OPTS=""
ENV SPRING_PROFILES_ACTIVE=docker

# 运行应用
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar app.jar"]