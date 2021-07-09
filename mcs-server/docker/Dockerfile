FROM centos8:java8
MAINTAINER Jonathan.Komar<AT>uibk.ac.at

ENV MCS_BASE=/usr/share/mcs
ENV LANG en_US.UTF-8
ENV PATH="${MCS_BASE}/bin:${PATH}"

ADD @RPM@ /
ADD setpermissions /setpermissions
ADD entrypoint.sh /entrypoint.sh

RUN rpm -ivh --nodigest --nofiledigest /@RPM@ && \
    chmod -R 777 /var/lib/mcs && \
    chmod +x /entrypoint.sh && \
    chown 0:0 /setpermissions && \
    chmod u+xs /setpermissions && \
    dnf clean all && \
    rm -rf /var/cache/yum

WORKDIR ${MCS_BASE}

VOLUME ["${MCS_BASE}/deploy","${MCS_BASE}/etc","${MCS_BASE}/data"]
EXPOSE 1099 8101 8181 44444 5005

# must be double quotes to use exec
ENTRYPOINT ["/entrypoint.sh"]