#!/bin/bash

yes | cp -rf staging.yaml deploy.yaml

sed -i "s/imgtag/$VERSION/" deploy.yaml

aws eks --region $AWS_DEFAULT_REGION update-kubeconfig --name $CLUSTER_NAME

kubectl apply -f deploy.yaml --namespace=$NAMESPACE
