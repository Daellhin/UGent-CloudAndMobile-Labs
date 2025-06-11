# Lab5.2 Securing Web Services
`Commands install`
```python
docker login
docker build --tag daellhin/egress:latest .
docker push daellhin/egress:latest
helm install "egress" helm
```

`Commands update`
```python
docker build --tag daellhin/egress:latest .
docker push daellhin/egress:latest
helm upgrade "egress" helm
kubectl get deployment
kubectl rollout restart deployment egress-helm
```
