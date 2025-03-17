package ricciliao.cache.configuration;


import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.parameters.HeaderParameter;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.apache.commons.lang3.StringUtils;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import ricciliao.x.component.cache.CacheConstants;
import ricciliao.x.component.cache.pojo.ConsumerIdentifierDto;

import java.util.Objects;

@Configuration
public class SpringdocConfiguration {

    private String projectVersion;

    @Value("${spring.application.version}")
    public void setProjectVersion(String projectVersion) {
        this.projectVersion = projectVersion;
    }

    @Bean
    public OpenAPI api() {

        return
                new OpenAPI()
                        .info(
                                new Info()
                                        .title("A title")
                                        .description("A description")
                                        .contact(new Contact().name("A contact"))
                                        .license(new License().name("A License"))
                                        .summary("A summary")
                                        .version(projectVersion)
                        )
                        .addSecurityItem(new SecurityRequirement().addList(HttpHeaders.AUTHORIZATION))
                        .components(
                                new Components()
                                        .addSecuritySchemes(
                                                HttpHeaders.AUTHORIZATION,
                                                new SecurityScheme()
                                                        .name(HttpHeaders.AUTHORIZATION)
                                                        .type(SecurityScheme.Type.HTTP)
                                                        .scheme("bearer")
                                        )
                        );
    }

    @Bean
    public OpenApiCustomizer openApiCustomizer() {
        Parameter customer =
                new HeaderParameter()
                        .name(CacheConstants.HTTP_HEADER_FOR_CACHE_CUSTOMER)
                        .description("the customer of cache")
                        .required(true);
        Parameter store =
                new HeaderParameter()
                        .name(CacheConstants.HTTP_HEADER_FOR_CACHE_STORE)
                        .description("the customer store of cache")
                        .required(true);

        return openApi -> {
            for (PathItem pathItem : openApi.getPaths().values()) {
                for (Operation readOperation : pathItem.readOperations()) {
                    readOperation
                            .addParametersItem(customer)
                            .addParametersItem(store);
                    readOperation
                            .getParameters()
                            .removeIf(parameter ->
                                    Objects.nonNull(parameter.getSchema())
                                            && StringUtils.isNotBlank(parameter.getSchema().get$ref())
                                            && parameter.getSchema().get$ref().contains(ConsumerIdentifierDto.class.getSimpleName()));
                }
            }
        };
    }

}
