FROM openjdk:8-jdk AS builder

FROM gcr.io/spark-operator/spark:v3.0.0

USER root

ENV NEOLOGD_PATH=/usr/local/lib/neologd
ENV NEOLOGD_WORK=/tmp/neologd

RUN apt-get update && apt-get upgrade -y
RUN apt-get install -y build-essential file git curl
RUN apt-get install -y mecab=0.996-6 libmecab-dev=0.996-6 mecab-ipadic-utf8
RUN apt-get clean && rm -rf /var/lib/apt/lists/*
RUN mkdir -p ${NEOLOGD_PATH} ${NEOLOGD_WORK}
WORKDIR ${NEOLOGD_WORK}
RUN git clone --depth 1 https://github.com/neologd/mecab-ipadic-neologd.git && \
    mecab-ipadic-neologd/bin/install-mecab-ipadic-neologd -n -y -p ${NEOLOGD_PATH} && \
    sed -i "s/^.*dicdir\ =.*$/dicdir\ =\ ${NEOLOGD_PATH//\//\\\/}/" /etc/mecabrc
RUN rm -rf ${NEOLOGD_WORK}

COPY --from=builder /usr/local/openjdk-8 /usr/local/openjdk-8
COPY mecab-java-0.996.tar.gz /tmp/
RUN cd /tmp && tar xf /tmp/mecab-java-0.996.tar.gz && \
    cd /tmp/mecab-java-0.996/ && sed -i "s/^INCLUDE=.*$/INCLUDE=\/usr\/local\/openjdk-8\/include/" Makefile && make && \
    mv /tmp/mecab-java-0.996/MeCab.jar /usr/local/lib/ && \
    mv /tmp/mecab-java-0.996/libMeCab.so /usr/local/lib/

USER 185

ENV LD_LIBRARY_PATH=/usr/local/lib:.
WORKDIR /opt/spark/work-dir
