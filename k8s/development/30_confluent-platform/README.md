# confluent-platform

# about config

This confluent platform is based on cp-helm-charts.

To use community edition, the following changes are applied in values.yaml.

* cp-enterprise-kafka -> cp-kafka
* control-center: disabled
* do not send metrics to control center
    * in kafka section, customEnv: {KAFKA_METRIC_REPORTERS: ""} (https://github.com/confluentinc/cp-helm-charts/issues/305)

# ui

With ingress.

* http://kafka-topics-ui.localhost/
* http://kafka-connect-ui.localhost/
* http://schema-registry-ui.localhost/
