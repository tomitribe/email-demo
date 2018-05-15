FROM ${images.owner}/email-demo-tomee:${project.version}
COPY ${project.build.finalName}.war /opt/tomee/webapps/ROOT.war
COPY tomee.xml /opt/tomee/conf
COPY email.jpg /opt/
RUN mkdir -p /opt/messages/
