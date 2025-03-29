package com.myorg;

import software.amazon.awscdk.Fn;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.ecs.Cluster;
import software.amazon.awscdk.services.ecs.ContainerImage;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedFargateService;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedTaskImageOptions;
import software.amazon.awscdk.services.elasticloadbalancingv2.HealthCheck;
import software.constructs.Construct;

import java.util.HashMap;
import java.util.Map;

public class AluraServiceStack extends Stack {
    public AluraServiceStack(final Construct scope, final String id, final Cluster cluster) {
        this(scope, id, null, cluster);
    }

    public AluraServiceStack(final Construct scope, final String id, final StackProps props, final Cluster cluster) {
        super(scope, id, props);

        Map<String, String> autenticacao = new HashMap<>();
        autenticacao.put("SPRING_DATASOURCE_URL", "jdbc:mysql://" + Fn.importValue("pedidos-db-endpoint") +
                ":3306/alurafood-pedidos");
        autenticacao.put("SPRING_DATASOURCE_USERNAME", "admin");
        autenticacao.put("SPRING_DATASOURCE_PASSWORD", Fn.importValue("pedidos-db-senha"));

//         Criando um serviço Fargate balanceado com o Application Load Balancer e tornando-o público
        ApplicationLoadBalancedFargateService.Builder.create(this, "AluraService")
                .serviceName("alura-service-pedidos-ms")
                .cluster(cluster)           // Cluster ECS para onde o serviço será implantado
                .cpu(512)                   // Configuração de CPU (padrão é 256)
                .desiredCount(1)            // Número desejado de instâncias (padrão é 1)
                .listenerPort(8080)         // Porta para o listener do ALB
                .assignPublicIp(true)       // Atribui um IP público ao serviço
                .taskImageOptions(
                        ApplicationLoadBalancedTaskImageOptions.builder()
                                .image(ContainerImage.fromRegistry("jacquelineoliveira/pedidos-ms"))  // Imagem do Docker
                                .containerPort(8080)  // A porta do container
                                .containerName("app_pedidos_ms")  // Nome do container
                                .environment(autenticacao)
                                .build())
                .memoryLimitMiB(2048)  // Limite de memória para o serviço
                .publicLoadBalancer(true)   // Habilita o Load Balancer público
                .build()
                .getTargetGroup()
                .configureHealthCheck(HealthCheck.builder().path("/pedidos").build());
    }
}
