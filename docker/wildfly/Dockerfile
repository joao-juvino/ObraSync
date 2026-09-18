FROM jboss/wildfly:24.0.0.Final

COPY docker/wildfly/postgresql.jar /opt/jboss/wildfly/modules/org/postgresql/main/postgresql.jar
COPY docker/wildfly/module.xml /opt/jboss/wildfly/modules/org/postgresql/main/module.xml
COPY docker/wildfly/configure-datasource.cli /opt/jboss/wildfly/configure-datasource.cli

RUN /opt/jboss/wildfly/bin/jboss-cli.sh --file=/opt/jboss/wildfly/configure-datasource.cli \
    && rm -f /opt/jboss/wildfly/configure-datasource.cli
