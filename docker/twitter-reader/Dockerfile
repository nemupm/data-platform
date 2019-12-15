FROM alpine:latest

ENV PYTHONUNBUFFERED=1

# python3, pip
RUN apk add --no-cache python3 && \
    if [ ! -e /usr/bin/python ]; then ln -sf python3 /usr/bin/python ; fi && \
    python3 -m ensurepip && \
    rm -r /usr/lib/python*/ensurepip && \
    pip3 install --no-cache --upgrade pip setuptools wheel && \
    if [ ! -e /usr/bin/pip ]; then ln -s pip3 /usr/bin/pip ; fi
# create a group and user
RUN addgroup -S www && adduser -S www -G www

USER www

COPY app/ /www/app/
WORKDIR /www/app
RUN pip3 install -r requirements.txt --user

ENTRYPOINT ["python", "sampled_stream.py"]
CMD ["all"]
