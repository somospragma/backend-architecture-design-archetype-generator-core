package ${packageName};

import org.springframework.stereotype.Component;

@Component
public class ${adapterName} {
    private final ${useCaseName} useCase;

    public ${adapterName}(${useCaseName} useCase) {
        this.useCase = useCase;
    }
<#list endpoints as endpoint>
    public ${endpoint.returnType} ${endpoint.useCaseMethod}(<#list endpoint.parameters as param>${param.type} ${param.name}<#sep>, </#sep></#list>) {
        return null;
    }
</#list>
}
