FROM centos:7

ENV container docker

# 1. Installing Kerberos server, admin and client
RUN yum install -y krb5-server krb5-libs krb5-auth-dialog
RUN yum install -y krb5-workstation krb5-libs


ENV REALM ${REALM:-EXAMPLE.COM}
ENV SUPPORTED_ENCRYPTION_TYPES ${SUPPORTED_ENCRYPTION_TYPES:-aes256-cts-hmac-sha1-96:normal}
ENV KADMIN_PRINCIPAL ${KADMIN_PRINCIPAL:-kadmin/admin}
ENV KADMIN_PASSWORD ${KADMIN_PASSWORD:-MITiys4K5}
COPY kdc-users-init.sh /tmp/
COPY init-script.sh /tmp/
CMD /tmp/init-script.sh