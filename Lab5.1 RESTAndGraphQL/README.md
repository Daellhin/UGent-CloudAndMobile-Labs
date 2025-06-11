# Lab5.1 REST and GraphQL
`Assignment:` [assignment](<Lab5 RESTAndGraphQL.pdf>)

`Report:` [report](report/Lab5.1.md)

## Useful commands
`Install pod`
```python
docker login
docker build --tag daellhin/egress:latest .
docker push daellhin/egress:latest
helm install "egress" helm
```

`Update pod`
```python
docker build --tag daellhin/egress:latest .
docker push daellhin/egress:latest
helm upgrade "egress" helm
kubectl get deployment
kubectl rollout restart deployment egress-helm
```
