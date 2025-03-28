package com.myorg;

import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.ecs.Cluster;
import software.amazon.awscdk.services.ecs.ContainerImage;
import software.amazon.awscdk.services.elasticloadbalancingv2.HealthCheck;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedFargateService;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedTaskImageOptions;
import software.amazon.awscdk.services.vpclattice.CfnTargetGroup;
import software.constructs.Construct;
import software.amazon.awscdk.Duration;

import java.util.List;

public class AluraServiceStack extends Stack {
    public AluraServiceStack(final Construct scope, final String id, final Cluster cluster) {
        this(scope, id, null, cluster);
    }

    public AluraServiceStack(final Construct scope, final String id, final StackProps props, final Cluster cluster) {
        super(scope, id, props);

//         Criando um serviço Fargate balanceado com o Application Load Balancer e tornando-o público
        var app = ApplicationLoadBalancedFargateService.Builder.create(this, "AluraService")
                .serviceName("alura-service-ola")
                .cluster(cluster)           // Cluster ECS para onde o serviço será implantado
                .cpu(512)                   // Configuração de CPU (padrão é 256)
                .desiredCount(1)            // Número desejado de instâncias (padrão é 1)
                .listenerPort(8080)         // Porta para o listener do ALB
                .assignPublicIp(true)       // Atribui um IP público ao serviço
                .taskImageOptions(
                        ApplicationLoadBalancedTaskImageOptions.builder()
                                .image(ContainerImage.fromRegistry("jacquelineoliveira/ola:1.0"))  // Imagem do Docker
                                .containerPort(8080)  // A porta do container
                                .containerName("app_ola")  // Nome do container
                                .build())
                .memoryLimitMiB(2048)  // Limite de memória para o serviço
                .publicLoadBalancer(true)   // Habilita o Load Balancer público
                .build();

        app.getTargetGroup().configureHealthCheck(HealthCheck.builder().path("/ola").build());
    }
}
