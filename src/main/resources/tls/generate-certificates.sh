#!/bin/bash
# Cleanup files
find . \( -type f -name "*.key" -o -name "*.crt" -o -name "*.csr" -o -name "*.pem" -o -name "*.p12" -o -name "*.pkcs12" -o -name "*_creds" \)  -delete
# Create Certification Authority  (CA) key & certificate
openssl req -new -nodes \
   -x509 \
   -days 365 \
   -newkey rsa:2048 \
   -keyout ca.key \
   -out ca.crt \
   -config ca.cnf

# Convert CA key to pem format
cat ca.crt ca.key > ca.pem

for i in kafka-1 kafka-2 kafka-3 client
do
	echo "------------------------------- $i -------------------------------"
    # Create server key & certificate signing request(.csr file)
    openssl req -new \
    -newkey rsa:2048 \
    -keyout $i-creds/$i.key \
    -out $i-creds/$i.csr \
    -config $i-creds/$i.cnf \
    -nodes

    # Sign server certificate with CA
    openssl x509 -req \
    -days 3650 \
    -in $i-creds/$i.csr \
    -CA ca.crt \
    -CAkey ca.key \
    -CAcreateserial \
    -out $i-creds/$i.crt \
    -extfile $i-creds/$i.cnf \
    -extensions v3_req

    # Convert server certificate to pkcs12 format
    openssl pkcs12 -export \
    -in $i-creds/$i.crt \
    -inkey $i-creds/$i.key \
    -chain \
    -CAfile ca.pem \
    -name $i \
    -out $i-creds/$i.p12 \
    -password pass:confluent

    # Create server keystore
    keytool -importkeystore \
    -deststorepass confluent \
    -destkeystore $i-creds/kafka.$i.keystore.pkcs12 \
    -srckeystore $i-creds/$i.p12 \
    -deststoretype PKCS12  \
    -srcstoretype PKCS12 \
    -noprompt \
    -srcstorepass confluent

    # Import the CA cert into the truststore
    keytool -keystore ./$i-creds/kafka.$i.truststore.pkcs12 \
        -alias CARoot \
        -import \
        -file ./ca.crt \
        -storepass confluent  \
        -noprompt \
        -storetype PKCS12

    # Save creds
    echo "confluent" > ${i}-creds/${i}_sslkey_creds
    echo "confluent" > ${i}-creds/${i}_keystore_creds
  	echo "confluent" > ${i}-creds/${i}_truststore_creds

done
cp -r client-creds /tmp/client-creds
