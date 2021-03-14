FROM centos:8
MAINTAINER Jonathan.Komar<AT>uibk.ac.at

ADD @RPM@ /
ADD setpermissions /setpermissions

RUN dnf install -y java-1.8.0-openjdk && \
    rpm -ivh --nodigest --nofiledigest /@RPM@ &&\
    chown 0:0 /setpermissions && \
    chmod u+xs /setpermissions

USER mcs

ENTRYPOINT /setpermissions $(id -u) $(id -g)
CMD /usr/share/mcs/bin/start-mcs server