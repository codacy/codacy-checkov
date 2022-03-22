FROM python:3.9-alpine
WORKDIR /workdir
COPY requirements.txt ./
RUN pip install --no-cache-dir -r requirements.txt && \
    apk update && \
    apk add --no-cache curl bash openssl && \
    curl -fsSL -o get_helm.sh https://raw.githubusercontent.com/helm/helm/master/scripts/get-helm-3 && \
    chmod 700 get_helm.sh && \
    /bin/bash get_helm.sh && \
    rm get_helm.sh
COPY src/*.py ./
COPY docs /docs
RUN adduser -u 2004 -D docker && \
    chown -R docker:docker /docs /home/docker
USER docker
ENTRYPOINT [ "python" ]
CMD [ "codacy_checkov.py" ]
