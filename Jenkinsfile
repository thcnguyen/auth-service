pipeline {
    agent any
    environment {
        // These variables are the same for all environments
        k8s_release_name = "auth-release"
        k8s_chart_name = "auth-chart"
        k8s_project_version = readMavenPom().getVersion()
        k8s_image_tag = "${k8s_project_version}.${env.GIT_COMMIT}"
        k8s_replicas = 1
        k8s_container_port = 9000
        k8s_service_port = 9000

        // These variables are for different environments, put it here to have a full list of all ports used
        k8s_service_node_port_development = 31000
        k8s_service_node_port_staging = 31100
        k8s_service_node_port_production = 31200

        k8s_test_port_development = 8700
        k8s_test_port_staging = 8800

    }
    options {
        buildDiscarder logRotator(artifactDaysToKeepStr: '', artifactNumToKeepStr: '', daysToKeepStr: '7', numToKeepStr: '10')
        disableConcurrentBuilds()
        timestamps()
    }
    stages {

        stage ('Prepare') {
            when {
                anyOf {
                    branch 'develop';
                    branch 'staging';
                }
            }
            steps {
                script {
                    // These variables are expected to be set for each branch/environment
                    k8s_app_env = "undefined"
                    k8s_test_port_pre = 0
                    k8s_test_port_post = 0
                    k8s_namespace = "undefined"
                    k8s_server_username = "undefined"
                    k8s_server_host = "undefined"
                    k8s_chart_home = "undefined"
                    k8s_service_node_port = 0
                    k8s_replicas = 1
                    if ("${env.BRANCH_NAME}" == "develop") {
                        k8s_app_env = "dev"
                        k8s_test_port_post = "${k8s_test_port_development}"
                        k8s_namespace = "development"
                        k8s_server_username = "ubuntu"
                        k8s_server_host = "ec2-18-136-153-17.ap-southeast-1.compute.amazonaws.com"
                        k8s_chart_home = "~/k8s/jenkins/${k8s_namespace}/${k8s_chart_name}"
                        k8s_service_node_port = ${k8s_service_node_port_development}
                    } else if ("${env.BRANCH_NAME}" == "staging") {
                        k8s_app_env = "staging"
                        k8s_test_port_pre = "${k8s_test_port_staging}"
                        k8s_namespace = "development"
                        k8s_server_username = "ubuntu"
                        k8s_server_host = "ec2-18-136-153-17.ap-southeast-1.compute.amazonaws.com"
                        k8s_chart_home = "~/k8s/jenkins/${k8s_namespace}/${k8s_chart_name}"
                        k8s_service_node_port = ${k8s_service_node_port_staging}
                    } else {
                        error 'Unsupported branch: ${env.BRANCH_NAME}'
                    }
                }
                sh "echo \"Finished preparing for ${env.BRANCH_NAME} with user $USER\""
            }
        }

        stage ('Test: PRE') {
            when {
                expression {
                    k8s_test_port_pre != 0
                }
            }
            steps {
                sh "./mvnw -U clean install -Dserver.port=${k8s_test_port_pre}"
            }
        }

        stage ('Package') {
            steps {
                sh "echo \"Packaging branch ${env.BRANCH_NAME} with user $USER\""
                sh "./mvnw clean install -DskipTests dockerfile:build@build-version dockerfile:tag@tag-version -Dgit-revision=$GIT_COMMIT"
            }
        }
        stage ('Push Image') {
            steps {
                sh "./mvnw dockerfile:push@push-version -Dgit-revision=$GIT_COMMIT"
            }
        }
        stage ('Deploy to k8s') {
            when {
                anyOf {
                    branch 'develop'
                }
            }
            steps {
                sh """
                    echo \"Deploying ${env.BRANCH_NAME} to namespace ${k8s_namespace} on kubernetes at ${k8s_server_host}\"
                    rm -rf ~/.ssh/${k8s_release_name}.pem
                    tee ~/.ssh/${k8s_release_name}.pem <<-EOF > /dev/null
-----BEGIN RSA PRIVATE KEY-----
MIIEpgIBAAKCAQEA6XLXGSqUlOjy03RgQhOORBSygNrRjWu32aTG6IyhNGQzE+X672nnY9Sh9CQQ
aLoP4+3LYI6jCWJPPvvajqZNVL+VDXI5Dn9LdzI9pFJG0mLOUgxVvvj0sB8bzKQS/XucGixCKgPw
1vNstn74NjbYcgFZMOpQ/6OE+iBtauASh1jGF0QzhzfNi9I+noEw+eFEWsAcKV0J+Fju36CS1MNM
G+LdJzxHtdaIy6aMR5HBZnbD9vuphHLdXC90apoKiAP0DQZp055gEj6+XHenu1jQ8kXzhNyy/1UT
sj1Jbup0O/E5NAiM9djQAusIVtd39DTwNWpFGEAlNYFi+qsb33IdeQIDAQABAoIBAQC41ja9NFuk
c7Uf+7rjVnyEJycWoyrcIHfXGYJsSjyxMMzIaoV+3olZH9iDZ7KeWQOP4o3vC8DHA1HfeqYX4FDG
U1J+7PuEIQHthJgN7R8qIRVfHWke5htG/7qREy/+B8sXZgeVIL2mU+K1tF5u/ont6mdmNYCgiCYX
rWk0/+lrwzkeTFTNHhtsqOn/es5M5PbwHmnWJxCKngcLEIDcx+yZUK7hjhMDihh8pirkYZ65Mnzq
AJwpOgik3Og0Y7DNoho+rcPIHnxXgtXSckNva7pAyuB5RtKZ3Q/DT7PdvjSoPZfH94zVeMkYoLSh
z8HfnKJtcvFWR7pBPkY+9jOkmycBAoGBAPdjFOeHOz5z/3AGPF2t7qtUCfWR5h7xA1ZuX3UlPFBE
99YBj+ZZkmLzGaSxMDFqpwsNr0W12w5/4FCgnqWXk0Yl4ZnIMFsWbA5tQe85y5XmLMFcUDfs7M50
SKNGfgCPMsUlwBxEPHy/PpUeMK5fERTrYYvueKOnbm27c4vIcdvxAoGBAPGThxGKYIJe5NxEwBhg
kEEByoQ6GSh97uaxQQ6fSsm/lgrRB3/fBMCHELXsiZZnLazzN5RahC7kVU+w3G+AEeq/vqWvoq/4
KpiLEahGv5LmlBLZfNyiOIumufREnQZ23mcVM/teq5i7Kg+ccdeviCecODqpcYq5/VT9j0SS0YIJ
AoGBAKOmvftzsDNeVQpsdZ6bIxnfnD8BZzPsyAJzPV6nZUDMfw7pNaJyeq5OlxYlyPXH0f0z7lC1
PtZWrbNorcppfRmYkadDkQmQyaa5jDaPCyh8ffrj8IUujHD+59ZrGYuRL0rP8EhCs6jqcqH+uMGB
TjGCjKMe/Ft2tUyOBp/f18JhAoGBANIn6kfLHrwru2MX+Bj84GF9ImW3eS1tqMhOCW+kCdbK8ceF
IsYOCL9IgLQTC3qpVeXwTDimKfn1L3Y9QPdK7ctPdZYD3j7BYKUFzp2atowgRU7En0f9Y72xlHG4
wvXdsQryOh+CszsSD7w2+B6PmJ4E3DHEOH9b39PxJZKdOt+pAoGBANVZP9vZrA6vDseMXvqM6/OU
qfaCa+344oYl4hloJsvTghgSiE6cKWCsD65Wm3w+Dnv0Ctar+3sARqnAbrmDIq38JfOOEg+2IONQ
yjYRvVk5fk4ZPScHghkdU0rcfLD2VnLC3uh3F53MYTQE3zhDnP6rjP3lT2llLux1Xr+6qZFu
-----END RSA PRIVATE KEY-----
EOF
                    chmod 400 ~/.ssh/${k8s_release_name}.pem

                    ssh-keyscan -H ${k8s_server_host} >> ~/.ssh/known_hosts
                    ssh -i ~/.ssh/${k8s_release_name}.pem ${k8s_server_username}@${k8s_server_host} <<-'ENDSSH'
rm -rf ${k8s_chart_home}
mkdir -p ${k8s_chart_home}
mkdir -p ${k8s_chart_home}/charts
mkdir -p ${k8s_chart_home}/templates

tee ${k8s_chart_home}/Chart.yaml <<-EOF > /dev/null
apiVersion: v1
appVersion: \"${k8s_image_tag}\"
description: ${k8s_chart_name}
name: ${k8s_chart_name}
version: ${k8s_image_tag}
EOF

tee ${k8s_chart_home}/templates/all.yaml <<-EOF > /dev/null
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: auth-deployment
  namespace: ${k8s_namespace}
spec:
  selector:
    matchLabels:
      app: auth-app
  replicas: ${k8s_replicas}
  revisionHistoryLimit: 0
  template:
    metadata:
      labels:
        app: auth-app
    spec:
      containers:
      - name: auth-app
        image: docker.velotrade.tech/velotrade/auth-service:${k8s_image_tag}
        env:
        - name: ENV_NAME
          value: \"${k8s_app_env}\"
        ports:
        - containerPort: ${k8s_container_port}
          name: server
      imagePullPolicy: Always
      imagePullSecrets:
      - name: regcred
---
apiVersion: v1
kind: Service
metadata:
  name: auth-service
  namespace: ${k8s_namespace}
spec:
  ports:
  - name: http
    protocol: TCP
    port: ${k8s_service_port}
    targetPort: ${k8s_container_port}
    nodePort: ${k8s_service_node_port}
  selector:
    app: auth-app
  type: LoadBalancer
EOF

helm upgrade ${k8s_release_name} ${k8s_chart_home} --install

ENDSSH

                """
            }
        }

        stage ('Test: POST') {
            when {
                expression {
                    k8s_test_port_post != 0
                }
            }
            steps {
                sh "./mvnw -U clean install -Dserver.port=${k8s_test_port_post}"
            }
        }

    }

    post {
        always {
            cleanWs()
        }
    }
}