FROM centos:8
MAINTAINER Jonathan.Komar<AT>uibk.ac.at

ENV MCS_BASE=/usr/share/mcs
ENV LANG en_US.UTF-8

ADD @RPM@ /
ADD setpermissions /setpermissions
ADD entrypoint.sh /entrypoint.sh

RUN dnf install -y java-1.8.0-openjdk && \
    rpm -ivh --nodigest --nofiledigest /@RPM@ && \
    ln -s /usr/share/mcs/bin/client /usr/local/bin/client && \
    chmod +x /entrypoint.sh && \
    chown 0:0 /setpermissions && \
    chmod u+xs /setpermissions

USER mcs
WORKDIR ${MCS_BASE}

VOLUME ["${MCS_BASE}/deploy","${MCS_BASE}/etc","${MCS_BASE}/data"]
EXPOSE 1099 8101 8181 44444 5005

ENTRYPOINT /entrypoint.sh