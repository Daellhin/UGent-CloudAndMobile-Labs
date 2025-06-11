# Lab3 Microservices
`Assignment:` [assignment](<Lab3 Microservices.pdf>)

`Report:` [report](<report/Lab3 report.md>)

## Useful commands
`Kubernetes`
```python
C:\Users\lorin\.kube
kubectl get pod
```

`Commands install`
```python
docker login
docker build --tag daellhin/helloworld:latest .
docker push daellhin/helloworld:latest
helm install "helloworld" chart
```

`Commands update`
```python
docker build --tag daellhin/helloworld:latest .
docker push daellhin/helloworld:latest
helm upgrade "helloworld" chart
```

`Commands update`
```python
docker build --tag daellhin/ingress-api:latest .
docker push daellhin/ingress-api:latest
helm upgrade "ingress-api" helm
kubectl get deployment
kubectl rollout restart deployment ingress-api-helm
```