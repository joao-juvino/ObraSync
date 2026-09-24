FROM maven:3.9.9-eclipse-temurin-11 AS build
WORKDIR /build
COPY pom.xml .
COPY src ./src
RUN mvn --batch-mode clean verify

FROM jboss/wildfly:24.0.0.Final
COPY --from=build /root/.m2/repository/org/postgresql/postgresql/42.7.2/postgresql-42.7.2.jar /opt/jboss/wildfly/modules/org/postgresql/main/postgresql.jar
COPY docker/wildfly/module.xml /opt/jboss/wildfly/modules/org/postgresql/main/module.xml
COPY docker/wildfly/configure-datasource.cli /opt/jboss/wildfly/configure-datasource.cli
RUN /opt/jboss/wildfly/bin/jboss-cli.sh --file=/opt/jboss/wildfly/configure-datasource.cli \
    && rm -rf /opt/jboss/wildfly/standalone/configuration/standalone_xml_history \
    && mkdir -p /opt/jboss/uploads
COPY --from=build /build/target/obrasync.war /opt/jboss/wildfly/standalone/deployments/obrasync.war
CMD ["/opt/jboss/wildfly/bin/standalone.sh", "-c", "standalone.xml", "-b", "0.0.0.0"]
