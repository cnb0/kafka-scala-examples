# kafka-stream-demo

## APIs

```bash
# status
http :3000/status

# metrics
http :3000/metrics

# env
http :3000/env
```

## Development

```bash
# run
sbt app/run
sbt "cli/run -p MyName --game Rock -a 2"

# console (remove -Xfatal-warnings)
sbt app/console

# hot reload
sbt ~reStart

# format code
sbt scalafmt

# check autogenerated header
sbt headerCheck

# update header manually
sbt headerCreate

# check style
sbt scalastyle

# show project dependencies
sbt dependencyTree

# verify dependencies
sbt "whatDependsOn ch.qos.logback logback-classic 1.2.3"

# show outdated dependencies
sbt dependencyUpdates

# generate, package and aggregate scaladoc
sbt makeSite packageSite

# view scaladoc in browser [mac|linux]
[open|xdg-open] ./target/scala-2.12/unidoc/index.html
```

## Testing

Unit
```bash
# run tests
sbt test

# run tests in a project only
sbt "project app" test

# run single test
sbt "test:testOnly *RoutesSpec"

# debug tests (remote)
sbt test -jvm-debug 5005

# run tests, verify coverage, generate report and aggregate results
sbt clean coverage test coverageReport coverageAggregate

# view coverage report in browser [mac|linux]
[open|xdg-open] ./target/scala-2.12/scoverage-report/index.html
```

Performance
```bash
# run simulations
sbt gatling:test
sbt gatling-it:test

# run a single simulation
sbt "gatling:testOnly *LocalSimulation"
sbt "gatling-it:testOnly *StressSimulation"

# view latest simulation report in browser [mac|linux]
[open|xdg-open] ./perf/target/gatling/$(ls -t perf/target/gatling/ | head -1)/index.html
[open|xdg-open] ./perf/target/gatling-it/$(ls -t perf/target/gatling-it/ | head -1)/index.html
```

## Packaging

Docker
```bash
# generate Dockerfile in target/docker/
sbt docker:stage

# build image
sbt docker:publishLocal

# start temporary container
docker run \
  --rm \
  -e HTTP_PORT="8080" \
  -p 80:8080 \
  --name kafka-stream-demo \
  kafka-stream-demo:latest

# start container in background
docker-compose up -d
# view container logs
tail -F /vol/log/kafka-stream-demo/kafka-stream-demo.log

# access container
docker exec -it kafka-stream-demo bash
docker exec -it --user root kafka-stream-demo bash

# verify packaging
sbt package
unzip app/target/scala-2.12/app_2.12-* -d app/target/scala-2.12/app
```

Uber Jar
```bash
# package uber jar
sbt assembly

# run cli example
java -jar cli/target/scala-2.12/cli-latest.jar \
  -p MyName \
  --game Rock \
  -a 2

# run app example
HTTP_PORT=8080 \
LOG_LEVEL=INFO \
  java -jar app/target/scala-2.12/app-latest.jar
```

## Local deployment

Requirements

* [Minikube](https://kubernetes.io/docs/getting-started-guides/minikube)

Setup
```bash
# verify installation
minikube version

# start local cluster
minikube start --vm-driver=virtualbox

# dashboard
export NO_PROXY=localhost,127.0.0.1,$(minikube ip)
minikube dashboard

# reuse minikube's built-in docker daemon
eval $(minikube docker-env)

# build image
sbt docker:publishLocal

# tag (no latest)
docker tag kafka-stream-demo niqdev/kafka-stream-demo:v1.0.0
```

Simple deployment
```bash
# namespace
kubectl create namespace niqdev
kubectl config set-context $(kubectl config current-context) --namespace=niqdev
kubectl get namespaces

# create pod and deployment
kubectl run kafka-stream-demo \
  --image=niqdev/kafka-stream-demo:v1.0.0 \
  --port=3000

# expose service
kubectl expose deployment kafka-stream-demo \
  --type=NodePort \
  --port=3000
```

Service
```bash
kubectl create -f local-deployment.yaml
```

Useful command
```bash
# verify load balancer
NODE_PORT=$(kubectl get services/kafka-stream-demo -o go-template='{{(index .spec.ports 0).nodePort}}')
http $(minikube ip):$NODE_PORT/status
http $(minikube ip):$NODE_PORT/env | jq ".MY_POD_IP"

# logs
kubectl logs -f kafka-stream-demo

# access pod
kubectl exec -it kafka-stream-demo bash
kubectl exec -it kafka-stream-demo -- /bin/bash

# scale
kubectl scale --replicas=10 deployment/kafka-stream-demo-deployment

# info
kubectl get pod,deployment,service,namespace,pv,pvc
kubectl get pod --output=yaml
kubectl get pod --watch
kubectl describe pod
kubectl describe services/kafka-stream-demo

# cleanup
kubectl delete service kafka-stream-demo
kubectl delete deployment kafka-stream-demo-deployment
kubectl delete namespace niqdev
kubectl delete pv local-pv-volume
kubectl delete pvc local-pv-claim
```

## Monitor

```bash
# gui
jvisualvm

# find vmid
jcmd | grep sbt
# find pid
ps auxww | grep sbt

# cli
jstat -gcutil VMID 2s 5
jstat -gccause VMID 2s 5
jstat -printcompilation VMID 2s 5
jstat -compiler VMID 2s 5
```