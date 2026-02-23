package ${packageName};

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${basePath}")
public class ${adapterName} {

    private final ${useCaseName} useCase;

    public ${adapterName}(${useCaseName} useCase) {
        this.useCase = useCase;
    }

<#list endpoints as endpoint>
    @${endpoint.method}Mapping("${endpoint.path}")
    public ${endpoint.returnType} ${endpoint.useCaseMethod}(<#list endpoint.parameters as param>@${param.paramType} ${param.type} ${param.name}<#sep>, </#sep></#list>) {
        return useCase.${endpoint.useCaseMethod}(<#list endpoint.parameters as param>${param.name}<#sep>, </#sep></#list>);
    }

</#list>
}
